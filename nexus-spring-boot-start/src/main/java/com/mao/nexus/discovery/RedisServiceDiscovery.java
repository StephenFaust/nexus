package com.mao.nexus.discovery;


import com.mao.nexus.io.common.MateInfo;
import com.mao.nexus.property.RegistryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author ：StephenMao
 * @date ：2022/6/14 14:03
 */
public class RedisServiceDiscovery implements ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(RedisServiceDiscovery.class);

    private static final String REDIS_KEY = "Nexus";

    private static final String parameter = "notify-keyspace-events";

    private final Set<MateInfo> mateInfos = new CopyOnWriteArraySet<>();

    private final Map<String, Set<MateInfo>> caches = new ConcurrentHashMap();

    private final RegistryProperties registryProperties;

    private JedisPool pool;

    private final ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(1, r -> {
        Thread thread = new Thread(r);
        thread.setName("dis-sch-" + Thread.currentThread().getId());
        thread.setDaemon(true);
        return thread;
    });


    private final ExecutorService psubscribeExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("dis-sub-" + Thread.currentThread().getId());
        thread.setDaemon(true);
        return thread;
    });


    public RedisServiceDiscovery(RegistryProperties registryProperties) {
        this.registryProperties = registryProperties;
    }


    @PostConstruct
    public void startTask() {
        logger.info("registry task started");
        Assert.isTrue(StringUtils.hasLength(registryProperties.getIp()), "registry ip can't be empty");
        Assert.isTrue(registryProperties.getPort() > 0, "registry port error");
        pool = new JedisPool(registryProperties.getIp(), registryProperties.getPort());
        scheduledExecutor.scheduleAtFixedRate(this::discoverTask, 0, 2, TimeUnit.SECONDS);
        //订阅过期事件
        psubscribeExecutor.execute(this::subExEvent);
    }

    private void subExEvent() {
        try {
            final Jedis jedis = pool.getResource();
            List<String> notify = jedis.configGet(parameter);
            if ("".equals(notify.get(1))) jedis.configSet(parameter, "Ex");
            jedis.psubscribe(new JedisPubSub() {
                                 @Override
                                 public void onPSubscribe(String pattern, int subscribedChannels) {
                                     logger.debug("subscribe ex event message-> "
                                             + pattern + " " + subscribedChannels);
                                 }

                                 @Override
                                 public void onPMessage(String pattern, String channel, String key) {
                                     logger.debug("receive ex event message-> "
                                             + pattern + " " + channel + " " + key);
                                     if (!key.contains(REDIS_KEY))
                                         return;
                                     mateInfos.remove(packageData(key));
                                 }
                             }
                    , "__keyevent@0__:expired");
        } catch (Exception ex) {
            logger.error("expired event fail,msg:" + ex.getMessage());
        }
    }


    private MateInfo packageData(String key) {
        final String[] keyData = key.split(":");
        final String[] address = keyData[2].split("-");
        return MateInfo.builder()
                .serviceName(keyData[1])
                .ip(address[0])
                .port(Integer.valueOf(address[1]))
                .build();
    }

    private void discoverTask() {
        final Jedis jedis = pool.getResource();
        try {
            final Set<String> keys = jedis.keys(String.format("%s:*", REDIS_KEY));
            final Iterator<String> iterator = keys.iterator();
            while (iterator.hasNext()) {
                final String key = iterator.next();
                final String[] keyData = key.split(":");
                final String[] address = keyData[2].split("-");
                final MateInfo info = MateInfo.builder()
                        .serviceName(keyData[1])
                        .ip(address[0])
                        .port(Integer.valueOf(address[1]))
                        .build();
                mateInfos.add(info);
            }
        } catch (Exception ex) {
            logger.info("discover schedule task error,msg:{}", ex.getMessage());
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public List<MateInfo> listServices(String serviceName) {
        List<MateInfo> services = mateInfos.stream().filter(s -> s.getServiceName().equals(serviceName)).collect(Collectors.toList());
        Assert.isTrue(services.size() > 0, "not find service");
        return services;
    }
}

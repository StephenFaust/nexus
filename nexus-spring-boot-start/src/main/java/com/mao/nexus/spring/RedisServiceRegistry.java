package com.mao.nexus.spring;

import com.mao.nexus.io.common.MateInfo;
import com.mao.nexus.io.common.ServiceInfo;
import com.mao.nexus.property.RegistryProperties;
import com.mao.nexus.registry.DefaultServiceRegistry;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author ：StephenMao
 * @date ：2022/6/14 14:29
 */
public class RedisServiceRegistry extends DefaultServiceRegistry {

    private static final String REDIS_KEY = "Nexus";

    private final JedisPool pool;


    private final ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("re-sch-" + Thread.currentThread().getId());
            thread.setDaemon(true);
            return thread;
        }
    });

    public RedisServiceRegistry(RegistryProperties registryProperties) {
        Assert.isTrue(StringUtils.hasLength(registryProperties.getIp()), "registry ip can't be empty");
        Assert.isTrue(registryProperties.getPort() > 0, "registry port error");
        pool = new JedisPool(registryProperties.getIp(), registryProperties.getPort());
    }


    @PostConstruct
    public void startHeartbeat() {
        logger.info("register start schedule task");
        scheduledExecutor.scheduleAtFixedRate(this::startRegisterTask, 0, 5, TimeUnit.SECONDS);
    }

    private void startRegisterTask() {
        final Jedis jedis = pool.getResource();
        try {
            for (Map.Entry<String, ServiceInfo> item : serviceInfos.entrySet()) {
                final MateInfo mateInfo = item.getValue();
                String key = String.format("%s:%s:%s-%d", REDIS_KEY, mateInfo.getServiceName(), mateInfo.getIp(), mateInfo.getPort());
                if (!jedis.exists(key)) {
                    jedis.set(key, "");
                }
                jedis.pexpire(key, 8000);
            }
        } catch (Exception ex) {
            logger.error("register scheduled task error,msg:{}", ex.getMessage());
        } finally {
            pool.returnResource(jedis);
        }

    }

}

package com.mao.nexus.spring;

import com.mao.nexus.annotation.NexusService;
import com.mao.nexus.io.common.ServiceInfo;
import com.mao.nexus.io.netty.server.network.RpcServer;
import com.mao.nexus.property.RpcProperties;
import com.mao.nexus.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author ：StephenMao
 * @date ：2022/6/14 13:36
 */
public class DefaultRpcListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRpcListener.class);

    private final ServiceRegistry serviceRegistry;

    private final RpcServer rpcServer;

    private final RpcProperties rpcProperties;

    public DefaultRpcListener(ServiceRegistry serviceRegistry,
                              RpcServer rpcServer,
                              RpcProperties rpcProperties) {
        this.serviceRegistry = serviceRegistry;
        this.rpcServer = rpcServer;
        this.rpcProperties = rpcProperties;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        final ApplicationContext applicationContext = event.getApplicationContext();
        // 是否为父容器
        if (applicationContext.getParent() == null) {
            // 启动rpc服务端
            startRpcServer(applicationContext);
        }
    }

    private void startRpcServer(ApplicationContext applicationContext) {
        final Map<String, Object> beans = applicationContext.getBeansWithAnnotation(NexusService.class);
        if (beans.size() == 0) {
            return;
        }
        for (Object obj : beans.values()) {
            final Class<?> clazz = obj.getClass();
            final Class<?>[] interfaces = clazz.getInterfaces();
            // 这里假设只实现了一个接口
            final Class<?> interfaceClazz = interfaces[0];
            final String serviceName = rpcProperties.getServiceName();
            final String clazzName = interfaceClazz.getName();
            String ip = "127.0.0.1";
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
            }
            final Integer port = rpcProperties.getPort();
            final ServiceInfo serviceInfo = new ServiceInfo(serviceName, ip, port, clazzName, interfaceClazz, obj);
            try {
                // 注册服务
                serviceRegistry.register(serviceInfo);
            } catch (Exception e) {
                logger.error("Fail to register service: {}", e.getMessage());
            }
        }
        // 启动 rpc 服务器，开始监听端口
        rpcServer.start();
    }
}


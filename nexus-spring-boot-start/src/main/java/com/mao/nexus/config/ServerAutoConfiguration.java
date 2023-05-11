package com.mao.nexus.config;

import com.mao.nexus.exception.handler.DefaultNexusExceptionHandler;
import com.mao.nexus.exception.handler.NexusExceptionHandler;
import com.mao.nexus.interceptor.NexusServerInterceptor;
import com.mao.nexus.io.netty.server.handler.RequestHandler;
import com.mao.nexus.io.netty.server.network.NettyRpcServer;
import com.mao.nexus.io.netty.server.network.RpcServer;
import com.mao.nexus.property.RpcProperties;
import com.mao.nexus.registry.ServiceRegistry;
import com.mao.nexus.serialize.Serializer;
import com.mao.nexus.spring.DefaultRpcListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

/**
 * @author StephenMao
 * @date 2022/6/26 12:57
 */
@Configuration
public class ServerAutoConfiguration {

    // 监听器
    @Bean
    public DefaultRpcListener defaultRpcListener(@Autowired ServiceRegistry serviceRegistry,
                                                 @Autowired RpcServer rpcServer,
                                                 @Autowired RpcProperties rpcProperties) {
        return new DefaultRpcListener(serviceRegistry, rpcServer, rpcProperties);
    }


    @Bean
    @Lazy
    @ConditionalOnMissingBean
    public NexusExceptionHandler exceptionHandler() {
        return new DefaultNexusExceptionHandler();
    }

    // 服务端
//    @Bean
//    public ServiceRegistry serviceRegister(@Autowired RegistryProperties registryProperties) {
//        ExtensionLoader<ServiceRegistry> extensionLoader =
//                ExtensionLoader.getExtensionLoader(ServiceRegistry.class);
//        return extensionLoader.getExtension(registryProperties.getProtocol(), new Class[]{RegistryProperties.class}, new Object[]{registryProperties});
//    }

    @Bean
    public RequestHandler requestHandler(@Autowired(required = false) ServiceRegistry serviceRegistry, @Autowired Serializer serializer, @Autowired NexusExceptionHandler exceptionInterceptor, @Autowired List<NexusServerInterceptor> interceptors) {
        return new RequestHandler(serviceRegistry, serializer, exceptionInterceptor, interceptors);
    }

    @Bean
    public RpcServer rpcServer(@Autowired RpcProperties rpcProperties,
                               @Autowired RequestHandler requestHandler) {
        final Integer port = rpcProperties.getPort();
        final String protocol = rpcProperties.getProtocol();
        final int maxWorkThreadCount = rpcProperties.getWorkThreadCount();
        return new NettyRpcServer(port, protocol, requestHandler, maxWorkThreadCount);
    }
}

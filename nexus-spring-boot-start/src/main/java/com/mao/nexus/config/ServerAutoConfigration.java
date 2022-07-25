package com.mao.nexus.config;

import com.mao.nexus.invocation.ClientProxyFactory;
import com.mao.nexus.io.netty.server.handler.RequestHandler;
import com.mao.nexus.io.netty.server.network.NettyRpcServer;
import com.mao.nexus.io.netty.server.network.RpcServer;
import com.mao.nexus.property.RegistryProperties;
import com.mao.nexus.property.RpcProperties;
import com.mao.nexus.registry.ServiceRegistry;
import com.mao.nexus.serialize.Serializer;
import com.mao.nexus.spi.ExtensionLoader;
import com.mao.nexus.spring.DefaultRpcListener;
import org.apache.log4j.pattern.BridgePatternConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author StephenMao
 * @date 2022/6/26 12:57
 */
@Configuration
//@ConditionalOnProperty(prefix = "nexus", name = "rpc-role", havingValue = "server")
public class ServerAutoConfigration {

    // 监听器
    @Bean
    public DefaultRpcListener defaultRpcListener(@Autowired ServiceRegistry serviceRegistry,
                                                 @Autowired RpcServer rpcServer,
                                                 @Autowired RpcProperties rpcProperties) {
        return new DefaultRpcListener(serviceRegistry, rpcServer, rpcProperties);
    }


//    // 服务端
//    @Bean
//    public ServiceRegistry serviceRegister(@Autowired RegistryProperties registryProperties) {
//        ExtensionLoader<ServiceRegistry> extensionLoader =
//                ExtensionLoader.getExtensionLoader(ServiceRegistry.class);
//        return extensionLoader.getExtension(registryProperties.getProtocol(), new Class[]{RegistryProperties.class}, new Object[]{registryProperties});
//    }

    @Bean
    public RequestHandler requestHandler(@Autowired(required = false) ServiceRegistry serviceRegistry, @Autowired Serializer serializer) {
        return new RequestHandler(serviceRegistry, serializer);
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

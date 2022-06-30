package com.mao.nexus.config;

import com.mao.nexus.discovery.ServiceDiscovery;
import com.mao.nexus.invocation.ClientProxyFactory;
import com.mao.nexus.io.netty.client.network.NettyRpcClient;
import com.mao.nexus.io.netty.client.network.NewNettyRpcClient;
import com.mao.nexus.io.netty.client.network.RpcClient;
import com.mao.nexus.cluster.loadbalance.LoadBalancer;
import com.mao.nexus.property.RegistryProperties;
import com.mao.nexus.property.RpcProperties;
import com.mao.nexus.serialize.Serializer;
import com.mao.nexus.spi.ExtensionLoader;
import com.mao.nexus.spring.DefaultRpcPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @author StephenMao
 * @date 2022/6/26 13:00
 */
@Configuration
@ConditionalOnProperty(prefix = "nexus", name = "rpc-role", havingValue = "client")
public class ClientAutoConfiguration {

    @Bean
    public BeanPostProcessor DefaultRpcPostProcessor(@Lazy @Autowired ClientProxyFactory clientProxyFactory) {
        return new DefaultRpcPostProcessor(clientProxyFactory);
    }


    // 客户端
    @Bean
    public ServiceDiscovery serviceDiscovery(@Autowired RegistryProperties registryProperties) {
        ExtensionLoader<ServiceDiscovery> extensionLoader =
                ExtensionLoader.getExtensionLoader(ServiceDiscovery.class);
        return extensionLoader.getExtension(registryProperties.getProtocol(), new Class[]{RegistryProperties.class}, new Object[]{registryProperties});
    }


    @Bean
    public ClientProxyFactory clientProxyFactory(@Autowired ServiceDiscovery serviceDiscovery, @Autowired Serializer serializer, @Autowired RpcClient rpcClient, @Autowired LoadBalancer loadBalancer, @Autowired RpcProperties rpcProperties) {
        return new ClientProxyFactory(serviceDiscovery, serializer, rpcClient, loadBalancer);
    }

    @Bean
    public RpcClient rpcClient(@Autowired RpcProperties rpcProperties, @Autowired Serializer serializer) {
        // return new NettyRpcClient(rpcProperties.getMaxConnection(), serializer);
        return new NewNettyRpcClient(rpcProperties.getTimeoutMillis(), serializer);
    }

}

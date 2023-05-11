package com.mao.nexus.config;

import com.mao.nexus.discovery.ServiceDiscovery;
import com.mao.nexus.interceptor.NexusClientInterceptor;
import com.mao.nexus.invocation.ClientProxyFactory;
import com.mao.nexus.io.netty.client.network.NettyRpcClient;
import com.mao.nexus.io.netty.client.network.RpcClient;
import com.mao.nexus.loadbalancer.LoadBalancer;
import com.mao.nexus.property.RegistryProperties;
import com.mao.nexus.property.RpcProperties;
import com.mao.nexus.serialize.Serializer;
import com.mao.nexus.spi.ExtensionLoader;
import com.mao.nexus.spring.DefaultRpcPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

/**
 * @author StephenMao
 * @date 2022/6/26 13:00
 */
@Configuration
public class ClientAutoConfiguration {

    @Bean
    public BeanPostProcessor DefaultRpcPostProcessor(@Lazy @Autowired ClientProxyFactory clientProxyFactory) {
        return new DefaultRpcPostProcessor(clientProxyFactory);
    }


    // 客户端
    @Bean
    public ServiceDiscovery serviceDiscovery(@Autowired RegistryProperties registryProperties, @Autowired DiscoveryClient discoveryClient) {
        ExtensionLoader<ServiceDiscovery> extensionLoader =
                ExtensionLoader.getExtensionLoader(ServiceDiscovery.class);
        return extensionLoader.getExtension(registryProperties.getProtocol(), new Class[]{RegistryProperties.class, DiscoveryClient.class}, new Object[]{registryProperties, discoveryClient});
    }


    @Bean
    public ClientProxyFactory clientProxyFactory(@Autowired ServiceDiscovery serviceDiscovery, @Autowired RpcClient rpcClient, @Autowired LoadBalancer loadBalancer, @Autowired List<NexusClientInterceptor> interceptors) {
        return new ClientProxyFactory(serviceDiscovery, rpcClient, loadBalancer, interceptors);
    }

    @Bean
    public RpcClient rpcClient(@Autowired RpcProperties rpcProperties, @Autowired Serializer serializer) {
        return new NettyRpcClient(rpcProperties.getMaxConnection(), rpcProperties.getTimeoutMillis(), serializer);
    }


}

package com.mao.nexus.config;


import com.mao.nexus.loadbalancer.LoadBalancer;
import com.mao.nexus.property.ClusterProperties;
import com.mao.nexus.property.RegistryProperties;
import com.mao.nexus.property.RpcProperties;
import com.mao.nexus.registry.ConsulServiceRegistry;
import com.mao.nexus.serialize.Serializer;
import com.mao.nexus.spi.ExtensionLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 16:53
 */
@Import({ClientAutoConfiguration.class, ServerAutoConfiguration.class, ConsulServiceRegistry.class})
@Configuration
@EnableConfigurationProperties({RpcProperties.class, ClusterProperties.class, RegistryProperties.class})
public class AutoConfiguration {


    @Bean
    public Serializer serializer(@Autowired RpcProperties rpcProperties) {
        ExtensionLoader<Serializer> extensionLoader =
                ExtensionLoader.getExtensionLoader(Serializer.class);
        return extensionLoader.getExtension(rpcProperties.getSerializationProtocol());
    }

    @Bean
    public LoadBalancer loadBalancer(@Autowired ClusterProperties clusterProperties) {
        ExtensionLoader<LoadBalancer> extensionLoader = ExtensionLoader.getExtensionLoader(LoadBalancer.class);
        return extensionLoader.getExtension(clusterProperties.getLoadBalance());
    }

}

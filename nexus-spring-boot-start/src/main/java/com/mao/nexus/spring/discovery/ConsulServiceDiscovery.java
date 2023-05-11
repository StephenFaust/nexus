package com.mao.nexus.spring.discovery;

import com.mao.nexus.discovery.ServiceDiscovery;
import com.mao.nexus.io.common.MateInfo;
import com.mao.nexus.property.RegistryProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ：huangjinmao
 * @date ：2022/7/12 15:22
 */
public class ConsulServiceDiscovery implements ServiceDiscovery {
    public ConsulServiceDiscovery(RegistryProperties registryProperties, DiscoveryClient discoveryClient) {
        this.registryProperties = registryProperties;
        this.discoveryClient = discoveryClient;
    }

    private final RegistryProperties registryProperties;

    private final DiscoveryClient discoveryClient;

    @Override
    public List<MateInfo> listServices(String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        return instances.stream().map(o -> MateInfo.builder()
                .serviceName(serviceName)
                .ip(o.getHost())
                .port(Integer.parseInt(o.getMetadata().get("nexus_port"))).build()).collect(Collectors.toList());
    }
}

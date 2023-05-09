package com.mao.nexus.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author StephenMao
 * @date 2022/6/25 11:34
 */
@ConfigurationProperties(prefix = "nexus.registry")
@Data
public class RegistryProperties {
    //注册中心方式
    private String protocol = DefaultProperties.DEFAULT_REGISTRY_PROTOCOL;

    private String ip;

    private int port;
}

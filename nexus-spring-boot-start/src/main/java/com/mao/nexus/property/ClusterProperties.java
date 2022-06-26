package com.mao.nexus.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ：StephenMao
 * @date ：2022/6/23 11:36
 */

@ConfigurationProperties(prefix = "nexus.cluster")
@Data
public class ClusterProperties {
    //负载均衡策略
    private String loadBalance = DefaultProperties.DEFAULT_LOAD_BALANCE;
}

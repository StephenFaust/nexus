package com.mao.nexus.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 16:53
 */

@ConfigurationProperties(prefix = "nexus")
@Data
public class RpcProperties {

    //服务名
    private String serviceName;
    //服务端端口
    private Integer port = DefaultProperties.DEFAULT_PORT;

    //协议
    private String protocol = DefaultProperties.DEFAULT_PROTOCOL;

    //客户端连接池最大连接数
    private int maxConnection = DefaultProperties.DEFAULT_MAX_CONNECTION;

    //服务端工作线程数
    private int workThreadCount = DefaultProperties.DEFAULT_WORK_THREAD_COUNT;

    //序列化方式
    private String serializationProtocol = DefaultProperties.DEFAULT_SERIALIZATION_PROTOCOL;

    //客户端超时时间
    private int timeoutMillis = DefaultProperties.DEFAULT_TIMEOUT_MILLIS;

}

package com.mao.nexus.property;

import com.mao.nexus.property.enums.RpcRole;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.DependsOn;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 16:53
 */

@ConfigurationProperties(prefix = "nexus")
@Data
public class RpcProperties {

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

    //客户端超市时间
    private int timeoutMillis = DefaultProperties.DEFAULT_Timeout_Millis;

    //角色
    private RpcRole rpcRole;
}

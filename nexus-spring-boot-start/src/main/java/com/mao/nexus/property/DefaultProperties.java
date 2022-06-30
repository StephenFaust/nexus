package com.mao.nexus.property;

import com.mao.nexus.property.enums.RpcRole;

/**
 * @author ：StephenMao
 * @date ：2022/6/22 16:07
 */
public class DefaultProperties {
    private DefaultProperties() {
    }

    public static final String DEFAULT_PROTOCOL = "nexus";
    //连接池默认连接数
    public static final int DEFAULT_MAX_CONNECTION = 15;
    //默认端口号
    public static final int DEFAULT_PORT = 1235;
    //默认工作线程数
    public static final int DEFAULT_WORK_THREAD_COUNT = Runtime.getRuntime().availableProcessors() << 1;

    public static final String DEFAULT_LOAD_BALANCE = "train";

    public static final String DEFAULT_SERIALIZATION_PROTOCOL = "kryo";

    public static final String DEFAULT_REGISTRY_PROTOCOL = "redis";

    //客户端超时时间，默认为20分钟
    public static final int DEFAULT_Timeout_Millis = 1200_000;

}

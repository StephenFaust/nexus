package com.mao.nexus.interceptor;

import com.mao.nexus.io.common.RpcRequest;
import com.mao.nexus.io.common.RpcResponse;

public interface NexusServerInterceptor {
    default boolean beforeInvoke(RpcRequest request, RpcResponse response) {
        return true;
    }
}

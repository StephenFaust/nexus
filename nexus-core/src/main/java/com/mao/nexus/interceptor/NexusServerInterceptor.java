package com.mao.nexus.interceptor;

import com.mao.nexus.io.common.RpcRequest;
import com.mao.nexus.io.common.RpcResponse;

/**
 * @author stephenmao
 */
public interface NexusServerInterceptor {
    default boolean beforeInvoke(RpcRequest request, RpcResponse response) {
        return true;
    }
}

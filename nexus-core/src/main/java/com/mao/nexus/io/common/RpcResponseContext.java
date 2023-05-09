package com.mao.nexus.io.common;


public class RpcResponseContext {


    private RpcResponseContext() {
    }


    private static final ThreadLocal<RpcResponse> responseThreadLocal = ThreadLocal.withInitial(RpcResponse::new);
}

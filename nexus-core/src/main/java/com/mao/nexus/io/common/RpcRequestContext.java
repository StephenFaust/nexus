package com.mao.nexus.io.common;


/**
 * @author stephenmao
 */
@Deprecated
public class RpcRequestContext {

    private RpcRequestContext() {
    }


    private static final ThreadLocal<RpcRequest> requestThreadLocal = ThreadLocal.withInitial(RpcRequest::new);


    public static RpcRequest getRequest() {
        return requestThreadLocal.get();
    }


    public static void setRequest(RpcRequest request) {
        requestThreadLocal.set(request);
    }

    public static void
    removeRequest() {
        requestThreadLocal.remove();
    }


}

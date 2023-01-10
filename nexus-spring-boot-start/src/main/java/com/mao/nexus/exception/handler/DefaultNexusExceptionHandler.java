package com.mao.nexus.exception.handler;

import com.mao.nexus.exception.RpcException;
import com.mao.nexus.io.common.RpcResponse;

public class DefaultNexusExceptionHandler implements NexusExceptionHandler {
    @Override
    public void handel(Throwable th, RpcResponse response) {
        response.setStatus("Fail");
        response.setException(new RpcException("Rpc invoke fail,msg: " + th.getCause(), th));
    }
}

package com.mao.nexus.exception.handler;

import com.mao.nexus.exception.RpcException;
import com.mao.nexus.io.common.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultNexusExceptionHandler implements NexusExceptionHandler {


    private static final Logger logger = LoggerFactory.getLogger(DefaultNexusExceptionHandler.class);

    @Override
    public void handel(Throwable th, RpcResponse response) {
        response.setStatus("Fail");
        RpcException rpcException = new RpcException("Rpc invoke fail,msg: " + th.getCause(), th);
        response.setException(rpcException);
        logger.error("nexus error ", rpcException);
    }
}

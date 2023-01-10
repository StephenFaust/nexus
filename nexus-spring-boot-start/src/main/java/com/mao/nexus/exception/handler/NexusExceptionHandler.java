package com.mao.nexus.exception.handler;

import com.mao.nexus.io.common.RpcResponse;

/**
 * @author ：StephenMao
 * @date ：2022/6/16 15:14
 */
public interface NexusExceptionHandler {
    void handel(Throwable th, RpcResponse response);


}

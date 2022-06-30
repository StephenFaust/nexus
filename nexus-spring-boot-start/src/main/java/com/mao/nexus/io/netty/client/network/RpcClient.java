package com.mao.nexus.io.netty.client.network;


import com.mao.nexus.io.common.MateInfo;
import com.mao.nexus.io.common.RpcRequest;
import com.mao.nexus.io.common.RpcResponse;

import java.util.concurrent.ExecutionException;

/**
 * @author ：StephenMao
 * @date ：2022/6/14 13:38
 */
public interface RpcClient {

    /**
     * 发起请求
     *
     * @param request
     * @param mateInfo
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    RpcResponse sendMessage(RpcRequest request, MateInfo mateInfo) throws ExecutionException, InterruptedException;
}

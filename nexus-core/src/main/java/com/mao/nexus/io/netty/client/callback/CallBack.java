package com.mao.nexus.io.netty.client.callback;

import com.mao.nexus.io.common.RpcResponse;

/**
 * @author ：StephenMao
 * @date ：2022/6/29 14:42
 */
public interface CallBack {

    /**
     * 处理结果
     *
     * @param response
     */
    void handle(RpcResponse response);


    /**
     * 关闭
     */
    void close();

    /**
     * 获取结果
     *
     * @param uniqueIdentification
     * @param timeoutMillis
     * @return
     */
    RpcResponse getResult(Long uniqueIdentification, int timeoutMillis);

}

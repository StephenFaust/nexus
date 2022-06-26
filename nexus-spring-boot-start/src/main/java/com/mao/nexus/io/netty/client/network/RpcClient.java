package com.mao.nexus.io.netty.client.network;


import com.mao.nexus.io.common.MateInfo;

import java.util.concurrent.ExecutionException;

/**
 * @author ：StephenMao
 * @date ：2022/6/14 13:38
 */
public interface RpcClient {
    /**
     *
     * @param data 待发送的消息
     * @param serviceInfo 消息接收者
     * @return 已发送消息
     */
    byte[] sendMessage(byte[] data, MateInfo serviceInfo) throws InterruptedException, ExecutionException;
}

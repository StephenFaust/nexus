package com.mao.nexus.io.netty.client.channel;

import com.mao.nexus.io.common.RpcResponse;
import com.mao.nexus.io.netty.client.callback.CallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ：StephenMao
 * @date ：2022/6/29 14:41
 */
public class ChannelCallBack implements CallBack {


    private static Logger logger = LoggerFactory.getLogger(ChannelCallBack.class);

    private RpcResponse resp = null;

    @Override
    public void handle(RpcResponse response) {
        this.resp = response;
        synchronized (this) {
            this.notify();
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            this.notify();
        }
    }


    @Override
    public RpcResponse getResult(Long uniqueIdentification, int timeoutMillis) {
        try {
            synchronized (this) {
                this.wait(timeoutMillis);
            }
        } catch (InterruptedException ex) {
            logger.error("result is interrupted,msg:{}", ex.getMessage());
        } finally {
            ChannelManger.CALLBACK_CACHES.remove(uniqueIdentification);
        }
        return resp;
    }
}

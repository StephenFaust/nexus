package com.mao.nexus.io.netty.client.network;

import com.mao.nexus.exception.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ：StephenMao
 * @date ：2022/6/14 13:38
 */
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientChannelHandler.class);

    private byte[] response;

    public ClientChannelHandler() {
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("client accept success");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("Client received massage: {}", msg);
        // 将 byteBuf 转成 byte[]
        final ByteBuf buffer = (ByteBuf) msg;
        try {
            response = new byte[buffer.readableBytes()];
            buffer.readBytes(response);
            synchronized (this) {
                this.notify();
            }
        } finally {
            ReferenceCountUtil.release(buffer);
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Catch exception: {}", cause.getMessage());
        ctx.close();
        synchronized (this) {
            this.notify();
        }
    }

    public byte[] response() throws InterruptedException {
        synchronized (this) {
            //这里使用锁才能使用wait/notify
            this.wait();
        }
        return response;
    }
}

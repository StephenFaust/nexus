package com.mao.nexus.io.netty.client.network.handler;

import com.mao.nexus.io.common.RpcResponse;
import com.mao.nexus.io.netty.client.callback.CallBack;
import com.mao.nexus.io.netty.client.channel.ChannelCallBack;
import com.mao.nexus.io.netty.client.channel.ChannelManger;
import com.mao.nexus.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ：StephenMao
 * @date ：2022/6/29 15:05
 */
@ChannelHandler.Sharable
public class NewClientChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NewClientChannelHandler.class);


    private final Serializer serializer;

    public NewClientChannelHandler(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("client accept success");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("Client received massage: {}", msg);
        final ByteBuf buffer = (ByteBuf) msg;
        try {
            byte[] response = new byte[buffer.readableBytes()];
            buffer.readBytes(response);
            RpcResponse rpcResponse = serializer.deserialize(response, RpcResponse.class);
            ChannelManger.UNIQUE_IDENTIFICATION_CACHES.set(rpcResponse.getUniqueIdentification());
            CallBack callBackService = ChannelManger.CALLBACK_CACHES.get(rpcResponse.getUniqueIdentification());
            if (callBackService != null) {
                callBackService.handle(rpcResponse);
            } else {
                logger.info("receive message fail");
            }
        } finally {
            //一定要将byteBuf释放掉，不然会内存泄漏
            ReferenceCountUtil.release(buffer);
        }
        // 将 byteBuf 转成 byte[]
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Catch exception: {}", cause.getMessage());
        try {
            Long uniqueIdentification = ChannelManger.UNIQUE_IDENTIFICATION_CACHES.get();
            CallBack callBackService = ChannelManger.CALLBACK_CACHES.get(uniqueIdentification);
            if (callBackService != null) {
                callBackService.close();
            } else {
                logger.info("close callBack fail");
            }
        } finally {
            ChannelManger.UNIQUE_IDENTIFICATION_CACHES.remove();
        }
    }

}

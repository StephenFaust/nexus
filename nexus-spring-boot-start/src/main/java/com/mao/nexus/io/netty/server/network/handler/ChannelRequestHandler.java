package com.mao.nexus.io.netty.server.network.handler;

import com.mao.nexus.io.netty.server.handler.RequestHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.*;

/**
 * @author ：StephenMao
 * @date ：2022/6/30 11:08
 */
public class ChannelRequestHandler extends AbsRequestHandler {


    public ChannelRequestHandler(RequestHandler requestHandler, Executor executor) {
        super(requestHandler);
        this.executor = executor;
    }


    private final Executor executor;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channel active: {}", ctx.channel().id());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        executor.execute(() -> doWork(ctx, msg));
    }


    private void doWork(ChannelHandlerContext ctx, Object msg) {
        logger.info("Server receive a massage {}", msg);
        final ByteBuf msgBuf = (ByteBuf) msg;
        try {
            final byte[] reqBytes = new byte[msgBuf.readableBytes()];
            msgBuf.readBytes(reqBytes);
            final byte[] respBytes;
            respBytes = requestHandler.handleRequest(reqBytes);
            final ByteBuf resBuf = Unpooled.buffer(respBytes.length);
            resBuf.writeBytes(respBytes);
            ctx.writeAndFlush(resBuf);
        } catch (Exception e) {
            logger.error("error,{}", e.getMessage());
        } finally {
            ReferenceCountUtil.release(msgBuf);
        }
    }

    @Override
    public void idlHandle(ChannelHandlerContext ctx) {
        logger.info("idl event execute");
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Catch exception", cause);
    }
}


package com.mao.nexus.io.netty.server.network.handler;

import com.mao.nexus.io.netty.server.handler.RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ：StephenMao
 * @date ：2022/6/30 11:01
 */

public abstract class AbsRequestHandler extends ChannelInboundHandlerAdapter {


    protected RequestHandler requestHandler;

    public AbsRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    protected static Logger logger = LoggerFactory.getLogger(AbsRequestHandler.class);

    @Override
    public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        idlHandle(ctx);
    }

    public void idlHandle(ChannelHandlerContext ctx) {


    }

}

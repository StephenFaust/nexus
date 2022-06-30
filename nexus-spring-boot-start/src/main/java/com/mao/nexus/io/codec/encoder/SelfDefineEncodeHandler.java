package com.mao.nexus.io.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ：huangjinmao
 * @date ：2022/6/30 13:40
 */
public class SelfDefineEncodeHandler extends MessageToByteEncoder<ByteBuf> {


    private final static Logger logger = LoggerFactory.getLogger(SelfDefineEncodeHandler.class);

    final int length;

    public SelfDefineEncodeHandler(int length) {
        this.length = length;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int i = msg.readableBytes();
        long unique = System.nanoTime();
        out.writeInt(i);
        out.writeLong(unique);
        out.writeBytes(msg);
    }
}
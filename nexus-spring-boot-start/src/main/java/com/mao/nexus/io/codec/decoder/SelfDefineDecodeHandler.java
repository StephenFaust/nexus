package com.mao.nexus.io.codec.decoder;

import com.mao.nexus.io.netty.client.channel.ChannelManger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author ：StephenMao
 * @date ：2022/6/30 13:42
 */
public class SelfDefineDecodeHandler extends ByteToMessageDecoder {


    //仅适用在64位平台上,为啥不用Integer.BYTES，这个会做除法运算，想省去
    private static final int INTEGER_LENGTH = 4;

    private static final int LONG_LENGTH = 8;


    @Override

    protected void decode(ChannelHandlerContext ctx, ByteBuf bufferIn, List<Object> out) throws Exception {
        if (bufferIn.readableBytes() < INTEGER_LENGTH) {
            return;
        }
        int beginIndex = bufferIn.readerIndex();
        int length = bufferIn.readInt();
        if (bufferIn.readableBytes() < length) {
            bufferIn.readerIndex(beginIndex);
            return;
        }
        ChannelManger.UNIQUE_IDENTIFICATION_CACHES.set(bufferIn.readLong());
        int countLength = INTEGER_LENGTH + LONG_LENGTH;
        bufferIn.readerIndex(beginIndex + countLength + length);
        bufferIn.skipBytes(countLength);
        ByteBuf otherByteBufRef = bufferIn.slice(beginIndex, length);
        otherByteBufRef.retain();
        out.add(otherByteBufRef);
    }
}

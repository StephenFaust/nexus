package com.mao.nexus.io.netty.client.channelpool.handler;


import com.mao.nexus.io.netty.client.channel.ChannelManger;
import com.mao.nexus.io.netty.client.network.handler.NewClientChannelHandler;
import com.mao.nexus.serialize.Serializer;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;


/**
 * @author ：StephenMao
 * @date ：2022/6/20 15:29
 */
public class DefaultChannelPoolHandler implements ChannelPoolHandler {

    private final Logger logger = LoggerFactory.getLogger(DefaultChannelPoolHandler.class);

    private final Serializer serializer;

    private final Executor executor;

    public DefaultChannelPoolHandler(Serializer serializer, Executor executor) {
        this.serializer = serializer;
        this.executor = executor;
    }

    @Override
    public void channelReleased(Channel channel) throws Exception {

    }

    @Override
    public void channelAcquired(Channel channel) throws Exception {

    }

    @Override
    public void channelCreated(Channel ch) throws Exception {
        logger.debug("channelCreated. Channel ID: " + ch.id());
        SocketChannel channel = (SocketChannel) ch;
        //channel.config().setKeepAlive(true);
        //channel.config().setTcpNoDelay(true);
        NewClientChannelHandler clientChannelHandler = new NewClientChannelHandler(serializer, executor);
        channel.attr(ChannelManger.attributeKey).set(clientChannelHandler);
        channel.pipeline()
                //心跳支持
                // .addLast(new IdleStateHandler(3, 0, 0, TimeUnit.SECONDS))
                //解决半包和粘包
                .addLast("encode", new LengthFieldPrepender(8))
                .addLast("decode", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0,
                        8, 0, 8))
                .addLast(clientChannelHandler);
    }


}

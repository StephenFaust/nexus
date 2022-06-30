package com.mao.nexus.io.netty.client.network;

import com.mao.nexus.exception.RpcException;
import com.mao.nexus.io.common.MateInfo;
import com.mao.nexus.io.common.RpcRequest;
import com.mao.nexus.io.common.RpcResponse;
import com.mao.nexus.io.netty.client.callback.CallBack;
import com.mao.nexus.io.netty.client.channel.ChannelCallBack;
import com.mao.nexus.io.netty.client.channel.ChannelManger;
import com.mao.nexus.io.netty.client.network.handler.NewClientChannelHandler;
import com.mao.nexus.serialize.Serializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

/**
 * @author ：huangjinmao
 * @date ：2022/6/29 15:01
 */
public class NewNettyRpcClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NewNettyRpcClient.class);

    private final Serializer serializer;

    private ChannelInboundHandlerAdapter clientHandler;

    private Bootstrap bootstrap;

    private final int timeoutMillis;

    public NewNettyRpcClient(int timeoutMillis,Serializer serializer) {
        this.timeoutMillis = timeoutMillis;
        this.serializer = serializer;
    }

    @PostConstruct
    private void init() {
        this.clientHandler = new NewClientChannelHandler(serializer);
        // 初始化 netty 客户端
        final NioEventLoopGroup eventGroup = new NioEventLoopGroup(20);
        bootstrap = new Bootstrap()
                .group(eventGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("encode", new LengthFieldPrepender(8))
                                .addLast("decode", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0,
                                        8, 0, 8))
                                .addLast(clientHandler);
                    }
                });
    }

    @Override
    public RpcResponse sendMessage(RpcRequest request, MateInfo mateInfo) {
        final String ip = mateInfo.getIp();
        final Integer port = mateInfo.getPort();
        InetSocketAddress address = new InetSocketAddress(ip, port);
        Channel channel = ChannelManger.CHANNEL_CACHES.get(address);
        try {
            if (null == channel) {
                channel = getChannel(ip, port);
            } else {
                if (!channel.isActive()) {
                    closeChannel(channel);
                    channel = getChannel(ip, port);
                }
            }
            ChannelManger.CHANNEL_CACHES.put(address, channel);
        } catch (Exception ex) {
            throw new RpcException(ex.getMessage());
        }
        logger.info("use channel:{}", channel);
        request.setUniqueIdentification(System.nanoTime());
        CallBack callback = new ChannelCallBack();
        ChannelManger.CALLBACK_CACHES.put(request.getUniqueIdentification(), callback);
        byte[] data = serializer.serialize(request);
        final ByteBuf buffer = Unpooled.buffer(data.length);
        buffer.writeBytes(data);
        channel.writeAndFlush(buffer);
        return callback.getResult(request.getUniqueIdentification(), timeoutMillis);
    }

    private Channel getChannel(String ip, int port) throws InterruptedException {
        return bootstrap.connect(ip, port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    logger.info("{}:{} connect success", ip, port);
                }
            }
        }).sync().channel();
    }

    private void closeChannel(Channel channel) {
        final ChannelId id = channel.id();
        channel.close().addListener(future -> {
            if (future.isSuccess()) {
                logger.info("close channel id:{}", id);
            }
        });
    }
}

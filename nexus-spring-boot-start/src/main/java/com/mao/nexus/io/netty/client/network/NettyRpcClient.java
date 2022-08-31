package com.mao.nexus.io.netty.client.network;

import com.mao.nexus.io.common.MateInfo;
import com.mao.nexus.io.common.RpcRequest;
import com.mao.nexus.io.common.RpcResponse;
import com.mao.nexus.io.netty.client.callback.CallBack;
import com.mao.nexus.io.netty.client.channel.ChannelCallBack;
import com.mao.nexus.io.netty.client.channel.ChannelManger;
import com.mao.nexus.io.netty.client.channelpool.NettyPoolClient;
import com.mao.nexus.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.concurrent.*;

/**
 * @author ：StephenMao
 * @date ：2022/6/14 13:38
 */
public class NettyRpcClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcClient.class);

    private final int maxConnection;

    private final Serializer serializer;

    private final int timeoutMillis;

    private final static int CLIENT_THREADS_NUMBER = Runtime.getRuntime().availableProcessors();

    public NettyRpcClient(int maxConnection, int timeoutMillis, Serializer serializer) {
        this.maxConnection = maxConnection;
        this.timeoutMillis = timeoutMillis;
        this.serializer = serializer;
    }


    @PostConstruct
    public void init() {
        final Executor executor = new ThreadPoolExecutor(CLIENT_THREADS_NUMBER, CLIENT_THREADS_NUMBER, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("client-work-th-" + thread.getId());
            return thread;
        });
        NettyPoolClient.init(maxConnection, serializer, executor);
    }


    @Override
    public RpcResponse sendMessage(RpcRequest request, MateInfo mateInfo) throws ExecutionException, InterruptedException {
        final String ip = mateInfo.getIp();
        final Integer port = mateInfo.getPort();
        final InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, port);
        final NettyPoolClient poolClient = NettyPoolClient.getInstance();
        //根据地址获得池时，如果poolMap没有这个池，则会put一个生成新的池
        final Channel channel = poolClient.getChannel(inetSocketAddress);
        CallBack callback = new ChannelCallBack();
        try {
            logger.info("use channel:{}", channel);
            request.setUniqueIdentification(System.nanoTime());
            ChannelManger.CALLBACK_CACHES.put(request.getUniqueIdentification(), callback);
            //序列化
            final byte[] data = serializer.serialize(request);
            final ByteBuf buffer = Unpooled.buffer(data.length);
            buffer.writeBytes(data);
            channel.writeAndFlush(buffer);
//            final ClientChannelHandler handler = (ClientChannelHandler) channel.attr(ChannelManger.attributeKey).get();
//            result = handler.response();
        } catch (Exception ex) {
            logger.error("send message error,msg:{}", ex.getMessage());
        } finally {
            //返还给连接池
            poolClient.release(inetSocketAddress, channel);
        }
        return callback.getResult(request.getUniqueIdentification(), timeoutMillis);
    }
}

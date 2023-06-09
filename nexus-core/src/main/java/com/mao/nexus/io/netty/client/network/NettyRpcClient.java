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
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author ：StephenMao
 * @date ：2022/6/14 13:38
 */
public class NettyRpcClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcClient.class);

    private final int maxConnection;

    private final Serializer serializer;

    private final int timeoutMillis;

    private static final int CLIENT_THREADS_NUMBER = Runtime.getRuntime().availableProcessors();

    private final AtomicLong IdentificationGenerator;

    public NettyRpcClient(int maxConnection, int timeoutMillis, Serializer serializer) {
        this.maxConnection = maxConnection;
        this.timeoutMillis = timeoutMillis;
        this.serializer = serializer;
        this.IdentificationGenerator = new AtomicLong();
    }


    @PostConstruct
    public void init() {
        final Executor executor = new ThreadPoolExecutor(CLIENT_THREADS_NUMBER, CLIENT_THREADS_NUMBER, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(String.format("nexus-client-executor-%d", thread.getId()));
            return thread;
        });
        NettyPoolClient.init(maxConnection, serializer, executor);
    }


    @Override
    public RpcResponse sendMessage(RpcRequest request, MateInfo mateInfo) {
        final String ip = mateInfo.getIp();
        final Integer port = mateInfo.getPort();
        final InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, port);
        final NettyPoolClient poolClient = NettyPoolClient.getInstance();
        CallBack callback = new ChannelCallBack();
        try {
            request.setUniqueIdentification(IdentificationGenerator.incrementAndGet());
            ChannelManger.CALLBACK_CACHES.put(request.getUniqueIdentification(), callback);
            //序列化
            final byte[] data = serializer.serialize(request);
            final ByteBuf buffer = Unpooled.buffer(data.length);
            buffer.writeBytes(data);
            //根据地址获得池时，如果poolMap没有这个池，则会put一个生成新的池
            final Channel channel = poolClient.getChannel(inetSocketAddress);
            logger.info("use channel:{}", channel);
            try {
                channel.writeAndFlush(buffer);
            } catch (Exception ex) {
                logger.error("send message error,msg:{}", ex.getMessage());
            } finally {
                //返还给连接池
                poolClient.release(inetSocketAddress, channel);
            }
        } catch (Exception ex) {
            logger.error("send message error,msg:{}", ex.getMessage());
        }
        return callback.getResult(request.getUniqueIdentification(), timeoutMillis);
    }
}

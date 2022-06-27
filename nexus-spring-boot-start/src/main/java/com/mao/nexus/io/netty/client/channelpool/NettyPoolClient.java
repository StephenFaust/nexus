package com.mao.nexus.io.netty.client.channelpool;


import com.mao.nexus.exception.RpcException;
import com.mao.nexus.io.netty.client.channelpool.handler.DefaultChannelPoolHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

/**
 * @author ：StephenMao
 * @date ：2022/6/20 15:24
 */
public class NettyPoolClient {

    private static volatile NettyPoolClient instance;

    private NettyPoolClient() {

    }

    /**
     * 初始化
     *
     * @param maxConnection
     */
    public static void init(int maxConnection) throws RpcException {
        try {
            instance = new NettyPoolClient();
            instance.build(maxConnection);
        } catch (Exception ex) {
            throw new RpcException("NettyPoolClient Initialization failed,msg:" + ex.getMessage());
        }
    }


    public static NettyPoolClient getInstance() {
        return instance;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        if (inetSocketAddress == null)
            throw new IllegalArgumentException("inetSocketAddress can't be null");
        final SimpleChannelPool pool = poolMap.get(inetSocketAddress);
        //获得池中的通道，写入参数，归还通道；这里是异步处理
        final Future<Channel> f = pool.acquire();
        //阻塞获取
        return f.get();
    }

    public void release(InetSocketAddress inetSocketAddress, Channel channel) {
        if (channel == null)
            return;
        final SimpleChannelPool pool = poolMap.get(inetSocketAddress);
        pool.release(channel);
    }

    private ChannelPoolMap<InetSocketAddress, SimpleChannelPool> poolMap;
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap strap = new Bootstrap();

    public void build(int maxConnection) {
        // ChannelOption.TCP_NODELAY 禁用nagle算法，从而可以发送较小的包，降低延迟
        // ChannelOption.SO_KEEPALIVE 隔一段时间（两小时左右）探测服务端是否活跃，如果没有活跃关闭socket--意义不大
        strap.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);
        poolMap = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {
            @Override
            protected SimpleChannelPool newPool(InetSocketAddress key) {
                //NettyChannelPoolHandler 实现ChannelPoolHandler，重写了创建通道、获得通道、归还通道方法
                return new FixedChannelPool(strap.remoteAddress(key), new DefaultChannelPoolHandler(), maxConnection);
            }
        };
    }
}

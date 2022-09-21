package com.mao.nexus.io.netty.server.network;

import com.mao.nexus.io.netty.server.handler.RequestHandler;
import com.mao.nexus.io.netty.server.network.handler.ChannelRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ：StephenMao
 * @date ：2022/6/14 9:41
 */
public class NettyRpcServer extends RpcServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);

    private Channel channel;


    public NettyRpcServer(int port, String protocol, RequestHandler requestHandler, int maxWorkThreadCount) {
        super(port, protocol, requestHandler, maxWorkThreadCount);
    }


    @Override
    public void start() {
        // 创建两个线程组，io线程池
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        // 业务线程池
        final Executor executor = new ThreadPoolExecutor(maxWorkThreadCount, maxWorkThreadCount, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("ser-work-th-" + thread.getId());
            return thread;
        });
//        EventExecutorGroup businessGroup = new DefaultEventExecutorGroup(maxWorkThreadCount, new ThreadFactory() {
//            @Override
//            public Thread newThread(@NotNull Runnable r) {
//                Thread thread = new Thread(r);
//                thread.setName("work-th-" + thread.getId());
//                thread.setDaemon(true);
//                return thread;
//            }
//        });
        try {
            // 创建服务端的启动对象
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    // 设置两个线程组
                    .group(bossGroup, workerGroup)
                    // 设置服务端通道实现类型
                    .channel(NioServerSocketChannel.class)
                    // 服务端用于接收进来的连接，也就是boosGroup线程, 线程队列大小
                    .option(ChannelOption.SO_BACKLOG, 200)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // child 通道，worker 线程处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 给 pipeline 管道设置自定义的处理器
                        @Override
                        public void initChannel(SocketChannel channel) {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.MINUTES));
                            pipeline.addLast("encoder", new LengthFieldPrepender(8));
                            pipeline.addLast("decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0,
                                    8, 0, 8));
                            pipeline.addLast("handler", new ChannelRequestHandler(requestHandler, executor));
                        }
                    });

            // 绑定端口号，同步启动服务
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            logger.info("[Nexus]Rpc Server started on port: {}", port);
            channel = channelFuture.channel();
            // 对关闭通道进行监听
            channelFuture.channel().closeFuture().addListener(future -> {
                if (future.isSuccess()) {
                    // 释放线程组资源
                    try {
                        bossGroup.shutdownGracefully();
                        workerGroup.shutdownGracefully();
                    } catch (Exception e) {
                        logger.error("server closed error.", e);
                    }

                }

            });

        } catch (Exception e) {
            logger.error("server error.", e);
        }
    }

    @Override
    public void stop() {
        channel.close();
    }

}


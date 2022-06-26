package com.mao.nexus.io.netty.server.network;

import com.mao.nexus.io.netty.server.handler.RequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;

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
        EventExecutorGroup businessGroup = new DefaultEventExecutorGroup(maxWorkThreadCount, new ThreadFactory() {
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("work-th-" + Thread.currentThread().getId());
                return thread;
            }
        });
        try {
            // 创建服务端的启动对象
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    // 设置两个线程组
                    .group(bossGroup, workerGroup)
                    // 设置服务端通道实现类型
                    .channel(NioServerSocketChannel.class)
                    // 服务端用于接收进来的连接，也就是boosGroup线程, 线程队列大小
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // child 通道，worker 线程处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 给 pipeline 管道设置自定义的处理器
                        @Override
                        public void initChannel(SocketChannel channel) {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast("encoder", new LengthFieldPrepender(8));
                            pipeline.addLast("decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0,
                                    8, 0, 8));
                            pipeline.addLast(businessGroup, "work executor", new ChannelRequestHandler());
                        }
                    });

            // 绑定端口号，同步启动服务
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            logger.info("[Nexus]Rpc Server started on port: {}", port);
            channel = channelFuture.channel();
            // 对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("server error.", e);
        } finally {
            // 释放线程组资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {
        channel.close();
    }

    private class ChannelRequestHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            logger.info("channel active: {}", ctx.channel().id());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // logger.info("Server receive a massage");
            try {
                final ByteBuf msgBuf = (ByteBuf) msg;
                final byte[] reqBytes = new byte[msgBuf.readableBytes()];
                msgBuf.readBytes(reqBytes);
                final byte[] respBytes;
                respBytes = requestHandler.handleRequest(reqBytes);
                final ByteBuf resBuf = Unpooled.buffer(respBytes.length);
                resBuf.writeBytes(respBytes);
                ctx.writeAndFlush(resBuf);
            } catch (Exception e) {
                logger.error("error,{}", e.getMessage());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.error("Catch exception", cause);
            ctx.close();
        }
    }
}


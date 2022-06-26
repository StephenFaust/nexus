package com.mao.nexus.io.netty.server.network;


import com.mao.nexus.io.netty.server.handler.RequestHandler;

/**
 * @author ：StephenMao
 * @date ：2022/6/14 8:50
 */
public abstract class RpcServer {
    protected int port;

    protected String protocol;

    protected RequestHandler requestHandler;

    protected int maxWorkThreadCount;

    public RpcServer(int port, String protocol, RequestHandler requestHandler, int maxWorkThreadCount) {
        this.port = port;
        this.protocol = protocol;
        this.requestHandler = requestHandler;
        this.maxWorkThreadCount = maxWorkThreadCount;
    }

    /**
     * 启动服务
     */
    public abstract void start();

    /**
     * 停止服务
     */
    public abstract void stop();

}


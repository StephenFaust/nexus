package com.mao.nexus.io.netty.server.handler;


import com.mao.nexus.exception.handler.NexusExceptionHandler;
import com.mao.nexus.io.common.RpcRequest;
import com.mao.nexus.io.common.RpcResponse;
import com.mao.nexus.io.common.ServiceInfo;
import com.mao.nexus.serialize.Serializer;
import com.mao.nexus.registry.ServiceRegistry;

import java.lang.reflect.Method;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 19:48
 */
public class RequestHandler {

    private final ServiceRegistry serviceRegistry;

    private final Serializer serializer;

    private final NexusExceptionHandler exceptionInterceptor;

    public RequestHandler(ServiceRegistry serviceRegistry, Serializer serializer, NexusExceptionHandler exceptionInterceptor) {
        this.serviceRegistry = serviceRegistry;
        this.serializer = serializer;
        this.exceptionInterceptor = exceptionInterceptor;
    }

    public byte[] handleRequest(byte[] data) {
        RpcResponse response = new RpcResponse();
        try {
            // 请求消息解码
            final RpcRequest rpcRequest = serializer.deserialize(data, RpcRequest.class);
            final String clazzName = rpcRequest.getClazzName();
            final ServiceInfo serviceInfo = serviceRegistry.getServiceInstance(clazzName);
            response.setUniqueIdentification(rpcRequest.getUniqueIdentification());
            if (serviceInfo == null) {
                response.setStatus("Not Found");
                return serializer.serialize(response);
            }
            // 通过反射调用目标方法
            final Method method = serviceInfo.getClazz().getMethod(rpcRequest.getMethod(), rpcRequest.getParameterTypes());
            final Object retValue = method.invoke(serviceInfo.getObj(), rpcRequest.getParameters());
            response.setStatus("Success");
            response.setData(retValue);
            serializer.serialize(response);
        } catch (Throwable ex) {
            doExceptionHandler(ex, response);
        }
        return serializer.serialize(response);
    }

    private void doExceptionHandler(Throwable ex, RpcResponse response) {
        exceptionInterceptor.handel(ex, response);
    }


}

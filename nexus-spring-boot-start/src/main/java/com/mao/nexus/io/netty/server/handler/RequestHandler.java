package com.mao.nexus.io.netty.server.handler;


import com.mao.nexus.exception.RpcException;
import com.mao.nexus.io.common.RpcRequest;
import com.mao.nexus.io.common.RpcResponse;
import com.mao.nexus.io.common.ServiceInfo;
import com.mao.nexus.serialize.Serializer;
import com.mao.nexus.registry.ServiceRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 19:48
 */
public class RequestHandler {

    private final ServiceRegistry serviceRegistry;

    private final Serializer serializer;

    public RequestHandler(ServiceRegistry serviceRegistry, Serializer serializer) {
        this.serviceRegistry = serviceRegistry;
        this.serializer = serializer;
    }

    public byte[] handleRequest(byte[] data) throws Exception {
        // 请求消息解码
        final RpcRequest rpcRequest = serializer.deserialize(data, RpcRequest.class);
        final String serviceName = rpcRequest.getServiceName();
        final ServiceInfo serviceInfo = serviceRegistry.getServiceInstance(serviceName);
        RpcResponse response = new RpcResponse();
        if (serviceInfo == null) {
            response.setStatus("Not Found");
            return serializer.serialize(response);
        }
        // 通过反射技术调用目标方法
        try {
            final Method method = serviceInfo.getClazz().getMethod(rpcRequest.getMethod(), rpcRequest.getParameterTypes());
            final Object retValue = method.invoke(serviceInfo.getObj(), rpcRequest.getParameters());
            response.setStatus("Success");
            response.setData(retValue);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            response.setStatus("Fail");
            response.setException(new RpcException(e.getMessage()));
        }
        return serializer.serialize(response);
    }


}

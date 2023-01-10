package com.mao.nexus.invocation;


import com.mao.nexus.exception.RpcException;
import com.mao.nexus.discovery.ServiceDiscovery;
import com.mao.nexus.interceptor.NexusInterceptor;
import com.mao.nexus.io.common.*;
import com.mao.nexus.io.netty.client.network.RpcClient;
import com.mao.nexus.cluster.loadbalance.LoadBalancer;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author ：StephenMao
 * @date ：2022/6/14 13:38
 * <p>
 * 代理工厂
 */
public class ClientProxyFactory {
    private static final Logger logger = LoggerFactory.getLogger(ClientProxyFactory.class);

    private final ServiceDiscovery serviceDiscovery;


    private final RpcClient rpcClient;

    private final LoadBalancer loadBalancer;


    private final List<NexusInterceptor> interceptors;

    public ClientProxyFactory(ServiceDiscovery serviceDiscovery, RpcClient rpcClient, LoadBalancer loadBalancer, List<NexusInterceptor> interceptors) {
        this.serviceDiscovery = serviceDiscovery;
        this.rpcClient = rpcClient;
        this.loadBalancer = loadBalancer;
        this.interceptors = interceptors;
    }

    /**
     * 获取代理对象，绑定 invoke 行为
     *
     * @param clazz
     * @param serviceName
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxyInstance(Class<T> clazz, String serviceName) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            String clazzName = clazz.getName();
            final List<MateInfo> serviceInfos = serviceDiscovery.listServices(serviceName);
            logger.info("Rpc server instance list: {}", serviceInfos);
            if (CollectionUtils.isEmpty(serviceInfos)) {
                throw new RpcException("No rpc servers found.");
            }
            // 负载均衡策略
            final MateInfo mateInfo = loadBalancer.getService(serviceInfos);
            final RpcRequest rpcRequest = RpcRequestContext.getRequest();
            rpcRequest.setServiceName(serviceName);
            rpcRequest.setClazzName(clazzName);
            rpcRequest.setMethod(method.getName());
            rpcRequest.setParameterTypes(method.getParameterTypes());
            rpcRequest.setParameters(args);
            RpcResponse response = getResponse(rpcRequest, mateInfo);
            RpcRequestContext.removeRequest();
            Assert.isTrue(response != null, "Server Exception:Response is null");
            if (response.getException() != null) {
                throw response.getException();
            }
            // 解析返回结果进行处理
            return response.getData();
        });
    }


    private RpcResponse getResponse(RpcRequest request, MateInfo mateInfo) {
        RpcResponse response = new RpcResponse();
        // 前置拦截
        if (!beforeDoIntercept(request, response)) {
            return response;
        }
        // 发送消息
        response = rpcClient.sendMessage(request, mateInfo);
        // 后置拦截
        afterDoIntercept(request, response);
        return response;
    }

    private boolean beforeDoIntercept(RpcRequest request, RpcResponse response) {
        for (NexusInterceptor interceptor : interceptors) {
            if (!interceptor.beforeInvoke(request, response)) {
                return false;
            }
        }
        return true;
    }


    private boolean afterDoIntercept(RpcRequest request, RpcResponse response) {
        for (NexusInterceptor interceptor : interceptors) {
            if (!interceptor.afterInvoke(request, response)) {
                return false;
            }
        }
        return true;
    }
}

package com.mao.nexus.invocation;


import com.mao.nexus.cluster.loadbalance.LoadBalancer;
import com.mao.nexus.discovery.ServiceDiscovery;
import com.mao.nexus.exception.RpcException;
import com.mao.nexus.interceptor.NexusClientInterceptor;
import com.mao.nexus.io.common.MateInfo;
import com.mao.nexus.io.common.RpcRequest;
import com.mao.nexus.io.common.RpcRequestContext;
import com.mao.nexus.io.common.RpcResponse;
import com.mao.nexus.io.netty.client.network.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

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


    private final List<NexusClientInterceptor> interceptors;

    public ClientProxyFactory(ServiceDiscovery serviceDiscovery, RpcClient rpcClient, LoadBalancer loadBalancer, List<NexusClientInterceptor> interceptors) {
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
    public <T> T getProxyInstance(Class<T> clazz, String serviceName, int retryCount, int retryInternal) {
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
            rpcRequest.setRetryCount(retryCount);
            rpcRequest.setRetryInternal(retryInternal);
            rpcRequest.setClazzName(clazzName);
            rpcRequest.setMethod(method.getName());
            rpcRequest.setParameterTypes(method.getParameterTypes());
            rpcRequest.setParameters(args);
            RpcResponse response = getResponse(rpcRequest, mateInfo);
            RpcRequestContext.removeRequest();
            Assert.isTrue(response != null, "Server Exception:Response is null");
            RpcException exception = response.getException();
            if (exception != null) {
                throw exception;
            }
            // 解析返回结果进行处理
            return response.getData();
        });
    }


    private boolean needRetry(RpcRequest request) {
        return request.getRetryCount() > 0;
    }

    private RpcResponse getResponse(RpcRequest request, MateInfo mateInfo) {
        RpcResponse response = new RpcResponse();
        // 前置拦截
        if (!beforeDoIntercept(request, response)) {
            return response;
        }
        if (needRetry(request)) {
            response = retry(response, request, mateInfo);
        } else {
            // 发送消息
            response = rpcClient.sendMessage(request, mateInfo);
        }
        // 后置拦截
        afterDoIntercept(request, response);
        return response;
    }


    private RpcResponse retry(RpcResponse response, final RpcRequest request, final MateInfo mateInfo) {
        int retryCount = request.getRetryCount();
        final int retryInternal = request.getRetryInternal();
        ++retryCount;
        while (retryCount > 0) {
            --retryCount;
            boolean flag = false;
            try {
                // 发送消息
                response = rpcClient.sendMessage(request, mateInfo);
            } catch (Exception ex) {
                logger.error("error request , retry count left {}", retryCount);
                flag = true;
            }
            if (response == null
                    || response.getException() != null) {
                flag = true;
            }
            if (flag) {
                try {
                    TimeUnit.MILLISECONDS.sleep(retryInternal);
                } catch (InterruptedException e) {
                    logger.info("InterruptedException {}", e.getMessage());
                }
            }
        }
        return response;
    }


    private boolean beforeDoIntercept(RpcRequest request, RpcResponse response) {
        for (NexusClientInterceptor interceptor : interceptors) {
            if (!interceptor.beforeInvoke(request, response)) {
                return false;
            }
        }
        return true;
    }


    private boolean afterDoIntercept(RpcRequest request, RpcResponse response) {
        for (NexusClientInterceptor interceptor : interceptors) {
            if (!interceptor.afterInvoke(request, response)) {
                return false;
            }
        }
        return true;
    }
}

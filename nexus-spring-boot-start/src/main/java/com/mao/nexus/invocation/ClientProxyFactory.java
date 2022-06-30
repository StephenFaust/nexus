package com.mao.nexus.invocation;


import com.mao.nexus.exception.RpcException;
import com.mao.nexus.discovery.ServiceDiscovery;
import com.mao.nexus.io.netty.client.network.RpcClient;
import com.mao.nexus.io.common.MateInfo;
import com.mao.nexus.io.common.RpcRequest;
import com.mao.nexus.io.common.RpcResponse;
import com.mao.nexus.serialize.Serializer;
import com.mao.nexus.cluster.loadbalance.LoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author ：StephenMao
 * @date ：2022/6/14 13:38
 * <p>
 * 代理工厂
 */
public class ClientProxyFactory {
    private static final Logger logger = LoggerFactory.getLogger(ClientProxyFactory.class);

    private ServiceDiscovery serviceDiscovery;

    private Serializer serializer;

    private RpcClient rpcClient;

    private LoadBalancer loadBalancer;

    public ClientProxyFactory(ServiceDiscovery serviceDiscovery, Serializer serializer, RpcClient rpcClient, LoadBalancer loadBalancer) {
        this.serviceDiscovery = serviceDiscovery;
        this.serializer = serializer;
        this.rpcClient = rpcClient;
        this.loadBalancer = loadBalancer;
    }

    /**
     * 获取代理对象，绑定 invoke 行为
     *
     * @param clazz 接口 class 对象
     * @param <T>   类型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxyInstance(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            String serviceName = clazz.getName();
            final List<MateInfo> serviceInfos = serviceDiscovery.listServices(serviceName);
            logger.info("Rpc server instance list: {}", serviceInfos);
            if (CollectionUtils.isEmpty(serviceInfos)) {
                throw new RpcException("No rpc servers found.");
            }
            final MateInfo mateInfo = loadBalancer.getService(serviceInfos);
            final RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setServiceName(serviceName);
            rpcRequest.setMethod(method.getName());
            rpcRequest.setParameterTypes(method.getParameterTypes());
            rpcRequest.setParameters(args);
            // 发送消息
            final RpcResponse rpcResponse = rpcClient.sendMessage(rpcRequest, mateInfo);
            Assert.isTrue(rpcResponse != null, "Server Exception:Response is null");
            if (rpcResponse.getException() != null) {
                throw rpcResponse.getException();
            }
            // 解析返回结果进行处理
            return rpcResponse.getData();
        });
    }
}

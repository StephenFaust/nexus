package com.mao.nexus.example.client.interceptor;

import com.mao.nexus.interceptor.NexusClientInterceptor;
import com.mao.nexus.io.common.RpcRequest;
import com.mao.nexus.io.common.RpcResponse;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class GlobalClientInterceptor implements NexusClientInterceptor {


    @Override
    public boolean beforeInvoke(RpcRequest request, RpcResponse response) {
        System.out.println(request);
        Map<String, String> headers = request.getHeaders();
        headers.put("1", "1");
//        response.setData(" 拦截了");
        return true;
    }

    @Override
    public boolean afterInvoke(RpcRequest request, RpcResponse response) {
        System.out.println("结束了");
        return true;
    }
}

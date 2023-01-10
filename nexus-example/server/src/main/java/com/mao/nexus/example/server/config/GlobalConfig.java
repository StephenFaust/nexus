package com.mao.nexus.example.server.config;

import com.mao.nexus.exception.RpcException;
import com.mao.nexus.exception.handler.NexusExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GlobalConfig {


    @Bean
    public NexusExceptionHandler exceptionHandler() {
        return (th, response) -> {
            System.out.println(11111);
            response.setStatus("Fail");
            response.setException(new RpcException("111", th));
        };
    }
}

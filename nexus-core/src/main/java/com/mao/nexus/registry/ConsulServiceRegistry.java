package com.mao.nexus.registry;

import com.mao.nexus.property.RpcProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ：huangjinmao
 * @date ：2022/7/1 15:11
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ConsulRegistration.class})
public class ConsulServiceRegistry extends DefaultServiceRegistry{

    @Autowired(required = false)
    private ConsulRegistration consulRegistration;

    @Autowired(required = false)
    private RpcProperties rpcProperties;

    public ConsulServiceRegistry() {
    }


    @PostConstruct
    public void init() {
        if (this.consulRegistration != null) {
            int port = this.rpcProperties.getPort();
            Map<String, String> meta = this.consulRegistration.getService().getMeta();
            if (meta == null) {
                meta = new HashMap();
            }
            if (-1 != port) {
                ((Map) meta).put("nexus_port", Integer.toString(port));
                this.consulRegistration.getService().setMeta(meta);
            }
        }

    }
}


package com.mao.nexus.registry;

import com.mao.nexus.io.common.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 19:43
 */
public abstract class DefaultServiceRegistry implements ServiceRegistry {
    protected Integer port;
    protected Map<String, ServiceInfo> serviceInfos = new HashMap<>(16);
    protected static final Logger logger = LoggerFactory.getLogger(DefaultServiceRegistry.class);

    @Override
    public void register(ServiceInfo serviceInfo) throws Exception {
        Assert.notNull(serviceInfo, "error,mateInfo not null");
        final String clazzName = serviceInfo.getClazzName();
        serviceInfos.put(clazzName, serviceInfo);
    }

    @Override
    public ServiceInfo getServiceInstance(String clazzName) throws Exception {
        return serviceInfos.get(clazzName);
    }
}

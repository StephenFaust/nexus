package com.mao.nexus.spring.registry;

import com.mao.nexus.io.common.ServiceInfo;
import com.mao.nexus.registry.DefaultServiceRegistry;

/**
 * @author StephenMao
 * @date 2022/6/26 8:51
 */
public class ZookeeperServiceRegistry extends DefaultServiceRegistry {
    @Override
    public void register(ServiceInfo serviceInfo) throws Exception {
        throw new IllegalArgumentException("Not supported at the moment");
    }

    @Override
    public ServiceInfo getServiceInstance(String name) throws Exception {
        throw new IllegalArgumentException("Not supported at the moment");
    }
}

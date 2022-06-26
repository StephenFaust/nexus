package com.mao.nexus.registry;

import com.mao.nexus.io.common.ServiceInfo;

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

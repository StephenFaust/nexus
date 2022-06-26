package com.mao.nexus.discovery;

import com.mao.nexus.io.common.MateInfo;

import java.util.List;

/**
 * @author StephenMao
 * @date 2022/6/26 8:48
 */
public class ZookeeperServiceDiscovery implements ServiceDiscovery {
    @Override
    public List<MateInfo> listServices(String serviceName) {
        throw new IllegalArgumentException("Not supported at the moment");
    }
}

package com.mao.nexus.cluster.loadbalance;

import com.mao.nexus.io.common.MateInfo;

import java.util.List;
import java.util.Random;

/**
 * @author ：StephenMao
 * @date ：2022/6/23 9:07
 */
public class RandomLoadBalancer implements LoadBalancer {

    private static final Random counter = new Random();

    @Override
    public MateInfo getService(List<MateInfo> services) {
        return services.get(counter.nextInt(services.size()));
    }
}

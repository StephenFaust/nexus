package com.mao.nexus.loadbalancer;


import com.mao.nexus.io.common.MateInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ：StephenMao
 * @date ：2022/6/22 16:52
 */
public class TrainLoadBalancer implements LoadBalancer {

    private static final int MAX_COUNT = 1000_000_000;

    private static final int INITIAL_VALUE = 0;

    private static final AtomicInteger counter = new AtomicInteger(INITIAL_VALUE);

    @Override
    public MateInfo getService(List<MateInfo> services) {
        if (null == services)
            return null;
        int count = counter.incrementAndGet();
        if (count >= MAX_COUNT) {
            synchronized (this) {
                if (counter.intValue() >= MAX_COUNT) {
                    counter.set(INITIAL_VALUE);
                    count = INITIAL_VALUE;
                } else {
                    count = counter.incrementAndGet();
                }
            }
        }
        return services.get(count % services.size());
    }
}

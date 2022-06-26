package com.mao.nexus.cluster.loadbalance;



import com.mao.nexus.io.common.MateInfo;
import com.mao.nexus.spi.annotation.SPI;
import java.util.List;

/**
 * @author ：StephenMao
 * @date ：2022/6/22 16:46
 */
@SPI
public interface LoadBalancer {
    MateInfo getService(List<MateInfo> services);
}

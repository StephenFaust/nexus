package com.mao.nexus.discovery;

import com.mao.nexus.io.common.MateInfo;
import com.mao.nexus.spi.annotation.SPI;

import java.util.List;

/**
 * @author ：StephenMao
 * @date ：2022/6/14 13:40
 */
@SPI
public interface ServiceDiscovery {

    /**
     * 通过服务名称获取服务提供者暴露的服务列表
     * @param serviceName 服务名称
     * @return 服务列表
     */
    List<MateInfo> listServices(String serviceName);
}
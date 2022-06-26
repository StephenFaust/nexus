package com.mao.nexus.registry;

import com.mao.nexus.io.common.ServiceInfo;
import com.mao.nexus.spi.annotation.SPI;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 19:43
 */
@SPI
public interface ServiceRegistry {
    /**
     * 注册服务信息
     *
     * @param serviceInfo 待注册的服务
     * @throws Exception 异常
     */
    void register(ServiceInfo serviceInfo) throws Exception;

    /**
     * 根据服务名称获取服务信息
     *
     * @param name 服务名称
     * @return 服务信息
     * @throws Exception 异常
     */
    ServiceInfo getServiceInstance(String name) throws Exception;
}

package com.mao.nexus.io.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author StephenMao
 * @date 2022/6/25 22:17
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ServiceInfo extends MateInfo {

    public ServiceInfo(String serviceName, String ip, Integer port, String clazzName, Class<?> clazz, Object obj) {
        super(serviceName, ip, port);
        this.clazzName = clazzName;
        this.clazz = clazz;
        this.obj = obj;
    }

    /**
     * 接口名
     */
    private String clazzName;

    /**
     * class 对象
     */
    private Class<?> clazz;

    /**
     * bean 对象
     */
    private Object obj;
}

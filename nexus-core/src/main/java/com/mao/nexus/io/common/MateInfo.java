package com.mao.nexus.io.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 17:53
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MateInfo {


    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * ip 地址
     */
    private String ip;

    /**
     * 端口号
     */
    private Integer port;


}

package com.mao.nexus.annotation;

import java.lang.annotation.*;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 16:52
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NexusClient {
    /**
     * serviceName
     *
     * @return service-name
     */
    String serviceName();

    /**
     * @return retry count
     */
    int retryCount() default -1;

    /**
     * unit:ms
     *
     * @return retryInternal
     */
    int retryInternal() default 0;


}

package com.mao.nexus.spring;


import com.mao.nexus.annotation.NexusClient;
import com.mao.nexus.invocation.ClientProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;


/**
 * @author ：StephenMao
 * @date ：2022/6/16 16:50
 */
public class DefaultRpcPostProcessor implements BeanPostProcessor {


    private static final Logger logger = LoggerFactory.getLogger(DefaultRpcPostProcessor.class);


    private final ClientProxyFactory clientProxyFactory;


    public DefaultRpcPostProcessor(ClientProxyFactory clientProxyFactory) {
        this.clientProxyFactory = clientProxyFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        final Class<?> clazz = bean.getClass();
        injectDependencyService(bean, clazz);
        return bean;
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }


    private void injectDependencyService(Object bean, Class<?> clazz) {
        // 遍历每个 bean 的成员属性，如果成员属性被 @ServiceReference 注解标记，说明依赖rpc远端接口
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            final NexusClient annotation = field.getAnnotation(NexusClient.class);
            if (annotation == null) {
                // 如果该成员属性没有标记该注解，继续找一下
                continue;
            }
            // 终于找到被注解标记的成员属性了
            Class<?> fieldClass = field.getType();
            // 获取服务名
            String serviceName = annotation.serviceName();
            //关闭安全检查
            field.setAccessible(true);
            try {
                // 注入代理对象值
                field.set(bean, clientProxyFactory.getProxyInstance(fieldClass, serviceName));
            } catch (IllegalAccessException e) {
                logger.error("Fail to inject service, bean.name: {}, error.msg: {}", bean, e.getMessage());
            }
        }
    }
}


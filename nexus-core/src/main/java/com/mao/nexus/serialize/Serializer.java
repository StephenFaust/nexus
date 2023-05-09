package com.mao.nexus.serialize;


import com.mao.nexus.exception.SerializeException;
import com.mao.nexus.spi.annotation.SPI;

/**
 * @author ：StephenMao
 * @date ：2022/6/23 9:27
 */
@SPI
public interface Serializer {

    /**
     * 对象序列化为二进制
     *
     * @param obj
     * @return
     * @throws SerializeException
     */
    byte[] serialize(Object obj) throws SerializeException;

    /**
     * 二进制反序列化为对象
     *
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     * @throws SerializeException
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializeException;
}

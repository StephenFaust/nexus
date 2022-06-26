package com.mao.nexus.serialize;


import com.mao.nexus.exception.SerializeException;
import com.mao.nexus.spi.annotation.SPI;

/**
 * @author ：StephenMao
 * @date ：2022/6/23 9:27
 */
@SPI
public interface Serializer {

    byte[] serialize(Object obj) throws SerializeException;

    <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializeException;
}

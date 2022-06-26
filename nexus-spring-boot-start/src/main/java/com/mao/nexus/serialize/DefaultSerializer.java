package com.mao.nexus.serialize;

import com.mao.nexus.exception.SerializeException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author ：StephenMao
 * @date ：2022/6/13 19:55
 * <p>
 * jdk序列化
 */
public class DefaultSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) throws SerializeException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos);) {
            byte[] bytes = null;
            oos.writeObject(obj);
            bytes = bos.toByteArray();
            return bytes;
        } catch (Exception ex) {
            throw new SerializeException("serialization failed");
        }

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws SerializeException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis);) {
            Object obj = ois.readObject();
            return clazz.cast(obj);
        } catch (Exception ex) {
            throw new SerializeException("deserialization failed");
        }
    }
}

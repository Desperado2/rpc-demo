package com.desperado.common;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化工具类(基于Protostuff实现)
 * 用于把对象序列化成字节数组，把字节数组反序列化成对象
 */
public class SerializationUtil {

    public static Map<Class<?>,Schema<?>> cacheSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();
    public static Objenesis objenesis = new ObjenesisStd(true);

    private SerializationUtil(){

    }

    /**
     * 获取类的schema
     * @param cls 类
     * @param <T>
     * @return
     */
    public static <T> Schema<T> getSchema(Class<T> cls){
        Schema<T> schema = (Schema<T>) cacheSchema.get(cls);
        if(schema == null){
            schema = RuntimeSchema.createFrom(cls);
            if(schema != null){
                cacheSchema.put(cls,schema);
            }
        }
        return schema;
    }

    /**
     * 序列化
     * @param obj
     * @param <T>
     * @return
     */
    public  static <T> byte[] serialize(T obj ){
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj,schema,buffer);
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(),e);
        }finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化
     * @param data
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] data, Class<T> cls){
        try {
            //* 如果一个类没有参数为空的构造方法时候，那么你直接调用newInstance方法试图得到一个实例对象的时候是会抛出异常的
            //  通过ObjenesisStd可以完美的避开这个问题
            T message = (T)objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data,message,schema);
            return message;
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(),e);
        }
    }
}

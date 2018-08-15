package com.yintong.erp.utils.common;

import java.util.Collection;
import java.util.Objects;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * @author lucifer.chan
 * @create 2018-06-07 上午2:29
 **/
public class CommonUtil {
    /**
     * 不存在时返回默认
     * @param t
     * @param defaultVale
     * @param <T>
     * @return
     */
    public static <T> T ifNotPresent(T t, T defaultVale){
        return Objects.nonNull(t) ? t : defaultVale;
    }

    /**
     * collection只能包含0或1个元素，否则抛异常
     * @param collection
     * @param message
     * @param <T>
     * @return
     */
    public static <T> T single(Collection<T> collection, String message){
        if(CollectionUtils.isEmpty(collection)){
            return null;
        }
        Assert.isTrue(collection.size() == 1, message);
        //noinspection unchecked
        return (T)collection.toArray()[0];
    }

    /**
     *
     * @param collection
     * @param <T>
     * @return
     */
    public static  <T> T single(Collection<T> collection){
        return single(collection, "存在脏数据,请联系管理员");
    }

}

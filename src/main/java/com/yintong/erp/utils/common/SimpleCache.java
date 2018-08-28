package com.yintong.erp.utils.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午3:19
 * 全局的建议缓存
 **/
public class SimpleCache<R> {

    private static Map cache = new ConcurrentHashMap<>();

    /**
     * 先从缓存中拿数据，
     *      1、有数据直接返回
     *      2、无数据时调用function获取，并放置到缓存中
     * @param key 缓存key
     * @param function 客户化方法
     * @return
     */
    public R getDataFromCache(String key, Function<String, R> function) {
        R value = from(key);
        if (null == value) {
            value = function.apply(key);
            to(key, value);
        }

        return value;
    }

    /**
     * 清除缓存
     */
    public void clearCache(){
        cache = new ConcurrentHashMap<>();
    }

    /**
     * 清除缓存
     * @param key
     */
    public void clearCache(String key){
        cache.remove(key);
    }


    /**
     * 取数据
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public R from(String key){
        return (R)cache.get(key);
    }

    /**
     * 存数据，若已有，则覆盖
     * @param key
     * @param value
     */
    @SuppressWarnings("unchecked")
    public void to(String key, R value){
        if(null != value)
            cache.put(key, value);
    }

    /**
     * 返回缓存size
     * @return
     */
    public int size(){
        return cache.size();
    }

    /**
     * 获取缓存map，建议对返回值只做读操作
     * @return
     */
    public Map getCache(){
        return cache;
    }

}

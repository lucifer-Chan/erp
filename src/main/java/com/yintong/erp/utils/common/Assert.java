package com.yintong.erp.utils.common;

import java.util.Collection;
import java.util.Map;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author lucifer.chan
 * @create 2018-09-04 下午10:09
 **/
public class Assert extends org.springframework.util.Assert {

    public static void isEmpty(Collection<?> collection, String message) {
        if (!CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isEmpty(Map<?, ?> map, String message) {
        if (!CollectionUtils.isEmpty(map)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isEmpty(String str, String message){
        if(!StringUtils.isEmpty(str)){
            throw new IllegalArgumentException(message);
        }
    }
}

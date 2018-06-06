package com.yintong.erp.utils.common;

import java.util.Objects;

/**
 * @author lucifer.chan
 * @create 2018-06-07 上午2:29
 **/
public class CommonUtil {
    public static <T> T ifNotPresent(T t, T defaultVale){
        return Objects.nonNull(t) ? t : defaultVale;
    }
}

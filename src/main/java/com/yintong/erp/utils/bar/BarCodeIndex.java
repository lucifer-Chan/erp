package com.yintong.erp.utils.bar;

import java.lang.annotation.*;

/**
 * @author lucifer.chan
 * @create 2018-05-10 上午12:10
 * 多个列的值作为条码序列
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BarCodeIndex {
    int value();
}

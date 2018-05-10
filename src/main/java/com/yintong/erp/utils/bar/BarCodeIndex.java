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

    /**
     * 次序
     * @return
     */
    int value();

    /**
     * 是否是占位符
     *  1-如果是的话，此属性的长度只能位1
     *  2-如果不是的话，length()有意义
     * @return
     */
    boolean holder() default false;

    /**
     * 为此属性的最大长度，默认为BarCodeConstants.ID_LENGTH
     * @return
     */
    int length() default BarCodeConstants.ID_LENGTH;
}

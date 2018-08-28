package com.yintong.erp.utils.bar;

import java.lang.annotation.*;

/**
 * @author lucifer.chan
 * @create 2018-05-09 下午5:53
 * 条形码要保存的列
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BarCodeColumn {
}

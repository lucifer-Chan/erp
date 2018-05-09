package com.yintong.erp.utils.bar;

import java.lang.annotation.*;

import static com.yintong.erp.utils.bar.BarCodeConstants.*;


/**
 * 条码注解
 *  功能：在数据新增的时候生成"前缀+id+@BarCodeIndex[...]"保存到拥有@BarCodeColumn注解的列中
 *  如果BarCodeIndex注解的值为null，则用"K"暂时替代
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BarCode {

    /**
     * 条码前缀
     * @return
     */
    BAR_CODE_PREFIX value();

}

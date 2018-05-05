package com.yintong.erp.utils.transform;

import java.lang.annotation.*;

/**
 * Created by lucifer.chan on 2017/6/16.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TransformerSourceProperty {
    /**
     * 源数据字段的名称,匹配到自身的字段
     * eg:
     *  source : username
     *  self : userName
     *  new DataTransformer.transformer(source, self)
     *      => source.username ==> self.userName
     * @return
     */
    String value() default "";
    String [] values() default {};
}

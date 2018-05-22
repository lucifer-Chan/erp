package com.yintong.erp.utils.query;

import java.lang.annotation.*;

/**
 * @author lucifer.chan
 * @create 2018-05-22 下午12:15
 * 查询的排序 作用在QueryParameterBuilder的继承类上
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OrderBy {

    /**
     * 根据那咧排序
     * @return
     */
    String fieldName();

    /**
     * 排序方式
     * @return
     */
    METHOD method() default  METHOD.desc;

    enum METHOD { desc, asc }
}

package com.yintong.erp.utils.query;

import java.lang.annotation.*;

/**
 * Created by lucifer.chan on 2017/12/8.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ParameterItem {

    /**
     * 匹配到实体类[DTO]上的具体field名-1对多
     * @return
     */
    String [] mappingTo() default {};
    COMPARES compare();
    TRANSFORMER transformer() default TRANSFORMER.NULL;
    enum COMPARES {
        lessThan,
        greaterThan,
        equal,
        like,
        notEqual,
        notLike
    }

    enum TRANSFORMER {
        NULL,
        strDate2Millis,//日期转换成毫秒
        strTime2Millis,//时间转换成毫秒
        strDate2Second,//日期转换成秒
        strTime2Second,//日期转换成秒
        str2Date,
        str2Time,
        str2Int
    }
}

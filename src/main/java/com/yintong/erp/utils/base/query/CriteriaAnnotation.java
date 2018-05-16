package com.yintong.erp.utils.base.query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CriteriaAnnotation {

    QueryType opType() default QueryType.EQUAL;
    String columnName() default "";

}

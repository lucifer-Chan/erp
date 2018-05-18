package com.yintong.erp.utils.transform;


import java.lang.annotation.*;

/**
 * json-忽略
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreWhatever {
}

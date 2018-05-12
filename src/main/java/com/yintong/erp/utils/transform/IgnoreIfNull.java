package com.yintong.erp.utils.transform;

import java.lang.annotation.*;

/**
 * 如果为null的话，忽略
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreIfNull {
}

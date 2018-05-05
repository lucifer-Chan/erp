package com.yintong.erp.utils.transform;

import java.lang.annotation.*;

/**
 * Created by lucifer.chan on 2017/7/7.
 * 不做数据转化的属性，比如list，可在转化结束之后手动添加
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TransformerIgnoreProperty {
}

package com.yintong.erp.utils.base.query;

/**
 * 条件查询基础接口
 * 对于某个接口的多条件查询时，查询DTO实现这个接口并且是component。
 * 这样做的目的是为了再工程启动的时候，加载这些查询DTO并对其进行解析。
 * 如果把这个工作放在运行时，会带来两个问题。1：需要反复解析。2：反射本身是低效的事情，应该尽量减少
 * */
public interface BaseQueryDTO {
}

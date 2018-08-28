package com.yintong.erp.domain.stock;

import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import org.springframework.util.Assert;

import static com.yintong.erp.utils.common.Constants.*;

/**
 * @author lucifer.chan
 * @create 2018-08-28 下午10:28
 * 可出入库的实体
 **/
public interface StockEntity<T extends BaseEntityWithBarCode> {
    /**
     * 入库
     * @param num
     */
    T stockIn(double num);

    /**
     * 出库
     * @param num
     */
    T stockOut(double num);

    /**
     * 获取实例
     * @return
     */
    T entity();

    /**
     * 货物的模版id
     * @return
     */
    Long templateId();

    /**
     * 货物的真实id
     * @return
     */
    Long realityId();

    /**
     * 货物类型
     * @return
     */
    WaresType waresType();

    /**
     * 出入库之前的校验 - 默认
     */
    default void stockValidate(){
        Assert.notNull(templateId(), "货物不能为空");
        Assert.notNull(realityId(), "货物不能为空");
        Assert.notNull(waresType(), "货物类型为空");
    }
}

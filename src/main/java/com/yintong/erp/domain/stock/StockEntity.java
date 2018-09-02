package com.yintong.erp.domain.stock;

import com.yintong.erp.domain.basis.TemplateWares;
import com.yintong.erp.service.purchase.PurchaseOrderService;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.SpringUtil;
import java.util.Objects;
import java.util.function.Function;
import net.sf.json.JSONObject;
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

    /**
     * 获取货物模版
     * @return
     */
    default TemplateWares template(){
        Function<Long, TemplateWares> function = SpringUtil.getBean(PurchaseOrderService.class).findWaresById().get(waresType());
        return function.apply(templateId());
    }

    default JSONObject templateJson(){
        TemplateWares templateWares = template();
        return Objects.isNull(templateWares) ? new JSONObject() : templateWares.getTemplate();
    }
}

package com.yintong.erp.service.stock;

import static com.yintong.erp.utils.common.Constants.*;

/**
 * @author lucifer.chan
 * @create 2018-08-14 上午10:19
 * 成品出库-具体的业务单处理
 **/
public interface StockOutProduct4Holder {
    /**
     * 判断目的是否和入参相同
     * @param holder
     * @return
     */
    boolean matchesOutProductHolder(StockHolder holder);

    /**
     * 实际操作
     * @param holder
     * @param holderId 制令单id、销售订单id、采购单id
     * @param productId 产品id
     * @param num 数量
     */
    void stockOutProduct(StockHolder holder, Long holderId, Long productId, double num);

    /**
     * 默认实现-数量、状态、日志
     * @param holder
     * @param holderId
     * @param productId
     * @param num
     */
    default void handle(StockHolder holder, Long holderId, Long productId, double num){
        if(matchesOutProductHolder(holder)){
            stockOutProduct(holder, holderId, productId, num);
        }
    }
}

package com.yintong.erp.service.stock;

import static com.yintong.erp.utils.common.Constants.*;

/**
 * @author lucifer.chan
 * @create 2018-08-14 上午2:10
 * 成品入库-具体的业务单处理
 **/
public interface StockInProduct4Holder {

    /**
     * 判断来源是否和入参相同
     * @param holder
     * @return
     */
    boolean matchesInProductHolder(StockHolder holder);

    /**
     * 实际操作
     * @param holder
     * @param holderId
     * @param productId
     * @param num
     */
    void stockInProduct(StockHolder holder, Long holderId, Long productId, double num);

    /**
     * 默认实现-数量、状态、日志
     * @param holder
     * @param holderId
     * @param productId
     * @param num
     */
    default void handle(StockHolder holder, Long holderId, Long productId, double num){
        if(matchesInProductHolder(holder)){
            stockInProduct(holder, holderId, productId, num);
        }
    }

}

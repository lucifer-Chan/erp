package com.yintong.erp.service.stock;

import com.yintong.erp.domain.stock.StockEntity;

import static com.yintong.erp.utils.common.Constants.*;

/**
 * @author lucifer.chan
 * @create 2018-08-29 上午12:05
 * 货物出库-具体的业务单处理
 **/
public interface StockOut4Holder {
    /**
     * 判断来源是否和入参相同 & stockEntity是否符合要求
     * @param holder
     * @param stockEntity
     * @return
     */
    boolean matchesOut(StockHolder holder, StockEntity stockEntity);

    /**
     * 实际操作
     * @param holder
     * @param holderId
     * @param stockEntity
     * @param num
     */
    void stockOut(StockHolder holder, Long holderId, StockEntity stockEntity, double num);

    /**
     * 默认实现-数量、状态、日志
     * @param holder
     * @param holderId
     * @param stockEntity
     * @param num
     */
    default void handleOut(StockHolder holder, Long holderId, StockEntity stockEntity, double num){
        if(matchesOut(holder, stockEntity)){
            stockOut(holder, holderId, stockEntity, num);
        }
    }
}

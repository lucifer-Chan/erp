package com.yintong.erp.service.stock;

import com.yintong.erp.domain.stock.StockEntity;

import static com.yintong.erp.utils.common.Constants.*;

/**
 * @author lucifer.chan
 * @create 2018-08-28 下午11:58
 * 货物入库-具体的业务单处理
 **/
public interface StockIn4Holder {

    /**
     * 判断来源是否和入参相同 & stockEntity是否符合要求
     * @param holder
     * @param stockEntity
     * @return
     */
    boolean matchesIn(StockHolder holder, StockEntity stockEntity);

    /**
     * 实际操作
     * @param holder
     * @param holderId
     * @param stockEntity
     * @param num
     */
    void stockIn(StockHolder holder, Long holderId, StockEntity stockEntity, double num);

    /**
     * 默认实现-数量、状态、日志
     * @param holder
     * @param holderId
     * @param stockEntity
     * @param num
     */
    default void handleIn(StockHolder holder, Long holderId, StockEntity stockEntity, double num){
        if(matchesIn(holder, stockEntity)){
            stockIn(holder, holderId, stockEntity, num);
        }
    }
}

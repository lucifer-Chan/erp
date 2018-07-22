package com.yintong.erp.domain.sale;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpSaleOrderItemRepository extends JpaRepository<ErpSaleOrderItem, Long>{
    /**
     * 根据产品id和订单状态查询
     * @param productId
     * @param statusCode
     * @return
     */
    List<ErpSaleOrderItem> findByProductIdAndStatusCode(Long productId, String statusCode);

    /**
     * 根据产品id、订单状态、时间段查询
     * @param productId
     * @param statusCode
     * @param start
     * @param end
     * @return
     */
    List<ErpSaleOrderItem> findByProductIdAndStatusCodeAndCreatedAtIsBetween(Long productId, String statusCode, Date start, Date end);
}

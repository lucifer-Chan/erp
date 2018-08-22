package com.yintong.erp.domain.sale;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpSaleOrderItemRepository extends JpaRepository<ErpSaleOrderItem, Long>{
    /**
     * 根据订单id查询
     * @param orderId
     * @return
     */
    List<ErpSaleOrderItem> findByOrderIdOrderByMoneyDesc(Long orderId);

    List<ErpSaleOrderItem> findByOrderId(Long orderId);

    List<ErpSaleOrderItem> findByOrderIdInOrderByMoney(List<Long> ids);

    /**
     * 根据产品id、订单状态、时间段查询
     * @param productId
     * @param statusCode
     * @param start
     * @param end
     * @return
     */
    List<ErpSaleOrderItem> findByProductIdAndStatusCodeAndCreatedAtIsBetween(Long productId, String statusCode, Date start, Date end);

    List<ErpSaleOrderItem> findByProductId(Long productId);

    List<ErpSaleOrderItem> findByProductIdAndStatusCode(Long productId, String statusCode);

    List<ErpSaleOrderItem> findByOrderIdAndStatusCodeNot(Long orderId, String statusCode);

    List<ErpSaleOrderItem> findByOrderIdAndProductId(Long orderId, Long productId);

    void deleteByOrderId(Long orderId);


}

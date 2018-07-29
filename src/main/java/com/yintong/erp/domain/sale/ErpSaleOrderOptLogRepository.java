package com.yintong.erp.domain.sale;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpSaleOrderOptLogRepository extends JpaRepository<ErpSaleOrderOptLog, Long> {

    /**
     * 根据订单id和状态查找
     * @param orderId
     * @param statusCode
     * @return
     */
    List<ErpSaleOrderOptLog> findByOrderIdAndStatusCodeOrderByCreatedAtDesc(Long orderId, String statusCode);

    /**
     * 根据订单id查找
     * @param orderId
     * @return
     */
    List<ErpSaleOrderOptLog> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    /**
     * 根据订单id删除
     * @param orderId
     */
    void deleteByOrderId(Long orderId);
}

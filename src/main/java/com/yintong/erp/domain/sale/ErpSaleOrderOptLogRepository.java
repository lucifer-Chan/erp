package com.yintong.erp.domain.sale;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpSaleOrderOptLogRepository extends JpaRepository<ErpSaleOrderOptLog, Long> {

    /**
     * 根据订单id和状态查找：理论上有0-1条数据
     * @param orderId
     * @param statusCode
     * @return
     */
    List<ErpSaleOrderOptLog> findByOrderIdAndStatusCode(Long orderId, String statusCode);
}

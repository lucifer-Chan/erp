package com.yintong.erp.domain.purchase;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpPurchaseOrderOptLogRepository extends JpaRepository<ErpPurchaseOrderOptLog, Long> {
    void deleteByOrderId(Long orderId);

    List<ErpPurchaseOrderOptLog> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}

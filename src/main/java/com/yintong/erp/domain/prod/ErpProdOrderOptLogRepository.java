package com.yintong.erp.domain.prod;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpProdOrderOptLogRepository extends JpaRepository<ErpProdOrderOptLog, Long> {

    void deleteByOrderId(Long orderId);

    List<ErpProdOrderOptLog> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}

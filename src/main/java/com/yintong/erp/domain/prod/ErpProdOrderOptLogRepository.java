package com.yintong.erp.domain.prod;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpProdOrderOptLogRepository extends JpaRepository<ErpProdOrderOptLog, Long> {

    void deleteByOrderId(Long orderId);
}

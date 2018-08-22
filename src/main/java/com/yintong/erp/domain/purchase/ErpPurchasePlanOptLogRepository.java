package com.yintong.erp.domain.purchase;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpPurchasePlanOptLogRepository extends JpaRepository<ErpPurchasePlanOptLog, Long> {

    void deleteByPlanId(Long planId);

    List<ErpPurchasePlanOptLog> findByPlanIdOrderByCreatedAtDesc(Long planId);
}

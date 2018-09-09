package com.yintong.erp.domain.prod;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpProdPlanOptLogRepository extends JpaRepository<ErpProdPlanOptLog, Long> {

    void deleteByPlanId(Long planId);

    List<ErpProdPlanOptLog> findByPlanIdOrderByCreatedAtDesc(Long planId);
}

package com.yintong.erp.domain.sale;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpSalePlanOptLogRepository extends JpaRepository<ErpSalePlanOptLog, Long> {

    /**
     * 获取一个销售计划单的历史修改记录
     * @param planId
     * @return
     */
    List<ErpSalePlanOptLog> findByPlanIdOrderByCreatedAtDesc(Long planId);

    void deleteByPlanId(Long planId);
}

package com.yintong.erp.domain.prod;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpProdOrderRepository extends JpaRepository<ErpProdOrder, Long> {
    List<ErpProdOrder> findByPlanId(Long id);
}

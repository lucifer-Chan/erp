package com.yintong.erp.domain.prod;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpProdPlanRepository extends JpaRepository<ErpProdPlan, Long> {

    List<ErpProdPlan> findByIdIn(Collection<Long> ids);

    Page<ErpProdPlan> findAll(Specification<ErpProdPlan> specification, Pageable pageable);
}

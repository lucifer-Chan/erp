package com.yintong.erp.domain.sale;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpSalePlanRepository extends JpaRepository<ErpSalePlan, Long> {

    List<ErpSalePlan> findByProductId(Long productId);


    Page<ErpSalePlan> findAll(Specification<ErpSalePlan> specification, Pageable pageable);
}

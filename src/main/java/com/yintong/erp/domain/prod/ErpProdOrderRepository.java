package com.yintong.erp.domain.prod;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpProdOrderRepository extends JpaRepository<ErpProdOrder, Long> {

    List<ErpProdOrder> findByPlanId(Long planId);

    List<ErpProdOrder> findByIdIn(List<Long> orderIds);

    Optional<ErpProdOrder> findByBarCode(String barcode);

    Page<ErpProdOrder> findAll(Specification<ErpProdOrder> specification, Pageable pageable);
}

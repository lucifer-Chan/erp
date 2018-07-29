package com.yintong.erp.domain.sale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpSaleOrderRepository extends JpaRepository<ErpSaleOrder, Long> {

    Page<ErpSaleOrder> findAll(Specification<ErpSaleOrder> specification, Pageable pageable);
}

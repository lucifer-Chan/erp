package com.yintong.erp.domain.purchase;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpPurchaseOrderRepository extends JpaRepository<ErpPurchaseOrder, Long> {

    Page<ErpPurchaseOrder> findAll(Specification<ErpPurchaseOrder> specification, Pageable pageable);

    List<ErpPurchaseOrder> findByBarCode(String barcode);

    List<ErpPurchaseOrder> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
}

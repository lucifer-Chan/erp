package com.yintong.erp.domain.purchase;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpPurchaseOrderItemRepository extends JpaRepository<ErpPurchaseOrderItem, Long> {

    List<ErpPurchaseOrderItem> findByWaresIdAndWaresTypeAndStatusCodeAndCreatedAtIsBetween(Long waresId, String waresType, String statusCode, Date start, Date end);
}
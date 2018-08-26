package com.yintong.erp.domain.purchase;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpPurchaseOrderItemRepository extends JpaRepository<ErpPurchaseOrderItem, Long> {

    List<ErpPurchaseOrderItem> findByWaresIdAndWaresTypeAndStatusCodeAndCreatedAtIsBetween(Long waresId, String waresType, String statusCode, Date start, Date end);

    List<ErpPurchaseOrderItem> findByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);

    List<ErpPurchaseOrderItem> findByOrderIdOrderByMoneyDesc(Long orderId);

    List<ErpPurchaseOrderItem> findByOrderIdAndWaresAssIdAndWaresType(Long orderId, Long waresAssId, String waresType);

    List<ErpPurchaseOrderItem> findByWaresAssIdAndWaresType(Long waresAssId, String waresType);
}
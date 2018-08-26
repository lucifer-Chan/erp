package com.yintong.erp.domain.stock;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpStockInOrderRepository extends JpaRepository<ErpStockInOrder, Long> {

    List<ErpStockInOrder> findByHolderAndHolderId(String holder, Long holderId);
}

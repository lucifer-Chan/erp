package com.yintong.erp.domain.stock;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpStockOutOrderRepository extends JpaRepository<ErpStockOutOrder, Long> {

    List<ErpStockOutOrder> findByHolderAndHolderId(String holder, Long holderId);
}

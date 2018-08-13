package com.yintong.erp.domain.stock;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpStockOptLogRepository extends JpaRepository<ErpStockOptLog, Long>{

    /**
     * 根据仓位查找
     * @param stockPlaceId
     * @return
     */
    List<ErpStockOptLog> findByStockPlaceId(Long stockPlaceId);
}

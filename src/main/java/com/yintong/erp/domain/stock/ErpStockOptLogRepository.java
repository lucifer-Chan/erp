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

    /**
     * 根据成品id查询
     * @param productId
     * @return
     */
    List<ErpStockOptLog> findByProductIdOrderByCreatedAtDesc(Long productId);

    /**
     * 根据模具id查询
     * @param mouldId
     * @return
     */
    List<ErpStockOptLog> findByMouldIdOrderByCreatedAtDesc(Long mouldId);

    List<ErpStockOptLog> findByProductIdIn(List<Long> productIds);
}

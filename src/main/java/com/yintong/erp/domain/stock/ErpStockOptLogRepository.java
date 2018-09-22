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

    List<ErpStockOptLog> findByStockPlaceIdAndMouldCode(Long placeId, String mouldCode);

    List<ErpStockOptLog> findByStockPlaceIdAndMouldId(Long placeId, Long mouldId);

    List<ErpStockOptLog> findByStockPlaceIdAndProductCode(Long placeId, String productCode);

    List<ErpStockOptLog> findByStockPlaceIdAndProductId(Long placeId, Long productId);

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

    /**
     * 通过模具的条码查找
     * @param mouldBarCode
     * @return
     */
    List<ErpStockOptLog> findByMouldCode(String mouldBarCode);

    /**
     * 通过成品的条码查找
     * @param productBarCode
     * @return
     */
    List<ErpStockOptLog> findByProductCode(String productBarCode);

    List<ErpStockOptLog> findByProductIdIn(List<Long> productIds);
}

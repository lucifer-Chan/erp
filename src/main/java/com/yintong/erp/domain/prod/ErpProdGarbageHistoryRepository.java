package com.yintong.erp.domain.prod;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author lucifer.chan
 * @create 2019-03-07 下午8:57
 **/
public interface ErpProdGarbageHistoryRepository extends JpaRepository<ErpProdGarbageHistory, Long> {

    List<ErpProdGarbageHistory> findByProdOrderIdAndTypeCode(Long prodOrderId, String typeCode);

    List<ErpProdGarbageHistory> findByProdOrderIdOrderByCreatedAtDesc(Long prodOrderId);
}

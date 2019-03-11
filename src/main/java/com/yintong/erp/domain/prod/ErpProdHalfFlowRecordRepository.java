package com.yintong.erp.domain.prod;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author lucifer.chan
 * @create 2019-03-08 下午7:17
 **/
public interface ErpProdHalfFlowRecordRepository extends JpaRepository<ErpProdHalfFlowRecord, Long>{
    
    List<ErpProdHalfFlowRecord> findByProdOrderIdOrderByCreatedAtDesc(Long id);

    Optional<ErpProdHalfFlowRecord> findByBarCode(String barcode);
}

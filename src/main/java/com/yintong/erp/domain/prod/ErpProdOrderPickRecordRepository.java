package com.yintong.erp.domain.prod;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpProdOrderPickRecordRepository extends JpaRepository<ErpProdOrderPickRecord, Long> {

    List<ErpProdOrderPickRecord> findByOrderIdOrderByCreatedAtDesc(Long id);
}

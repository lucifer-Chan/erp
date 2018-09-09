package com.yintong.erp.domain.prod;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author lucifer.chan
 * @create 2018-09-03 下午2:42
 **/
public interface ErpProdMouldRepository extends JpaRepository<ErpProdMould, Long>{

    List<ErpProdMould> findByHolderAndHolderId(String holder, Long holderId);

    List<ErpProdMould> findByHolderAndHolderIdAndRealityMouldId(String holder, Long holderId, Long assId);

    List<ErpProdMould> findByHolderAndRealityMouldId(String holder, Long realityMouldId);

    void deleteByHolderAndHolderId(String name, Long planId);
}

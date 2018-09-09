package com.yintong.erp.domain.prod;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpProdProductBomRepository extends JpaRepository<ErpProdProductBom, Long> {

    List<ErpProdProductBom> findByHolderAndHolderId(String name, Long id);

    List<ErpProdProductBom> findByIdIn(Collection<Long> ids);

    List<ErpProdProductBom> findByHolderAndRealityMaterialId(String holder, Long realityMaterialId);

    void deleteByHolderAndHolderId(String holder, Long holderId);
}

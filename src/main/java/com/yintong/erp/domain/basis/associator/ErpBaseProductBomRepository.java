package com.yintong.erp.domain.basis.associator;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author lucifer.chan
 * @create 2018-08-14 下午3:15
 **/
public interface ErpBaseProductBomRepository extends JpaRepository<ErpBaseProductBom, Long>{

    List<ErpBaseProductBom> findByProductId(Long productId);

    List<ErpBaseProductBom> findByMaterialId(Long materialId);


    List<ErpBaseProductBom> findByProductIdOrderByCreatedAtDesc(Long productId);
}

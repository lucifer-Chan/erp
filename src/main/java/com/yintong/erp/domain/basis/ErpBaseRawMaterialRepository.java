package com.yintong.erp.domain.basis;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by jianqiang on 2018/5/9 0009.
 */
public interface ErpBaseRawMaterialRepository extends JpaRepository<ErpBaseRawMaterial, Long> {

    List<ErpBaseRawMaterial> findAll();

    Page<ErpBaseRawMaterial> findAll(Specification<ErpBaseRawMaterial> specification, Pageable pageable);
    @Query(value = "SELECT count(imported_at) num, imported_at FROM erp_base_raw_material WHERE imported_at IS NOT NULL GROUP BY imported_at ORDER BY imported_at DESC",
            nativeQuery = true)
    List<Object []> groupByImportAt();

    List<ErpBaseRawMaterial> findByImportedAt(String importedAt);

    List<ErpBaseRawMaterial> findByRawNameAndSpecification(String rawName, String specification);

    List<ErpBaseRawMaterial> findByRawNameAndSpecificationAndIdNot(String rawName, String specification, Long id);


    List<ErpBaseRawMaterial> findByIdNotIn(List<Long> ids);

}

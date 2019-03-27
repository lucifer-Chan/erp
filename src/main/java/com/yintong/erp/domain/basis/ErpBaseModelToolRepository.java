package com.yintong.erp.domain.basis;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;

/**
 * Created by jianqiang on 2018/5/10 0010.
 */
public interface ErpBaseModelToolRepository extends JpaRepository<ErpBaseModelTool,Long> {


    Page<ErpBaseModelTool> findAll(Specification<ErpBaseModelTool> specification, Pageable pageable);


    List<ErpBaseModelTool> findByModelToolNameAndSpecification(String modelToolName, String specification);

    List<ErpBaseModelTool> findByModelToolNameAndSpecificationAndIdNot(String modelToolName, String specification, Long id);

    List<ErpBaseModelTool> findAllByOrderByModelToolTypeCode();

    Optional<ErpBaseModelTool> findByBarCode(String barcode);


    List<ErpBaseModelTool> findByModelPlace(String modelPlace);

    List<ErpBaseModelTool> findByModelPlaceAndIdNot(String modelPlace, Long id);

    @Query(value = "SELECT count(imported_at) num, imported_at FROM erp_base_model_tool WHERE imported_at IS NOT NULL GROUP BY imported_at ORDER BY imported_at DESC",
            nativeQuery = true)
    List<Object []> groupByImportAt();

    List<ErpBaseModelTool> findByImportedAt(String importedAt);

    List<ErpBaseModelTool> findAllByOrderById();
}

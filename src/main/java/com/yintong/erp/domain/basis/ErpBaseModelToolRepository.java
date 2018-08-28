package com.yintong.erp.domain.basis;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by jianqiang on 2018/5/10 0010.
 */
public interface ErpBaseModelToolRepository extends JpaRepository<ErpBaseModelTool,Long> {


    Page<ErpBaseModelTool> findAll(Specification<ErpBaseModelTool> specification, Pageable pageable);


    List<ErpBaseModelTool> findByModelToolNameAndSpecification(String modelToolName, String specification);

    List<ErpBaseModelTool> findByModelToolNameAndSpecificationAndIdNot(String modelToolName, String specification, Long id);

    List<ErpBaseModelTool> findAllByOrderByModelToolTypeCode();

    Optional<ErpBaseModelTool> findByBarCode(String barcode);
}

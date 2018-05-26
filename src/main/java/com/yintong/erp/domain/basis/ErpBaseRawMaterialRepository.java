package com.yintong.erp.domain.basis;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by jianqiang on 2018/5/9 0009.
 */
public interface ErpBaseRawMaterialRepository extends JpaRepository<ErpBaseRawMaterial, Long> {

    List<ErpBaseRawMaterial> findAll();

    ErpBaseRawMaterial findById(String  rawNo);

    Page<ErpBaseRawMaterial> findAll(Specification<ErpBaseRawMaterial> specification, Pageable pageable);
}

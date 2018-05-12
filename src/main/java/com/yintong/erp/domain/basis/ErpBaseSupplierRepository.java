package com.yintong.erp.domain.basis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by jianqiang on 2018/5/12.
 */
public interface ErpBaseSupplierRepository extends JpaRepository<ErpBaseSupplier, Long> {

    List<ErpBaseSupplier> findAll();

    ErpBaseSupplier findBySupplierNo(String supplierNo);
}

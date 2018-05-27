package com.yintong.erp.domain.basis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ErpBaseCategoryRepository extends JpaRepository<ErpBaseCategory, Long>{

    List<ErpBaseCategory> findByFullName(String fullName);
}
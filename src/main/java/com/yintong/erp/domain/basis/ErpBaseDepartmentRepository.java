package com.yintong.erp.domain.basis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ErpBaseDepartmentRepository extends JpaRepository<ErpBaseDepartment, Long>{

    List<ErpBaseDepartment> findByParentIdAndName(Long parentId, String name);
}

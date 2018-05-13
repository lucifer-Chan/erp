package com.yintong.erp.domain.basis.associator;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpEmployeeDepartmentRepository extends JpaRepository<ErpEmployeeDepartment, Long>{

    void deleteByDepartmentIdIn(Iterable<Long> departmentIds);
}

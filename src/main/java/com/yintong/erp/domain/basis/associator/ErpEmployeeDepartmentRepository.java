package com.yintong.erp.domain.basis.associator;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ErpEmployeeDepartmentRepository extends JpaRepository<ErpEmployeeDepartment, Long>{

    void deleteByEmployeeId(Long employeeId);

    List<ErpEmployeeDepartment> findByEmployeeId(Long employeeId);

    void deleteByDepartmentIdIn(Iterable<Long> departmentIds);
}

package com.yintong.erp.domain.basis.security;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ErpEmployeeRepository extends JpaRepository<ErpEmployee, Long> {

    List<ErpEmployee> findByLoginName(String loginName);

    List<ErpEmployee> findByMobile(String mobile);

    List<ErpEmployee> findByLoginNameAndIdIsNot(String loginName, Long id);

    List<ErpEmployee> findByMobileAndIdIsNot(String mobile, Long id);

    Page<ErpEmployee> findAll(Specification<ErpEmployee> specification, Pageable pageable);

    @Query(value = "select * from erp_employee e where exists (select 1 from erp_employee_department d where d.employee_id = e.id and d.department_id = ?1) order by e.created_at desc",
            countQuery = "select count(1) from erp_employee e where exists (select 1 from erp_employee_department d where d.employee_id = e.id and d.department_id = ?1)",
            nativeQuery = true)
    Page<ErpEmployee> findByDepartmentId(String departmentId, Pageable pageable);
}

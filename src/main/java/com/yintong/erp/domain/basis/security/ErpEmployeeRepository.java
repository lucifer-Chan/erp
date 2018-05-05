package com.yintong.erp.domain.basis.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ErpEmployeeRepository extends JpaRepository<ErpEmployee, Long> {

    List<ErpEmployee> findByLoginName(String loginName);
}

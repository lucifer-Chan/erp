package com.yintong.erp.domain.basis.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ErpEmployeeMenuRepository extends JpaRepository<ErpEmployeeMenu, Long> {

    List<ErpEmployeeMenu> findByEmployeeId(Long employeeId);

    ErpEmployeeMenu findByEmployeeIdAndMenuCode(Long employeeId, String menuCode);

    void deleteByEmployeeId(Long employeeId);

}

package com.yintong.erp.domain.basis.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ErpMenuRepository extends JpaRepository<ErpMenu, String>{

    List<ErpMenu> findByParentCodeOrderByCode(String parentCode);

    List<ErpMenu> findByParentCode(String parentCode);

    List<ErpMenu> findByParentCodeIsNullOrderByCode();

    List<ErpMenu> findByParentCodeIsNotNullOrderByCode();

    List<ErpMenu> findByCodeInOrderByCode(Iterable<String> codes);

}

package com.yintong.erp.domain.basis.security;

import com.yintong.erp.domain.BaseRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ErpEmployeeRepository extends BaseRepository<ErpEmployee> {

    List<ErpEmployee> findByLoginName(String loginName);
}

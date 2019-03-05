package com.yintong.erp.domain.basis.security;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author lucifer.chan
 * @create 2019-03-04 下午5:28
 **/
public interface ErpMiniRoleRepository extends JpaRepository<ErpMiniRole, String> {

    List<ErpMiniRole> findByCodeIn(List<String> miniRoleCodes);
}

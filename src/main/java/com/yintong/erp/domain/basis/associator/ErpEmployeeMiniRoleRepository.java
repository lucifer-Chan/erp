package com.yintong.erp.domain.basis.associator;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author lucifer.chan
 * @create 2019-03-04 下午5:31
 **/
public interface ErpEmployeeMiniRoleRepository extends JpaRepository<ErpEmployeeMiniRole, Long> {

    List<ErpEmployeeMiniRole> findByUserId(Long userId);

    void deleteByUserId(Long employeeId);
}

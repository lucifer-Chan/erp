package com.yintong.erp.domain.basis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by jianqiang on 2018/5/10 0010.
 */
public interface ErpBaseModelToolRepository extends JpaRepository<ErpBaseModelTool,Long> {

    List<ErpBaseModelTool> findAll();
}

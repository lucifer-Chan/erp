package com.yintong.erp.domain.basis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by jianqiang on 2018/5/9 0009.
 */
public interface ErpBaseEndProductRepository extends JpaRepository<ErpBaseEndProduct,Long> {

    List<ErpBaseEndProduct> findAll();

    ErpBaseEndProduct findByEndProductNo(String  endProductNo);
}

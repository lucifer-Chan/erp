package com.yintong.erp.domain.basis;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by jianqiang on 2018/5/9 0009.
 */
public interface ErpBaseEndProductRepository extends JpaRepository<ErpBaseEndProduct,Long> {



    ErpBaseEndProduct findByEndProductNo(String  endProductNo);

    Page<ErpBaseEndProduct> findAll(Specification<ErpBaseEndProduct> specification, Pageable pageable);
}

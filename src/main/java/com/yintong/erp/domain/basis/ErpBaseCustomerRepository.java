package com.yintong.erp.domain.basis;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by jianqiang on 2018/5/12.
 */
public interface ErpBaseCustomerRepository extends JpaRepository<ErpBaseCustomer, Long> {

    Page<ErpBaseCustomer> findAll(Specification<ErpBaseCustomer> specification, Pageable pageable);

}

package com.yintong.erp.domain.basis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by jianqiang on 2018/5/12.
 */
public interface ErpBaseCustomerRepository extends JpaRepository<ErpBaseCustomer, Long> {

    List<ErpBaseCustomer> findAll();

}

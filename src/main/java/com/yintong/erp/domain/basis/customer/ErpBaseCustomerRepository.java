package com.yintong.erp.domain.basis.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by jianqiang on 2018/5/10 0010.
 */
public interface ErpBaseCustomerRepository extends JpaRepository<ErpBaseCustomer,Long> {

    List<ErpBaseCustomer> findAll();

    ErpBaseCustomer findByCustNo(String custNo);
}

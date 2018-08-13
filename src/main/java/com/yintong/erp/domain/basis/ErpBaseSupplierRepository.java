package com.yintong.erp.domain.basis;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by jianqiang on 2018/5/12.
 */
public interface ErpBaseSupplierRepository extends JpaRepository<ErpBaseSupplier, Long> {

    Page<ErpBaseSupplier> findAll(Specification<ErpBaseSupplier> specification, Pageable pageable);

    List<ErpBaseSupplier> findByIdNotIn(List<Long> ids);
}

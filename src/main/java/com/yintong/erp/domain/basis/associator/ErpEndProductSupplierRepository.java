package com.yintong.erp.domain.basis.associator;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by jianqiang on 2018/5/12.
 */
public interface ErpEndProductSupplierRepository extends JpaRepository<ErpEndProductSupplier, Long> {

    List<ErpEndProductSupplier> findBySupplierId(Long supplierId);

    List<ErpEndProductSupplier> findByEndProductId(Long productId);

    Optional<ErpEndProductSupplier> findByEndProductIdAndSupplierId(Long productId, Long supplierId);

    void deleteBySupplierId(Long supplierId);

    void deleteByEndProductId(Long productId);
}

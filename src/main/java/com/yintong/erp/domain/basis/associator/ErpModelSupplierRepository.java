package com.yintong.erp.domain.basis.associator;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by jianqiang on 2018/5/12.
 */
public interface ErpModelSupplierRepository extends JpaRepository<ErpModelSupplier,Long> {

    List<ErpModelSupplier> findBySupplierId(Long supplierId);

    List<ErpModelSupplier> findByModelId(Long modelId);

    Optional<ErpModelSupplier> findByModelIdAndSupplierId(Long modelId, Long supplierId);

    Optional<ErpModelSupplier> findByBarCode(String barcode);

    void deleteBySupplierId(Long supplierId);

    void deleteByModelId(Long rawMaterId);
}

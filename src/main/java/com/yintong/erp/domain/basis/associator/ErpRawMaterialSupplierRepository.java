package com.yintong.erp.domain.basis.associator;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by jianqiang on 2018/5/12.
 */
public interface ErpRawMaterialSupplierRepository extends JpaRepository<ErpRawMaterialSupplier,Long> {

    List<ErpRawMaterialSupplier> findBySupplierId(Long supplierId);

    List<ErpRawMaterialSupplier> findByRawMaterId(Long rawMaterId);

    Optional<ErpRawMaterialSupplier> findByRawMaterIdAndSupplierId(Long rawMaterId, Long supplierId);

    Optional<ErpRawMaterialSupplier> findByBarCode(String barcode);

    void deleteBySupplierId(Long supplierId);

    void deleteByRawMaterId(Long rawMaterId);
}

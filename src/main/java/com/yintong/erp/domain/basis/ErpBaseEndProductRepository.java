package com.yintong.erp.domain.basis;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by jianqiang on 2018/5/9 0009.
 */
public interface ErpBaseEndProductRepository extends JpaRepository<ErpBaseEndProduct,Long> {

    Page<ErpBaseEndProduct> findAll(Specification<ErpBaseEndProduct> specification, Pageable pageable);

    @Query(value = "SELECT count(imported_at) num, imported_at FROM erp_base_end_product WHERE imported_at IS NOT NULL GROUP BY imported_at ORDER BY imported_at DESC",
            nativeQuery = true)
    List<Object []> groupByImportAt();

    List<ErpBaseEndProduct> findByImportedAt(String importedAt);

    List<ErpBaseEndProduct> findByEndProductNameAndSpecification(String endProductName, String specification);

    List<ErpBaseEndProduct> findByEndProductNameAndSpecificationAndIdNot(String endProductName, String specification, Long id);

    Optional<ErpBaseEndProduct> findByBarCode(String barcode);

    List<ErpBaseEndProduct> findAllByOrderById();
}

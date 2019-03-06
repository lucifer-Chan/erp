package com.yintong.erp.domain.stock;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpStockPlaceRepository extends JpaRepository<ErpStockPlace, Long> {

    ErpStockPlace findFirstByBarCode(String barCode);

    Page<ErpStockPlace> findAll(Specification<ErpStockPlace> specification, Pageable pageable);


    List<ErpStockPlace> findByMaterialSupplierAssId(Long materialSupplierAssId);

    List<ErpStockPlace> findByIdIn(List<Long> ids);

    List<ErpStockPlace> findByMaterialSupplierAssIdIn(List<Long> materialSupplierAssIds);

    List<ErpStockPlace> findByMaterialSupplierBarCode(String materialSupplierBarCode);

    ErpStockPlace findByPlaceCode(String placeCode);
}

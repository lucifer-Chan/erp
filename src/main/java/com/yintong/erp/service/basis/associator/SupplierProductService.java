package com.yintong.erp.service.basis.associator;

import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.domain.basis.ErpBaseSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpEndProductSupplier;
import com.yintong.erp.domain.basis.associator.ErpEndProductSupplierRepository;
import com.yintong.erp.validator.OnDeleteProductValidator;
import com.yintong.erp.validator.OnDeleteSupplierValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author lucifer.chan
 * @create 2018-06-03 下午7:02
 * 供应商成品关联的服务
 **/
@Service
public class SupplierProductService implements OnDeleteProductValidator, OnDeleteSupplierValidator {

    @Autowired ErpBaseSupplierRepository supplierRepository;

    @Autowired ErpBaseEndProductRepository productRepository;

    @Autowired ErpEndProductSupplierRepository productSupplierRepository;

    /**
     * 建立供应商和成品的关联
     * @param association
     */
    public void save(ErpEndProductSupplier association){
        ErpEndProductSupplier shouldBeNull = productSupplierRepository
                .findByEndProductIdAndSupplierId(association.getEndProductId(), association.getSupplierId())
                .orElse(null);
        if(Objects.isNull(shouldBeNull))
            productSupplierRepository.save(association);
    }

    /**
     * 批量保存
     * @param associations
     */
    public void batchSave(List<ErpEndProductSupplier> associations) {
        associations.forEach(this::save);
    }

    /**
     * 删除关联
     * @param productId
     * @param supplierId
     */
    public void delete(Long productId, Long supplierId){
        productSupplierRepository.deleteByEndProductIdAndSupplierId(productId, supplierId);

    }

    /**
     * 根据成品id删除
     * @param productId
     */
    public void deleteByProductId(Long productId){
        productSupplierRepository.deleteByEndProductId(productId);
    }

    /**
     * 根据供应商id删除
     * @param supplierId
     */
    public void deleteBySupplierId(Long supplierId){
        productSupplierRepository.deleteBySupplierId(supplierId);
    }

    @Override
    public void onDeleteProduct(Long productId) {
        Assert.isTrue(
                CollectionUtils.isEmpty(productSupplierRepository.findByEndProductId(productId)),
                "请先删除成品和供应商之间的关联。"
        );
    }

    @Override
    public void onDeleteSupplier(Long supplierId) {
        Assert.isTrue(
                CollectionUtils.isEmpty(productSupplierRepository.findBySupplierId(supplierId)),
                "请先删除供应商和成品之间的关联."
        );
    }
}

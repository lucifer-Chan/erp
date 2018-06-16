package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.domain.basis.associator.ErpEndProductSupplier;
import com.yintong.erp.domain.basis.associator.ErpEndProductSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplier;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplierRepository;
import com.yintong.erp.service.basis.SupplierService;
import com.yintong.erp.service.basis.SupplierService.SupplierParameterBuilder;
import com.yintong.erp.service.basis.associator.SupplierProductService;
import com.yintong.erp.service.basis.associator.SupplierRawMaterialService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

/**
 * @author lucifer.chan
 * @create 2018-05-22 上午12:11
 * 供应商
 **/
@RestController
@RequestMapping("basis/supplier")
public class SupplierController {

    @Autowired SupplierService supplierService;

    @Autowired SupplierProductService supplierProductService;

    @Autowired ErpEndProductSupplierRepository productSupplierRepository;

    @Autowired
    SupplierRawMaterialService supplierRawMaterialService;
    @Autowired
    ErpRawMaterialSupplierRepository erpRawMaterialSupplierRepository;

    /**
     * 组合查询
     * @param parameter
     * @return
     */
    @GetMapping
    public BaseResult query(SupplierParameterBuilder parameter){
        Page<ErpBaseSupplier> page = supplierService.query(parameter);
        return page2BaseResult(page);
    }

    /**
     * 新增供应商
     * @param supplier
     * @return
     */
    @PostMapping
    public BaseResult create(@RequestBody ErpBaseSupplier supplier){
        return new BaseResult().addPojo(supplierService.create(supplier));
    }

    /**
     * 更新供应商
     * @param supplier
     * @return
     */
    @PutMapping
    public BaseResult update(@RequestBody ErpBaseSupplier supplier){
        return new BaseResult().addPojo(supplierService.update(supplier));
    }

    /**
     * 根据id删除
     * @param supplierId
     * @return
     */
    @DeleteMapping("{supplierId}")
    public BaseResult delete(@PathVariable Long supplierId){
        supplierService.delete(supplierId);
        return new BaseResult().setErrmsg("删除成功");
    }

    /**
     * 根据供应商id查找供应商
     * @param supplierId
     * @return
     */
    @GetMapping("{supplierId}")
    public BaseResult one(@PathVariable Long supplierId){
        return new BaseResult().addPojo(supplierService.one(supplierId));
    }

    /**
     * 根据供应商id获取所有的未关联的成品树[包括类别节点]->ztree
     * @param supplierId
     * @return
     */
    @GetMapping("product/nodes/unassociated")
    public BaseResult unAssociatedProductNodes(Long supplierId){
        return new BaseResult().addList(supplierProductService.unAssociatedNodes(supplierId));
    }

    /**
     * 根据供应商id获取所有的已关联的成品树[包括类别节点]->ztree
     * @param supplierId
     * @return
     */
    @GetMapping("product/nodes/associated")
    public BaseResult associatedProductNodes(Long supplierId){
        return new BaseResult().addList(supplierProductService.associatedNodes(supplierId));
    }

    /**
     * 获取所有的成品的树
     * @return
     */
    @GetMapping("product/nodes/all")
    public BaseResult allNodes(){
        return new BaseResult().addList(supplierProductService.productTreeNodes());
    }


    /**
     * 保存供应商和成品之间的关联
     */
    @PostMapping("{supplierId}/product")
    public BaseResult saveProductAss(@PathVariable Long supplierId, @RequestBody List<Long> productIds){
        supplierProductService.batchSave(supplierId, productIds);
        return new BaseResult().setErrmsg("保存成功");
    }

    /**
     * 保存供应商的成品的上下限
     */
    @PatchMapping("{supplierId}/product/{productId}")
    public BaseResult saveProductWarning(@PathVariable Long supplierId, @PathVariable Long productId, Integer alertLower, Integer alertUpper){
        ErpEndProductSupplier one = productSupplierRepository.findByEndProductIdAndSupplierId(productId, supplierId).orElse(null);
        Assert.notNull(one, "未找到关联");
        one.setAlertLower(alertLower);
        one.setAlertUpper(alertUpper);
        return new BaseResult().addPojo(productSupplierRepository.save(one)).setErrmsg("保存成功");
    }

    /**
     * 保存供应商和成品的关联
     */
    @DeleteMapping("{supplierId}/product/{productId}")
    public BaseResult deleteProductAss(@PathVariable Long supplierId, @PathVariable Long productId){
        supplierProductService.delete(productId, supplierId);
        return new BaseResult().setErrmsg("删除成功");
    }




    /**
     * 根据供应商id获取所有的未关联的原材料树[包括类别节点]->ztree
     * @param supplierId
     * @return
     */
    @GetMapping("rawMaterial/nodes/unassociated")
    public BaseResult unAssociatedRawMaterialNodes(Long supplierId){
        return new BaseResult().addList(supplierRawMaterialService.unAssociatedNodes(supplierId));
    }

    /**
     * 根据供应商id获取所有的已关联的原材料树[包括类别节点]->ztree
     * @param supplierId
     * @return
     */
    @GetMapping("rawMaterial/nodes/associated")
    public BaseResult associatedRawMaterialNodes(Long supplierId){
        return new BaseResult().addList(supplierRawMaterialService.associatedNodes(supplierId));
    }

    /**
     * 获取所有的原材料的树
     * @return
     */
    @GetMapping("rawMaterial/nodes/all")
    public BaseResult allRawNodes(){
        return new BaseResult().addList(supplierRawMaterialService.rawMaterTreeNodes());
    }


    /**
     * 保存供应商和原材料之间的关联
     */
    @PostMapping("{supplierId}/rawMaterial")
    public BaseResult saveRawMaterialAss(@PathVariable Long supplierId, @RequestBody List<Long> rawMaterials){
        supplierRawMaterialService.batchSave(supplierId, rawMaterials);
        return new BaseResult().setErrmsg("保存成功");
    }

    /**
     * 保存供应商的原材料的上下限
     */
    @PatchMapping("{supplierId}/rawMaterial/{rawMaterId}")
    public BaseResult saveRawMaterialWarning(@PathVariable Long supplierId, @PathVariable Long rawMaterId, Integer alertLower, Integer alertUpper){
        ErpRawMaterialSupplier one = erpRawMaterialSupplierRepository.findByRawMaterIdAndSupplierId(rawMaterId, supplierId).orElse(null);
        Assert.notNull(one, "未找到关联");
        one.setAlertLower(alertLower);
        one.setAlertUpper(alertUpper);
        return new BaseResult().addPojo(erpRawMaterialSupplierRepository.save(one)).setErrmsg("保存成功");
    }

    /**
     * 保存供应商和成品的关联
     */
    @DeleteMapping("{supplierId}/rawMaterial/{rawMaterId}")
    public BaseResult deleteRawMaterialAss(@PathVariable Long supplierId, @PathVariable Long rawMaterId){
        supplierRawMaterialService.delete(rawMaterId, supplierId);
        return new BaseResult().setErrmsg("删除成功");
    }

}

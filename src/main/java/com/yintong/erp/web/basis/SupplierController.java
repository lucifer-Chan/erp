package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.service.basis.SupplierService;
import com.yintong.erp.service.basis.SupplierService.SupplierParameterBuilder;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

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

}

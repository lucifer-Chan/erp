package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.service.basis.ProductService;
import com.yintong.erp.service.basis.SupplierService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

/**
 * Created by jianqiang on 2018/5/26 0026.
 * 成品
 */
@RestController
@RequestMapping("basis/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SupplierService supplierService;

    @GetMapping("findSupplierAll")
    public BaseResult findSupplierAll(){
        List<ErpBaseSupplier> supplierList= supplierService.findSupplierAll();
        return new BaseResult().addList(supplierList);
    }

    @GetMapping
    public BaseResult query(ProductService.ProductParameterBuilder parameter){
        Page<ErpBaseEndProduct> page = productService.query(parameter);
        return page2BaseResult(page);
    }

    /**
     * 新增成品
     * @param product
     * @return
     */
    @PostMapping
    public BaseResult create(@RequestBody ErpBaseEndProduct product){
        return new BaseResult().addPojo(productService.create(product));
    }

    /**
     * 根据成品id查找供应商
     * @param productId
     * @return
     */
    @GetMapping("{productId}")
    public BaseResult one(@PathVariable Long productId){
        return new BaseResult().addPojo(productService.one(productId));
    }

    /**
     * 根据id删除
     * @param productId
     * @return
     */
    @DeleteMapping("{productId}")
    public BaseResult delete(@PathVariable Long productId){
        productService.delete(productId);
        return new BaseResult();
    }


    /**
     * 更新成品
     * @param product
     * @return
     */
    @PutMapping
    public BaseResult update(@RequestBody ErpBaseEndProduct product){
        return new BaseResult().addPojo(productService.update(product));
    }
}

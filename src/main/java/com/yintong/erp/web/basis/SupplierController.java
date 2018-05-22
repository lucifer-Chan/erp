package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.service.basis.SupplierService;
import com.yintong.erp.service.basis.SupplierService.SupplierParameterBuilder;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping
    public BaseResult query(SupplierParameterBuilder parameter){
        Page<ErpBaseSupplier> page = supplierService.query(parameter);
        return page2BaseResult(page);
    }

}

package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseModelTool;
import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.service.basis.MouldService;
import com.yintong.erp.service.basis.SupplierService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

/**
 * Created by jianqiang on 2018/5/22 0022.
 * 模具
 */
@RestController
@RequestMapping("basis/mould")
public class MouldController {

    @Autowired
    private MouldService mouldService;

    @Autowired
    private SupplierService supplierService;

    @GetMapping("findSupplierAll")
    public BaseResult findSupplierAll(){
        List<ErpBaseSupplier> supplierList= supplierService.findSupplierAll();
        return new BaseResult().addList(supplierList);
    }

    @GetMapping
    public BaseResult query(MouldService.MouldParameterBuilder parameter){
        Page<ErpBaseModelTool> page = mouldService.query(parameter);
        return page2BaseResult(page);
    }

    /**
     * 新增模具
     * @param mould
     * @return
     */
    @PostMapping
    public BaseResult create(@RequestBody ErpBaseModelTool mould){
        return new BaseResult().addPojo(mouldService.create(mould));
    }

    /**
     * 根据模具id查找供应商
     * @param mouldId
     * @return
     */
    @GetMapping("{mouldId}")
    public BaseResult one(@PathVariable Long mouldId){
        return new BaseResult().addPojo(mouldService.one(mouldId));
    }

    /**
     * 根据id删除
     * @param mouldId
     * @return
     */
    @DeleteMapping("{mouldId}")
    public BaseResult delete(@PathVariable Long mouldId){
        mouldService.delete(mouldId);
        return new BaseResult();
    }


    /**
     * 更新供应商
     * @param mould
     * @return
     */
    @PutMapping
    public BaseResult update(@RequestBody ErpBaseModelTool mould){
        return new BaseResult().addPojo(mouldService.update(mould));
    }
}

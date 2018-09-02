package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseModelTool;
import com.yintong.erp.domain.basis.ErpBaseModelToolRepository;
import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.service.basis.MouldService;
import com.yintong.erp.service.basis.SupplierService;
import com.yintong.erp.service.basis.associator.SupplierMouldService;
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

    @Autowired MouldService mouldService;

    @Autowired SupplierService supplierService;

    @Autowired ErpBaseModelToolRepository modelToolRepository;

    @Autowired SupplierMouldService supplierMouldService;

    /**
     * 余量
     * @param mouldAssId
     * @return total
     */
    @GetMapping("{mouldAssId}/stockRemain")
    public BaseResult stockRemain(@PathVariable Long mouldAssId) {
        return new BaseResult().put("total", supplierMouldService.stockRemain(mouldAssId));
    }

    @GetMapping("all")
    public BaseResult findAll(){
        return new BaseResult().addList(modelToolRepository.findAllByOrderByModelToolTypeCode());
    }

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
     * 根据成品找已关联供应商
     * @param mouldId
     * @return
     */
    @GetMapping("{mouldId}/supplier")
    public BaseResult findSuppliersByMaterialId(@PathVariable Long mouldId){
        return new BaseResult().addList(supplierMouldService.findSuppliersAss(mouldId));
    }

    /**
     * 根据成品找未关联供应商
     * @param mouldId
     * @return
     */
    @GetMapping("{mouldId}/supplier/unassociated")
    public BaseResult findUnassociatedSuppliers(@PathVariable Long mouldId){
        return new BaseResult().addList(supplierMouldService.findUnassociatedSuppliers(mouldId));
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

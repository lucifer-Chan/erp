package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseModelTool;
import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.service.basis.MouldService;
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

    @GetMapping("findSupplierAll")
    public BaseResult findSupplierAll(){
        List<ErpBaseSupplier> supplierList= mouldService.FindSupplierAll();
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
}

package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseRawMaterial;
import com.yintong.erp.service.basis.RawMaterialService;
import com.yintong.erp.service.basis.RawMaterialService.RawMaterialParameterBuilder;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

/**
 * Created by Zangtao on 2018/5/26.
 * 原材料
 */
@RestController
@RequestMapping("basis/rawMaterial")
public class RawMaterialController {

    @Autowired
    private RawMaterialService rawMaterialService;

    @RequestMapping("list")
    public BaseResult list(RawMaterialParameterBuilder parameterBuilder){
        Page<ErpBaseRawMaterial> list = rawMaterialService.list(parameterBuilder);
        return page2BaseResult(list);
    }

    @RequestMapping("save")
    public BaseResult add(@RequestBody ErpBaseRawMaterial erpBaseRawMaterial){
        rawMaterialService.save(erpBaseRawMaterial);
        return new BaseResult().addPojo(erpBaseRawMaterial);
    }

    @RequestMapping("remove")
    public BaseResult remove(Long id){
        rawMaterialService.remove(id);
        return new BaseResult().addPojo(id);
    }


}

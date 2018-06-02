package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseRawMaterial;
import com.yintong.erp.service.basis.RawMaterialService;
import com.yintong.erp.service.basis.RawMaterialService.RawMaterialParameterBuilder;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public BaseResult query(RawMaterialParameterBuilder parameter){
        Page<ErpBaseRawMaterial> page = rawMaterialService.query(parameter);
        return page2BaseResult(page);
    }

    /**
     * 新增原材料
     * @param material
     * @return
     */
    @PostMapping
    public BaseResult create(@RequestBody ErpBaseRawMaterial material){
        return new BaseResult().addPojo(rawMaterialService.create(material));
    }

    /**
     * 根据成品id查找原材料
     * @param materialId
     * @return
     */
    @GetMapping("{materialId}")
    public BaseResult one(@PathVariable Long materialId){
        return new BaseResult().addPojo(rawMaterialService.one(materialId));
    }

    /**
     * 根据id删除
     * @param materialId
     * @return
     */
    @DeleteMapping("{materialId}")
    public BaseResult delete(@PathVariable Long materialId){
        rawMaterialService.delete(materialId);
        return new BaseResult();
    }


    /**
     * 更新成原材料
     * @param material
     * @return
     */
    @PutMapping
    public BaseResult update(@RequestBody ErpBaseRawMaterial material){
        return new BaseResult().addPojo(rawMaterialService.update(material));
    }
}

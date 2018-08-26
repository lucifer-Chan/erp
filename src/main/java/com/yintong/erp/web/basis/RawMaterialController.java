package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseRawMaterial;
import com.yintong.erp.domain.basis.ErpBaseRawMaterialRepository;
import com.yintong.erp.service.basis.RawMaterialService;
import com.yintong.erp.service.basis.RawMaterialService.RawMaterialParameterBuilder;
import com.yintong.erp.service.basis.associator.SupplierRawMaterialService;
import com.yintong.erp.utils.base.BaseResult;
import com.yintong.erp.utils.excel.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

/**
 * Created by Zangtao on 2018/5/26.
 * 原材料
 */
@Slf4j
@RestController
@RequestMapping("basis/rawMaterial")
public class RawMaterialController {

    @Autowired RawMaterialService rawMaterialService;

    @Autowired ErpBaseRawMaterialRepository rawMaterialRepository;

    @Autowired SupplierRawMaterialService supplierRawMaterialService;

    /**
     * 余量
     * @param materialAssId 和供应商关联的原材料id
     * @return total
     */
    @GetMapping("{materialAssId}/stockRemain")
    public BaseResult stockRemain(@PathVariable Long materialAssId) {
        return new BaseResult().put("total", supplierRawMaterialService.stockRemain(materialAssId));

    }

    @GetMapping("all")
    public BaseResult findAll(){
        return new BaseResult().addList(rawMaterialRepository.findAllByOrderByRawTypeCode());
    }

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
     * 根据原材料id查找原材料
     * @param materialId
     * @return
     */
    @GetMapping("{materialId}")
    public BaseResult one(@PathVariable Long materialId){
        return new BaseResult().addPojo(rawMaterialService.one(materialId));
    }

    /**
     * 根据原材料找已关联供应商
     * @param materialId
     * @return
     */
    @GetMapping("{materialId}/supplier")
    public BaseResult findSuppliersByMaterialId(@PathVariable Long materialId){
        return new BaseResult().addList(rawMaterialService.findSuppilersAss(materialId));
    }

    /**
     * 根据原材料找未关联供应商
     * @param materialId
     * @return
     */
    @GetMapping("{materialId}/supplier/unassociated")
    public BaseResult findUnassociatedSuppliers(@PathVariable Long materialId){
        return new BaseResult().addList(rawMaterialService.findUnassociatedSuppliers(materialId));
    }


    /**
     * 保存原材料和供应商的关联
     * @param materialId
     * @param supplierId
     * @param up
     * @param low
     * @return
     */
    @PostMapping("{materialId}/supplier/{supplierId}")
    public BaseResult saveAss(@PathVariable Long materialId, @PathVariable Long supplierId, Integer up, Integer low){
        boolean result = supplierRawMaterialService.save(materialId, supplierId, up, low);
        return new BaseResult().setErrmsg(result ? "保存成功" : "已存在关联");
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

    /**
     * 导入Excel文件
     * @param file
     * @param type
     * @return
     * @throws IOException
     */
    @PostMapping("upload")
    public BaseResult upload(@RequestParam(value = "excelFile", required = false) MultipartFile file, String type) throws IOException {
        log.info("正在导入：" + type);
        ExcelUtil.ExcelImporter<ErpBaseRawMaterial> result =  rawMaterialService.import0(file.getInputStream());
        return new BaseResult().put("successNum", result.getSuccessData().size());
    }

    /**
     * 按照导入时间group数量
     * @return
     */
    @GetMapping("group")
    public BaseResult group(){
        List<Map<String, Object>> ret = rawMaterialRepository.groupByImportAt().stream()
                .map(array-> new HashMap<String, Object>(){{
                    put("num", array[0]);
                    put("importedAt", array[1]);
                }})
                .collect(Collectors.toList());
        return new BaseResult().addList(ret);
    }

    /**
     * 根据导入时间批量删除
     * @param importedAt
     * @return
     */
    @DeleteMapping("batch/{importedAt}")
    public BaseResult batchDelete(@PathVariable String importedAt){
        log.info("importedAt", importedAt);
        Assert.hasLength(importedAt, "参数无效");
        List<ErpBaseRawMaterial> products = rawMaterialRepository.findByImportedAt(importedAt);
        int sum = products.size();
        int count = 0;
        for(ErpBaseRawMaterial material : products){
            try{
                rawMaterialService.delete(material.getId());
                count ++;
            } catch (Exception e){

            }
        }
        return new BaseResult().setErrmsg("成功删除（" + count + "/" + sum + "）");
    }
}

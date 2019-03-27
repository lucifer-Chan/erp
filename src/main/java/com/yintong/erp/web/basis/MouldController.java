package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseModelTool;
import com.yintong.erp.domain.basis.ErpBaseModelToolRepository;
import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.service.basis.MouldService;
import com.yintong.erp.service.basis.SupplierService;
import com.yintong.erp.service.basis.associator.SupplierMouldService;
import com.yintong.erp.utils.base.BaseResult;
import com.yintong.erp.utils.base.JsonWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import static com.yintong.erp.utils.excel.ExcelUtil.ExcelImporter;
import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

/**
 * Created by jianqiang on 2018/5/22 0022.
 * 模具
 */
@RestController
@RequestMapping("basis/mould")
@Slf4j
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

//    @GetMapping("all")
//    public BaseResult findAll(){
//        return new BaseResult().addList(modelToolRepository.findAllByOrderByModelToolTypeCode());
//    }

    /**
     * 只提供id和描述
     * @return
     */
    @GetMapping("all")
    public BaseResult findAll(){
        return new BaseResult().addList(
                modelToolRepository.findAllByOrderByModelToolTypeCode().stream().map(product -> product.filter("id", "description")).collect(Collectors.toList())
        );
    }

    @GetMapping("lookup")
    public BaseResult lookup(){
        return new BaseResult().addList(
                modelToolRepository.findAll().stream().filter(it -> StringUtils.hasLength(it.getModelPlace()))
                    .map(it -> JsonWrapper.builder().add("code", it.getModelPlace()).add("name", it.getModelPlace()).build())
                    .collect(Collectors.toList())
        );
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
        ExcelImporter<ErpBaseModelTool> result =  mouldService.import0(file.getInputStream());
        return new BaseResult().put("successNum", result.getSuccessData().size());
    }

    /**
     * 按照导入时间group数量
     * @return
     */
    @GetMapping("group")
    public BaseResult group(){
        List<Map<String, Object>> ret = modelToolRepository.groupByImportAt().stream()
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
        log.info("importedAt:{}", importedAt);
        Assert.hasLength(importedAt, "参数无效");
        List<ErpBaseModelTool> moulds = modelToolRepository.findByImportedAt(importedAt);
        int sum = moulds.size();
        int count = 0;
        for(ErpBaseModelTool mould : moulds){
            try{
                mouldService.delete(mould.getId());
                count ++;
            } catch (Exception e){

            }
        }
        return new BaseResult().setErrmsg("成功删除（" + count + "/" + sum + "）");
    }










}

package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.service.basis.ProductService;
import com.yintong.erp.service.basis.SupplierService;
import com.yintong.erp.service.basis.associator.SupplierProductService;
import com.yintong.erp.utils.base.BaseResult;
import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.utils.excel.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

/**
 * Created by jianqiang on 2018/5/26 0026.
 * 成品
 */
@Slf4j
@RestController
@RequestMapping("basis/product")
public class ProductController {

    @Autowired ProductService productService;

    @Autowired SupplierService supplierService;

    @Autowired ErpBaseEndProductRepository productRepository;

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
     * 更新成品
     * @param product
     * @return
     */
    @PutMapping
    public BaseResult update(@RequestBody ErpBaseEndProduct product){
        return new BaseResult().addPojo(productService.update(product)).setErrmsg("更新成功");
    }

    /**
     * 新增成品
     * @param product
     * @return
     */
    @PostMapping
    public BaseResult create(@RequestBody ErpBaseEndProduct product){
        return new BaseResult().addPojo(productService.create(product)).setErrmsg("保存成功");
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
        return new BaseResult().setErrmsg("删除成功");
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
        ExcelUtil.ExcelImporter<ErpBaseEndProduct> result =  productService.import0(file.getInputStream());
        return new BaseResult().put("successNum", result.getSuccessData().size());
    }

    /**
     * 按照导入时间group数量
     * @return
     */
    @GetMapping("group")
    public BaseResult group(){
        List<Map<String, Object>> ret = productRepository.groupByImportAt().stream()
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
        List<ErpBaseEndProduct> products = productRepository.findByImportedAt(importedAt);
        int sum = products.size();
        int count = 0;
        for(ErpBaseEndProduct product : products){
            try{
                productService.delete(product.getId());
                count ++;
            } catch (Exception e){

            }
        }
        return new BaseResult().setErrmsg("成功删除（" + count + "/" + sum + "）");
    }

}

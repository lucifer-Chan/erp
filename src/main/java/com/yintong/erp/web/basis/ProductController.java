package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.domain.basis.associator.ErpBaseProductBom;
import com.yintong.erp.service.basis.ProductService;
import com.yintong.erp.service.basis.SupplierService;
import com.yintong.erp.service.basis.associator.ProductBomService;
import com.yintong.erp.service.basis.associator.SupplierProductService;
import com.yintong.erp.service.stock.StockOptService;
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
 * Created by jianqiang on 2018/5/26 0026.
 * 成品
 */
@Slf4j
@RestController
@RequestMapping("basis/product")
public class ProductController {

    @Autowired ProductService productService;

    @Autowired SupplierService supplierService;

    @Autowired SupplierProductService supplierProductService;

    @Autowired ProductBomService bomService;

    @Autowired StockOptService stockOptService;

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
     * 出入库历史
     * @param productId
     * @return
     */
    @GetMapping("{productId}/stock")
    public BaseResult stockHistory(@PathVariable Long productId){
        return new BaseResult().addList(stockOptService.findOptsByProductId(productId));
    }

    /**
     * 余量
     * @param productId
     * @return safe,total
     */
    @GetMapping("{productId}/stockRemain")
    public BaseResult stockRemain(@PathVariable Long productId) {
        return new BaseResult().add(productService.stockRemain(productId));

    }

    @GetMapping("all")
    public BaseResult findAll(){
        return new BaseResult().addList(productRepository.findAll());
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
     * 保存成品的上下限
     * @param productId
     * @param alertLower
     * @param alertUpper
     * @return
     */
    @PatchMapping("{productId}")
    public BaseResult saveRawMaterialWarning(@PathVariable Long productId, Integer alertLower, Integer alertUpper){
        ErpBaseEndProduct one = productService.one(productId);
        one.setAlertLower(alertLower);
        one.setAlertUpper(alertUpper);
        return new BaseResult().addPojo(productRepository.save(one)).setErrmsg("保存成功");
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
     * 根据成品找已关联供应商
     * @param productId
     * @return
     */
    @GetMapping("{productId}/supplier")
    public BaseResult findSuppliersByMaterialId(@PathVariable Long productId){
        return new BaseResult().addList(supplierProductService.findSuppliersAss(productId));
    }

    /**
     * 根据成品找未关联供应商
     * @param productId
     * @return
     */
    @GetMapping("{productId}/supplier/unassociated")
    public BaseResult findUnassociatedSuppliers(@PathVariable Long productId){
        return new BaseResult().addList(supplierProductService.findUnassociatedSuppliers(productId));
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
        log.info("importedAt:{}", importedAt);
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

    /**
     * 获取物料清单
     * @param productId
     * @return
     */
    @GetMapping("{productId}/bom")
    public BaseResult findBoms(@PathVariable Long productId){
        return new BaseResult().addList(bomService.findBomList(productId));
    }

    /**
     * 获取单个物料清单Item
     * @param bomId
     * @return
     */
    @GetMapping("bom/{bomId}")
    public BaseResult findOneBom(@PathVariable Long bomId){
        return new BaseResult().addPojo(bomService.one(bomId));
    }

    /**
     * 供选的原材料
     * @param productId
     * @return
     */
    @GetMapping("{productId}/materials")
    public BaseResult findMaterials(@PathVariable Long productId){
        return new BaseResult().addList(bomService.lookup(productId));
    }

    /**
     * 创建单个bom
     * @param bom
     * @return
     */
    @PostMapping("bom")
    public BaseResult createBom(@RequestBody ErpBaseProductBom bom){
        return new BaseResult().addPojo(bomService.create(bom));
    }

    /**
     * 更新bom的数量
     * @param bomId
     * @param materialNum
     * @return
     */
    @PatchMapping("bom/{bomId}")
    public BaseResult updateBom(@PathVariable Long bomId, String materialNum){
        return new BaseResult().addPojo(bomService.update(bomId, materialNum));
    }

    @DeleteMapping("bom/{bomId}")
    public BaseResult deleteBom(@PathVariable Long bomId){
        bomService.delete(bomId);
        return new BaseResult().setErrmsg("删除成功");
    }


}

package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.domain.basis.associator.ErpEndProductSupplier;
import com.yintong.erp.domain.sale.ErpSaleOrderItem;
import com.yintong.erp.domain.sale.ErpSaleOrderItemRepository;
import com.yintong.erp.service.basis.associator.SupplierProductService;
import com.yintong.erp.utils.bar.BarCodeConstants;
import static com.yintong.erp.utils.common.Constants.SaleOrderStatus.STATUS_003;

import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.utils.excel.ExcelUtil;
import com.yintong.erp.utils.excel.ExcelUtil.ExcelImporter;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import com.yintong.erp.validator.OnDeleteProductValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.*;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.like;
import static javax.persistence.criteria.Predicate.BooleanOperator.OR;

/**
 * Created by jianqiang on 2018/5/26 0026.
 * 成品
 */
@Slf4j
@Service
public class ProductService {

    @Autowired  ErpBaseEndProductRepository productRepository;

    @Autowired ErpSaleOrderItemRepository orderItemRepository;
    
    @Autowired SupplierProductService supplierProductService;

    @Autowired(required = false) List<OnDeleteProductValidator> onDeleteProductValidators;

    /**
     *查询列表
     * @param parameter
     * @return
     */
    public Page<ErpBaseEndProduct> query(ProductService.ProductParameterBuilder parameter){
        return productRepository.findAll(parameter.specification(), parameter.pageable());
    }
    /**
     * 根据id查找成品
     * @param productId
     * @return
     */
    public ErpBaseEndProduct one(Long productId){
        ErpBaseEndProduct product = productRepository.findById(productId).orElse(null);
        Assert.notNull(product, "未找到成品");
        return product;
    }

    /**
     * 根据barcode查找成品
     * @param barcode
     * @return
     */
    public ErpBaseEndProduct findByBarcode(String barcode){
        ErpBaseEndProduct product = productRepository.findByBarCode(barcode).orElse(null);
        Assert.notNull(product, "未找到成品");
        return product;
    }

    /**
     * 导入
     * @param excel
     * @return
     */
    public ExcelImporter<ErpBaseEndProduct> import0(InputStream excel) throws IOException {
        ExcelImporter<ErpBaseEndProduct> importer = new ExcelUtil(excel).builder(ErpBaseEndProduct.class);
        List<ErpBaseEndProduct> entities = importer.getSuccessData();
        Date importedAt = new Date();
        List<ErpBaseEndProduct> count = new ArrayList<>();
        for (ErpBaseEndProduct entity : entities){
            try{
                entity.setImportedAt(DateUtil.getDateTimeString(importedAt));
                count.add(create(entity));
            } catch (Exception e){
                log.error("导入成品失败", e);
            }
        }
        return importer.setSuccessData(count);
    }

    /**
     * 创建成品
     * @param product
     * @return
     */
    @Transactional
    public ErpBaseEndProduct create(ErpBaseEndProduct product){
        product.setId(null);//防止假数据
        validateProductType(product);
        return productRepository.save(product);
    }
    /**
     * 更新成品
     * @param product
     * @return
     */
    public ErpBaseEndProduct update(ErpBaseEndProduct product){
        Assert.notNull(product.getId(), "成品id不能为空");
        ErpBaseEndProduct inDb = productRepository.findById(product.getId()).orElse(null);
        Assert.notNull(inDb, "未找到模具");
        validateProductType(product);
        product.setBarCode(inDb.getBarCode());
        product.setAlertLower(inDb.getAlertLower());
        product.setAlertUpper(inDb.getAlertUpper());
        return productRepository.save(product);
    }
    /**
     * 删除成品
     * @param productId
     */
    @Transactional
    public void delete(Long productId){
        if(!CollectionUtils.isEmpty(onDeleteProductValidators))
            onDeleteProductValidators.forEach(validator -> validator.onDeleteProduct(productId));
        productRepository.deleteById(productId);
    }


    /**
     * 验证成品类型
     * @param product
     */
    private void validateProductType(ErpBaseEndProduct product){
        Assert.notNull(product, "成品null");
        String type = product.getEndProductTypeCode();
        Assert.hasLength(type, "类型不能为空");
        Assert.hasLength(product.getEndProductName(), "成品名称不能为空");
        Assert.isTrue(Arrays.asList(PAF0, PAT0, PTD0, PTT0, PTW0, PTU0, PNR0, PNY0, PNM0, PNF0, PNX0)
                .contains(BarCodeConstants.BAR_CODE_PREFIX.valueOf(type)), "成品类型不正确");
//        Assert.isTrue(Arrays.asList(PTT0,PTD0,PTW0,PTU0,PNR0,
//                PNY0,PNM0,PNF0,PRT0,PRD0,PRW0,PRU0,PRR0,PRY0,
//                PRM0,PRF0).contains(BarCodeConstants.BAR_CODE_PREFIX.valueOf(type)), "成品类型不正确");
        product.uniqueValidate();
    }

    /**
     * 查询余量
     * @param productId
     * @return safe,total
     */
    public Map<String,Object> stockRemain(Long productId) {
        ErpBaseEndProduct product = one(productId);
        //审核通过的成品数量
        double approval = orderItemRepository.findByProductIdAndStatusCode(productId, STATUS_003.name())
                .stream()
                .filter(Objects::nonNull)
                .mapToDouble(ErpSaleOrderItem::getNum)
                .sum();
        double total = product.getTotalNum();
        return new HashMap<String, Object>(){{
            put("total", total);
            put("safe", (total - approval));
        }};
    }

    /**
     * 构造前端返回的参数
     */
    @Getter @Setter @OrderBy(fieldName = "id")
    public static class ProductParameterBuilder extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "endProductName","drawingNo"}, compare = like, group = OR)
        String cause;
        @ParameterItem(mappingTo = "endProductTypeCode", compare = like)
        String typeC;
    }

}

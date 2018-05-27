package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.utils.bar.BarCodeConstants;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import com.yintong.erp.validator.OnDeleteProductValidator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.*;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.equal;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.like;
import static javax.persistence.criteria.Predicate.BooleanOperator.OR;

/**
 * Created by jianqiang on 2018/5/26 0026.
 * 成品
 */
@Service
public class ProductService {

    @Autowired
    private ErpBaseEndProductRepository erpBaseEndProductRepository;

    @Autowired(required = false)
    private List<OnDeleteProductValidator> onDeleteProductalidator;

    /**
     *查询列表
     * @param parameter
     * @return
     */
    public Page<ErpBaseEndProduct> query(ProductService.ProductParameterBuilder parameter){
        return erpBaseEndProductRepository.findAll(parameter.specification(), parameter.pageable());
    }
    /**
     * 根据模具id查找供应商
     * @param productId
     * @return
     */
    public ErpBaseEndProduct one(Long productId){
        ErpBaseEndProduct mould = erpBaseEndProductRepository.findById(productId).orElse(null);
        Assert.notNull(mould, "未找到成品");
        return mould;
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
        return erpBaseEndProductRepository.save(product);
    }
    /**
     * 更新成品
     * @param product
     * @return
     */
    public ErpBaseEndProduct update(ErpBaseEndProduct product){
        Assert.notNull(product.getId(), "模具id不能为空");
        ErpBaseEndProduct inDb = erpBaseEndProductRepository.findById(product.getId()).orElse(null);
        Assert.notNull(inDb, "未找到模具");
        validateProductType(product);
        product.setBarCode(inDb.getBarCode());
        return erpBaseEndProductRepository.save(product);
    }
    /**
     * 删除成品
     * @param productId
     */
    @Transactional
    public void delete(Long productId){
        if(!CollectionUtils.isEmpty(onDeleteProductalidator))
            onDeleteProductalidator.forEach(validator -> validator.validate(productId));
        erpBaseEndProductRepository.deleteById(productId);

    }

    /**
     * 验证成品类型
     * @param product
     */
    private void validateProductType(ErpBaseEndProduct product){
        Assert.notNull(product, "成品null");
        String type = product.getEndProductTypeCode();
        Assert.hasLength(type, "类型不能为空");
        Assert.isTrue(Arrays.asList(PTT0,PTD0,PTW0,PTU0,PNR0,
                PNY0,PNM0,PNF0,PRT0,PRD0,PRW0,PRU0,PRR0,PRY0,
                PRM0,PRF0).contains(BarCodeConstants.BAR_CODE_PREFIX.valueOf(type)), "成品类型不正确");
    }

    /**
     * 构造前端返回的参数
     */
    @Getter
    @Setter
    @OrderBy(fieldName = "id")
    public static class ProductParameterBuilder extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "endProductName","drawingNo"}, compare = like, group = OR)
        String cause;
        @ParameterItem(mappingTo = "endProductTypeCode", compare = equal)
        String typeC;
    }

}

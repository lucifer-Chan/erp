package com.yintong.erp.service.basis.associator;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.domain.basis.ErpBaseRawMaterial;
import com.yintong.erp.domain.basis.ErpBaseRawMaterialRepository;
import com.yintong.erp.domain.basis.associator.ErpBaseProductBom;
import com.yintong.erp.domain.basis.associator.ErpBaseProductBomRepository;
import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.validator.OnDeleteProductValidator;
import com.yintong.erp.validator.OnDeleteRawMaterialValidator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author lucifer.chan
 * @create 2018-08-14 下午3:28
 * 成品和物料清单
 **/
@Service
public class ProductBomService implements OnDeleteProductValidator, OnDeleteRawMaterialValidator {

    @Autowired ErpBaseProductBomRepository productBomRepository;

    @Autowired ErpBaseRawMaterialRepository materialRepository;

    @Autowired ErpBaseEndProductRepository productRepository;



    @Override
    public void onDeleteProduct(Long productId) {
        Assert.isTrue(
                CollectionUtils.isEmpty(productBomRepository.findByProductId(productId)),
                "请先删除成品和物料清单之间的关联。"
        );
    }

    @Override
    public void onDeleteMaterial(Long rawMaterialId) {

        List<String> productNameList = productBomRepository.findByMaterialId(rawMaterialId)
                .stream()
                .map(ass -> productRepository.findById(ass.getProductId()).orElse(null))
                .filter(Objects::nonNull)
                .map(ErpBaseEndProduct::getEndProductName)
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(productNameList)) return;
        String productNames = StringUtils.collectionToCommaDelimitedString(productNameList);
        Assert.isTrue(StringUtils.isEmpty(productNames),
                "请先删除该原材料在成品[" + productNames +"]中物料清单信息。");
    }

    /**
     * 新增
     * @param bom
     * @return
     */
    public ErpBaseProductBom create(ErpBaseProductBom bom){
        bom.setId(null);
        return productBomRepository.save(bom);
    }

    /**
     * 修改 - 只修改数量
     * @param id
     * @param materialNum
     * @return
     */
    public ErpBaseProductBom update(Long id, String materialNum){
        ErpBaseProductBom bom = one(id);
        bom.setMaterialNum(CommonUtil.parseDouble(materialNum));
        return productBomRepository.save(bom);
    }

    /**
     * 删除
     * @param id
     */
    public void delete(Long id) {
       productBomRepository.deleteById(id);
    }

    /**
     * 获取单个
     * @param id
     * @return
     */
    public ErpBaseProductBom one(Long id){
        ErpBaseProductBom bom = productBomRepository.findById(id).orElse(null);
        Assert.notNull(bom, "未找到id为[" + id + "]的物料清单");
        return bom;
    }

    /**
     * 根据成品查找
     * @param productId
     * @return
     */
    public List<ErpBaseProductBom> findBomList(Long productId){
        return productBomRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    /**
     * 下拉列表
     * @param productId
     * @return
     */
    public List<ErpBaseRawMaterial> lookup(Long productId){
        //1-已在清单里的原材料
        List<Long> materialIds = productBomRepository.findByProductId(productId).stream().map(ErpBaseProductBom::getMaterialId).collect(Collectors.toList());

        return CollectionUtils.isEmpty(materialIds) ? materialRepository.findAll() : materialRepository.findByIdNotIn(materialIds);
    }
}

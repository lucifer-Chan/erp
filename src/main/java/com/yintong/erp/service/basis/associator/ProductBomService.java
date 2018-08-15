package com.yintong.erp.service.basis.associator;

import com.yintong.erp.domain.basis.ErpBaseRawMaterial;
import com.yintong.erp.domain.basis.ErpBaseRawMaterialRepository;
import com.yintong.erp.domain.basis.associator.ErpBaseProductBom;
import com.yintong.erp.domain.basis.associator.ErpBaseProductBomRepository;
import com.yintong.erp.validator.OnDeleteProductValidator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * @author lucifer.chan
 * @create 2018-08-14 下午3:28
 * 成品和物料清单
 **/
@Service
public class ProductBomService implements OnDeleteProductValidator {

    @Autowired ErpBaseProductBomRepository productBomRepository;

    @Autowired ErpBaseRawMaterialRepository materialRepository;



    @Override
    public void onDeleteProduct(Long productId) {
        Assert.isTrue(
                CollectionUtils.isEmpty(productBomRepository.findByProductId(productId)),
                "请先删除成品和物料清单之间的关联。"
        );
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
    public ErpBaseProductBom update(Long id, double materialNum){
        ErpBaseProductBom bom = one(id);
        bom.setMaterialNum(materialNum);
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

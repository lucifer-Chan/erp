package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseRawMaterial;
import com.yintong.erp.domain.basis.ErpBaseRawMaterialRepository;
import com.yintong.erp.utils.bar.BarCodeConstants;
import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.utils.excel.ExcelUtil;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import com.yintong.erp.validator.OnDeleteRawMaterialValidator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.*;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.like;
import static javax.persistence.criteria.Predicate.BooleanOperator.OR;

/**
 * Created by Zangtao on 2018/5/26.
 * 原材料
 */
@Service
public class RawMaterialService {

    @Autowired
    private ErpBaseRawMaterialRepository erpBaseRawMaterialRepository;

    @Autowired(required = false)
    private List<OnDeleteRawMaterialValidator> onDeleteRawMaterialValidator;

    /**
     *查询列表
     * @param parameter
     * @return
     */
    public Page<ErpBaseRawMaterial> query(RawMaterialService.RawMaterialParameterBuilder parameter){
        return erpBaseRawMaterialRepository.findAll(parameter.specification(), parameter.pageable());
    }
    /**
     * 根据模具id查找原材料
     * @param materialId
     * @return
     */
    public ErpBaseRawMaterial one(Long materialId){
        ErpBaseRawMaterial material = erpBaseRawMaterialRepository.findById(materialId).orElse(null);
        Assert.notNull(material, "未找到原材料");
        return material;
    }



    /**
     * 创建原材料
     * @param material
     * @return
     */
    @Transactional
    public ErpBaseRawMaterial create(ErpBaseRawMaterial material){
        material.setId(null);//防止假数据
        validateMaterialType(material);
        return erpBaseRawMaterialRepository.save(material);
    }
    /**
     * 更新原材料
     * @param material
     * @return
     */
    public ErpBaseRawMaterial update(ErpBaseRawMaterial material){
        Assert.notNull(material.getId(), "原材料id不能为空");
        ErpBaseRawMaterial inDb = erpBaseRawMaterialRepository.findById(material.getId()).orElse(null);
        Assert.notNull(inDb, "未找到原材料");
        validateMaterialType(material);
        material.setBarCode(inDb.getBarCode());
        return erpBaseRawMaterialRepository.save(material);
    }
    /**
     * 删除原材料
     * @param materialId
     */
    @Transactional
    public void delete(Long materialId){
        if(!CollectionUtils.isEmpty(onDeleteRawMaterialValidator))
            onDeleteRawMaterialValidator.forEach(validator -> validator.onDeleteMaterial(materialId));
        erpBaseRawMaterialRepository.deleteById(materialId);
    }

    /**
     * 验证原材料类型
     * @param material
     */
    private void validateMaterialType(ErpBaseRawMaterial material){
        Assert.notNull(material, "原材料null");
        String type = material.getRawTypeCode();
        Assert.hasLength(type, "类型不能为空");
        Assert.hasLength(material.getRawName(), "原材料名称不能为空");
        Assert.isTrue(Arrays.asList(MA00,MZR0,MZY0,MZB0,MZN0,
                MZQ0,MM00,MF00,MRA0,
                MRZR,MRZY,MRZB,MRZN,MRZQ,
                MRZ0).contains(BarCodeConstants.BAR_CODE_PREFIX.valueOf(type)), "原材料类型不正确");
        material.validate();
    }

    /**
     * 导入
     * @param excel
     * @return
     */
    public ExcelUtil.ExcelImporter<ErpBaseRawMaterial> import0(InputStream excel) throws IOException {
        ExcelUtil.ExcelImporter<ErpBaseRawMaterial> importer = new ExcelUtil(excel).builder(ErpBaseRawMaterial.class);
        List<ErpBaseRawMaterial> entities = importer.getSuccessData();
        Date importedAt = new Date();
        entities.forEach(entity-> entity.setImportedAt(DateUtil.getDateTimeString(importedAt)));
        erpBaseRawMaterialRepository.saveAll(entities);
        return importer;
    }


    @Getter
    @Setter
    @OrderBy(fieldName = "id")
    public static class RawMaterialParameterBuilder extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "rawName"}, compare = like, group = OR)
        String cause;
        @ParameterItem(mappingTo = "rawTypeCode", compare = like)
        String typeC;
    }
}

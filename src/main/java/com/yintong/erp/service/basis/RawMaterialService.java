package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseRawMaterial;
import com.yintong.erp.domain.basis.ErpBaseRawMaterialRepository;
import com.yintong.erp.domain.basis.ErpBaseSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplier;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplierRepository;
import com.yintong.erp.utils.bar.BarCodeConstants;
import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.utils.excel.ExcelUtil;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import com.yintong.erp.validator.OnDeleteRawMaterialValidator;
import java.util.stream.Collectors;
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
import java.util.ArrayList;
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
@Slf4j
@Service
public class RawMaterialService {

    @Autowired ErpBaseRawMaterialRepository erpBaseRawMaterialRepository;

    @Autowired ErpRawMaterialSupplierRepository rawMaterialSupplierRepository;

    @Autowired ErpBaseSupplierRepository supplierRepository;

    @Autowired(required = false) List<OnDeleteRawMaterialValidator> onDeleteRawMaterialValidator;

    /**
     *查询列表
     * @param parameter
     * @return
     */
    public Page<ErpBaseRawMaterial> query(RawMaterialService.RawMaterialParameterBuilder parameter){
        return erpBaseRawMaterialRepository.findAll(parameter.specification(), parameter.pageable());
    }
    /**
     * 根据原材料id查找原材料
     * @param materialId
     * @return
     */
    public ErpBaseRawMaterial one(Long materialId){
        ErpBaseRawMaterial material = erpBaseRawMaterialRepository.findById(materialId).orElse(null);
        Assert.notNull(material, "未找到原材料");
        return material;
    }

    /**
     * 根据原材料id查找供应商关联
     * @param materialId
     * @return
     */
    public List<ErpRawMaterialSupplier> findSuppilersAss(Long materialId){
        return rawMaterialSupplierRepository.findByRawMaterId(materialId);
    }

    /**
     * 根据原材料id查找未关联的供应商
     * @param materialId
     * @return
     */
    public Iterable findUnassociatedSuppliers(Long materialId) {
        List<Long> associatedSupplierIds = rawMaterialSupplierRepository.findByRawMaterId(materialId).stream().map(ErpRawMaterialSupplier::getSupplierId).collect(Collectors.toList());
        return CollectionUtils.isEmpty(associatedSupplierIds) ?
                supplierRepository.findAll() : supplierRepository.findByIdNotIn(associatedSupplierIds);
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
        Assert.notNull(material, "原材料不能为null");
        String type = material.getRawTypeCode();
        Assert.hasLength(type, "类型不能为空");
        Assert.hasLength(material.getRawName(), "原材料名称不能为空");
        Assert.isTrue(
                Arrays.asList(MA00,MZR0,MZY0,MZB0,MZN0,MZQ0,MM00,MF00)
                        .contains(BarCodeConstants.BAR_CODE_PREFIX.valueOf(type))
                , "原材料类型不正确"
        );
//        Assert.isTrue(Arrays.asList(MA00,MZR0,MZY0,MZB0,MZN0,
//                MZQ0,MM00,MF00,MRA0,
//                MRZR,MRZY,MRZB,MRZN,MRZQ,
//                MRZ0).contains(BarCodeConstants.BAR_CODE_PREFIX.valueOf(type)), "原材料类型不正确");
        material.uniqueValidate();
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
        List<ErpBaseRawMaterial> count = new ArrayList<>();
        for(ErpBaseRawMaterial entity : entities){
            try{
                entity.setImportedAt(DateUtil.getDateTimeString(importedAt));
                count.add(create(entity));
            } catch (Exception e){
                log.error("导入原材料失败", e);
            }
        }
        return importer.setSuccessData(count);
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

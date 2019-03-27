package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseModelTool;
import com.yintong.erp.domain.basis.ErpBaseModelToolRepository;
import com.yintong.erp.utils.bar.BarCodeConstants;
import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.utils.excel.ExcelUtil;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import com.yintong.erp.validator.OnDeleteMouldValidator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.D100;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.D200;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.D300;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.D400;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.D500;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.D600;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.D700;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.D800;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.like;
import static javax.persistence.criteria.Predicate.BooleanOperator.OR;
import static com.yintong.erp.utils.excel.ExcelUtil.ExcelImporter;

/**
 * Created by jianqiang on 2018/5/22 0022.
 * 模具
 */
@Service
@Slf4j
public class MouldService {

    @Autowired ErpBaseModelToolRepository modelToolRepository;

    @Autowired(required = false)
    private List<OnDeleteMouldValidator> onDeleteMouldValidator;

    /**
     *查询列表
     * @param parameter
     * @return
     */
    public Page<ErpBaseModelTool> query(MouldParameterBuilder parameter){
        return modelToolRepository.findAll(parameter.specification(), parameter.pageable());
    }
    /**
     * 根据模具id查找供应商
     * @param mouldId
     * @return
     */
    public ErpBaseModelTool one(Long mouldId){
        ErpBaseModelTool mould = modelToolRepository.findById(mouldId).orElse(null);
        Assert.notNull(mould, "未找到供应商");
        return mould;
    }

    /**
     * 根据barcode查找模具
     * @param barcode
     * @return
     */
    public ErpBaseModelTool findByBarcode(String barcode){
        ErpBaseModelTool mould = modelToolRepository.findByBarCode(barcode).orElse(null);
        Assert.notNull(mould, "未找到模具");
        return mould;
    }


    /**
     * 创建模具
     * @param mould
     * @return
     */
    @Transactional
    public ErpBaseModelTool create(ErpBaseModelTool mould){
        mould.setId(null);//防止假数据
        validateModelType(mould);
        mould.uniqueValidate();
        return modelToolRepository.save(mould);
    }
    /**
     * 更新模具
     * @param mould
     * @return
     */
    public ErpBaseModelTool update(ErpBaseModelTool mould){
        Assert.notNull(mould.getId(), "模具id不能为空");
        ErpBaseModelTool inDb = modelToolRepository.findById(mould.getId()).orElse(null);
        Assert.notNull(inDb, "未找到模具");
        validateModelType(mould);
        mould.setBarCode(inDb.getBarCode());
        mould.uniqueValidate();
        return modelToolRepository.save(mould);
    }
    /**
     * 删除供应商
     * @param mouldId
     */
    @Transactional
    public void delete(Long mouldId){
        if(!CollectionUtils.isEmpty(onDeleteMouldValidator))
            onDeleteMouldValidator.forEach(validator -> validator.onDeleteMould(mouldId));
        modelToolRepository.deleteById(mouldId);
    }

    /**
     * 验证供应商类型
     * @param mould
     */
    private void validateModelType(ErpBaseModelTool mould){
        Assert.notNull(mould, "模具不能为null");
//        String type = mould.getModelToolTypeCode();
//        Assert.hasLength(type, "类型不能为空");
        Assert.hasText(mould.getModelPlace(), "模具位不能为空");
//        Assert.isTrue(Arrays.asList(D100,D200,D300,D400,D500,D600,D700,D800).contains(BarCodeConstants.BAR_CODE_PREFIX.valueOf(type)), "模具类型不正确");
    }

    /**
     * 构造前端返回的参数
     */
    @Getter
    @Setter
    @OrderBy(fieldName = "id")
    public static class MouldParameterBuilder extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"modelPlace", "modelToolNo", "specification", "angle"}, compare = like, group = OR)
        String cause;
    }

    /**
     * 导入
     * @param excel
     * @return
     */
    public ExcelImporter<ErpBaseModelTool> import0(InputStream excel) throws IOException {
        ExcelImporter<ErpBaseModelTool> importer = new ExcelUtil(excel).builder(ErpBaseModelTool.class);
        List<ErpBaseModelTool> entities = importer.getSuccessData();
        Date importedAt = new Date();
        List<ErpBaseModelTool> count = new ArrayList<>();
        for (ErpBaseModelTool entity : entities){
            try{
                entity.setImportedAt(DateUtil.getDateTimeString(importedAt));
                count.add(create(entity));
            } catch (Exception e){
                log.error("导入模具失败", e);
            }
        }
        return importer.setSuccessData(count);
    }

}

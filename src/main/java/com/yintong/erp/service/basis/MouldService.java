package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseModelTool;
import com.yintong.erp.domain.basis.ErpBaseModelToolRepository;
import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.domain.basis.ErpBaseSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpModelSupplier;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplier;
import com.yintong.erp.utils.bar.BarCodeConstants;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import lombok.Getter;
import lombok.Setter;
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
 * Created by jianqiang on 2018/5/22 0022.
 * 模具
 */
@Service
public class MouldService {

    @Autowired
    private ErpBaseModelToolRepository modelToolRepositor;

    @Autowired
    private ErpBaseSupplierRepository supplierRepository;

    /**
     *查询列表
     * @param parameter
     * @return
     */
    public Page<ErpBaseModelTool> query(MouldParameterBuilder parameter){
        return modelToolRepositor.findAll(parameter.specification(), parameter.pageable());
    }

    /**
     * 查询供应商
     * @return
     */
    public List<ErpBaseSupplier> FindSupplierAll(){
        return supplierRepository.findAll();
    }

    /**
     * 创建模具
     * @param mould
     * @return
     */
    @Transactional
    public ErpBaseModelTool create(ErpBaseModelTool mould){
        mould.setId(null);//防止假数据
        validateSupplierType(mould);
        return modelToolRepositor.save(mould);
    }

    /**
     * 验证供应商类型
     * @param mould
     */
    private void validateSupplierType(ErpBaseModelTool mould){
        Assert.notNull(mould, "模具null");
        String type = mould.getModelToolTypeCode();
        Assert.hasLength(type, "类型不能为空");
        Assert.isTrue(Arrays.asList(D100,D200,D300,D400,D500,D600,D700,D800).contains(BarCodeConstants.BAR_CODE_PREFIX.valueOf(type)), "模具类型不正确");
    }

    /**
     * 构造前端返回的参数
     */
    @Getter
    @Setter
    @OrderBy(fieldName = "id")
    public static class MouldParameterBuilder extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "modelToolName"}, compare = like, group = OR)
        String cause;
        @ParameterItem(mappingTo = "modelToolTypeCode", compare = equal)
        String type;
    }
}

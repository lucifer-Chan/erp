package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.domain.basis.ErpBaseSupplierRepository;
import com.yintong.erp.utils.bar.BarCodeConstants.*;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import com.yintong.erp.validator.OnDeleteSupplierValidator;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.List;

import static com.yintong.erp.utils.query.ParameterItem.COMPARES.*;
import static javax.persistence.criteria.Predicate.BooleanOperator.*;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.*;
/**
 * @author lucifer.chan
 * @create 2018-05-22 上午12:04
 * 供应商
 **/
@Service
public class SupplierService {

    @Autowired ErpBaseSupplierRepository supplierRepository;

    @Autowired(required = false) List<OnDeleteSupplierValidator> onDeleteSuppliers;

    /**
     * 动态查询
     * @param parameter
     * @return
     */
    public Page<ErpBaseSupplier> query(SupplierParameterBuilder parameter){
        return supplierRepository.findAll(parameter.specification(), parameter.pageable());
    }

    /**
     * 创建供应商
     * @param supplier
     * @return
     */
    public ErpBaseSupplier create(ErpBaseSupplier supplier){
        supplier.setId(null);//防止假数据
        validateSupplierType(supplier);
        return supplierRepository.save(supplier);
    }

    /**
     * 更新供应商
     * @param supplier
     * @return
     */
    public ErpBaseSupplier update(ErpBaseSupplier supplier){
        validateSupplierType(supplier);
        return supplierRepository.save(supplier);
    }

    /**
     * 删除供应商
     * @param supplierId
     */
    @Transactional
    public void delete(Long supplierId){
        if(!CollectionUtils.isEmpty(onDeleteSuppliers))
            onDeleteSuppliers.forEach(validator -> validator.validate(supplierId));
        supplierRepository.deleteById(supplierId);
        //TODO 因为涉及到具体的删除验证（产品、原材料等）需要调用其他service的delete方法去删除关联
    }

    /**
     * 验证供应商类型
     * @param supplier
     */
    private void validateSupplierType(ErpBaseSupplier supplier){
        Assert.notNull(supplier, "供应商不能为null");
        String type = supplier.getSupplierTypeCode();
        Assert.hasLength(type, "类型不能为空");
        Assert.isTrue(Arrays.asList(USC0, USS0, USE0).contains(BAR_CODE_PREFIX.valueOf(type)), "供应商类型不正确");
    }


    @Getter @Setter
    @OrderBy(fieldName = "id")
    public static class SupplierParameterBuilder extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "supplierName", "contactName", "contactMobile", "contactPhone"}, compare = like, group = OR)
        String cause;
        @ParameterItem(mappingTo = "supplierTypeCode", compare = equal)
        String type;
        @ParameterItem(mappingTo = "rank", compare = equal)
        String rank;
    }
}

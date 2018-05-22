package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.domain.basis.ErpBaseSupplierRepository;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import static com.yintong.erp.utils.query.ParameterItem.COMPARES.*;
import static javax.persistence.criteria.Predicate.BooleanOperator.*;

/**
 * @author lucifer.chan
 * @create 2018-05-22 上午12:04
 * 供应商
 **/
@Service
public class SupplierService {

    @Autowired ErpBaseSupplierRepository supplierRepository;

    /**
     * 动态查询
     * @param parameter
     * @return
     */
    public Page<ErpBaseSupplier> query(SupplierParameterBuilder parameter){
        return supplierRepository.findAll(parameter.specification(), parameter.pageable());
    }




    @Getter @Setter
    @OrderBy(fieldName = "id")
    public static class SupplierParameterBuilder extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "supplierName", "contactName", "contactMobile", "contactPhone"}, compare = like, group = OR)
        String cause;
        @ParameterItem(mappingTo = "supplierTypeCode", compare = equal)
        String type;
    }
}

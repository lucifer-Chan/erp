package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.domain.basis.ErpBaseSupplierRepository;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;

import java.util.List;

import static com.yintong.erp.utils.query.ParameterItem.COMPARES.*;

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
        PageRequest pageRequest = PageRequest.of(parameter.getPageNum(), parameter.getPerPageNum());
        if(StringUtils.isEmpty(parameter.cause) && StringUtils.isEmpty(parameter.type))
            return supplierRepository.findAll(pageRequest);
        return supplierRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
                    List<Predicate> predicates = parameter.build(root, criteriaBuilder);
                    criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdAt")));
                    Predicate typePredicate = criteriaBuilder.equal(root.get("supplierTypeCode"), parameter.type);
                    return StringUtils.isEmpty(parameter.type) ?
                            criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()])) :
                            CollectionUtils.isEmpty(predicates) ?
                                    criteriaBuilder.and(typePredicate) :
                                    criteriaBuilder.and(typePredicate, criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()])));
                }, pageRequest);
    }




    @Getter
    @Setter
    public static class SupplierParameterBuilder extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "supplierName", "contactName", "contactMobile", "contactPhone"}, compare = like)
        String cause;
//        @ParameterItem(mappingTo = "supplierTypeCode", compare = equal)
        String type;
    }
}

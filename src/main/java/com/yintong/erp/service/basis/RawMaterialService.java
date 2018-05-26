package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseRawMaterial;
import com.yintong.erp.domain.basis.ErpBaseRawMaterialRepository;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.yintong.erp.utils.query.ParameterItem.COMPARES.equal;

/**
 * Created by Zangtao on 2018/5/26.
 * 原材料
 */
@Service
public class RawMaterialService {

    @Autowired
    private ErpBaseRawMaterialRepository erpBaseRawMaterialRepository;

    public Page<ErpBaseRawMaterial> list(RawMaterialParameterBuilder parameterBuilder) {
        return erpBaseRawMaterialRepository.findAll(parameterBuilder.specification(),parameterBuilder.pageable());
    }

    @Transactional
    public void save(ErpBaseRawMaterial erpBaseRawMaterial) {
        erpBaseRawMaterialRepository.save(erpBaseRawMaterial);
    }

    public void remove(Long id) {
        erpBaseRawMaterialRepository.deleteById(id);
    }

    @Getter
    @Setter
    @OrderBy(fieldName = "id")
    public static class RawMaterialParameterBuilder extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"rawMaterType"}, compare = equal)
        String rawMaterType;
        @ParameterItem(mappingTo = "supplierType", compare = equal)
        String supplierType;
    }
}

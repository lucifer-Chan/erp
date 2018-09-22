package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.domain.basis.ErpBaseModelToolRepository;
import com.yintong.erp.domain.basis.ErpBaseRawMaterialRepository;
import com.yintong.erp.domain.basis.TemplateWares;
import com.yintong.erp.domain.basis.associator.ErpEndProductSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpModelSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplierRepository;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.yintong.erp.utils.common.Constants.*;

/**
 * @author lucifer.chan
 * @create 2018-09-20 下午4:22
 * 公用
 **/
@Service
public class CommonService {
    @Autowired ErpEndProductSupplierRepository productSupplierRepository;

    @Autowired ErpRawMaterialSupplierRepository materialSupplierRepository;

    @Autowired ErpModelSupplierRepository mouldSupplierRepository;

    @Autowired ErpBaseEndProductRepository productRepository;

    @Autowired ErpBaseRawMaterialRepository materialRepository;

    @Autowired ErpBaseModelToolRepository mouldRepository;

    /**
     * 根据货物id查找模版货物
     * @return
     */
    public Map<WaresType, Function<Long, TemplateWares>> findWaresById(){
        return new HashMap<WaresType, Function<Long, TemplateWares>>(){{
            put(WaresType.P, id -> productRepository.findById(id).orElse(null));
            put(WaresType.M, id -> materialRepository.findById(id).orElse(null));
            put(WaresType.D, id -> mouldRepository.findById(id).orElse(null));
        }};
    }

    /**
     * 通过关联id查找已关联供应商的货物编码
     * @param waresType
     * @param assId
     * @return
     */
    public String findRealityWaresBarCode(String waresType, Long assId){
        if(null == assId) return "";
        BaseEntityWithBarCode entity = null;
        if(WaresType.P.name().equals(waresType)){
            entity = productSupplierRepository.findById(assId).orElse(null);
        } else if(WaresType.D.name().equals(waresType)){
            entity = mouldSupplierRepository.findById(assId).orElse(null);
        } else if(WaresType.M.name().equals(waresType)){
            entity = materialSupplierRepository.findById(assId).orElse(null);
        }

        return Objects.isNull(entity) ? "" : entity.getBarCode();
    }
}

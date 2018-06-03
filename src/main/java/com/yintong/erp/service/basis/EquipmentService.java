package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseEquipment;
import com.yintong.erp.domain.basis.ErpBaseEquipmentRepository;
import com.yintong.erp.utils.bar.BarCodeConstants;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import com.yintong.erp.validator.OnDeleteEquipmentValidator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
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
 * Created by jianqiang on 2018/6/2.
 */
@Service
public class EquipmentService {

    @Autowired
    private ErpBaseEquipmentRepository erpBaseEquipmentRepository;

    @Autowired(required = false)
    private List<OnDeleteEquipmentValidator> onDeleteEquipmentValidator;

    /**
     *查询列表
     * @param parameter
     * @return
     */
    public Page<ErpBaseEquipment> query(EquipmentService.EquipmentParameterBuilder parameter){
        return erpBaseEquipmentRepository.findAll(parameter.specification(), parameter.pageable());
    }
    /**
     * 根据模具id查找设备
     * @param equipmentId
     * @return
     */
    public ErpBaseEquipment one(Long equipmentId){
        ErpBaseEquipment equipment  = erpBaseEquipmentRepository.findById(equipmentId).orElse(null);
        Assert.notNull(equipment, "未找到设备");
        return equipment;
    }



    /**
     * 创建设备
     * @param equipment
     * @return
     */
    @Transactional
    public ErpBaseEquipment create(ErpBaseEquipment equipment){
        equipment.setId(null);//防止假数据
        validateEquipmentType(equipment);
        return erpBaseEquipmentRepository.save(equipment);
    }
    /**
     * 更新设备
     * @param equipment
     * @return
     */
    public ErpBaseEquipment update(ErpBaseEquipment equipment){
        Assert.notNull(equipment.getId(), "设备id不能为空");
        ErpBaseEquipment inDb = erpBaseEquipmentRepository.findById(equipment.getId()).orElse(null);
        Assert.notNull(inDb, "未找到设备");
        validateEquipmentType(equipment);
        equipment.setBarCode(inDb.getBarCode());
        return erpBaseEquipmentRepository.save(equipment);
    }
    /**
     * 删除设备
     * @param equipmentId
     */
    @Transactional
    public void delete(Long equipmentId){
        if(!CollectionUtils.isEmpty(onDeleteEquipmentValidator))
            onDeleteEquipmentValidator.forEach(validator -> validator.validate(equipmentId));
        erpBaseEquipmentRepository.deleteById(equipmentId);

    }

    /**
     * 验证设备类型
     * @param equipment
     */
    private void validateEquipmentType(ErpBaseEquipment equipment){
        Assert.notNull(equipment, "设备null");
        String type = equipment.getEquipmentTypeCode();
        Assert.hasLength(type, "类型不能为空");
        Assert.hasLength(equipment.getEquipmentName(), "设备名称不能为空");
        Assert.isTrue(Arrays.asList(EJ00,EL00,EH00,EQ00).contains(BarCodeConstants.BAR_CODE_PREFIX.valueOf(type)), "设备类型不正确");
    }

    /**
     * 构造前端返回的参数
     */
    @Getter
    @Setter
    @OrderBy(fieldName = "id")
    public static class EquipmentParameterBuilder extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "equipmentName"}, compare = like, group = OR)
        String cause;
        @ParameterItem(mappingTo = "equipmentTypeCode", compare = equal)
        String type;
    }
}

package com.yintong.erp.validator;

/**
 * Created by jianqiang on 2018/6/2.
 * 删除设备时的验证
 */
public interface OnDeleteEquipmentValidator {

    /**
     * 如果模具有关联操作
     * @param equipmentId
     */
    void validate(Long equipmentId);
}

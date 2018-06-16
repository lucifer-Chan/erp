package com.yintong.erp.validator;

/**
 * Created by jianqiang on 2018/6/16.
 * 删除供应商原材料关联时的验证
 */
public interface OnDeleteSupplierRawMeterialValidator {

    void onDeleteSupplierRawMeterial(Long supplierId, Long rawMaterId);
}

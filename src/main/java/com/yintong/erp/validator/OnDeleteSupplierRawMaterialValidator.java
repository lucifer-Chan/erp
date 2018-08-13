package com.yintong.erp.validator;

import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplier;

/**
 * Created by jianqiang on 2018/6/16.
 * 删除供应商原材料关联时的验证
 */
public interface OnDeleteSupplierRawMaterialValidator {

    void onDeleteSupplierRawMaterial(ErpRawMaterialSupplier materialSupplier);
}

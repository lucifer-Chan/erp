package com.yintong.erp.validator;

/**
 * @author lucifer.chan
 * @create 2018-06-10 上午00:46
 * 删除供应商成品关联时的验证
 **/
public interface OnDeleteSupplierProductValidator {

    void onDeleteSupplierProduct(Long id);
}

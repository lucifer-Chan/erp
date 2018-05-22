package com.yintong.erp.validator;

/**
 * @author lucifer.chan
 * @create 2018-05-22 下午8:24
 * 删除供应商时的验证
 **/
public interface OnDeleteSupplierValidator {
    /**
     * 1-未完成的采购单如果有关联，抛异常：采购单XXXX未完成，且存在该产品，暂时不能删除
     * @param supplierId
     */
    void validate(Long supplierId);
}

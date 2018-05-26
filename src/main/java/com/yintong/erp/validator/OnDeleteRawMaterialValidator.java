package com.yintong.erp.validator;

/**
 * Created by Zangtao on 2018/5/26.
 * 删除原材料时的验证
 */
public interface OnDeleteRawMaterialValidator {
    /**
     * 1-未完成的采购单如果有关联，抛异常：采购单XXXX未完成，且存在该产品，暂时不能删除
     * @param rawMaterialId
     */
    void validate(Long rawMaterialId);
}

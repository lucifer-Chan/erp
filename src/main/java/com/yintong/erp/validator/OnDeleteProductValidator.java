package com.yintong.erp.validator;

/**
 * @author lucifer.chan
 * @create 2018-05-22 下午8:16
 * 删除产品时的验证，供具体的service继承
 **/
public interface OnDeleteProductValidator {
    /**
     * 1-供应商如果有关联，抛异常：先解除关联
     * 2-未完成的销售单如果有关联，抛异常：销售单XXXX未完成，且存在该产品，暂时不能删除。
     * etc
     * @param productId
     */
    void onDeleteProduct(Long productId);
}

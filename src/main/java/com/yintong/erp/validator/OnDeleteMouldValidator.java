package com.yintong.erp.validator;

/**
 * Created by jianqiang on 2018/5/25 0025.
 * 删除模具时的验证
 */
public interface OnDeleteMouldValidator {
    /**
     * 如果模具有关联操作
     * @param mouldId
     */
    void onDeleteMould(Long mouldId);
}

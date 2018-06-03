package com.yintong.erp.validator;

/**
 * @author lucifer.chan
 * @create 2018-05-22 下午8:16
 * 删除员工时的验证，供具体的N个service继承
 **/
public interface OnDeleteEmployeeValidator {
    /**
     * 1-销售单、采购单、制令单、如果有关联，抛异常：先解除关联
     * etc
     * @param employeeId
     */
    void onDeleteEmployee(Long employeeId);
}

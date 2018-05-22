/**
 * 在一些删除的场景需要做的验证
 *  eg：供应商的删除，需要看关联的成品、原材料等信息在系统中是否有在役，有的话抛异常
 *  example :
 *      -- public void validate(Long id){ Assert.isTrue(expression, message);}
 */
package com.yintong.erp.validator;
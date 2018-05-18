package com.yintong.erp.web;

import com.yintong.erp.service.basis.EmployeeService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lucifer.chan
 * @create 2018-05-06 上午1:17
 * 任何登陆用户可访问的接口
 **/
@RestController
@RequestMapping("profile")
public class ProfileController {
    @Autowired EmployeeService employeeService;

    /**
     * 更新自身电话号码
     * @return
     */
    @PatchMapping("mobile")
    public BaseResult updateMobile(String mobile){
        return new BaseResult().addPojo(employeeService.updateMobile(mobile));
    }

    /**
     * 更新自身密码
     * @param old
     * @param newed
     * @return
     */
    @PatchMapping("password")
    public BaseResult updatePassword(String old, String newed){
        return new BaseResult().addPojo(employeeService.updatePassword(old, newed));
    }
}

package com.yintong.erp.web;

import com.yintong.erp.service.MenuService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午11:42
 * 人员管理
 **/
@RestController
@RequestMapping("employee")
public class EmployeeController {
    @Autowired MenuService menuService;

    /**
     * 获取员工的菜单
     * @param employeeId
     * @return
     */
    @GetMapping("{employeeId}/menus")
    public BaseResult getMenusByEmployee(@PathVariable("employeeId") Long employeeId){
        return new BaseResult().addList(menuService.getMenusByEmployeeId(employeeId));
    }


}

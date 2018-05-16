package com.yintong.erp.web;

import com.yintong.erp.service.basis.MenuService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 获取所有菜单
     * @return
     */
    @GetMapping("menus")
    public BaseResult getAllMenus(){
        return new BaseResult().addList("menus", menuService.allMenus());
    }

    /**
     * 获取员工的菜单
     * @param employeeId
     * @return
     */
    @GetMapping("{employeeId}/menus")
    public BaseResult getMenusByEmployee(@PathVariable("employeeId") Long employeeId){
        return new BaseResult().addList("menus", menuService.getMenusByEmployeeId(employeeId));
    }

    /**
     * 修改用户权限
     * @param employeeId
     * @param menus
     * @return
     */
    @PostMapping("{employeeId}/menus")
    public BaseResult saveMenus(@PathVariable("employeeId") Long employeeId, @RequestBody List<String> menus){
        menuService.updateMenusOfEmployee(employeeId, menus);
        return new BaseResult();
    }


}

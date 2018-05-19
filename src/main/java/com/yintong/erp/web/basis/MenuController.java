package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.security.ErpMenuRepository;
import com.yintong.erp.service.basis.MenuService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午11:42
 * 菜单管理
 **/
@RestController
@RequestMapping("basis/menus")
public class MenuController {
    @Autowired MenuService menuService;

    @Autowired ErpMenuRepository menuRepository;

    /**
     * 获取所有菜单-tree
     * @return
     */
    @GetMapping("all/tree")
    public BaseResult getAllMenusTree(){
        return new BaseResult().addList("menus", menuService.allMenusTree());
    }

    /**
     * 获取可操作的菜单
     * @return
     */
    @GetMapping("all/operation")
    public BaseResult getAllMenusOfOperation(){
        return new BaseResult().addList(menuRepository.findByParentCodeIsNotNullOrderByCode());
    }

    /**
     * 获取员工的菜单
     * @param employeeId
     * @return
     */
    @GetMapping("employee/{employeeId}")
    public BaseResult getMenusByEmployee(@PathVariable("employeeId") Long employeeId){
        return new BaseResult().addList("menus", menuService.getMenusByEmployeeId(employeeId));
    }

    /**
     * 修改用户菜单关联
     * @param employeeId
     * @param menus
     * @return
     */
    @PostMapping("employee/{employeeId}")
    public BaseResult saveMenus(@PathVariable("employeeId") Long employeeId, @RequestBody List<String> menus){
        menuService.updateMenusOfEmployee(employeeId, menus);
        return new BaseResult().setErrmsg("修改用户菜单成功！");
    }

    /**
     * 获取当前登陆用户的菜单
     * @return
     */
    @GetMapping("current/tree")
    public BaseResult getCurrentMenusTree(){
        return new BaseResult().addList("menus", menuService.getMenusOfCurrentUser());
    }

}

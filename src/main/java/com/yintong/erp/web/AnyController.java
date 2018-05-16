package com.yintong.erp.web;

import com.yintong.erp.service.basis.MenuService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lucifer.chan
 * @create 2018-05-06 上午1:17
 * 任何登陆用户可访问的接口
 **/
@RestController
@RequestMapping("any")
public class AnyController {
    @Autowired MenuService menuService;

    /**
     * 获取登陆用户的菜单
     * @return
     */
    @GetMapping("menus")
    public BaseResult getMyMenus(){
        return new BaseResult().addList("menus", menuService.getMenusOfCurrentUser());
    }
}

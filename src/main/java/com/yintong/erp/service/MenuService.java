package com.yintong.erp.service;

import com.yintong.erp.domain.basis.security.ErpEmployeeMenu;
import com.yintong.erp.domain.basis.security.ErpEmployeeMenuRepository;
import com.yintong.erp.domain.basis.security.ErpMenu;
import com.yintong.erp.domain.basis.security.ErpMenuRepository;
import com.yintong.erp.utils.common.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午11:43
 * 菜单服务
 **/
@Service
public class MenuService {
    @Autowired ErpEmployeeMenuRepository employeeMenuRepository;

    @Autowired ErpMenuRepository menuRepository;

    /**
     * 根据员工Id获取员工的菜单-非树型结构
     * @param employeeId
     * @return
     */
    public List<ErpMenu> getMenusByEmployeeId(Long employeeId){
        List<String> menuCodes = employeeMenuRepository.findByEmployeeId(employeeId).stream()
                .map(ErpEmployeeMenu::getMenuCode)
                .collect(toList());
        return menuRepository.findByCodeInOrderByCode(menuCodes);
    }

    /**
     * 获取当前登陆用户的菜单-tree
     * @return
     */
    public List<ErpMenu> getMenusOfCurrentUser(){
        if(SessionUtil.getEmployeeDetails().isAdmin())
            return allMenus();
        Map<String, List<ErpMenu>> menuMap =
                getMenusByEmployeeId(SessionUtil.getCurrentUserId()).stream()
                    .collect(groupingBy(ErpMenu::getParentCode));
        List<ErpMenu> ret = menuRepository.findByCodeInOrderByCode(menuMap.keySet());
        ret.forEach(menu-> menu.setChildren(menuMap.get(menu.getCode())));
        return ret;
    }

    /**
     * 获取全部菜单-tree
     * @return
     */
    private List<ErpMenu> allMenus(){
        List<ErpMenu> ret = menuRepository.findByParentCodeIsNullOrderByCode();
        ret.forEach(menu-> menu.setChildren(menuRepository.findByParentCodeOrderByCode(menu.getCode())));
        return ret;
    }

}

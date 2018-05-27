package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.security.*;
import com.yintong.erp.utils.common.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.yintong.erp.utils.common.Constants.Roles.ADMIN_ROLE_CODE;
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

    @Autowired ErpEmployeeRepository employeeRepository;

    /**
     * 根据员工Id获取员工的菜单-非树型结构
     * @param employeeId
     * @return
     */
    public List<ErpMenu> getMenusByEmployeeId(Long employeeId){
        if(isAdmin(employeeId))
            return menuRepository.findAll().stream().filter(menu-> StringUtils.hasLength(menu.getParentCode())).collect(toList());
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
            return allMenusTree();
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
    public List<ErpMenu> allMenusTree(){
        List<ErpMenu> ret = menuRepository.findByParentCodeIsNullOrderByCode();
        ret.forEach(menu-> menu.setChildren(menuRepository.findByParentCodeOrderByCode(menu.getCode())));
        return ret;
    }

    /**
     * 更新用户权限
     * @param employeeId
     * @param menuCodes
     */
    @Transactional
    public void updateMenusOfEmployee(Long employeeId, List<String> menuCodes){
        ErpEmployee employee = employeeRepository.findById(employeeId).orElse(null);
        Assert.notNull(employee, "未找到用户!");
        Assert.isTrue(!isAdmin(employeeId), "无权修改管理员的权限!");
        employeeMenuRepository.deleteByEmployeeId(employeeId);
        if(CollectionUtils.isEmpty(menuCodes)) return;
        employeeMenuRepository.saveAll(
                menuCodes.stream()
                        .map(code->ErpEmployeeMenu.builder().employeeId(employeeId).menuCode(code).build())
                        .collect(toList())
        );
    }

    /**
     * 查询员工是否是管理员
     * @param employeeId
     * @return
     */
    public boolean isAdmin(Long employeeId){
        return Objects.nonNull(employeeMenuRepository.findByEmployeeIdAndMenuCode(employeeId, ADMIN_ROLE_CODE));
    }

}

package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseDepartment;
import com.yintong.erp.domain.basis.security.ErpMenu;
import com.yintong.erp.dto.basis.ErpEmployeeDTO;
import com.yintong.erp.service.basis.ErpEmployeeService;
import com.yintong.erp.service.basis.MenuService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/erp/basis/employee")
public class ErpEmployeeController {

    @Autowired
    private ErpEmployeeService erpEmployeeService;

    @Autowired
    private MenuService menuService;


    @RequestMapping("save")
    public BaseResult save(@RequestBody ErpEmployeeDTO erpEmployeeDTO){
        erpEmployeeService.save(erpEmployeeDTO);
        return new BaseResult();
    }

    @RequestMapping("updatePassword")
    public BaseResult updatePassword(Long employeeId,String password){
        erpEmployeeService.updatePassword(employeeId,password);
        return new BaseResult();
    }

    @RequestMapping("getEmployeeInfo")
    public BaseResult getEmployeeInfo(Long employeeId){
        ErpEmployeeDTO erpEmployeeDTO = erpEmployeeService.findById(employeeId);
        return new BaseResult().addPojo(erpEmployeeDTO);
    }

    @RequestMapping("allMenus")
    public BaseResult getMenus(){
        List<ErpMenu> menuList = menuService.allMenusTree();
        return new BaseResult().addList(menuList);
    }

    @RequestMapping("allDepartments")
    public BaseResult getDepartments(){
        List<ErpBaseDepartment> erpBaseDepartments = erpEmployeeService.allDepartments();
        return new BaseResult().addList(erpBaseDepartments);
    }

    @RequestMapping("findAll")
    public BaseResult findAll(){
        List<ErpEmployeeDTO> resultDTOS = erpEmployeeService.findAll();
        return new BaseResult().addList(resultDTOS);
    }

}

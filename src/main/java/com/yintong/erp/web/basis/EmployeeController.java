package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.security.ErpEmployee;
import com.yintong.erp.service.basis.EmployeeService;
import com.yintong.erp.service.basis.MenuService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.yintong.erp.service.basis.EmployeeService.*;

import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

/**
 * @author lucifer.chan
 * @create 2018-05-18 上午12:59
 * 员工管理
 **/
@RestController
@RequestMapping("basis/employee")
public class EmployeeController {

    @Autowired EmployeeService employeeService;

    @Autowired MenuService menuService;

    /**
     * 创建用户
     * @param employee
     * @return
     */
    @PostMapping
    public BaseResult create(@RequestBody ErpEmployee employee){
        return new BaseResult().addPojo(employeeService.create(employee));
    }

    /**
     * 更新用户
     * @param employee
     * @return
     */
    @PutMapping
    public BaseResult update(@RequestBody ErpEmployee employee){
        return new BaseResult().addPojo(employeeService.update(employee));
    }

    /**
     * 更新用户密码
     * @param employeeId
     * @param password
     * @return
     */
    @PatchMapping("{employeeId}")
    public BaseResult updatePassword(@PathVariable Long employeeId, String password){
        return new BaseResult().addPojo(employeeService.updatePassword(employeeId, password));
    }

    @DeleteMapping("{employeeId}")
    public BaseResult delete(@PathVariable Long employeeId){
        String employeeName = employeeService.delete(employeeId);
        return new BaseResult().setErrmsg("删除" + employeeName + "成功");
    }

    /**
     * 组合查询
     * @return
     */
    @GetMapping
    public BaseResult query(EmployeeParameterBuilder parameter){
        Page<ErpEmployee> page = employeeService.query(parameter);
        return page2BaseResult(page, (employee)->!menuService.isAdmin(employee.getId()));
    }
}

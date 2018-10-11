package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.security.ErpEmployee;
import com.yintong.erp.domain.basis.security.ErpEmployeeRepository;
import com.yintong.erp.service.basis.EmployeeService;
import com.yintong.erp.service.basis.EmployeeService.EmployeeParameterBuilder;
import com.yintong.erp.service.basis.MenuService;
import com.yintong.erp.utils.base.BaseResult;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

/**
 * @author lucifer.chan
 * @create 2018-05-18 上午12:59
 * 员工管理
 **/
@RestController
@RequestMapping("basis/employee")
public class EmployeeController {

    @Autowired ErpEmployeeRepository employeeRepository;

    @Autowired EmployeeService employeeService;

    @Autowired MenuService menuService;

    /**
     * 创建用户
     * @param employee
     * @return
     */
    @PostMapping
    public BaseResult create(@RequestBody ErpEmployee employee){
        Assert.hasLength(employee.getName(), "姓名不能为空");
        return new BaseResult().addPojo(employeeService.create(employee));
    }

    /**
     * 保存用户部门
     * @param employeeId
     * @param departmentIds
     * @return
     */
    @PostMapping("{employeeId}/departments")
    public BaseResult saveDepartmentsByEmployeeId(@PathVariable Long employeeId, @RequestBody List<Long> departmentIds){
        return new BaseResult().addPojo(employeeService.saveDepartments(employeeId, departmentIds));
    }

    /**
     * 保存用户权限
     * @param employeeId
     * @param menuCodes
     * @return
     */
    @PostMapping("{employeeId}/menus")
    public BaseResult saveMenusByEmployeeId(@PathVariable Long employeeId, @RequestBody List<String> menuCodes){
        return new BaseResult().addPojo(employeeService.saveMenus(employeeId, menuCodes));
    }

    /**
     * 更新用户
     * @param employee
     * @return
     */
    @PutMapping
    public BaseResult update(@RequestBody ErpEmployee employee){
        Assert.hasLength(employee.getName(), "姓名不能为空");
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

    /**
     * 删除员工
     * @param employeeId
     * @return
     */
    @DeleteMapping("{employeeId}")
    public BaseResult delete(@PathVariable Long employeeId){
        String employeeName = employeeService.delete(employeeId);
        return new BaseResult().setErrmsg("删除" + employeeName + "成功");
    }

    /**
     * 根据员工id查找
     * @param employeeId
     * @return
     */
    @GetMapping("{employeeId}")
    public BaseResult findOne(@PathVariable Long employeeId){
        return new BaseResult().addPojo(employeeService.findOne(employeeId));
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

    @GetMapping("all")
    public BaseResult all(){
        return new BaseResult().addList(
                employeeRepository.findAll()
                        .stream()
                        .filter(employee -> !menuService.isAdmin(employee.getId()))
                        .collect(Collectors.toList())
        );
    }

    /**
     * chaxun
     * @param employeeId
     * @return
     */
    @GetMapping("{employeeId}/orders")
    public BaseResult orders(@PathVariable Long employeeId){
        return new BaseResult().add(employeeService.findOrders(employeeId));
    }
}

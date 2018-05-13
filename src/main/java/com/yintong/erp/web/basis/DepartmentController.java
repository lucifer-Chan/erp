package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseDepartment;
import com.yintong.erp.service.basis.DepartmentService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author lucifer.chan
 * @create 2018-05-14 上午12:34
 * 部门管理
 **/
@RestController
public class DepartmentController {
    @Autowired DepartmentService departmentService;

    /**
     * 获取部门树
     * @return
     */
    @GetMapping("department/all/tree")
    public BaseResult getAllDepartmentsTree(){
        return new BaseResult().addList("tree", departmentService.tree());
    }

    /**
     * 获取某个节点下的部门树
     * @param id
     * @return
     */
    @GetMapping("department/{id}/tree")
    public BaseResult getDepartmentsTreeById(@PathVariable Long id){
        return new BaseResult().addList("tree",departmentService.tree(id));
    }

    /**
     * 创建部门
     * @param department
     * @return
     */
    @PostMapping("department")
    public BaseResult createDepartment(@RequestBody ErpBaseDepartment department){
        return new BaseResult()
                .addPojo( departmentService.createDepartment(department))
                .setErrmsg("成功创建：" + department.getName());
    }

    /**
     * 更新部门名称
     * @param id
     * @param name
     * @return
     */
    @PatchMapping("department/{id}")
    public BaseResult modifyDepartmentName(@PathVariable Long id, String name){
        return new BaseResult()
                .addPojo(departmentService.updateDepartment(id, name))
                .setErrmsg("成功修改部门名称为：" + name);
    }

    /**
     * 删除部门-级联
     * @param id
     * @return
     */
    @DeleteMapping("department/{id}")
    public BaseResult deleteDepartmentName(@PathVariable Long id){
        return new BaseResult()
                .setErrmsg("成功删除" + departmentService.deleteDepartment(id) + "及下属所有部门");
    }
}

package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseDepartment;
import com.yintong.erp.domain.basis.ErpBaseDepartmentRepository;
import com.yintong.erp.domain.basis.associator.ErpEmployeeDepartmentRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * @author lucifer.chan
 * @create 2018-05-13 上午12:08
 * 组织服务
 **/
@Service
public class DepartmentService {
    @Autowired ErpBaseDepartmentRepository departmentRepository;

    @Autowired ErpEmployeeDepartmentRepository employeeDepartmentRepository;

    /**
     * 新建部门
     * @param department
     * @return
     */
    public ErpBaseDepartment createDepartment(ErpBaseDepartment department){
        Assert.isNull(department.getId(), "新建部门，id必须为空");
        ErpBaseDepartment parent = Objects.isNull(department.getParentId()) ? null :
                departmentRepository.findById(department.getParentId()).orElse(null);
        if(Objects.isNull(parent))
            department.setParentId(-1L);
        String name = department.getName();
        Assert.hasLength(name, "部门名称不能为空！");
        List<ErpBaseDepartment> departments = departmentRepository.findByParentIdAndName(department.getParentId(), name);
        Assert.isTrue(CollectionUtils.isEmpty(departments), "同级部门下已有名称为'" + name + "'的部门，请重新命名！");
        return departmentRepository.save(department);
    }

    /**
     * 更行部门名称
     * @param id
     * @param name
     * @return
     */
    public ErpBaseDepartment updateDepartment(Long id, String name) {
        ErpBaseDepartment department = departmentRepository.findById(id).orElse(null);
        Assert.notNull(department, "未找到id为" + id + "的部门");
        department.setName(name);
        return departmentRepository.save(department);
    }

    /**
     * 删除部门-级联删除
     * @param id
     */
    @Transactional
    public String deleteDepartment(Long id){
        ErpBaseDepartment department = departmentRepository.findById(id).orElse(null);
        Assert.notNull(department, "未找到id为" + id + "的部门");
        String ret =  department.getName();
        List<ErpBaseDepartment> departments = append(department, null);
        List<Long> departmentIds = departments.stream().map(ErpBaseDepartment::getId).collect(toList());
        departmentRepository.deleteInBatch(departments);
        employeeDepartmentRepository.deleteByDepartmentIdIn(departmentIds);
        return ret;
    }

    /**
     * 展开一棵树
     * @param root
     * @param ret 返回值
     * @return
     */
    private List<ErpBaseDepartment> append(ErpBaseDepartment root, List<ErpBaseDepartment> ret){
        if(Objects.isNull(ret)) ret = new ArrayList<>();
        ret.add(root);
        List<ErpBaseDepartment> children = children(root.getId());
        if(!CollectionUtils.isEmpty(children)){
            for (ErpBaseDepartment child : children)
                ret = append(child, ret);
        }
        return ret;
    }

        /**
     * 直属children
     * @param parentId
     * @return
     */
    private List<ErpBaseDepartment> children(@NonNull Long parentId) {
        return departmentRepository.findAll().stream()
                .filter(department -> parentId.equals(department.getParentId()))
                .collect(toList());
    }

}

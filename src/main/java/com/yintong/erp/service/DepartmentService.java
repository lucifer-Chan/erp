package com.yintong.erp.service;

import com.yintong.erp.domain.basis.ErpBaseDepartment;
import com.yintong.erp.domain.basis.ErpBaseDepartmentRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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


    /**
     * 树型结构
     * @return
     */
    public List<ErpBaseDepartment> tree(Long rootId){
        ErpBaseDepartment root = departmentRepository.findById(rootId).orElse(null);
        if(Objects.isNull(root))
            return new ArrayList<>();
        List<ErpBaseDepartment> children = children(root.getId());
        if(!CollectionUtils.isEmpty(children)){
            for (ErpBaseDepartment child : children)
                child.setChildren(tree(child.getId()));
            root.setChildren(children);
        }
        return root.getChildren();
    }

    /**
     * 树型结构
     * @return
     */
    public List<ErpBaseDepartment> tree(){
        List<ErpBaseDepartment> roots = departmentRepository.findByParentIdIsNull();
        for (ErpBaseDepartment department : roots){
            department.setChildren(tree(department.getId()));
        }
        return roots;
    }

    private List<ErpBaseDepartment> children(@NonNull Long parentId) {
        return departmentRepository.findAll().stream()
                .filter(department -> parentId.equals(department.getParentId()))
                .collect(toList());
    }
}

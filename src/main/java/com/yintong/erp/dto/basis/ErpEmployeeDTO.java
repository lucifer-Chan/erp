package com.yintong.erp.dto.basis;

import com.yintong.erp.domain.basis.security.ErpEmployee;
import lombok.Data;

import java.util.List;

@Data
public class ErpEmployeeDTO extends ErpEmployee{

    private List<String> departmentNames;//部门名称列表

    private List<Long> departmentIds;//部门id列表

    private List<String> menuNames;//菜单名称列表

    private List<String> menuCodes;//菜单code列表

}

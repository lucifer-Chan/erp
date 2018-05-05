package com.yintong.erp.domain.basis.security;

import com.yintong.erp.utils.base.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午5:53
 * 员工和菜单[角色]的关联表
 **/
@Data
@Entity
public class ErpEmployeeMenu extends BaseEntity{
    @Id
    private Long id;
    @Column(columnDefinition = "bigint(20) comment '员工id'")
    private Long employeeId;
    @Column(columnDefinition = "varchar(10) comment '菜单编码[角色编码]'", nullable = false)
    private String menuCode;
}

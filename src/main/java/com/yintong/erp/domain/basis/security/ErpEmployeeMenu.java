package com.yintong.erp.domain.basis.security;

import com.yintong.erp.utils.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午5:53
 * 员工和菜单[角色]的关联表
 **/
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class ErpEmployeeMenu extends BaseEntity{
    @Id @GeneratedValue
    private Long id;
    @Column(columnDefinition = "bigint(20) comment '员工id'")
    private Long employeeId;
    @Column(columnDefinition = "varchar(10) comment '菜单编码[角色编码]'", nullable = false)
    private String menuCode;
}

package com.yintong.erp.domain.basis.security;

import com.yintong.erp.utils.base.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午5:38
 * 菜单
 **/
@Data
@Entity
public class ErpMenu extends BaseEntity{
    @Id
    @Column(columnDefinition = "varchar(10) comment '菜单编码[角色编码]'")
    private String code;
    @Column(columnDefinition = "varchar(40) comment '菜单名称'")
    private String name;
    @Column(columnDefinition = "varchar(100) comment '可访问的api，英文逗号隔开，支持通配符'")
    private String matches;
    @Column(columnDefinition = "varchar(10) comment '父节点编码'")
    private String parentCode;
}

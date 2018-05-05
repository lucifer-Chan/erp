package com.yintong.erp.domain.basis.security;

import com.yintong.erp.utils.base.BaseEntity;
import lombok.Data;

import javax.persistence.*;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午5:20
 * 员工实体类
 **/
@Data
@Entity
public class ErpEmployee extends BaseEntity{
    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition = "varchar(40) comment '登录名'")
    private String loginName;
    @Column(columnDefinition = "varchar(40) comment '姓名'")
    private String name;
    @Column(columnDefinition = "varchar(100) comment '密码-加密'")
    private String password;
    @Column(columnDefinition = "varchar(40) comment '电话号码'")
    private String mobile;
    @Column(columnDefinition = "varchar(20) comment '条形码'")
    private String barCode;
}

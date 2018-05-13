package com.yintong.erp.domain.basis.security;

import com.yintong.erp.utils.base.BaseEntity;
import com.yintong.erp.utils.transform.IgnoreIfNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.List;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午5:38
 * 菜单-只有两层，无parentCode的为菜单组，有parentCode的为可操作菜单
 **/
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class ErpMenu extends BaseEntity{
    @Id
    @Column(columnDefinition = "varchar(10) comment '菜单编码[角色编码]'")
    private String code;
    @Column(columnDefinition = "varchar(40) comment '菜单名称'")
    private String name;
    @Column(columnDefinition = "varchar(2000) comment '可访问的api，英文逗号隔开，支持通配符'")
    private String matches;
    @Column(columnDefinition = "varchar(10) comment '父节点编码'")
    private String parentCode;
    @Column(columnDefinition = "varchar(100) comment '页面uri'")
    private String uri;

    @Transient @IgnoreIfNull
    private List<ErpMenu> children;
}

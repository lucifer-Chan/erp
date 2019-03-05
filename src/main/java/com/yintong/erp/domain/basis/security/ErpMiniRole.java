package com.yintong.erp.domain.basis.security;

import com.yintong.erp.utils.base.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小程序权限
 *
 * @author lucifer.chan
 * @create 2019-03-04 下午5:26
 **/
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class ErpMiniRole extends BaseEntity {

    @Id
    @Column(columnDefinition = "varchar(30) comment '小程序角色编码'")
    private String code;
    @Column(columnDefinition = "varchar(40) comment '小程序角色名称'")
    private String name;
}

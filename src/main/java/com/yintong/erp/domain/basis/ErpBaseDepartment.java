package com.yintong.erp.domain.basis;

import com.yintong.erp.utils.base.BaseEntity;
import com.yintong.erp.utils.transform.IgnoreIfNull;
import lombok.*;

import javax.persistence.*;
import java.util.List;

/**
 * @author lucifer.chan
 * @create 2018-05-12 下午10:31
 * 部门
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpBaseDepartment extends BaseEntity{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "varchar(20) comment '编号'")
    private String code;

    @Column(columnDefinition = "varchar(40) comment '名称'")
    private String name;

    @Column(columnDefinition = "bigint(20) comment '父节点id'")
    private Long parentId;

    @Column(columnDefinition = "varchar(100) comment '说明'")
    private String description;

    @Transient @IgnoreIfNull
    private List<ErpBaseDepartment> children;
}

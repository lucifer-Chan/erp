package com.yintong.erp.domain.basis;

import com.yintong.erp.utils.base.BaseEntity;
import lombok.*;

import javax.persistence.*;

/**
 * @author lucifer.chan
 * @create 2018-05-21 下午10:33
 * 下拉供选项
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpBaseLookup extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "varchar(10) comment '编号'")
    private String code;

    @Column(columnDefinition = "varchar(100) comment '名称'")
    private String name;

    @Column(columnDefinition = "varchar(10) comment '分类'")
    private String type;

    @Column(columnDefinition = "bigint(20) comment '排序'")
    private Long tag;

    @Column(columnDefinition = "varchar(200) comment '说明'")
    private String description;
}

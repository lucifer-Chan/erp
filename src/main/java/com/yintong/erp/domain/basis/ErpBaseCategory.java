package com.yintong.erp.domain.basis;

import com.yintong.erp.utils.base.BaseEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.*;

/**
 * @author lucifer.chan
 * @create 2018-05-11 上午1:33
 * 基础数据类别
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpBaseCategory extends BaseEntity {
    @Id @GeneratedValue
    private Long id;

    @Column(columnDefinition = "varchar(4) comment '编号：父节点+自身编号'")
    private String code;

    @Column(columnDefinition = "varchar(40) comment '名称'")
    private String name;

    @Column(columnDefinition = "varchar(100) comment '全名'")
    private String fullName;

    @Column(columnDefinition = "varchar(10) comment '父节点编号'")
    private String parentCode;

}

package com.yintong.erp.domain.basis.modelTool;

import com.yintong.erp.domain.basis.baseCommon.ErpBaseCommon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by jianqiang on 2018/5/10 0010.
 * 模具表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ErpBaseModelTool extends ErpBaseCommon {
    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition = "varchar(64) comment '模具编号'")
    private String modelToolNo;
    @Column(columnDefinition = "varchar(20) comment '模具名称'")
    private String modelToolName;
    @Column(columnDefinition = "varchar(20) comment '模具类别编码'")
    private String modelToolTypeCode;
    @Column(columnDefinition = "varchar(64) comment '规格描述'")
    private String specification;
    @Column(columnDefinition = "varchar(128) comment '备注'")
    private String remark;
}

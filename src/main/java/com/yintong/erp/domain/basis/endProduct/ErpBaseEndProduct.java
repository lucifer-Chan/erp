package com.yintong.erp.domain.basis.endProduct;


import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by jianqiang on 2018/5/9 0009.
 * 成品表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ErpBaseEndProduct  extends BaseEntityWithBarCode {

    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition = "varchar(64) comment '成品表编号'")
    private String endProductNo;
    @Column(columnDefinition = "varchar(20) comment '成品表名称'")
    private String endProductName;
    @Column(columnDefinition = "varchar(20) comment '成品表类别编码'")
    private String endProductTypeCode;
    @Column(columnDefinition = "varchar(64) comment '规格描述'")
    private String specification;
    @Column(columnDefinition = "varchar(64) comment '图纸编号'")
    private String drawingNo;
    @Column(columnDefinition = "varchar(20) comment '模具位'")
    private String ModelLocation;
    @Column(columnDefinition = "varchar(128) comment '备注'")
    private String remark;

}

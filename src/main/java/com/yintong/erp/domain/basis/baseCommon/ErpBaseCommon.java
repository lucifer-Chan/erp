package com.yintong.erp.domain.basis.baseCommon;

import com.yintong.erp.utils.base.BaseEntity;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Created by jianqiang on 2018/5/9 0009.
 * 材料、成品公共属性
 */
@MappedSuperclass
@Getter
@Setter
public class ErpBaseCommon extends BaseEntityWithBarCode {


    @Column(columnDefinition = "varchar(64) comment '规格描述'")
    private String specification;
    @Column(columnDefinition = "varchar(12) comment '单位'")
    private String unit;
    @Column(columnDefinition = "varchar(12) comment '库存量'")
    private String stock;
    @Column(columnDefinition = "varchar(12) comment '库存量报警上限'")
    private String callUpperLimit;
    @Column(columnDefinition = "varchar(12) comment '库存量报警下'")
    private String callLowerLimit;
    @Column(columnDefinition = "varchar(20) comment '仓位号'")
    private String warehouseNo;
    @Column(columnDefinition = "varchar(20) comment '仓位名'")
    private String warehouseName;
    @Column(columnDefinition = "varchar(20) comment '供应商'")
    private String supplier;
    @Column(columnDefinition = "varchar(20) comment '供应商表号'")
    private String supplierNo;
    @Column(columnDefinition = "varchar(64) comment '条码'")
    private String barCode;
    @Column(columnDefinition = "varchar(128) comment '备注'")
    private String remark;
}

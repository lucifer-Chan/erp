package com.yintong.erp.domain.basis.baseCommon;

import com.yintong.erp.utils.base.BaseEntity;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Created by jianqiang on 2018/5/10 0010.
 * 客户供应商公共属性
 */
@MappedSuperclass
@Getter
@Setter
public class ErpBaseCustCommon extends BaseEntityWithBarCode {

    @Column(columnDefinition = "varchar(64) comment '客户/供应商编号'")
    private String custNo;
    @Column(columnDefinition = "varchar(64) comment '客户/供应全称'")
    private String custName;
    @Column(columnDefinition = "varchar(20) comment '类型'")
    private String custType;
    @Column(columnDefinition = "varchar(64) comment '地址'")
    private String custAddrDetail;
    @Column(columnDefinition = "varchar(20) comment '联系人'")
    private String contactName;
    @Column(columnDefinition = "varchar(20) comment '手机号'")
    private String contactMobile;
    @Column(columnDefinition = "varchar(20) comment '固定电话'")
    private String contactPhone;
    @Column(columnDefinition = "varchar(64) comment '传真号码'")
    private String FaxNo;
    @Column(columnDefinition = "varchar(64) comment '常用托运部'")
    private String consign;
    @Column(columnDefinition = "varchar(20) comment '所属业务员'")
    private String salesman;
    @Column(columnDefinition = "varchar(64) comment '客户/供应商所属地'")
    private String possession;
    @Column(columnDefinition = "varchar(10) comment '等级'")
    private String rank;
    @Column(columnDefinition = "varchar(128) comment '备注'")
    private String remark;

}

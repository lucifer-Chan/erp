package com.yintong.erp.domain.basis.security;

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
 * Created by jianqiang on 2018/5/11 0011.
 * 人员-供应商
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ErpBaseSupplier extends BaseEntityWithBarCode {

    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition = "varchar(64) comment '供应商编号'")
    private String supplierNo;
    @Column(columnDefinition = "varchar(20) comment '供应商名称'")
    private String supplierName;
    @Column(columnDefinition = "varchar(20) comment '供应商类别编码'")
    private String supplierTypeCode;
    @Column(columnDefinition = "varchar(64) comment '地址'")
    private String address;
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
    @Column(columnDefinition = "varchar(64) comment '所属地'")
    private String possession;
    @Column(columnDefinition = "varchar(10) comment '等级'")
    private String rank;
    @Column(columnDefinition = "varchar(128) comment '备注'")
    private String remark;
}

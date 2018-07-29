package com.yintong.erp.domain.basis;

import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by jianqiang on 2018/5/11 0011.
 * 人员-客户
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ErpBaseCustomer extends BaseEntityWithBarCode {


    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition = "varchar(64) comment '客户编号'")
    private String customerNo;
    @Column(columnDefinition = "varchar(40) comment '客户名称'")
    private String customerName;
    @BarCode
    @Column(columnDefinition = "varchar(20) comment '客户类别编码[公司部-C:UCC0,散户-S:UCS0]'")
    private String customerTypeCode;
    @Column(columnDefinition = "varchar(64) comment '地址'")
    private String address;
    @Column(columnDefinition = "varchar(20) comment '联系人'")
    private String contactName;
    @Column(columnDefinition = "varchar(20) comment '手机号'")
    private String contactMobile;
    @Column(columnDefinition = "varchar(20) comment '固定电话'")
    private String contactPhone;
    @Column(columnDefinition = "varchar(64) comment '传真号码'")
    private String faxNo;
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

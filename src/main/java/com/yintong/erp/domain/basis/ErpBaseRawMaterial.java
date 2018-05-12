package com.yintong.erp.domain.basis;

import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by jianqiang on 2018/5/9 0009.
 * 原材料信息表
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ErpBaseRawMaterial extends BaseEntityWithBarCode {

    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition = "varchar(64) comment '原材料编号'")
    private String rawNo;
    @Column(columnDefinition = "varchar(20) comment '原材料名称'")
    private String rawName;
    @BarCode
    @Column(columnDefinition = "varchar(20) comment '原材料类别编码[银丝-A:MA00,铜丝-Z:{紫铜丝-R:MZR0,黄铜丝-Y:MZY0,铜基丝-B:MZB0,铜镍丝-N:MZN0,白铜丝-Q:MZQ0},铝丝-M:MM00,铁丝-F:MF00,废品-R:{银丝-A:MRA0,铜丝-Z:[0|R|Y|B|N|Q]-MRZ0MZZR}]'")
    private String rawTypeCode;
    @Column(columnDefinition = "varchar(64) comment '规格描述'")
    private String specification;
    @Column(columnDefinition = "varchar(128) comment '备注'")
    private String remark;

}

package com.yintong.erp.domain.basis;

import com.yintong.erp.utils.bar.BarCode;
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
 * Created by jianqiang on 2018/5/10 0010.
 * 设备表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ErpBaseEquipment extends BaseEntityWithBarCode {

    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition = "varchar(64) comment '设备编号'")
    private String equipmentNo;
    @Column(columnDefinition = "varchar(20) comment '设备名称'")
    private String equipmentName;
    @BarCode
    @Column(columnDefinition = "varchar(20) comment '设备类别编码[三复合机-J:EJ10,柳州机-L:EL10,上海机-H:EH20,半空心机-Q:EQ20]'")
    private String equipmentTypeCode;
    @Column(columnDefinition = "varchar(64) comment '规格描述'")
    private String specification;
    @Column(columnDefinition = "varchar(128) comment '备注'")
    private String remark;
}

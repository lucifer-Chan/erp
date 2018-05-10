package com.yintong.erp.domain.basis.warehouse;

import com.yintong.erp.utils.base.BaseEntity;
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
 * 仓位
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ErpBaseWarehouse extends BaseEntityWithBarCode {

    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition = "varchar(64) comment '资产编号'")
    private String assetNo;
    @Column(columnDefinition = "varchar(20) comment '仓位号'")
    private String warehouseNo;
    @Column(columnDefinition = "varchar(20) comment '仓位名'")
    private String warehouseName;
    @Column(columnDefinition = "varchar(20) comment '货架'")
    private String rack;

}

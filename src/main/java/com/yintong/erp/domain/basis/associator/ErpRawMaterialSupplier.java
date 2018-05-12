package com.yintong.erp.domain.basis.associator;

import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.bar.BarCodeIndex;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by jianqiang on 2018/5/12.
 * 原材料-供应商关系表
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Entity
public class ErpRawMaterialSupplier extends BaseEntityWithBarCode {

    @Id
    @GeneratedValue
    private Long id;
    @BarCode(excludeId = true)//4位
    @Column(columnDefinition = "varchar(40) comment '原材料类型'")
    private String rawMaterType;
    @BarCodeIndex(1)
    @Column(columnDefinition = "bigint(20) comment '原材料表id'")
    private Long rawMatertId;
    @BarCodeIndex(holder = true, value = 2)// 1位
    @Column(columnDefinition = "varchar(1) comment '供应商类型'")
    private String supplierType;
    @BarCodeIndex(3)
    @Column(columnDefinition = "bigint(20) comment '供应商id'")
    private Long supplierId;
}

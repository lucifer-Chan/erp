package com.yintong.erp.domain.basis.rawMaterial;

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
 * Created by jianqiang on 2018/5/9 0009.
 * 原材料信息表
 */
@Data
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
    @Column(columnDefinition = "varchar(64) comment '规格描述'")
    private String specification;
    @Column(columnDefinition = "varchar(64) comment '原材料类别'")
    private String rawType;

}

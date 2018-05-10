package com.yintong.erp.domain.basis.endProduct;

import com.yintong.erp.domain.basis.baseCommon.ErpBaseCommon;
import com.yintong.erp.utils.base.BaseEntity;
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
public class ErpBaseEndProduct  extends ErpBaseCommon {

    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition = "varchar(64) comment '成品编号'")
    private String productNo;
    @Column(columnDefinition = "varchar(20) comment '成品名称'")
    private String productName;
    @Column(columnDefinition = "varchar(64) comment '成品类别'")
    private String productType;
    @Column(columnDefinition = "varchar(64) comment '图纸编号'")
    private String drawingNo;
    @Column(columnDefinition = "varchar(20) comment '模具位'")
    private String ModelLocation;

}

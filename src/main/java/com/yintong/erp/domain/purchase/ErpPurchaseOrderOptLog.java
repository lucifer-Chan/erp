package com.yintong.erp.domain.purchase;

import com.yintong.erp.utils.base.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author lucifer.chan
 * @create 2018-08-19 下午1:42
 * 采购单操作记录
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpPurchaseOrderOptLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '采购单id'")
    private Long orderId;

    @Column(columnDefinition = "varchar(100) comment '修改订单状态'")
    private String content;

    @Column(columnDefinition = "varchar(20) comment '当前状态编码'")
    private String statusCode;

    @Column(columnDefinition = "bigint(20) comment '货物id-模版'")
    private Long waresId;

    @Column(columnDefinition = "bigint(20) comment '货物id-关联id'")
    private Long waresAssId;

    @Column(columnDefinition = "varchar(20) DEFAULT '' comment '货物类型-原材料|成品|模具->M|P|D'")
    private String waresType;

    @Column(columnDefinition = "varchar(200) DEFAULT '' comment '货物名称'")
    private String waresName;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '货物条形码-可为空'")
    private String waresBarcode;

    @Column(columnDefinition = "varchar(20) DEFAULT 'order' comment '类型：order-订单操作，item-明细修改， status-状态变更'")
    private String optType;
}

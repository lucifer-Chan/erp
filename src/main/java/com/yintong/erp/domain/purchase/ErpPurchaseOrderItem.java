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
 * @create 2018-08-19 下午1:41
 * 采购单明细
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpPurchaseOrderItem  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '采购订单id'")
    private Long orderId;

    @Column(columnDefinition = "varchar(100) comment '采购订单编号'")
    private String orderCode;

    @Column(columnDefinition = "bigint(20) comment '货物id-模版'")
    private Long waresId;

    @Column(columnDefinition = "bigint(20) comment '货物id-关联id'")
    private Long waresAssId;

    @Column(columnDefinition = "varchar(20) DEFAULT '' comment '货物类型-原材料|成品|模具->M|P|D'")
    private String waresType;

    @Column(columnDefinition = "double(16,9) comment '总额'")
    private Double money;

    @Column(columnDefinition = "double(16,9) comment '数量'")
    private Double num;

    @Column(columnDefinition = "double(16,9) DEFAULT 0 comment '已入库数量'")
    private Double inNum;

    @Column(columnDefinition = "double(16,9) comment '单价'")
    private Double unitPrice;

    @Column(columnDefinition = "varchar(20) comment '状态编码'")
    private String statusCode;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '描述'")
    private String remark;
}

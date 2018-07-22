package com.yintong.erp.domain.sale;

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
 * @create 2018-07-22 下午12:25
 * 订单操作记录
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpSaleOrderOptLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '销售单id'")
    private Long orderId;

    @Column(columnDefinition = "varchar(100) comment '修改订单状态'")
    private String content;

    @Column(columnDefinition = "varchar(20) comment '当前状态编码'")
    private String statusCode;
}

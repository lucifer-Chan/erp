package com.yintong.erp.domain.stock;

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


import static com.yintong.erp.utils.common.Constants.*;
/**
 * @author lucifer.chan
 * @create 2018-08-04 下午10:04
 * 库存操作记录-出入库
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpStockOptLog extends BaseEntity{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '仓位id'")
    private Long stockPlaceId;

    @Column(columnDefinition = "bigint(20) comment '成品id[当仓位为成品仓位时有值]'")
    private Long productId;

    @Column(columnDefinition = "double(10,2) comment '出入库数量'")
    private Double num;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '备注'")
    private String remark;

    @Column(columnDefinition = "varchar(20) DEFAULT '' comment '出入库[IN|OUT]'")
    private StockOpt operation;

    @Column(columnDefinition = "varchar(20) DEFAULT '' comment '来源或目的[销售|退货|采购|生产]'")
    private StockHolder holder;

    @Column(columnDefinition = "bigint(20) comment '来源id：制令单id、销售订单id、采购单id'")
    private Long holderId;


}

package com.yintong.erp.domain.prod;

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
 * 生产制令单的废料入库记录
 *
 * @author lucifer.chan
 * @create 2019-03-07 下午8:51
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpProdGarbageHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '生产制令单id'")
    private Long prodOrderId;

    @Column(columnDefinition = "varchar(30) not null comment '废料类型:MA-废银;MZ-废铜'")
    private String typeCode;

    @Column(columnDefinition = "varchar(30) not null comment '废料类型:MA-废银;MZ-废铜'")
    private String typeName;

    @Column(columnDefinition = "double(10,2) default 0 comment '回收重量(kg)'")
    private double num;
}

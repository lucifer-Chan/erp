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
 * @author lucifer.chan
 * @create 2018-09-03 下午3:58
 * 制令单挑拣记录
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpProdOrderPickRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '制令单id'")
    private Long prodId;

    @Column(columnDefinition = "double(16,9) comment '成品总数'")
    private Double productTotalNum;

    @Column(columnDefinition = "double(16,9) comment '挑拣的质量合格的成品数'")
    private Double productValidNum;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '备注'")
    private String remark;
}

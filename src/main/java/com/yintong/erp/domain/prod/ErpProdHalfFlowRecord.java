package com.yintong.erp.domain.prod;

import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.WF00;

/**
 * 制令单半成品流转记录
 *
 * @author lucifer.chan
 * @create 2019-03-08 下午2:04
 **/
@Entity
@BarCode(prefix = WF00)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpProdHalfFlowRecord extends BaseEntityWithBarCode {
    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '生产制令单id'")
    private Long prodOrderId;

    @Column(columnDefinition = "int(2) default 1 comment '1:流转;2:后处理;3:挑拣;4:包装'")
    private int stage;

    @Column(columnDefinition = "double(10,2) comment '流转重量(kg)'")
    private Double stage1Kg;

    @Column(columnDefinition = "double(10,2) comment '后处理重量(kg)'")
    private Double stage2Kg;

    @Column(columnDefinition = "double(10,2) comment '挑拣重量(kg)'")
    private Double stage3Kg;

    @Column(columnDefinition = "double(10,2) comment '包装重量(kg)'")
    private Double stage4Kg;

    @Column(columnDefinition = "int(11) comment '流转数量(只)'")
    private Integer stage1Num;

    @Column(columnDefinition = "int(11) comment '后处理数量(只)'")
    private Integer stage2Num;

    @Column(columnDefinition = "int(11) comment '挑拣数量(只)'")
    private Integer stage3Num;

    @Column(columnDefinition = "int(11) comment '包装数量(只)'")
    private Integer stage4Num;

    private Date stage1Time;
    private Date stage2Time;
    private Date stage3Time;
    private Date stage4Time;

    private Long stage2UserId;
    private Long stage3UserId;
    private Long stage4UserId;

    private String stage2UserName;
    private String stage3UserName;
    private String stage4UserName;


}

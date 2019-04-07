package com.yintong.erp.domain.prod;

import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.WF00;
import static com.yintong.erp.utils.common.CommonUtil.defaultIfEmpty;

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

    @Column(columnDefinition = "double(10,4) comment '流转重量(kg)'")
    private Double stage1Kg;

    @Column(columnDefinition = "double(10,4) comment '后处理重量(kg)'")
    private Double stage2Kg;

    @Column(columnDefinition = "double(10,4) comment '挑拣重量(kg)'")
    private Double stage3Kg;

    @Column(columnDefinition = "double(10,4) comment '包装重量(kg)'")
    private Double stage4Kg;

    @Column(columnDefinition = "int(11) comment '流转数量(只)'")
    private Integer stage1Num;

    @Column(columnDefinition = "int(11) comment '后处理数量(只)'")
    private Integer stage2Num;

    @Column(columnDefinition = "int(11) comment '挑拣数量(只)'")
    private Integer stage3Num;

    @Column(columnDefinition = "int(11) comment '包装数量(只)'")
    private Integer stage4Num;

    //20170407 新增
    @Column(columnDefinition = "double(10,4) default 0.00 comment '入库重量(kg)'")
    private Double inKg;
    @Column(columnDefinition = "int default 0 comment '入库数量(只)'")
    private Integer inNum;

    @Column(columnDefinition = "double(10,4) default 0.00 comment '挑拣后重量(kg)'")
    private Double afterPickKg;

    @Column(columnDefinition = "int default 0 default 0 comment '总数量'")
    private Integer totalNum;

    @Column(columnDefinition = "int default 0 default 0 comment '包数'")
    private Integer packCount;

    @Column(columnDefinition = "double(10,4) default 0.00 comment '零头重量(kg)'")
    private Double remnantKg;

    @Column(columnDefinition = "int default 0 default 0 comment '零头数量(只)'")
    private Integer remnantNum;

    @Column(columnDefinition = "int default 0 default 0 comment '每包数量(只)'")
    private Integer perPackNum;

    @Column(columnDefinition = "double(10,4) default 0.00 comment '每包重量(kg)'")
    private Double perPackKg;

    @Column(columnDefinition = "varchar(100) comment '批次'")
    private String sn;

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

    public String getSn(){
        if(StringUtils.hasLength(sn)) return sn;
        if(Objects.isNull(id) || Objects.isNull(getCreatedAt())) return "";
        String defaultSn = new SimpleDateFormat("yyyyMMdd").format(this.getCreatedAt()).concat("-").concat(this.id + "");
        return defaultIfEmpty(this.sn, defaultSn);
    }
}

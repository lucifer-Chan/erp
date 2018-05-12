package com.yintong.erp.domain.basis;


import com.yintong.erp.utils.bar.BarCode;
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
 * 成品表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ErpBaseEndProduct  extends BaseEntityWithBarCode {

    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition = "varchar(64) comment '成品表编号'")
    private String endProductNo;
    @Column(columnDefinition = "varchar(20) comment '成品表名称'")
    private String endProductName;
    @BarCode
    @Column(columnDefinition = "varchar(20) comment '成品表类别编码[触点-T:{三复合银点-T:PTT0,二复合银点-D:PTD0,整体银点-W:PTW0,铜触点-U:PTU0}柳钉-N:{紫铜柳钉-R:PNR0,黄铜柳钉-Y:PNY0,铝柳钉-M:PNM0,铁柳钉-F:PNF0}废品-R:{三复合银点-T:PRT0,二复合银点-D:PRD0,整体银点-W:PRW0,铜触点-U:PRU0,紫铜柳钉-R:PRR0,黄铜柳钉-Y:PRY0,铝柳钉-M:PRM0,铁柳钉-F:PRF0)]'")
    private String endProductTypeCode;
    @Column(columnDefinition = "varchar(64) comment '规格描述'")
    private String specification;
    @Column(columnDefinition = "varchar(64) comment '图纸编号'")
    private String drawingNo;
    @Column(columnDefinition = "varchar(20) comment '模具位'")
    private String ModelLocation;
    @Column(columnDefinition = "varchar(128) comment '备注'")
    private String remark;

}

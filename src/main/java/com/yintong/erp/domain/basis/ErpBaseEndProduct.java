package com.yintong.erp.domain.basis;


import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.common.SpringUtil;
import com.yintong.erp.utils.excel.Importable;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by jianqiang on 2018/5/9 0009.
 * 成品表
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ErpBaseEndProduct  extends BaseEntityWithBarCode implements Importable{

    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition = "varchar(64) comment '成品编号'")
    private String endProductNo;
    @Column(columnDefinition = "varchar(20) comment '成品名称'")
    private String endProductName;
    @BarCode
    @Column(columnDefinition = "varchar(20) comment '成品类别编码[触点-T:{三复合银点-T:PTT0,二复合银点-D:PTD0,整体银点-W:PTW0,铜触点-U:PTU0}柳钉-N:{紫铜柳钉-R:PNR0,黄铜柳钉-Y:PNY0,铝柳钉-M:PNM0,铁柳钉-F:PNF0}废品-R:{三复合银点-T:PRT0,二复合银点-D:PRD0,整体银点-W:PRW0,铜触点-U:PRU0,紫铜柳钉-R:PRR0,黄铜柳钉-Y:PRY0,铝柳钉-M:PRM0,铁柳钉-F:PRF0)]'")
    private String endProductTypeCode;
    @Column(columnDefinition = "varchar(64) comment '规格描述'")
    private String specification;
    @Column(columnDefinition = "varchar(64) comment '客户图号'")
    private String custDrawingNo;
    @Column(columnDefinition = "varchar(64) comment '图纸编号'")
    private String drawingNo;
    @Column(columnDefinition = "varchar(20) comment '模具位'")
    private String modelLocation;
    @Column(columnDefinition = "varchar(10) comment '头径D(mm)上限'")
    private String spHdmmUpper;
    @Column(columnDefinition = "varchar(10) comment '头径D(mm)下限'")
    private String spHdmmLower;
    @Column(columnDefinition = "varchar(10) comment '头厚T(mm)上限'")
    private String spHtmmUpper;
    @Column(columnDefinition = "varchar(10) comment '头厚T(mm)下限'")
    private String spHtmmLower;
    @Column(columnDefinition = "varchar(10) comment '脚径d(mm)上限'")
    private String spFtmmUpper;
    @Column(columnDefinition = "varchar(10) comment '脚径d(mm)下限'")
    private String spFtmmLower;
    @Column(columnDefinition = "varchar(10) comment '脚长L(mm)上限'")
    private String spFlmmUpper;
    @Column(columnDefinition = "varchar(10) comment '脚长L(mm)下限'")
    private String spFlmmLower;
    @Column(columnDefinition = "varchar(10) comment '头径银层S1(mm)上限'")
    private String spHdsmmUpper;
    @Column(columnDefinition = "varchar(10) comment '头径银层S1(mm)下限'")
    private String spHdsmmLower;
    @Column(columnDefinition = "varchar(10) comment '脱模角度θ(° )上限'")
    private String spTmammUpper;
    @Column(columnDefinition = "varchar(10) comment '脱模角度θ(° )下限'")
    private String spTmammLower;
    @Column(columnDefinition = "varchar(10) comment '球半径SR(mm)上限'")
    private String spSrammUpper;
    @Column(columnDefinition = "varchar(10) comment '球半径SR(mm)下限'")
    private String spSrammLower;
    @Column(columnDefinition = "varchar(10) comment '同轴度◎(mm)上限'")
    private String spAxlemmUpper;
    @Column(columnDefinition = "varchar(10) comment '同轴度◎(mm)下限'")
    private String spAxlemmLower;
    @Column(columnDefinition = "varchar(10) comment '边缘S1(mm)上限'")
    private String spEdgemmUpper;
    @Column(columnDefinition = "varchar(10) comment '边缘S1(mm)下限'")
    private String speEdgeemmLower;
    @Column(columnDefinition = "varchar(10) comment '钉脚S2(mm)上限'")
    private String spFdsmmUpper;
    @Column(columnDefinition = "varchar(10) comment '钉脚S2(mm)下限'")
    private String speFdsmmLower;
    @Column(columnDefinition = "varchar(10) comment '脚边缘S2(mm)上限'")
    private String spFaxlemmUpper;
    @Column(columnDefinition = "varchar(10) comment '脚边缘S2(mm)下限'")
    private String speFaxlemmLower;
    @Column(columnDefinition = "varchar(10) comment '头部复合强度'")
    private String spHCstrength;
    @Column(columnDefinition = "varchar(10) comment '脚部复合强度'")
    private String spFCstrength;
    @Column(columnDefinition = "varchar(20) comment '单粒银耗(g)'")
    private String unitSilverLoss;
    @Column(columnDefinition = "varchar(20) comment '单粒银铜(g)'")
    private String unitSilverCopper;
    @Column(columnDefinition = "varchar(64) comment '技术要求'")
    private String technicalRequirements;
    @Column(columnDefinition = "varchar(64) comment '自定义属性1'")
    private String userDefinedOne;
    @Column(columnDefinition = "varchar(64) comment '自定义属性2'")
    private String userDefinedTwo;
    @Column(columnDefinition = "varchar(64) comment '自定义属性3'")
    private String userDefinedThree;
    @Column(columnDefinition = "varchar(2000) comment '备注'")
    private String remark;
    @Column(columnDefinition = "varchar(20) comment '导入时间,空值表示录入'")
    private String importedAt;

    @Transient
    private String endProductTypeName;

    public void setEndProductTypeName(String endProductTypeName){
        this.endProductTypeName = endProductTypeName;
        List<ErpBaseCategory> list = SpringUtil.getBean(ErpBaseCategoryRepository.class).findByFullName(endProductTypeName);
        if(CollectionUtils.isNotEmpty(list)){
            ErpBaseCategory category = list.stream().filter(c->c.getCode().length() == 4).findAny().orElse(null);
            if(Objects.nonNull(category))
                this.endProductTypeCode = category.getCode();
        }
    }

    @Override
    public void validate(){
        Assert.hasLength(endProductTypeCode, "未找到类别");
    }

    @Transient
    private String supplierTypeCode;
}

package com.yintong.erp.domain.basis;

import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.bar.BarCodeConstants;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.common.SpringUtil;
import com.yintong.erp.utils.excel.Importable;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;
import org.springframework.util.StringUtils;

/**
 * Created by jianqiang on 2018/5/9 0009.
 * 原材料信息表
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ErpBaseRawMaterial extends BaseEntityWithBarCode implements Importable, TemplateWares {

    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "varchar(20) comment '原材料名称'")
    private String rawName;

    @Column(columnDefinition = "varchar(12) comment '单位'")
    private String unit;

    @BarCode
    @Column(columnDefinition = "varchar(20) comment '原材料类别编码[银丝-A:MA00,铜丝-Z:{紫铜丝-R:MZR0,黄铜丝-Y:MZY0,铜基丝-B:MZB0,铜镍丝-N:MZN0,白铜丝-Q:MZQ0},铝丝-M:MM00,铁丝-F:MF00,废品-R:{银丝-A:MRA0,铜丝-Z:[0|R|Y|B|N|Q]-MRZ0MZZR}]'")
    private String rawTypeCode;

    @Column(columnDefinition = "varchar(64) comment '规格描述'")
    private String specification;

    @Column(columnDefinition = "varchar(128) comment '备注'")
    private String remark;

    @Column(columnDefinition = "varchar(20) comment '导入时间,空值表示录入'")
    private String importedAt;



//    @Column(columnDefinition = "varchar(10) comment '头径D(mm)上限'")
//    private String spHdmmUpper;
//    @Column(columnDefinition = "varchar(10) comment '头径D(mm)下限'")
//    private String spHdmmLower;
//    @Column(columnDefinition = "varchar(10) comment '头厚T(mm)上限'")
//    private String spHtmmUpper;
//    @Column(columnDefinition = "varchar(10) comment '头厚T(mm)下限'")
//    private String spHtmmLower;
//    @Column(columnDefinition = "varchar(10) comment '脚径d(mm)上限'")
//    private String spFtmmUpper;
//    @Column(columnDefinition = "varchar(10) comment '脚径d(mm)下限'")
//    private String spFtmmLower;
//    @Column(columnDefinition = "varchar(10) comment '脚长L(mm)上限'")
//    private String spFlmmUpper;
//    @Column(columnDefinition = "varchar(10) comment '脚长L(mm)下限'")
//    private String spFlmmLower;
//    @Column(columnDefinition = "varchar(10) comment '头径银层S1(mm)上限'")
//    private String spHdsmmUpper;
//    @Column(columnDefinition = "varchar(10) comment '头径银层S1(mm)下限'")
//    private String spHdsmmLower;
//    @Column(columnDefinition = "varchar(10) comment '脱模角度θ(° )上限'")
//    private String spTmammUpper;
//    @Column(columnDefinition = "varchar(10) comment '脱模角度θ(° )下限'")
//    private String spTmammLower;
//    @Column(columnDefinition = "varchar(10) comment '球半径SR(mm)上限'")
//    private String spSrammUpper;
//    @Column(columnDefinition = "varchar(10) comment '球半径SR(mm)下限'")
//    private String spSrammLower;
//    @Column(columnDefinition = "varchar(10) comment '同轴度◎(mm)上限'")
//    private String spAxlemmUpper;
//    @Column(columnDefinition = "varchar(10) comment '同轴度◎(mm)下限'")
//    private String spAxlemmLower;
//    @Column(columnDefinition = "varchar(10) comment '边缘S1(mm)上限'")
//    private String spEdgemmUpper;
//    @Column(columnDefinition = "varchar(10) comment '边缘S1(mm)下限'")
//    private String speEdgeemmLower;
//    @Column(columnDefinition = "varchar(10) comment '钉脚S2(mm)上限'")
//    private String spFdsmmUpper;
//    @Column(columnDefinition = "varchar(10) comment '钉脚S2(mm)下限'")
//    private String speFdsmmLower;
//    @Column(columnDefinition = "varchar(10) comment '脚边缘S2(mm)上限'")
//    private String spFaxlemmUpper;
//    @Column(columnDefinition = "varchar(10) comment '脚边缘S2(mm)下限'")
//    private String speFaxlemmLower;
//    @Column(columnDefinition = "varchar(10) comment '头部复合强度'")
//    private String spHCstrength;
//    @Column(columnDefinition = "varchar(10) comment '脚部复合强度'")
//    private String spFCstrength;
//    @Column(columnDefinition = "varchar(64) comment '自定义属性1'")
//    private String userDefinedOne;
//    @Column(columnDefinition = "varchar(64) comment '自定义属性2'")
//    private String userDefinedTwo;
//    @Column(columnDefinition = "varchar(64) comment '自定义属性3'")
//    private String userDefinedThree;

    @Transient
    private String rawTypeName;

    @Transient
    private String description;

    @Transient
    private String supplierTypeCode;

    public String getRawTypeName() {
         if(StringUtils.isEmpty(rawTypeCode)) return rawTypeName = "";
         String prefix = BarCodeConstants.BAR_CODE_PREFIX.valueOf(rawTypeCode).description();
         return rawTypeName = prefix.substring("原材料-".length(), prefix.length());
    }

    public String getDescription(){
        if(StringUtils.hasText(description)) return description;
        return description = (this.getRawTypeName() + "-" + this.getRawName() + "-" + this.getSpecification());
    }

    @Override
    public Long getWaresId() {
        return id;
    }

    @Override
    public String getSimpleName() {
        return rawName;
    }

    @Override
    public String getCategoryCode() {
        return rawTypeCode;
    }

    public void setRawTypeName(String rawTypeName){
        rawTypeName = "原材料-" + rawTypeName;
        this.rawTypeName = rawTypeName;
        List<ErpBaseCategory> list = SpringUtil.getBean(ErpBaseCategoryRepository.class).findByFullName(rawTypeName);
        if(CollectionUtils.isNotEmpty(list)){
            ErpBaseCategory category = list.stream().filter(c->c.getCode().length() == 4).findAny().orElse(null);
            if(Objects.nonNull(category))
                this.rawTypeCode = category.getCode();
        }
    }

    @Override
    public void requiredValidate(){
        Assert.hasLength(rawTypeCode, "未找到类别");
    }

    @Override
    public void uniqueValidate(){
        ErpBaseRawMaterialRepository repository = SpringUtil.getBean(ErpBaseRawMaterialRepository.class);
        List<ErpBaseRawMaterial> shouldBeEmpty
                = Objects.isNull(id)
                ? repository.findByRawNameAndSpecificationAndRawTypeCode(rawName, specification, rawTypeCode)
                : repository.findByRawNameAndSpecificationAndRawTypeCodeAndIdNot(rawName, specification, rawTypeCode, id);
        Assert.isTrue(CollectionUtils.isEmpty(shouldBeEmpty), "名称-类别-规格重复");
    }
}

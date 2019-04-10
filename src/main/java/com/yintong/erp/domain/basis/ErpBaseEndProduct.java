package com.yintong.erp.domain.basis;

import com.yintong.erp.domain.stock.ErpStockPlace;
import com.yintong.erp.domain.stock.StockEntity;
import com.yintong.erp.domain.stock.StockPlaceFinder;
import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.bar.BarCodeConstants;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.base.JsonWrapper;
import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.SpringUtil;
import com.yintong.erp.utils.excel.Importable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.sf.json.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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
public class ErpBaseEndProduct  extends BaseEntityWithBarCode implements Importable, StockEntity<ErpBaseEndProduct>, TemplateWares {

    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition = "varchar(12) comment '单位'")
    private String unit;
    @Column(columnDefinition = "varchar(20) comment '成品名称'")
    private String endProductName;
    @BarCode
    @Column(columnDefinition = "varchar(20) comment '成品类别编码[触点-T:{三复合银点-T:PTT0,二复合银点-D:PTD0,整体银点-W:PTW0,铜触点-U:PTU0}铆钉-N:{紫铜铆钉-R:PNR0,黄铜铆钉-Y:PNY0,铝铆钉-M:PNM0,铁铆钉-F:PNF0}废品-R:{三复合银点-T:PRT0,二复合银点-D:PRD0,整体银点-W:PRW0,铜触点-U:PRU0,紫铜铆钉-R:PRR0,黄铜铆钉-Y:PRY0,铝铆钉-M:PRM0,铁铆钉-F:PRF0)]'")
    private String endProductTypeCode;
    @Column(columnDefinition = "varchar(64) comment '规格描述'")
    private String specification;

    @Column(columnDefinition = "int(20) comment '预警上限'")
    private Integer alertUpper;
    @Column(columnDefinition = "int(20) comment '预警下限'")
    private Integer alertLower;
    @Column(columnDefinition = "double(20,5) comment '库存总量'")
    private Double totalNum;

    @Column(columnDefinition = "varchar(64) comment '客户图号'")
    private String custDrawingNo;
    @Column(columnDefinition = "varchar(64) comment '客户代码(旧)'")
    private String custCodeOld;
    @Column(columnDefinition = "varchar(64) comment '客户代码(新)'")
    private String custCodeNew;
    @Column(columnDefinition = "varchar(64) comment '客户名称'")
    private String custName;
    @Column(columnDefinition = "varchar(10) comment '只/kg'")
    private String onlyOrKg;
    @Column(columnDefinition = "varchar(10) comment '难度系数'")
    private String courseRating;
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
    @Column(columnDefinition = "varchar(20) comment '内控单粒银耗(g)'")
    private String unitSilverLoss;
    @Column(columnDefinition = "varchar(20) comment '内控单粒耗铜(g)'")
    private String unitSilverCopper;
    @Column(columnDefinition = "varchar(200) comment '技术要求'")
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


    //20180808新增
    @Column(columnDefinition = "varchar(20) comment '产品等级'")
    private String level;

    @Column(columnDefinition = "varchar(128) comment '材料名称'")
    private String materialName;

    @Column(columnDefinition = "varchar(20) comment '同心度'")
    private String concentricity;

    //20190216新增
    @Column(columnDefinition = "varchar(20) comment '单粒银耗(g)-展示用，不加入计算'")
    private String unitSilver;
    @Column(columnDefinition = "varchar(20) comment '单粒耗铜(g)，不加入计算'")
    private String unitCopper;

    @Transient
    private String endProductTypeName;

    @Transient
    private String typeName;

    @Transient
    private String description;

    @Transient
    private String supplierTypeCode;

    @Transient
    private JSONObject module;

    public JSONObject getModule(){
        if(Objects.nonNull(module)) return module;

        if(StringUtils.isEmpty(modelLocation)) return module = new JSONObject();

        ErpBaseModelToolRepository repository = SpringUtil.getBean(ErpBaseModelToolRepository.class);

        List<ErpBaseModelTool> modules = repository.findByModelPlace(getModelLocation());

        if(CollectionUtils.isEmpty(modules)){
            return module = new JSONObject();
        }

        ErpBaseModelTool m = modules.get(0);

        return module = JsonWrapper.builder()
                .add("modelPlace", m.getModelPlace())
                .add("modelToolNo", m.getModelToolNo())
                .add("specification", m.getSpecification())
                .add("angle", m.getAngle())
                .build();
    }

    public String getTypeName(){
        return StringUtils.hasText(endProductTypeCode) ? BarCodeConstants.BAR_CODE_PREFIX.valueOf(endProductTypeCode).description() : "";
    }

    public String getDescription(){
        if(StringUtils.hasText(description)) return description;
        String _type;
        try {
            String prefix = BarCodeConstants.BAR_CODE_PREFIX.valueOf(endProductTypeCode).description();
            _type = prefix.substring("成品-".length(), prefix.length()) + "-";
        } catch (Exception e){
            _type = "";
        }
        return description = (_type + this.getEndProductName() + (StringUtils.hasText(specification) ? ("-" + specification) : ""));
    }

    @Override
    public JSONObject extInfo(){
        List<ErpStockPlace> places = StockPlaceFinder.findPlaces(String.valueOf(id), null);
        return JsonWrapper.builder()
                .add("onlyOrKg", onlyOrKg)
                .add("places", CommonUtil.defaultIfEmpty(places.stream().map(ErpStockPlace::getPlaceCode).collect(Collectors.joining(",")), "无"))
                .add("_materialName", materialName)
                .build();
    }

    @Override
    public Long getWaresId() {
        return id;
    }

    @Override
    public String getSimpleName() {
        return endProductName;
    }

    @Override
    public String getCategoryCode() {
        return endProductTypeCode;
    }

    public Double getTotalNum(){
        return Objects.isNull(totalNum) ? 0d :totalNum;
    }

    public void setEndProductTypeName(String endProductTypeName){
        endProductTypeName = "成品-" + endProductTypeName;
        this.endProductTypeName = endProductTypeName;
        List<ErpBaseCategory> list = SpringUtil.getBean(ErpBaseCategoryRepository.class).findByFullName(endProductTypeName);
        if(CollectionUtils.isNotEmpty(list)){
            ErpBaseCategory category = list.stream().filter(c->c.getCode().length() == 4).findAny().orElse(null);
            if(Objects.nonNull(category))
                this.endProductTypeCode = category.getCode();
        }
    }

    public String getEndProductTypeName(){
        try{
            String fullName = BarCodeConstants.BAR_CODE_PREFIX.valueOf(this.getEndProductTypeCode()).description();
            return fullName.substring("成品-".length());
        } catch (Exception e){
            return "";
        }
    }

    @Override
    public void requiredValidate(){
        Assert.hasLength(endProductTypeCode, "未找到类别");
    }

    @Override
    public void uniqueValidate(){
        ErpBaseEndProductRepository repository = SpringUtil.getBean(ErpBaseEndProductRepository.class);
        //候选
        List<ErpBaseEndProduct> candidates
                = Objects.isNull(id)
                ? repository.findByEndProductNameAndSpecification(endProductName, specification)
                : repository.findByEndProductNameAndSpecificationAndIdNot(endProductName, specification, id);
        if(CollectionUtils.isEmpty(candidates)) return;

        Set<String> uniqueStrings = candidates.stream().map(ErpBaseEndProduct::toUniqueString).collect(Collectors.toSet());

        Assert.isTrue(!uniqueStrings.contains(this.toUniqueString()), "货物名称-规格-材料名称-铜耗-银耗 重复");
    }

    /**
     * 唯一性字符串 : 货物名称、规格描述、材料名称、铜耗、银耗
     * @return
     */
    private String toUniqueString(){
        return "N:" + endProductName + ",S:" + specification + ",M:" + materialName + ",A:" + unitSilverLoss + ",C:" + unitSilverCopper;
    }

    /**
     * 入库
     * @param num
     * @return
     */
    @Override
    public ErpBaseEndProduct stockIn(double num){
        setTotalNum(this.getTotalNum() + num);
        return this;
    }

    /**
     * 出库
     * @param num
     * @return
     */
    @Override
    public ErpBaseEndProduct stockOut(double num){
        setTotalNum(this.getTotalNum() - num);
        return this;
    }

    @Override
    public ErpBaseEndProduct entity() {
        return this;
    }

    @Override
    public Long templateId() {
        return id;
    }

    @Override
    public Long realityId() {
        return id;
    }

    @Override
    public Constants.WaresType waresType() {
        return Constants.WaresType.P;
    }

    @Override
    protected void prePersist(){
        this.unit = "kg";
        CommonUtil.trim(this);
    }

    @Override
    protected void preUpdate(){
        this.unit = "kg";
        CommonUtil.trim(this);
    }

}

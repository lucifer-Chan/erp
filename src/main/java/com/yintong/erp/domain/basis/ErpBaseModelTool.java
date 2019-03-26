package com.yintong.erp.domain.basis;

import com.yintong.erp.domain.stock.ErpStockPlace;
import com.yintong.erp.domain.stock.StockEntity;
import com.yintong.erp.service.stock.StockPlaceService;
import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.bar.BarCodeConstants;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.SpringUtil;
import com.yintong.erp.utils.excel.Importable;
import java.util.List;
import java.util.Objects;
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
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.D100;

/**
 * Created by jianqiang on 2018/5/10 0010.
 * 模具位、编号、规格、角度，其中“模具位”不可重复；
 * 模具表
 */
@Entity
@BarCode(prefix = D100)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpBaseModelTool extends BaseEntityWithBarCode implements Importable, StockEntity<ErpBaseModelTool>, TemplateWares{
    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "varchar(64) comment '模具位'")
    private String modelPlace;

    @Column(columnDefinition = "varchar(64) comment '模具编号'")
    private String modelToolNo;

    @Column(columnDefinition = "varchar(64) comment '规格描述'")
    private String specification;

    @Column(columnDefinition = "varchar(64) comment '角度'")
    private String angle;




    @Column(columnDefinition = "varchar(20) comment '模具类别编码'")
    private String modelToolTypeCode = D100.name();

    @Column(columnDefinition = "varchar(20) comment '模具名称'")
    private String modelToolName;

    @Column(columnDefinition = "varchar(64) comment '单位'")
    private String unit = "件";

    @Column(columnDefinition = "varchar(128) comment '备注'")
    private String remark;

    @Column(columnDefinition = "double(20,5) comment '库存总量'")
    private Double totalNum;

    @Transient
    private String supplierTypeCode;

    @Transient
    private String description;

    @Transient
    private String mouldTypeName;

    @Transient
    private String placeNames;

    /**
     * 有库存的仓位名称
     * @return
     */
    public String getPlaceNames(){
        if(StringUtils.hasText(placeNames)) return placeNames;
        if(!StringUtils.hasText(getBarCode())) return placeNames = "";
        List<ErpStockPlace> places = SpringUtil.getBean(StockPlaceService.class).findPlacesByMouldCode(getBarCode());

        return placeNames = places.stream().map(ErpStockPlace::getName).collect(Collectors.joining(","));
    }

    public String getMouldTypeName(){
        return D100.description();

    }

    public String getDescription(){
        if(StringUtils.hasText(description)) return description;
        return description = modelPlace;
    }

    @Override
    public Long getWaresId() {
        return id;
    }

    @Override
    public String getSimpleName() {
        return modelPlace;
    }

    @Override
    public String getCategoryCode() {
        return modelToolTypeCode;
    }

    public double getTotalNum(){
        return Objects.isNull(totalNum) ? 0d :totalNum;
    }

    @Override
    public void requiredValidate(){
        Assert.hasLength(modelPlace, "未找到模具位");
    }

    @Override
    public void uniqueValidate(){
        ErpBaseModelToolRepository repository = SpringUtil.getBean(ErpBaseModelToolRepository.class);
        List<ErpBaseModelTool> shouldBeEmpty
                = Objects.isNull(id)
                ? repository.findByModelPlace(modelPlace)
                : repository.findByModelPlaceAndIdNot(modelPlace, id);
        Assert.isTrue(CollectionUtils.isEmpty(shouldBeEmpty), "模具位重复");
    }

    @Override
    public ErpBaseModelTool stockIn(double num) {
        setTotalNum(this.getTotalNum() + num);
        return this;
    }

    @Override
    public ErpBaseModelTool stockOut(double num) {
        setTotalNum(this.getTotalNum() - num);
        return this;
    }

    @Override
    public ErpBaseModelTool entity() {
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
        return Constants.WaresType.D;
    }
}

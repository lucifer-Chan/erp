package com.yintong.erp.domain.basis.associator;

import com.yintong.erp.domain.basis.ErpBaseModelTool;
import com.yintong.erp.domain.basis.ErpBaseModelToolRepository;
import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.domain.basis.ErpBaseSupplierRepository;
import com.yintong.erp.domain.basis.TemplateWares;
import com.yintong.erp.domain.stock.ErpStockPlace;
import com.yintong.erp.domain.stock.StockEntity;
import com.yintong.erp.service.stock.StockPlaceService;
import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.bar.BarCodeConstants;
import com.yintong.erp.utils.bar.BarCodeIndex;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.SpringUtil;
import java.util.List;
import java.util.Objects;
import javax.persistence.Transient;
import lombok.*;
import net.sf.json.JSONObject;
import org.apache.commons.collections4.KeyValue;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

/**
 * Created by jianqiang on 2018/5/12.
 * 模具-供应商关系表
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Entity
public class ErpModelSupplier extends BaseEntityWithBarCode  implements StockEntity<ErpModelSupplier> {

    @Id
    @GeneratedValue
    private Long id;
    @BarCode(excludeId = true)//4位
    @Column(columnDefinition = "varchar(40) comment '模具类型'")
    private String modelType;
    @BarCodeIndex(1)
    @Column(columnDefinition = "bigint(20) comment '模具表id'")
    private Long modelId;
    @BarCodeIndex(holder = true, value = 2)// 1位
    @Column(columnDefinition = "varchar(1) comment '供应商类型'")
    private String supplierType;
    @BarCodeIndex(3)
    @Column(columnDefinition = "bigint(20) comment '供应商id'")
    private Long supplierId;
    @Column(columnDefinition = "int(20) comment '预警上限'")
    private Integer alertUpper;
    @Column(columnDefinition = "int(20) comment '预警下限'")
    private Integer alertLower;
    @Column(columnDefinition = "datetime comment '关联时间'")
    private Date associateAt;

    @Column(columnDefinition = "double(20,5) comment '库存总量'")
    private Double totalNum;

    @Transient
    private JSONObject wares;

    @Transient
    private String supplierName;

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

    //供应商名称
    public String getSupplierName(){
        if(StringUtils.hasText(supplierName)) return supplierName;

        if(Objects.nonNull(supplierId)){
            ErpBaseSupplier supplier = SpringUtil.getBean(ErpBaseSupplierRepository.class).findById(supplierId).orElse(null);
            supplierName = Objects.isNull(supplier) ? "" : supplier.getSupplierName();
        }

        return supplierName;
    }

    @Override
    protected void prePersist(){
        validate();
    }

    @Override
    protected void preUpdate(){
        validate();
    }

    private void validate(){
        Assert.notNull(modelId, "模具id不能为空");
        Assert.notNull(modelType, "模具类型不能为空");
        Assert.notNull(supplierId, "供应商id不能为空");
        Assert.notNull(supplierType, "供应商类型不能为空");
        Assert.isTrue(BarCodeConstants.mouldTypes().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList())
                        .contains(modelType),
                "模具类型不正确"
        );
        Assert.isTrue(
                BarCodeConstants.supplierTypes().stream()
                        .map(BarCodeConstants.BAR_CODE_PREFIX::third)
                        .map(KeyValue::getKey)
                        .collect(Collectors.toList())
                        .contains(supplierType),
                "供应商类型不正确"
        );
    }

    public double getTotalNum(){
        return Objects.isNull(totalNum) ? 0d :totalNum;
    }

    @Override
    public ErpModelSupplier stockIn(double num) {
        setTotalNum(this.getTotalNum() + num);
        ErpBaseModelToolRepository mouldRepository = SpringUtil.getBean(ErpBaseModelToolRepository.class);
        ErpBaseModelTool mould = mouldRepository.findById(templateId()).orElse(null);
        if(Objects.nonNull(mould)){
            mouldRepository.save(mould.stockIn(num));
        }
        return this;
    }

    @Override
    public ErpModelSupplier stockOut(double num) {
        setTotalNum(this.getTotalNum() - num);
        ErpBaseModelToolRepository mouldRepository = SpringUtil.getBean(ErpBaseModelToolRepository.class);
        ErpBaseModelTool mould = mouldRepository.findById(templateId()).orElse(null);
        if(Objects.nonNull(mould)){
            mouldRepository.save(mould.stockOut(num));
        }
        return this;
    }

    @Override
    public ErpModelSupplier entity() {
        return this;
    }

    @Override
    public Long templateId() {
        return modelId;
    }

    @Override
    public Long realityId() {
        return id;
    }

    @Override
    public Constants.WaresType waresType() {
        return Constants.WaresType.D;
    }

    public JSONObject getWares(){
        if(Objects.nonNull(wares)) return wares;
        TemplateWares templateWares = template();
        return wares = (Objects.isNull(templateWares) ? null : templateWares.getTemplate());
    }
}

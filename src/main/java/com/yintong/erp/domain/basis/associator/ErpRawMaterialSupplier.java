package com.yintong.erp.domain.basis.associator;

import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.domain.basis.ErpBaseSupplierRepository;
import com.yintong.erp.domain.stock.ErpStockPlace;
import com.yintong.erp.domain.stock.ErpStockPlaceRepository;
import com.yintong.erp.domain.stock.StockEntity;
import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.bar.BarCodeConstants;
import com.yintong.erp.utils.bar.BarCodeIndex;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.SpringUtil;
import java.util.Objects;
import javax.persistence.Transient;
import lombok.*;
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
 * 原材料-供应商关系表
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Entity
public class ErpRawMaterialSupplier extends BaseEntityWithBarCode implements StockEntity<ErpRawMaterialSupplier> {

    @Id
    @GeneratedValue
    private Long id;
    @BarCode(excludeId = true)//4位
    @Column(columnDefinition = "varchar(40) comment '原材料类型'")
    private String rawMaterType;
    @BarCodeIndex(1)
    @Column(columnDefinition = "bigint(20) comment '原材料表id'")
    private Long rawMaterId;
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
    private String supplierName;

    @Transient
    private String placeNames;//仓位名称

    @Override
    protected void prePersist(){
        validate();
    }

    @Override
    protected void preUpdate(){
        validate();
    }

    private void validate(){
        Assert.notNull(rawMaterId, "原材料id不能为空");
        Assert.notNull(rawMaterType, "原材料类型不能为空");
        Assert.notNull(supplierId, "供应商id不能为空");
        Assert.notNull(supplierType, "供应商类型不能为空");
        Assert.isTrue(BarCodeConstants.rawMaterialTypes().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList())
                        .contains(rawMaterType),
                "原材料类型不正确"
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

    //供应商名称
    public String getSupplierName(){
        if(StringUtils.hasText(supplierName)) return supplierName;

        if(Objects.nonNull(supplierId)){
            ErpBaseSupplier supplier = SpringUtil.getBean(ErpBaseSupplierRepository.class).findById(supplierId).orElse(null);
            supplierName = Objects.isNull(supplier) ? "" : supplier.getSupplierName();
        }

        return supplierName;
    }

    public double getTotalNum(){
        return Objects.isNull(totalNum) ? 0d :totalNum;
    }

    public String getPlaceNames(){
        if(StringUtils.hasText(placeNames)) return placeNames;
        if(Objects.isNull(id)) return "";
        return placeNames = SpringUtil.getBean(ErpStockPlaceRepository.class)
                .findByMaterialSupplierAssId(id)
                .stream()
                .map(ErpStockPlace::getPlaceCode)
                .collect(Collectors.joining(","));
    }


    @Override
    public ErpRawMaterialSupplier stockIn(double num) {
        setTotalNum(this.getTotalNum() + num);
        return this;
    }

    @Override
    public ErpRawMaterialSupplier stockOut(double num) {
        setTotalNum(this.getTotalNum() - num);
        return this;
    }

    @Override
    public ErpRawMaterialSupplier entity() {
        return this;
    }

    @Override
    public Long templateId() {
        return this.rawMaterId;
    }

    @Override
    public Long realityId() {
        return id;
    }

    @Override
    public Constants.WaresType waresType() {
        return Constants.WaresType.M;
    }
}

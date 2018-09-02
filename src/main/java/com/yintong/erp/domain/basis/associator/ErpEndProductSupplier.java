package com.yintong.erp.domain.basis.associator;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.domain.basis.ErpBaseSupplierRepository;
import com.yintong.erp.domain.basis.TemplateWares;
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
 * 成品-供应商关系表
 */

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Entity
public class ErpEndProductSupplier extends BaseEntityWithBarCode implements StockEntity<ErpEndProductSupplier> {
    @Id
    @GeneratedValue
    private Long id;
    @BarCode(excludeId = true)//4位
    @Column(columnDefinition = "varchar(40) not null comment '成品类型'")
    private String endProductType;
    @BarCodeIndex(1)
    @Column(columnDefinition = "bigint(20) not null comment '成品表id'")
    private Long endProductId;
    @BarCodeIndex(holder = true, value = 2)// 1位
    @Column(columnDefinition = "varchar(1) not null comment '供应商类型'")
    private String supplierType;
    @BarCodeIndex(3)
    @Column(columnDefinition = "bigint(20) not null comment '供应商id'")
    private Long supplierId;
    @Column(columnDefinition = "int(20) comment '预警上限'")
    private Integer alertUpper;
    @Column(columnDefinition = "int(20) comment '预警下限'")
    private Integer alertLower;
    @Column(columnDefinition = "datetime comment '关联时间'")
    private Date associateAt;

    @Column(columnDefinition = "double(16,9) comment '库存总量'")
    private Double totalNum;

    @Transient
    private JSONObject wares;

    @Transient
    private String supplierName;

    @Override
    protected void prePersist(){
        validate();
    }

    @Override
    protected void preUpdate(){
        validate();
    }

    private void validate(){
        Assert.notNull(endProductId, "成品id不能为空");
        Assert.notNull(endProductType, "成品类型不能为空");
        Assert.notNull(supplierId, "供应商id不能为空");
        Assert.notNull(supplierType, "供应商类型不能为空");
        Assert.isTrue(BarCodeConstants.productTypes().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList())
                        .contains(endProductType),
                "成品类型不正确"
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
    public ErpEndProductSupplier stockIn(double num) {
        setTotalNum(this.getTotalNum() + num);
        ErpBaseEndProductRepository productRepository = SpringUtil.getBean(ErpBaseEndProductRepository.class);

        ErpBaseEndProduct product = productRepository.findById(templateId()).orElse(null);
        if(Objects.nonNull(product)){
            productRepository.save(product.stockIn(num));
        }

        return this;
    }

    @Override
    public ErpEndProductSupplier stockOut(double num) {
        setTotalNum(this.getTotalNum() - num);
        ErpBaseEndProductRepository productRepository = SpringUtil.getBean(ErpBaseEndProductRepository.class);

        ErpBaseEndProduct product = productRepository.findById(templateId()).orElse(null);
        if(Objects.nonNull(product)){
            productRepository.save(product.stockOut(num));
        }
        return this;
    }

    @Override
    public ErpEndProductSupplier entity() {
        return this;
    }

    @Override
    public Long templateId() {
        return endProductId;
    }

    @Override
    public Long realityId() {
        return id;
    }

    @Override
    public Constants.WaresType waresType() {
        return Constants.WaresType.P;
    }

    public JSONObject getWares(){
        if(Objects.nonNull(wares)) return wares;
        TemplateWares templateWares = template();
        return wares = (Objects.isNull(templateWares) ? null : templateWares.getTemplate());
    }
}

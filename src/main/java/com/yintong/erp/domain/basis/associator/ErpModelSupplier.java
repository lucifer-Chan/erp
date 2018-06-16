package com.yintong.erp.domain.basis.associator;

import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.bar.BarCodeConstants;
import com.yintong.erp.utils.bar.BarCodeIndex;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import lombok.*;
import org.apache.commons.collections4.KeyValue;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;
import java.util.stream.Collectors;

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
public class ErpModelSupplier extends BaseEntityWithBarCode {

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
    @Column(columnDefinition = "int(20) comment '数量'")
    private Integer totalNum;

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
}

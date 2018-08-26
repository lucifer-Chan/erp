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
 * Created by jianqiang on 2018/5/10 0010.
 * 模具表
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ErpBaseModelTool extends BaseEntityWithBarCode implements Importable {
    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "varchar(64) comment '模具编号'")
    private String modelToolNo;

    @Column(columnDefinition = "varchar(20) comment '模具名称'")
    private String modelToolName;

    @BarCode
    @Column(columnDefinition = "varchar(20) comment '模具类别编码'")
    private String modelToolTypeCode;

    @Column(columnDefinition = "varchar(64) comment '规格描述'")
    private String specification;

    @Column(columnDefinition = "varchar(128) comment '备注'")
    private String remark;

    @Transient
    private String supplierTypeCode;

    @Transient
    private String description;

    public String getDescription(){
        if(StringUtils.hasText(description)) return description;
        String _type;
        try {
            String prefix = BarCodeConstants.BAR_CODE_PREFIX.valueOf(modelToolTypeCode).description();
            _type = prefix.substring("模具-".length(), prefix.length()) + "-";
        } catch (Exception e){
            _type = "";
        }

        return description = (_type + this.getModelToolName() + (StringUtils.hasText(specification)? ("-" + specification) : ""));
    }


    @Override
    public void requiredValidate(){
        Assert.hasLength(modelToolTypeCode, "未找到类别");
    }

    @Override
    public void uniqueValidate(){
        ErpBaseModelToolRepository repository = SpringUtil.getBean(ErpBaseModelToolRepository.class);
        List<ErpBaseModelTool> shouldBeEmpty
                = Objects.isNull(id)
                ? repository.findByModelToolNameAndSpecification(modelToolName, specification)
                : repository.findByModelToolNameAndSpecificationAndIdNot(modelToolName, specification, id);
        Assert.isTrue(CollectionUtils.isEmpty(shouldBeEmpty), "名称-规格重复");
    }

}

package com.yintong.erp.domain.basis.associator;

import org.springframework.util.Assert;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.yintong.erp.domain.basis.ErpBaseRawMaterial;
import com.yintong.erp.domain.basis.ErpBaseRawMaterialRepository;
import com.yintong.erp.utils.base.BaseEntity;
import com.yintong.erp.utils.common.SpringUtil;


/**
 * @author lucifer.chan
 * @create 2018-08-14 下午3:03
 * 物料清单-成品
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpBaseProductBom extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '成品id'")
    private Long productId;

    @Column(columnDefinition = "bigint(20) comment '原材料id-未关联供应商'")
    private Long materialId;

    @Column(columnDefinition = "double(20,5) comment '切丝长度'")
    private Double materialNum;

    @Transient
    private ErpBaseRawMaterial material;

    public ErpBaseRawMaterial getMaterial(){
        if(Objects.nonNull(material)) return material;
        if(Objects.nonNull(materialId)){
            this.material = SpringUtil.getBean(ErpBaseRawMaterialRepository.class)
                    .findById(materialId).orElse(null);
        }
        return material;
    }

    protected void prePersist(){
        onPreCommit();
    }

    protected void preUpdate(){
        onPreCommit();
    }

    /**
     * 存库前的验证
     */
    private void onPreCommit(){
        Assert.notNull(productId, "请选择成品");
        Assert.notNull(materialId , "请选择原材料");
    }
}

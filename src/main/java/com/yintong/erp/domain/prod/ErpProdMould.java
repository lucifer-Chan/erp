package com.yintong.erp.domain.prod;

import com.yintong.erp.domain.basis.ErpBaseModelTool;
import com.yintong.erp.domain.basis.ErpBaseModelToolRepository;
import com.yintong.erp.domain.basis.associator.ErpModelSupplier;
import com.yintong.erp.domain.basis.associator.ErpModelSupplierRepository;
import com.yintong.erp.utils.base.BaseEntity;
import com.yintong.erp.utils.common.SpringUtil;
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
import org.springframework.util.Assert;

/**
 * @author lucifer.chan
 * @create 2018-09-03 下午2:37
 * 生产所需的模具清单[计划单一份、制令单一份]
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpProdMould extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '成品id'")
    private Long productId;

    @Column(columnDefinition = "bigint(20) comment '供应商id-冗余'")
    private Long supplierId;

    @Column(columnDefinition = "varchar(40) comment 'ProdBomHolder枚举，计划单|制令单'")
    private String holder;

    @Column(columnDefinition = "bigint(20) comment '计划单|制令单id'")
    private Long holderId;

    @Column(columnDefinition = "bigint(20) comment '模具id-未关联供应商'")
    private Long mouldId;

    @Column(columnDefinition = "bigint(20) comment '模具id-关联供应商'")
    private Long realityMouldId;

    @Column(columnDefinition = "double(20,5) comment '模具数量-需求'")
    private Double realityMouldNum;

    @Column(columnDefinition = "double(20,5) comment '模具数量-出库-针对制令单'")
    private Double numOut;

    @Column(columnDefinition = "double(20,5) comment '模具数量-入库-针对制令单[用完回收]'")
    private Double numIn;

    /**
     * 模具模版
     */
    @Transient
    private ErpBaseModelTool mould;

    /**
     * 实际模具
     */
    @Transient
    private ErpModelSupplier realityMould;

    public double getNumIn(){
        return Objects.isNull(numIn) ? 0d : numIn;
    }

    public double getNumOut(){
        return Objects.isNull(numOut) ? 0d : numOut;
    }

    public double getRealityMouldNum(){
        return Objects.isNull(realityMouldNum) ? 0d : realityMouldNum;
    }

    /**
     * 获取模具模版
     * @return
     */
    public ErpBaseModelTool getMould(){
        if(Objects.nonNull(mould)) return mould;
        if(Objects.nonNull(mouldId)){
            this.mould = SpringUtil.getBean(ErpBaseModelToolRepository.class)
                    .findById(mouldId).orElseThrow(()->new IllegalArgumentException("模具信息[" + mouldId + "]缺失！"));
        }
        return mould;
    }

    /**
     * 获取实际的厂家模具
     * @return
     */
    public ErpModelSupplier getRealityMould(){
        if(Objects.nonNull(realityMould)) return realityMould;
        if(Objects.nonNull(realityMouldId)){
            this.realityMould = SpringUtil.getBean(ErpModelSupplierRepository.class)
                    .findById(realityMouldId).orElse(null);
        }

        if(Objects.isNull(realityMould) && Objects.nonNull(supplierId) && -1L != supplierId){
            this.realityMould = SpringUtil.getBean(ErpModelSupplierRepository.class)
                    .findByModelIdAndSupplierId(mouldId, supplierId).orElse(null);
        }

        return realityMould;
    }

    protected void prePersist(){
        validate();
        numOut = 0D;
        numIn = 0D;
    }

    protected void preUpdate(){
        validate();
    }

    /**
     * 存库前的验证
     */
    public void validate(){
        Assert.notNull(productId, "请选择成品");
        Assert.notNull(mouldId , "请选择模具");
//        Assert.notNull(realityMouldId , "请选择供应商模具");
        Assert.notNull(realityMouldNum , "请输入供应商模具数量");
        Assert.notNull(holder, "请选择单据类型[计划单|制令单]");
        Assert.notNull(holderId, "请选择单据[计划单|制令单]");
        if(supplierId == -1L){
            realityMouldId = null;
        } else {
            ErpModelSupplier _realityMould = getRealityMould();
            if(Objects.nonNull(_realityMould)){
                this.realityMouldId = _realityMould.getId();
            }
        }
    }

    /**
     * 拷贝一份
     * @param source
     * @param holder
     * @param holderId
     * @param num
     * @return
     */
    public static ErpProdMould copy(ErpProdMould source, String holder, Long holderId, Double num){
        ErpProdMould ret = new ErpProdMould();
        ret.setProductId(source.productId);
        ret.setSupplierId(source.supplierId);
        ret.setHolder(holder);
        ret.setHolderId(holderId);
        ret.setMouldId(source.mouldId);
        ret.setRealityMouldId(source.realityMouldId);
        ret.setRealityMouldNum(num);
        return ret;
    }
}

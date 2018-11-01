package com.yintong.erp.domain.prod;

import com.yintong.erp.dto.BomDto;
import com.yintong.erp.domain.basis.ErpBaseRawMaterial;
import com.yintong.erp.domain.basis.ErpBaseRawMaterialRepository;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplier;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplierRepository;
import com.yintong.erp.utils.base.BaseEntity;
import com.yintong.erp.utils.common.SpringUtil;
import java.util.Objects;
import java.util.function.BiFunction;
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
import org.apache.commons.lang.ObjectUtils;
import org.springframework.util.Assert;

import static com.yintong.erp.utils.common.Constants.ProdBomHolder.ORDER;
import static com.yintong.erp.utils.common.Constants.ProdBomHolder.PLAN;


/**
 * @author lucifer.chan
 * @create 2018-09-03 下午2:04
 * 生产-产品的物料清单[计划单一份、制令单一份]
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpProdProductBom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '成品id-冗余数据-和holder中一致'")
    private Long productId;

    @Column(columnDefinition = "varchar(40) comment 'ProdBomHolder枚举，计划单|制令单'")
    private String holder;

    @Column(columnDefinition = "bigint(20) comment '计划单|制令单id'")
    private Long holderId;

    @Column(columnDefinition = "bigint(20) comment '原材料id-未关联供应商'")
    private Long materialId;

    @Column(columnDefinition = "bigint(20) comment '供应商id-冗余'")
    private Long supplierId;

    @Column(columnDefinition = "varchar(100) comment '供应商名称-冗余'")
    private String supplierName;

    @Column(columnDefinition = "bigint(20) comment '原材料id-关联供应商'")
    private Long realityMaterialId;

    @Column(columnDefinition = "double(20,5) comment '原材料数量-耗银|耗铜，总数-单位g'")
    private Double realityMaterialNum;

    @Column(columnDefinition = "double(20,5) comment '原材料数量-出库-针对制令单'")
    private Double numOut;

    @Column(columnDefinition = "double(20,5) comment '原材料数量-入库-针对制令单[用完回收]'")
    private Double numIn;

    @Column(columnDefinition = "double(20,5) comment '切丝长度'")
    private Double materialNum;

    /**
     * 原材料模版
     */
    @Transient
    private ErpBaseRawMaterial material;

    /**
     * 实际原材料
     */
    @Transient
    private ErpRawMaterialSupplier realityMaterial;

    public double getNumIn(){
        return Objects.isNull(numIn) ? 0d : numIn;
    }

    public double getNumOut(){
        return Objects.isNull(numOut) ? 0d : numOut;
    }

    public double getRealityMaterialNum(){
        return Objects.isNull(realityMaterialNum) ? 0d : realityMaterialNum;
    }

    /**
     * 获取原材料模版
     * @return
     */
    public ErpBaseRawMaterial getMaterial(){
        if(Objects.nonNull(material)) return material;
        if(Objects.nonNull(materialId)){
            this.material = SpringUtil.getBean(ErpBaseRawMaterialRepository.class)
                    .findById(materialId).orElse(null);
        }
        return material;
    }

    /**
     * 获取实际的厂家原材料
     * @return
     */
    public ErpRawMaterialSupplier getRealityMaterial(){
        if(Objects.nonNull(realityMaterial)) return realityMaterial;
        if(Objects.nonNull(realityMaterialId)){
            this.realityMaterial = SpringUtil.getBean(ErpRawMaterialSupplierRepository.class)
                    .findById(realityMaterialId).orElse(null);
        }
        return realityMaterial;
    }

    protected void prePersist(){
        onPreCommit();
        numOut = 0D;
        numIn = 0D;
    }

    protected void preUpdate(){
        onPreCommit();
    }

    /**
     * 保存前的验证
     */
    private void onPreCommit(){
        Assert.notNull(productId, "请选择成品");
        Assert.notNull(materialId , "请选择原材料");
        Assert.notNull(realityMaterialId , "请选择供应商原材料");
        Assert.notNull(realityMaterialNum , "请输入供应商原材料数量");
        Assert.notNull(holder, "请选择单据类型[计划单|制令单]");
        Assert.notNull(holderId, "请选择单据[计划单|制令单]");
    }

    /**
     * 根据生产制令单和成品物料清单构建
     * @param orderId
     * @param planBom -从计划单里拿到的bom
     * @param num - 数量
     * @return
     */
    public static ErpProdProductBom copy4CreateOrder(Long orderId, ErpProdProductBom planBom, Double num){
        ErpProdProductBom bom = new ErpProdProductBom();
        bom.setHolder(ORDER.name());
        bom.setHolderId(orderId);
        bom.setMaterialId(planBom.getMaterialId());
        bom.setRealityMaterialNum(num);
        bom.setRealityMaterialId(planBom.getRealityMaterialId());
        bom.setSupplierId(planBom.getSupplierId());
        bom.setSupplierName(planBom.getSupplierName());
        bom.setMaterialNum(planBom.getMaterialNum());
        bom.setProductId(planBom.getProductId());
        return bom;
    }

    /**
     * 根据生产计划单和成品物料清单构建
     * @param plan
     * @param source
     * @param findAssIdBySupplierIdAndMaterialIdFunction
     * @return
     */
    public static ErpProdProductBom build4Plan(ErpProdPlan plan, BomDto source
            , BiFunction<Long, Long, Long> findAssIdBySupplierIdAndMaterialIdFunction){
        ErpProdProductBom bom = new ErpProdProductBom();
        bom.setProductId(plan.getProductId());
        bom.copy4Plan(source, findAssIdBySupplierIdAndMaterialIdFunction);
        bom.setHolder(PLAN.name());
        bom.setHolderId(plan.getId());
        return bom;
    }

    private ErpProdProductBom copy4Plan(BomDto source, BiFunction<Long, Long, Long> findAssIdBySupplierIdAndMaterialIdFunction){
        this.setMaterialId(source.getMaterialId());
        this.setRealityMaterialNum(source.getNum());
        this.setRealityMaterialId(findAssIdBySupplierIdAndMaterialIdFunction.apply(source.getSupplierId(), source.getMaterialId()));
        this.setSupplierId(source.getSupplierId());
        this.setSupplierName(source.getSupplierName());
        this.setMaterialNum(source.getMaterialNum());
        return this;
    }

    /**
     * 更新时的校验，若无更改，则返回null
     * @return
     */
    public ErpProdProductBom copy4UpdatePlan(BomDto source, BiFunction<Long, Long, Long> findAssIdBySupplierIdAndMaterialIdFunction){
        boolean modified = false;
        if(!this.getSupplierId().equals(source.getSupplierId())){
            modified = true;
        }
        if(!ObjectUtils.equals(getRealityMaterialNum(), source.getNum())){
            modified = true;
        }

        if(!ObjectUtils.equals(getMaterialNum(), source.getMaterialNum())){
            modified = true;
        }

        return modified ? copy4Plan(source, findAssIdBySupplierIdAndMaterialIdFunction) : null;
    }
}

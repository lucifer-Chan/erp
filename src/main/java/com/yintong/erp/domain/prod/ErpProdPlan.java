package com.yintong.erp.domain.prod;

import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.SpringUtil;
import java.util.Date;
import java.util.List;
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
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.R000;

/**
 * @author lucifer.chan
 * @create 2018-09-03 下午1:44
 * 生产计划- 对单个成品进行计划
 **/
@Entity
@BarCode(prefix = R000)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpProdPlan extends BaseEntityWithBarCode {
    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '生产成品id'")
    private Long productId;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '生产成品-冗余数据'")
    private String productName;

    @Column(columnDefinition = "double(16,9) comment '计划生产数量'")
    private Double planNum;

    @Column(columnDefinition = "double(16,9) comment '已分配的数量-分配给制令单'")
    private Double distributedNum;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '生产计划单描述'")
    private String description;

    @Column(columnDefinition = "date comment '计划开始年月日'")
    private Date startDate;

    @Column(columnDefinition = "date comment '计划结束年月日'")
    private Date endDate;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '备注'")
    private String remark;

    @Column(columnDefinition = "double(16,9) comment '当前达成数量'")
    private Double finishNum;

    @Transient
    private List<ErpProdOrder> prodOrders;

    @Transient
    private List<ErpProdProductBom> boms;

    @Transient
    private List<ErpProdMould> moulds;

    public List<ErpProdOrder> getProdOrders(){
        if(!CollectionUtils.isEmpty(prodOrders)){
            return prodOrders;
        }
        return prodOrders = SpringUtil.getBean(ErpProdOrderRepository.class).findByPlanId(id);
    }

    public List<ErpProdProductBom> getBoms(){
        if(!CollectionUtils.isEmpty(boms)){
            return boms;
        }
        return boms = SpringUtil.getBean(ErpProdProductBomRepository.class).findByHolderAndHolderId(Constants.ProdBomHolder.PLAN.name(), id);
    }

    public List<ErpProdMould> getMoulds(){
        if(!CollectionUtils.isEmpty(moulds)){
            return moulds;
        }
        return moulds = SpringUtil.getBean(ErpProdMouldRepository.class).findByHolderAndHolderId(Constants.ProdBomHolder.PLAN.name(), id);
    }

    protected void prePersist(){
        onPreCommit();
    }

    protected void preUpdate(){
        onPreCommit();
    }

    /**
     * 保存前的验证
     */
    private void onPreCommit(){
        Assert.notNull(productId, "成品不能为空");
        Assert.hasText(productName, "成品不能为空");
        Assert.notNull(planNum , "数量不能为空");
        Assert.notNull(startDate , "计划开始时间不能为空");
        Assert.hasText(description , "计划单描述不能为空");
        if(null == distributedNum) distributedNum = 0D;
    }
}

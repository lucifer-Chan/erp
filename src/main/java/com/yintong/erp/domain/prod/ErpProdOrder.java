package com.yintong.erp.domain.prod;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.service.basis.EmployeeService;
import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.SpringUtil;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
import org.springframework.util.StringUtils;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.Q000;

/**
 * @author lucifer.chan
 * @create 2018-09-03 下午3:14
 * 生产制令单- 对单个成品进行生产 计划单：指令单 1：N
 **/
@Entity
@BarCode(prefix = Q000)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpProdOrder extends BaseEntityWithBarCode {
    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '计划单id'")
    private Long planId;

    @Column(columnDefinition = "bigint(20) comment '生产成品id-冗余数据'")
    private Long productId;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '生产成品-冗余数据'")
    private String productName;

    @Column(columnDefinition = "varchar(30) DEFAULT '' comment '订单状态'")
    private String statusCode;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '订单状态'")
    private String statusName;

    @Column(columnDefinition = "bigint(20) comment '员工id'")
    private Long employeeId;

    @Column(columnDefinition = "double(20,5) comment '生产数量,计划单分配过来的数量'")
    private Double prodNum;

    @Column(columnDefinition = "double(20,5) comment '已挑拣数量-计算值（挑拣+）'")
    private Double pickNum;

    @Column(columnDefinition = "double(20,5) comment '当前达成数量-计算值（入库+）'")
    private Double finishNum;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '生产制令单描述'")
    private String description;

    @Column(columnDefinition = "date comment '开始年月日'")
    private Date startDate;

    @Column(columnDefinition = "date comment '完成年月日'")
    private Date finishDate;

    @Column(columnDefinition = "integer DEFAULT 0 comment '是否可出库[1-可以|0-不可以]'")
    private Integer preStockOut;

    @Column(columnDefinition = "integer DEFAULT 0 comment '是否可入库[1-可以|0-不可以]'")
    private Integer preStockIn;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '备注'")
    private String remark;

    @Column(columnDefinition = "varchar(5) comment '单位：kg|只'")
    private String unit;

    @Transient
    private String employeeName;

    @Transient
    private List<ErpProdProductBom> boms;

    @Transient
    private List<ErpProdMould> moulds;

    @Transient
    private List<ErpProdOrderPickRecord> pickRecords;

    @Transient
    private ErpBaseEndProduct product;

    /**
     * 获取挑拣记录
     * @return
     */
    public List<ErpProdOrderPickRecord> getPickRecords(){
        if(!CollectionUtils.isEmpty(pickRecords)){
            return pickRecords;
        }
        return pickRecords = SpringUtil.getBean(ErpProdOrderPickRecordRepository.class).findByOrderIdOrderByCreatedAtDesc(id);
    }

    public List<ErpProdProductBom> getBoms(){
        if(!CollectionUtils.isEmpty(boms)){
            return boms;
        }
        return boms = SpringUtil.getBean(ErpProdProductBomRepository.class).findByHolderAndHolderId(Constants.ProdBomHolder.ORDER.name(), id);
    }

    public List<ErpProdMould> getMoulds(){
        if(!CollectionUtils.isEmpty(moulds)){
            return moulds;
        }
        return moulds = SpringUtil.getBean(ErpProdMouldRepository.class).findByHolderAndHolderId(Constants.ProdBomHolder.ORDER.name(), id);
    }

    public String getEmployeeName(){
        if(StringUtils.hasText(employeeName)) return employeeName;
        if(Objects.nonNull(employeeId)){
            employeeName = SpringUtil.getBean(EmployeeService.class).findOne(employeeId).getName();
        }

        return employeeName;
    }

    public ErpBaseEndProduct getProduct(){
        if(Objects.nonNull(product)) return product;
        if(Objects.nonNull(productId)){
            product = SpringUtil.getBean(ErpBaseEndProductRepository.class).findById(productId).orElse(null);
        }
        return product;
    }

    protected void prePersist(){
        this.setPickNum(0D);
        this.setFinishNum(0D);
        this.setPreStockIn(0);
        this.setPreStockOut(0);
        this.setStatusCode(Constants.ProdOrderStatus.S_001.name());
        onPreCommit();
    }

    protected void preUpdate(){
        onPreCommit();
    }

    /**
     * 保存前的验证
     */
    private void onPreCommit(){
        Assert.notNull(planId, "生产计划单不能为空");
        Assert.notNull(productId, "成品不能为空");
        Assert.notNull(employeeId, "工人不能为空");
        Assert.notNull(prodNum , "数量不能为空");
        Assert.notNull(startDate , "开始时间不能为空");
        Assert.hasText(productName, "成品不能为空");
        Assert.hasText(description , "制令单描述不能为空");
        Assert.hasText(statusCode, "状态不能为空");
        this.setStatusName(Constants.ProdOrderStatus.valueOf(statusCode).description());
    }

    /**
     * 从计划单里获取基本数据-不包括bom
     * @param plan
     */
    public ErpProdOrder copyFromPlan(ErpProdPlan plan) {
        planId = plan.getId();
        productId = plan.getProductId();
        productName = plan.getProductName();
        unit = plan.getUnit();
        return this;

    }

}

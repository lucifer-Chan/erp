package com.yintong.erp.domain.sale;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.J000;

/**
 * @author lucifer.chan
 * @create 2018-07-21 下午4:08
 * 销售计划单
 **/
@Entity
@BarCode(prefix = J000)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpSalePlan extends BaseEntityWithBarCode {
    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '成品id'")
    private Long productId;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '计划单描述'")
    private String description;

    @Column(columnDefinition = "double(20,5) comment '计划销售金额'")
    private Double planMoney;

    @Column(columnDefinition = "date comment '计划开始年月日'")
    private Date startDate;

    @Column(columnDefinition = "date comment '计划结束年月日'")
    private Date endDate;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '备注'")
    private String remark;

    //达成金额
    @Transient
    private Double currentMoney;

    //达成记录
    @Transient
    private List<ErpSaleOrderItem> finishHistory;

    @Transient
    private String productName;

    public String getProductName(){
        if(StringUtils.hasText(productName)) return productName;

        if(Objects.nonNull(productId)){
            ErpBaseEndProduct product = SpringUtil.getBean(ErpBaseEndProductRepository.class)
                    .findById(productId).orElse(null);
            this.productName = Objects.isNull(product) ? "" :
                    product.getDescription();
        }
        return productName;
    }

    /**
     * 计算达成记录
     * @return
     */
    public List<ErpSaleOrderItem> getFinishHistory(){
        if(CollectionUtils.isEmpty(finishHistory)
                && Objects.nonNull(startDate)
                && Objects.nonNull(endDate)
                && Objects.nonNull(productId)){
            finishHistory =
                    SpringUtil.getBean(ErpSaleOrderItemRepository.class)
                        .findByProductIdAndStatusCodeAndCreatedAtIsBetween(
                                productId, Constants.SaleOrderStatus.STATUS_007.name(), startDate, endDate
                        );
        }
        return finishHistory;

    }


    /**
     * 计算达成金额
     * @return
     */
    public Double getCurrentMoney(){
        if(Objects.nonNull(currentMoney)) return currentMoney;
        double ret = 0.00d;
        List<ErpSaleOrderItem> orderItems = getFinishHistory();
        if(!CollectionUtils.isEmpty(orderItems)) {
            ret = orderItems.stream()
                    .mapToDouble(ErpSaleOrderItem ::getMoney)
                    .sum();
        }
        return ret;
    }

}

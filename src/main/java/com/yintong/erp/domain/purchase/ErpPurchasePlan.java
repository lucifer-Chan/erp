package com.yintong.erp.domain.purchase;

import com.yintong.erp.utils.bar.BarCode;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.B000;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.SpringUtil;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleFunction;
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

/**
 * @author lucifer.chan
 * @create 2018-08-19 下午12:50
 * 采购计划单 - 对单类原材料|成品|模具 进行计划
 **/
@Entity
@BarCode(prefix = B000)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpPurchasePlan extends BaseEntityWithBarCode {
    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '货物id-模版'")
    private Long waresId;

    @Column(columnDefinition = "varchar(20) comment '货物类型-原材料|成品|模具->M|P|D'")
    private String waresType;

    @Column(columnDefinition = "varchar(200) DEFAULT '' comment '货物名称'")
    private String waresName;

    @Column(columnDefinition = "double(16,9) comment '计划花费金额'")
    private Double planMoney;

    @Column(columnDefinition = "double(16,9) comment '计划采购数量'")
    private Double planNum;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '采购计划单描述'")
    private String description;

    @Column(columnDefinition = "date comment '计划开始年月日'")
    private Date startDate;

    @Column(columnDefinition = "date comment '计划结束年月日'")
    private Date endDate;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '备注'")
    private String remark;

    //当前达成数量
    @Transient
    private Double currentNum;

    //当前花费金额
    @Transient
    private Double currentMoney;

    //达成记录
    @Transient
    private List<ErpPurchaseOrderItem> finishHistory;

    /**
     * 计算达成记录
     * @return
     */
    public List<ErpPurchaseOrderItem> getFinishHistory(){
        if(CollectionUtils.isEmpty(finishHistory)
                && Objects.nonNull(startDate)
                && Objects.nonNull(endDate)
                && Objects.nonNull(waresId)
                && StringUtils.hasText(waresType)){
            finishHistory =
                    SpringUtil.getBean(ErpPurchaseOrderItemRepository.class)
                            .findByWaresIdAndWaresTypeAndStatusCodeAndCreatedAtIsBetween(
                                    waresId, waresType, Constants.PurchaseOrderStatus.STATUS_007.name(), startDate, endDate
                            );
        }
        return finishHistory;
    }

    /**
     * 计算达成数量
     * @return
     */
    public Double getCurrentNum(){
        if(Objects.nonNull(currentNum)) return currentNum;
        return currentNum = current(ErpPurchaseOrderItem::getNum);
    }

    /**
     * 计算花费金额
     * @return
     */
    public Double getCurrentMoney(){
        if(Objects.nonNull(currentMoney)) return currentMoney;
        return currentMoney = current(ErpPurchaseOrderItem::getMoney);
    }

    private Double current(ToDoubleFunction<? super ErpPurchaseOrderItem> mapper){
        double ret = 0.00d;
        List<ErpPurchaseOrderItem> orderItems = getFinishHistory();
        if(!CollectionUtils.isEmpty(orderItems)) {
            ret = orderItems.stream()
                    .mapToDouble(mapper)
                    .sum();
        }
        return ret;
    }
}

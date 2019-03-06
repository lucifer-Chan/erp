package com.yintong.erp.domain.purchase;

import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.common.Constants;
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
import org.springframework.util.StringUtils;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.V000;

/**
 * @author lucifer.chan
 * @create 2018-08-19 下午1:41
 * 采购订单
 **/
@Entity
@BarCode(prefix = V000)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpPurchaseOrder  extends BaseEntityWithBarCode {
    @Id @GeneratedValue
    private Long id;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '采购订单描述'")
    private String description;

    @Column(columnDefinition = "bigint(20) comment '供应商id'")
    private Long supplierId;

    @Column(columnDefinition = "varchar(40) comment '供应商名称-冗余数据，方便查询'")
    private String supplierName;

    @Column(columnDefinition = "double(20,5) comment '采购金额-计算值'")
    private Double money;

    @Column(columnDefinition = "varchar(20) comment '状态编码'")
    private String statusCode;

    @Column(columnDefinition = "date comment '订单日期'")
    private Date orderDate;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '备注'")
    private String remark;

    @Column(columnDefinition = "integer DEFAULT 0 comment '是否可入库[1-可以|0-不可以]'")
    private Integer preStockIn;

    @Column(columnDefinition = "integer DEFAULT 0 comment '是否可出库[1-可以|0-不可以]-针对退货'")
    private Integer preStockOut;

    @Transient
    private String statusName;

    /**
     * 订单明细-controller传入
     */
    @Transient
    private List<ErpPurchaseOrderItem> items;

    /**
     * 操作记录
     */
    @Transient
    private List<ErpPurchaseOrderOptLog> opts;

    public void setStatusCode(String statusCode){
        this.statusCode = statusCode;
    }

    public ErpPurchaseOrder setStatusCode(Constants.PurchaseOrderStatus statusCode){
        this.statusCode = statusCode.name();
        return this;
    }

    public String getStatusName(){
        if(StringUtils.hasText(statusName)) return statusName;
        if(StringUtils.isEmpty(statusCode)) return statusName = "";
        return statusName = Constants.PurchaseOrderStatus.valueOf(statusCode).description();
    }

    @Override
    protected void prePersist(){
        preCommit();
        setLastUpdatedAt(new Date());
    }

    @Override
    protected void preUpdate(){
        preCommit();
    }

    private void preCommit(){
        Assert.hasText(description, "订单描述不能为空");
        Assert.notNull(supplierId, "供应商不能为空");
        Assert.notNull(orderDate, "订单日期不能为空");
    }
}

package com.yintong.erp.domain.sale;

import com.yintong.erp.utils.bar.BarCode;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.X000;
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

/**
 * @author lucifer.chan
 * @create 2018-07-21 下午10:21
 * 销售订单
 **/
@Entity
@BarCode(prefix = X000)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpSaleOrder extends BaseEntityWithBarCode {
    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '销售订单描述'")
    private String description;

    @Column(columnDefinition = "bigint(20) comment '客户id'")
    private Long customerId;

    @Column(columnDefinition = "varchar(40) comment '客户名称-冗余数据，方便查询'")
    private String customerName;

    @Column(columnDefinition = "double(10,9) comment '销售金额-计算值'")
    private Double money;

    @Column(columnDefinition = "varchar(20) comment '状态编码'")
    private String statusCode;

    @Column(columnDefinition = "date comment '订单日期'")
    private Date orderDate;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '备注'")
    private String remark;

    @Column(columnDefinition = "integer DEFAULT 0 comment '是否可出库[1-可以|0-不可以]'")
    private Integer preStockOut;


    /**
     * 订单明细
     */
    @Transient
    private List<ErpSaleOrderItem> items;

    /**
     * 操作记录
     */
    @Transient
    private List<ErpSaleOrderOptLog> opts;

    public void setStatusCode(String statusCode){
        this.statusCode = statusCode;
    }

    public ErpSaleOrder setStatusCode(Constants.SaleOrderStatus statusCode){
        this.statusCode = statusCode.name();
        return this;
    }

    @Override
    protected void prePersist(){
        setLastUpdatedAt(new Date());
    }

}

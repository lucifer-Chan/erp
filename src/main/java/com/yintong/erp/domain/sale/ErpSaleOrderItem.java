package com.yintong.erp.domain.sale;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.utils.base.BaseEntity;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.SpringUtil;
import java.util.Date;
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
import org.springframework.util.StringUtils;

/**
 * @author lucifer.chan
 * @create 2018-07-21 下午10:43
 * 销售订单条目
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpSaleOrderItem extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '销售订单id'")
    private Long orderId;

    @Column(columnDefinition = "varchar(100) comment '销售订单编号'")
    private String orderCode;

    @Column(columnDefinition = "bigint(20) comment '成品id-模版id|供应商关联的id'")
    private Long productId;

    @Column(columnDefinition = "double(16,9) comment '总额'")
    private Double money;

    @Column(columnDefinition = "double(16,9) comment '数量'")
    private Double num;

    @Column(columnDefinition = "double(16,9) DEFAULT 0 comment '已出库数量'")
    private Double outedNum;

    @Column(columnDefinition = "double(16,9) DEFAULT 0 comment '已入库数量-退货'")
    private Double inNum;

    @Column(columnDefinition = "double(16,9) comment '单价'")
    private Double unitPrice;

    @Column(columnDefinition = "varchar(20) comment '状态编码'")
    private String statusCode;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '描述'")
    private String remark;

    @Transient
    private ErpBaseEndProduct product;

    @Transient
    private String productName;

    @Transient
    private Date orderOptDate;//订单操作时间-对应状态的最近一次操作时间

    public Double getOutedNum(){
        return Objects.isNull(outedNum) ? 0d : outedNum;
    }

    public Double getInNum(){
        return Objects.isNull(inNum) ? 0d : inNum;
    }

    public String getProductName(){
        if(StringUtils.hasText(productName)) return productName;
        ErpBaseEndProduct _product = getProduct();
        this.productName = Objects.isNull(_product) ? "" : _product.getDescription();
        return productName;
    }

    public ErpBaseEndProduct getProduct(){
        if(Objects.nonNull(product)) return product;
        if(Objects.nonNull(productId)){
            this.product = SpringUtil.getBean(ErpBaseEndProductRepository.class)
                    .findById(productId).orElse(null);
        }
        return product;
    }

    public void setStatusCode(String statusCode){
        this.statusCode = statusCode;
    }

    public ErpSaleOrderItem setStatusCode(Constants.SaleOrderStatus statusCode){
        this.statusCode = statusCode.name();
        return this;
    }

    public Double getMoney(){
        return Objects.isNull(money) ? 0.00d : money;
    }

    /**
     * 订单操作时间-对应状态的最近一次操作时间
     * @return
     */
    public Date getOrderOptDate(){
        if(Objects.nonNull(orderOptDate)) return orderOptDate;
        ErpSaleOrderOptLog orderOptLog =
                SpringUtil.getBean(ErpSaleOrderOptLogRepository.class).findByOrderIdAndStatusCodeOrderByCreatedAtDesc(orderId, statusCode)
                    .stream().findFirst().orElse(null);
        return orderOptDate = (Objects.isNull(orderOptLog) ? null : orderOptLog.getCreatedAt());
    }

    /**
     * 必填项校验
     */
    public void validateRequired(){
        Assert.notNull(getProductId(), "成品信息不能为空");
        Assert.notNull(getStatusCode(), "状态码不能为空");
        Assert.notNull(getMoney(), "总额不能为空");
        Assert.notNull(getNum(), "数量不能为空");
        Assert.notNull(getUnitPrice(), "单价不能为空");
        Assert.notNull(getOrderId(), "销售订单id不能为空");
        Assert.notNull(getOrderCode(), "销售订单编号不能为空");
    }
}

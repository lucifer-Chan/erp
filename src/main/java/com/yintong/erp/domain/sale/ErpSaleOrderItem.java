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

    @Column(columnDefinition = "bigint(20) comment '成品id'")
    private Long productId;

    @Column(columnDefinition = "double(10,2) comment '总额'")
    private Double money;

    @Column(columnDefinition = "double(10,2) comment '数量'")
    private Double num;

    @Column(columnDefinition = "double(10,2) comment '单价'")
    private Double unitPrice;

    @Column(columnDefinition = "varchar(20) comment '状态编码'")
    private String statusCode;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '描述'")
    private String remark;

    @Transient
    private String productName;

    public String getProductName(){
        if(StringUtils.hasText(productName)) return productName;
        if(Objects.nonNull(productId)){
            ErpBaseEndProduct product = SpringUtil.getBean(ErpBaseEndProductRepository.class)
                    .findById(productId).orElse(null);
            this.productName = Objects.isNull(product) ? "" : product.getEndProductName() + "-" + product.getSpecification();
        }

        return productName;
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
     * 订单操作时间
     * @return
     */
    @Transient
    public Date getOrderOptDate(){
        ErpSaleOrderOptLog orderOptLog =
                SpringUtil.getBean(ErpSaleOrderOptLogRepository.class).findByOrderIdAndStatusCode(orderId, statusCode)
                    .stream().findFirst().orElse(null);
        return Objects.isNull(orderOptLog) ? null : orderOptLog.getCreatedAt();
    }
}

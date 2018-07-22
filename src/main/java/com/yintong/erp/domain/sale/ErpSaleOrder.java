package com.yintong.erp.domain.sale;

import com.yintong.erp.service.basis.CustomerService;
import com.yintong.erp.service.basis.EmployeeService;
import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.SpringUtil;
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

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.X000;
import org.springframework.util.StringUtils;

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

    @Column(columnDefinition = "double(10,2) comment '销售金额-计算值'")
    private Double money;

    @Column(columnDefinition = "varchar(20) comment '状态编码'")
    private String statusCode;

    @Transient
    private String customerName;

    public void setStatusCode(String statusCode){
        this.statusCode = statusCode;
    }

    public ErpSaleOrder setStatusCode(Constants.SaleOrderStatus statusCode){
        this.statusCode = statusCode.name();
        return this;
    }

    /**
     * 获取客户姓名
     * @return
     */
    public String getCustomerName(){
        if(StringUtils.hasText(customerName)) return customerName;

        if(Objects.nonNull(customerId)){
            try {
                this.customerName = SpringUtil.getBean(CustomerService.class).one(customerId).getCustomerName();
            } catch (Exception ignored){

            }
        }
        return customerName;
    }

}

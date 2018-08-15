package com.yintong.erp.domain.stock;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.utils.base.BaseEntity;
import com.yintong.erp.utils.common.SpringUtil;
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
 * @create 2018-08-04 下午10:04
 * 库存操作记录-出入库
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpStockOptLog extends BaseEntity{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '仓位id'")
    private Long stockPlaceId;

    @Column(columnDefinition = "bigint(20) comment '成品id[当仓位为成品仓位时有值]'")
    private Long productId;

    @Column(columnDefinition = "varchar(100) comment '成品条码，可以为采购的成品'")
    private String productCode;

    @Column(columnDefinition = "double(10,9) comment '出入库数量'")
    private Double num;

    @Column(columnDefinition = "varchar(20) DEFAULT '' comment '出入库[IN|OUT]'")
    private String operation;

    @Column(columnDefinition = "varchar(20) DEFAULT '' comment '来源或目的[销售|退货|采购|生产]'")
    private String holder;

    @Column(columnDefinition = "bigint(20) comment '来源id：制令单id、销售订单id、采购单id'")
    private Long holderId;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '来源barcode，初始入库为：初始化'")
    private String holderBarCode;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '备注'")
    private String remark;

    @Transient
    private String productName;

    @Transient
    private String placeName;

    @Transient
    private ErpStockPlace place;

    public String getProductName(){
        if(StringUtils.hasText(productName)) return productName;
        if(Objects.nonNull(productId)){
            ErpBaseEndProduct product = SpringUtil.getBean(ErpBaseEndProductRepository.class).findById(productId).orElse(null);
            if(Objects.nonNull(product)){
                productName = product.getEndProductName() + "-" + product.getSpecification();
            }
        }
        return StringUtils.hasText(productName) ? productName : "";
    }


    public ErpStockPlace getPlace(){
        if(Objects.nonNull(place)) return place;
        if(Objects.nonNull(stockPlaceId)){
            this.place = SpringUtil.getBean(ErpStockPlaceRepository.class).findById(stockPlaceId).orElse(null);
        }
        return this.place;
    }

    public String getPlaceName(){
        ErpStockPlace _place = getPlace();
        return Objects.isNull(_place) ? "" : _place.getName();
    }
}

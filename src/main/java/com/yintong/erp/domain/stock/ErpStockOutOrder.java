package com.yintong.erp.domain.stock;

import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import java.util.List;
import java.util.stream.Collectors;
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
import org.springframework.util.StringUtils;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.O000;

/**
 * @author lucifer.chan
 * @create 2018-08-15 下午3:16
 * 出库单 要么全是原材料，要么全是成品
 **/
@Entity
@BarCode(prefix = O000)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpStockOutOrder extends BaseEntityWithBarCode implements StockPlaceFinder{
    @Id @GeneratedValue
    private Long id;

    @Column(columnDefinition = "varchar(20) DEFAULT '' comment '来源或目的[销售|生产]'")
    private String holder;

    @Column(columnDefinition = "bigint(20) comment '制令单id、销售订单id'")
    private Long holderId;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '来源barcode'")
    private String holderBarCode;

    @Column(columnDefinition = "varchar(100) comment '出库成品id列表,英文逗号隔开'")
    private String productIds;

    @Column(columnDefinition = "varchar(1000) comment '出库成品名称列表,英文逗号隔开'")
    private String productNames;

    @Column(columnDefinition = "varchar(100) comment '出库实际原材料id列表'")
    private String materialIds;

    @Column(columnDefinition = "varchar(1000) comment '出库实际原材料名称列表,英文逗号隔开'")
    private String materialNames;

    @Transient
    private String placeNames;//仓位列表，由于仓位和物料的弱关联，所以不入数据库

    /**
     * 获取仓位名称，逗号隔开 - 出库时便于查找
     * @return
     */
    public String getPlaceNames(){
        if(StringUtils.hasText(placeNames)) return placeNames;

        List<ErpStockPlace> places = this.getPlaces(productIds, materialIds);

        return this.placeNames = places.isEmpty() ? "" :
                StringUtils.collectionToCommaDelimitedString(
                        places.stream().map(ErpStockPlace::getName).collect(Collectors.toList())
                );
    }

//    public String getPlaceNames(){
//        if(StringUtils.hasText(placeNames)) return placeNames;
//
//        if(StringUtils.isEmpty(productIds) && StringUtils.isEmpty(materialIds)){
//            return this.placeNames = "";
//        }
//        //成品出库单
//        if(StringUtils.hasText(productIds)){
//            List<Long> _productIds = Arrays.stream(productIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
//            //仓位id列表
//            List<Long> placeIds = SpringUtil.getBean(ErpStockOptLogRepository.class)
//                    .findByProductIdIn(_productIds).stream().map(ErpStockOptLog::getStockPlaceId).collect(Collectors.toList());
//            return this.placeNames = CollectionUtils.isEmpty(placeIds) ? "" :
//                    StringUtils.collectionToCommaDelimitedString(
//                            SpringUtil.getBean(ErpStockPlaceRepository.class).findByIdIn(placeIds).stream()
//                                    .map(ErpStockPlace::getName)
//                                    .collect(Collectors.toList())
//                    );
//        }
//        //原材料出库单
//        if(StringUtils.hasText(materialIds)){
//            List<Long> _materialIds = Arrays.stream(materialIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
//
//            return this.placeNames = StringUtils.collectionToCommaDelimitedString(
//                    SpringUtil.getBean(ErpStockPlaceRepository.class).findByMaterialSupplierAssIdIn(_materialIds).stream()
//                            .map(ErpStockPlace::getName)
//                            .collect(Collectors.toList())
//            );
//        }
//        return this.placeNames = "";
//    }
}

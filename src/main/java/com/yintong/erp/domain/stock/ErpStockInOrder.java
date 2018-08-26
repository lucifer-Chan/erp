package com.yintong.erp.domain.stock;

import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
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
import org.springframework.util.CollectionUtils;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.I000;

/**
 * @author lucifer.chan
 * @create 2018-08-22 下午11:52
 * 入库单
 **/
@Entity
@BarCode(prefix = I000)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpStockInOrder extends BaseEntityWithBarCode implements StockPlaceFinder{
    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "varchar(20) DEFAULT '' comment '来源或目的[销售|生产]'")
    private String holder;

    @Column(columnDefinition = "bigint(20) comment '制令单id、采购订单id、销售订单'")
    private Long holderId;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '来源barcode'")
    private String holderBarCode;

    @Column(columnDefinition = "varchar(100) comment '入库成品id列表,英文逗号隔开'")
    private String productIds;

    @Column(columnDefinition = "varchar(1000) comment '入库成品名称列表,英文逗号隔开'")
    private String productNames;

    @Column(columnDefinition = "varchar(100) comment '入库实际原材料id列表,英文逗号隔开'")
    private String materialIds;

    @Column(columnDefinition = "varchar(1000) comment '入库实际原材料名称列表,英文逗号隔开'")
    private String materialNames;

    @Column(columnDefinition = "varchar(100) comment '入库实际模具id列表,英文逗号隔开'")
    private String mouldIds;

    @Column(columnDefinition = "varchar(1000) comment '入库实际模具名称列表,英文逗号隔开'")
    private String mouldNames;

    @Transient
    private List<ErpStockPlace> referencePlaces;

    /**
     * 获取参考仓位
     * @return
     */
    public List<ErpStockPlace> getReferencePlaces(){
        if(! CollectionUtils.isEmpty(referencePlaces)) return referencePlaces;
        return this.referencePlaces = getPlaces(productIds, materialIds);
    }
}
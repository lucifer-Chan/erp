package com.yintong.erp.domain.stock;

import com.yintong.erp.service.basis.associator.SupplierRawMaterialService;
import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.bar.BarCodeIndex;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.SpringUtil;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.S000;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author lucifer.chan
 * @create 2018-08-04 下午5:33
 * 仓位
 **/
@Entity
@BarCode(prefix = S000)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpStockPlace extends BaseEntityWithBarCode {

    @Id
    @GeneratedValue
    private Long id;

    @BarCodeIndex(holder = true, value = 1)// 1位
    @Column(columnDefinition = "varchar(10) comment '仓位类型 [M-原材料|P-成品]'")
    private String stockPlaceType;

    @Column(columnDefinition = "varchar(100) comment '仓位名称'")
    private String name;

    @BarCodeIndex(value = 2, nullable = true)
    @Column(columnDefinition = "bigint(20) comment '关联供应商之后的原材料id，当stock_place_type为M时有效'")
    private Long materialSupplierAssId;

    @Column(columnDefinition = "varchar(40) comment '可存物料名称，当stock_place_type为M时为原材料明细，否则为\"成品\"'")
    private String materialName;

    @Column(columnDefinition = "varchar(20) DEFAULT 'ON' comment '状态编码[STOP-停役|ON-在役]，停役之后不许再入库'")
    private String statusCode;

    @Column(columnDefinition = "integer DEFAULT 0 comment '库存上限'")
    private Integer upperLimit;

    @Column(columnDefinition = "double(16,9) DEFAULT 0 comment '当前存量'")
    private Double currentStorageNum;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '仓位描述'")
    private String description;

    public Double getCurrentStorageNum(){
        return Objects.isNull(currentStorageNum) ? 0d : currentStorageNum;
    }


    protected void prePersist(){
        onPreCommit();
    }

    protected void preUpdate(){
        onPreCommit();
    }

    /**
     * 存库前的验证
     */
    private void onPreCommit(){
        Assert.hasText(name, "仓位名称不能为空");
        Assert.isTrue(Objects.isNull(upperLimit) || upperLimit > 0 , "库存上限不能小于0");
        Assert.isTrue(Objects.isNull(currentStorageNum) || currentStorageNum > 0, "当前存量不能小于0");

        if(Constants.StockPlaceType.P.name().equals(stockPlaceType)){
            this.setMaterialSupplierAssId(null);
            this.setMaterialName("成品");
        }

        if(Constants.StockPlaceType.M.name().equals(stockPlaceType)){
            Assert.notNull(materialSupplierAssId, "原材料仓位需要选择原材料");
            if(StringUtils.isEmpty(materialName)){
                materialName = SpringUtil.getBean(SupplierRawMaterialService.class).description(materialSupplierAssId);
            }
        }

        try{
            Constants.StockPlaceType.valueOf(stockPlaceType);
            Constants.StockPlaceStatus.valueOf(statusCode);
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException("仓位类型或仓位状态不能为空");
        } catch (NullPointerException e){
            throw new IllegalArgumentException("仓位类型或仓位状态不正确");
        }
    }

}

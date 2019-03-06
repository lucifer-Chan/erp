package com.yintong.erp.domain.purchase;

import com.yintong.erp.domain.basis.TemplateWares;
import com.yintong.erp.domain.stock.ErpStockPlace;
import com.yintong.erp.domain.stock.StockPlaceFinder;
import com.yintong.erp.service.basis.CommonService;
import com.yintong.erp.utils.base.BaseEntity;
import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.SpringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import net.sf.json.JSONObject;
import org.springframework.util.Assert;

/**
 * @author lucifer.chan
 * @create 2018-08-19 下午1:41
 * 采购单明细
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpPurchaseOrderItem  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '采购订单id'")
    private Long orderId;

    @Column(columnDefinition = "varchar(100) comment '采购订单编号'")
    private String orderCode;

    @Column(columnDefinition = "bigint(20) comment '货物id-模版'")
    private Long waresId;

    @Column(columnDefinition = "bigint(20) comment '货物id-关联id'")
    private Long waresAssId;

    @Column(columnDefinition = "varchar(20) DEFAULT '' comment '货物类型-原材料|成品|模具->M|P|D'")
    private String waresType;

    @Column(columnDefinition = "varchar(200) DEFAULT '' comment '货物名称'")
    private String waresName;

    @Column(columnDefinition = "double(20,5) comment '总额'")
    private Double money;

    @Column(columnDefinition = "double(20,5) comment '数量'")
    private Double num;


    @Column(columnDefinition = "double(20,5) DEFAULT 0 comment '已入库数量'")
    private Double inNum;

    @Column(columnDefinition = "double(20,5) comment '需要退货的数量'")
    private Double shouldRtNum;

    @Column(columnDefinition = "double(20,5) comment '退款'")
    private Double rtMoney;

    @Column(columnDefinition = "double(20,5) comment '已出库的数量（退货）'")
    private Double outNum;

    @Column(columnDefinition = "double(20,5) comment '单价'")
    private Double unitPrice;

    @Column(columnDefinition = "varchar(20) comment '状态编码'")
    private String statusCode;

    @Column(columnDefinition = "varchar(5) comment '单位：kg|只'")
    private String unit;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '描述'")
    private String remark;

    @Transient
    private JSONObject wares;

    public Double getInNum(){
        return Objects.isNull(inNum) ? 0D : inNum;

    }

    public Double getOutNum(){
        return CommonUtil.ifNotPresent(outNum, 0d);
    }

//
//    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '货物名称-实际名称-冗余数据-打印用'")
//    private String simpleName;
//
//    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '货物规格-冗余数据-打印用'")
//    private String specification;
//
//    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '货物类别-冗余数据-打印用'")
//    private String category;

    /**
     * 必填项校验
     */
    public ErpPurchaseOrderItem validateRequired(){
        Assert.notNull(waresAssId, "货物名称不能为空");
        Assert.hasText(waresName, "货物名称不能为空");
        Assert.hasText(waresType, "货物类型不能为空");
        Assert.hasText(unit, "单位不能为空");
        Assert.notNull(getStatusCode(), "状态码不能为空");
        Assert.notNull(getMoney(), "总额不能为空");
        Assert.notNull(getNum(), "数量不能为空");
        Assert.notNull(getUnitPrice(), "单价不能为空");
        Assert.notNull(getOrderId(), "采购订单id不能为空");
        Assert.notNull(getOrderCode(), "采购订单编号不能为空");
        return this;
    }

    public JSONObject getWares(){
        if(Objects.nonNull(wares)) return wares;
        if(null == waresType) return wares = null;
        Function<Long, TemplateWares> function = SpringUtil.getBean(CommonService.class).findWaresById().get(Constants.WaresType.valueOf(waresType));
        TemplateWares templateWares = function.apply(waresId);
        wares = (Objects.isNull(templateWares) ? new JSONObject() : templateWares.getTemplate());

        if(Constants.WaresType.M.name().equals(waresType)){
            wares.put("places", CommonUtil.defaultIfEmpty(StockPlaceFinder.findPlaces(null, String.valueOf(waresAssId)).stream().map(ErpStockPlace::getPlaceCode).collect(Collectors.joining(",")), "无"));
        }
        if(Constants.WaresType.D.name().equals(waresType)){
            wares.put("places", CommonUtil.defaultIfEmpty(StockPlaceFinder.findMouldPlaces(String.valueOf(waresAssId).concat(",").concat(String.valueOf(waresId))).stream().map(ErpStockPlace::getPlaceCode).collect(Collectors.joining(",")), "无"));
        }
        return wares;
    }

    public TemplateWares templateWares(){
        Function<Long, TemplateWares> function = SpringUtil.getBean(CommonService.class).findWaresById().get(Constants.WaresType.valueOf(waresType));
        return function.apply(waresId);
    }

    /**
     * 从采购订单里复制信息
     * @param order
     * @return
     */
    public ErpPurchaseOrderItem copy(ErpPurchaseOrder order){
        setStatusCode(order.getStatusCode());
        setOrderId(order.getId());
        setOrderCode(order.getBarCode());
        return this;
    }

    @Override
    protected void prePersist(){
        preCommit();
    }

    @Override
    protected void preUpdate(){
        preCommit();
    }

    private void preCommit(){
        if(waresName.startsWith("【")) return;
        waresName = "【" + Constants.WaresType.valueOf(waresType).description() + "】 " + waresName;
    }
}

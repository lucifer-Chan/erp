package com.yintong.erp.mini.service;

import com.yintong.erp.domain.basis.associator.ErpModelSupplier;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplier;
import com.yintong.erp.domain.prod.ErpProdMould;
import com.yintong.erp.domain.prod.ErpProdOrder;
import com.yintong.erp.domain.prod.ErpProdProductBom;
import com.yintong.erp.domain.purchase.ErpPurchaseOrder;
import com.yintong.erp.domain.purchase.ErpPurchaseOrderItem;
import com.yintong.erp.domain.sale.ErpSaleOrder;
import com.yintong.erp.domain.sale.ErpSaleOrderItem;
import com.yintong.erp.domain.stock.ErpStockPlace;
import com.yintong.erp.domain.stock.StockEntity;
import com.yintong.erp.domain.stock.StockPlaceFinder;
import com.yintong.erp.service.basis.CommonService;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.base.JsonWrapper;
import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.utils.common.SpringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.sf.json.JSONObject;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author lucifer.chan
 * @create 2018-09-20 下午2:10
 * 制造小程序使用的dto
 **/
public class MiniDtoWrapper {

    /**
     * 包装货物
     * @param stockEntity
     * @return
     */
    public static JSONObject buildWares(StockEntity stockEntity){
        JSONObject ret = JsonWrapper.builder()
                .add("template", stockEntity.template().getTemplate())
                .add("type", stockEntity.waresType().description())
                .add("entity", stockEntity.toJSONObject(false))
            .build();
        if(Objects.nonNull(stockEntity.realityId())){
            String realityBarCode = SpringUtil.getBean(CommonService.class).findRealityWaresBarCode(stockEntity.waresType().name(), stockEntity.realityId());
            if(StringUtils.hasText(realityBarCode)){
                ret.put("barCode", realityBarCode);
            }
        }

        return ret;
    }

    /**
     * 包装订单
     * @param entity
     * @return {
     *     id, barCode, name, createdAt items : [{type, name, supplier(可为空), }]
     * }
     */
    public static JSONObject buildOrder(BaseEntityWithBarCode entity, Constants.StockOpt stockOpt){
        if(entity instanceof ErpPurchaseOrder){
            return buildOrder((ErpPurchaseOrder)entity, stockOpt);
        }

        if(entity instanceof ErpSaleOrder){
            return buildOrder((ErpSaleOrder)entity);
        }

        if(entity instanceof ErpProdOrder){
            return buildOrder((ErpProdOrder)entity);
        }
        throw new IllegalArgumentException("订单有误[" + entity.getClass().getSimpleName() + "]");
    }

    /**
     * 采购单
     * @param order
     * @return
     */
    private static JSONObject buildOrder(ErpPurchaseOrder order, Constants.StockOpt stockOpt){
        Assert.notNull(stockOpt, "操作类型不能为空");
        List<JSONObject> items = CommonUtil.ifNotPresent(order.getItems(), new ArrayList<ErpPurchaseOrderItem>())
                .stream()
                .map(item -> {
                    JSONObject json = item.getWares();
                    json.put("unit", item.getUnit());
                    json.put("type", item.getWaresType());
                    json.put("total", stockOpt == Constants.StockOpt.IN ? item.getNum() : item.getShouldRtNum());
                    json.put("in", item.getInNum());
                    //需要出库的数量
                    json.put("shouldOut", item.getShouldRtNum());
                    //已经出库的数量
                    json.put("out", item.getOutNum());
                    json.put("strict", false);
                    String realityBarCode = SpringUtil.getBean(CommonService.class).findRealityWaresBarCode(item.getWaresType(), item.getWaresAssId());
                    if(StringUtils.hasText(realityBarCode)){
                        json.put("barCode", realityBarCode);
                        json.put("strict", true);//严格barCode，不允许使用模版barCode
                    }
                    return json;
                })
                .collect(Collectors.toList());
        return JsonWrapper.builder()
                    .add("id", order.getId())
                    .add("barCode", order.getBarCode())
                    .add("name", order.getDescription())
                    .add("creator", order.getCreatedName())
                    .add("time", DateUtil.getDateString(order.getOrderDate()))
                    .add("supplier", order.getSupplierName())
                    .add("items", items)
                .build();
    }

    /**
     * 销售单 成品
     * @param order
     * @return
     * default JSONObject getTemplate(){
        return JsonWrapper.builder()
            .add("name",getDescription())
            .add("waresId", getWaresId())
            .add("barCode", getBarCode())
            .add("unit", getUnit())
            .add("simpleName", getSimpleName())
            .add("specification", getSpecification())
            .add("category", BarCodeConstants.BAR_CODE_PREFIX.valueOf(getCategoryCode()).description())
        .build();
        }
     */
    private static JSONObject buildOrder(ErpSaleOrder order){
        List<JSONObject> items = CommonUtil.ifNotPresent(order.getItems(), new ArrayList<ErpSaleOrderItem>())
                .stream()
                .map(item -> {
                    JSONObject json = item.getProduct().getTemplate();
                    json.put("type", "P");
                    json.put("total", item.getNum());
                    json.put("in", item.getInNum());
                    json.put("out", item.getOutedNum());
                    return json;
                })
                .collect(Collectors.toList());

        return JsonWrapper.builder()
                    .add("id", order.getId())
                    .add("barCode", order.getBarCode())
                    .add("name", order.getDescription())
                    .add("creator", order.getCreatedName())
                    .add("time", DateUtil.getDateString(order.getOrderDate()))
                    .add("customer", order.getCustomerName())
                    .add("items", items)
                .build();
    }

    /**
     * 制令单
     * @param order
     * @return
     */
    private static JSONObject buildOrder(ErpProdOrder order){
        List<JSONObject> boms = CommonUtil.ifNotPresent(order.getBoms(), new ArrayList<ErpProdProductBom>())
                .stream()
                .map(bom -> {
                    JSONObject json = bom.getMaterial().getTemplate();
                    json.put("type", "M");
                    json.put("total", bom.getRealityMaterialNum());
                    json.put("in", bom.getNumIn());
                    json.put("out", bom.getNumOut());
                    //这意义不大，原材料必然是关联后的
                    ErpRawMaterialSupplier reality = bom.getRealityMaterial();
                    if(Objects.nonNull(reality)){
                        List<ErpStockPlace> places = StockPlaceFinder.findPlaces(null, String.valueOf(reality.getId()));
                        String placeCodes = CommonUtil.defaultIfEmpty(places.stream().map(ErpStockPlace::getPlaceCode).collect(Collectors.joining(",")), "无");
                        json.put("barCode", reality.getBarCode());
                        json.put("strict", true);//严格barCode，不允许使用模版barCode
                        json.put("places", placeCodes);
                    }
                    return json;
                })
                .collect(Collectors.toList());

        List<JSONObject> moulds = CommonUtil.ifNotPresent(order.getMoulds(), new ArrayList<ErpProdMould>())
                .stream()
                .map(mould -> {
                    JSONObject json = mould.getMould().getTemplate();
                    json.put("type", "D");
                    json.put("total", mould.getRealityMouldNum());
                    json.put("in", mould.getNumIn());
                    json.put("out", mould.getNumOut());
                    json.put("strict", false);

                    List<ErpStockPlace> places = StockPlaceFinder.findMouldPlaces(String.valueOf(mould.getMould().getId()));

                    ErpModelSupplier reality = mould.getRealityMould();
                    if(Objects.nonNull(reality)){
                        places.addAll(StockPlaceFinder.findMouldPlaces(String.valueOf(reality.getId())));
                        json.put("barCode", reality.getBarCode());
                        json.put("strict", true);//严格barCode，不允许使用模版barCode
                    }

                    json.put("places", CommonUtil.defaultIfEmpty(places.stream().map(ErpStockPlace::getPlaceCode).distinct().collect(Collectors.joining(",")), "无"));
                    return json;
                })
                .collect(Collectors.toList());
        boms.addAll(moulds);

        JSONObject product = order.getProduct().getTemplate();
        product.put("total", order.getProdNum());
        product.put("in", order.getFinishNum());

        return JsonWrapper.builder()
                    .add("id", order.getId())
                    .add("barCode", order.getBarCode())
                    .add("name", order.getDescription())
                    .add("creator", order.getCreatedName())
                    .add("time", DateUtil.getDateString(order.getStartDate()))
                    .add("product", product)
                    .add("items", boms)
                .build();
    }

}

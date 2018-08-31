package com.yintong.erp.service.stock;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.domain.basis.ErpBaseModelTool;
import com.yintong.erp.domain.basis.ErpBaseModelToolRepository;
import com.yintong.erp.domain.basis.associator.ErpEndProductSupplier;
import com.yintong.erp.domain.basis.associator.ErpEndProductSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpModelSupplier;
import com.yintong.erp.domain.basis.associator.ErpModelSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplier;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplierRepository;
import com.yintong.erp.domain.stock.ErpStockOptLog;
import com.yintong.erp.domain.stock.ErpStockOptLogRepository;
import com.yintong.erp.domain.stock.ErpStockPlace;
import com.yintong.erp.domain.stock.ErpStockPlaceRepository;
import com.yintong.erp.domain.stock.StockEntity;
import com.yintong.erp.service.sale.SaleOrderService;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import static com.yintong.erp.utils.common.Constants.*;
import static com.yintong.erp.utils.common.Constants.StockHolder.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author lucifer.chan
 * @create 2018-08-13 下午1:05
 * 库存管理
 **/
@Service
public class StockOptService {

    @Autowired ErpStockOptLogRepository stockOptLogRepository;

    @Autowired ErpStockPlaceRepository stockPlaceRepository;

    @Autowired ErpBaseEndProductRepository productRepository;

    @Autowired ErpEndProductSupplierRepository productSupplierRepository;

    @Autowired ErpBaseModelToolRepository mouldRepository;

    @Autowired ErpModelSupplierRepository mouldSupplierRepository;

    @Autowired ErpRawMaterialSupplierRepository materialSupplierRepository;

    @Autowired SaleOrderService saleOrderService;

    @Autowired(required = false) List<StockIn4Holder> stockIn4Holders;

    @Autowired(required = false) List<StockOut4Holder> stockOut4Holders;


    /**
     * 货物入库 : 仓位表、成品[原材料|模具]表、库存操作日志表
     * @param stockPlaceId - 仓位id
     * @param stockEntity - 货物实体
     * @param holder - 来源
     * @param holderId  - 来源id ：holder为INIT时，holderId为null
     * @param holderBarCode 来源条形码 ：holder为INIT时，holderBarCode为null
     * @param num - 数量 > 0
     * @return 仓位
     */
    @Transactional
    public ErpStockPlace stockIn(Long stockPlaceId, StockEntity stockEntity,
                                 StockHolder holder, Long holderId, String holderBarCode,
                                 double num){
        Assert.notNull(stockPlaceId, "请选择仓位");
        Assert.notNull(holder, "来源不能为空");
        stockEntity.stockValidate();
        Assert.isTrue(num > 0, "入库数量需大于0");
        if(holder != INIT){
            Assert.isTrue(Objects.nonNull(holderId) && StringUtils.hasText(holderBarCode), holder.description() + "不能为空");
        } else {
            holderId = null;
            holderBarCode = "初始化";
        }
        //1- stockPlace +
        ErpStockPlace place = stockPlaceRepository.findById(stockPlaceId).orElse(null);
        Assert.notNull(place, "未找到仓位");

        WaresType waresType = stockEntity.waresType();
        Long waresId = stockEntity.templateId();
        String waresBarcode = stockEntity.entity().getBarCode();
        BaseEntityWithBarCode entity = stockEntity.entity();

        //1.1 计算仓位空闲
        double remain = place.getUpperLimit() - place.getCurrentStorageNum();
        Assert.isTrue((remain - num) > 0, place.getName() + "剩余空间为" + remain + ",无法入库数量为" + num + "的" + waresType.description());
        place.setCurrentStorageNum(place.getCurrentStorageNum() + num);
        stockPlaceRepository.save(place);

        //2- stockOptLog
        ErpStockOptLog optLog = ErpStockOptLog.builder()
                .stockPlaceId(stockPlaceId)
                .num(num)
                .operation(StockOpt.IN.name())
                .holder(holder.name())
                .holderId(holderId)
                .holderBarCode(holderBarCode)
                .build();
        if(entity instanceof ErpBaseEndProduct){
            //a - 自产成品
            optLog.setProductId(waresId);
            optLog.setProductCode(waresBarcode);
            productRepository.save( ((ErpBaseEndProduct) entity).stockIn(num));
        } else if (entity instanceof ErpEndProductSupplier){
            //b - 供应商成品
            optLog.setProductId(waresId);
            optLog.setProductCode(waresBarcode);
            productSupplierRepository.save(((ErpEndProductSupplier) entity).stockIn(num));
        } else if (entity instanceof ErpBaseModelTool){
            //c - 不区分供应商的模具
            optLog.setMouldId(waresId);
            optLog.setMouldCode(waresBarcode);
            mouldRepository.save(((ErpBaseModelTool) entity).stockIn(num));
        } else if (entity instanceof ErpModelSupplier){
            //d - 供应商的模具
            optLog.setMouldId(waresId);
            optLog.setMouldCode(waresBarcode);
            mouldSupplierRepository.save(((ErpModelSupplier) entity).stockIn(num));
        } else if (entity instanceof ErpRawMaterialSupplier){
            //e - 原材料
            materialSupplierRepository.save(((ErpRawMaterialSupplier) entity).stockIn(num));
        }

        //3- 对应的单据
        final Long _holderId = holderId;
        if(holder != INIT && !CollectionUtils.isEmpty(stockIn4Holders)){
            stockIn4Holders.forEach(service-> service.handleIn(holder, _holderId, stockEntity, num));
        }
        return place;
    }

    /**
     * 货物出库 : 仓位表、成品[原材料|模具]表、库存操作日志表
     * @param stockPlaceId - 仓位id
     * @param stockEntity - 货物实体
     * @param holder - 订单类型
     * @param holderId - 订单id
     * @param holderBarCode - 订单barcode
     * @param num - 数量 > 0
     * @return 仓位
     */
    @Transactional
    public ErpStockPlace stockOut(Long stockPlaceId, StockEntity stockEntity,
                                  StockHolder holder, Long holderId, String holderBarCode, double num){
        Assert.notNull(stockPlaceId, "请选择仓位");
        Assert.isTrue(Objects.nonNull(holder) && Objects.nonNull(holderId) && StringUtils.hasText(holderBarCode), "订单不能为空");
        stockEntity.stockValidate();
        Assert.isTrue(num > 0, "出库数量需大于0");

        //1- stockPlace -
        ErpStockPlace place = stockPlaceRepository.findById(stockPlaceId).orElse(null);
        Assert.notNull(place, "未找到仓位");

        WaresType waresType = stockEntity.waresType();
        Long waresId = stockEntity.templateId();
        String waresBarcode = stockEntity.entity().getBarCode();
        BaseEntityWithBarCode entity = stockEntity.entity();
        //1.1 计算仓位余量
        Assert.isTrue(num < place.getCurrentStorageNum(), place.getName() + "剩余库存为" + place.getCurrentStorageNum() + ",无法出库数量为" + num + "的" + waresType.description());
        place.setCurrentStorageNum(place.getCurrentStorageNum() - num);
        stockPlaceRepository.save(place);
        //2- stockOptLog
        ErpStockOptLog optLog = ErpStockOptLog.builder()
                .stockPlaceId(stockPlaceId)
                .num(num)
                .operation(StockOpt.OUT.name())
                .holder(holder.name())
                .holderId(holderId)
                .holderBarCode(holderBarCode)
            .build();
        if(entity instanceof ErpBaseEndProduct){
            //a - 自产成品
            optLog.setProductId(waresId);
            optLog.setProductCode(waresBarcode);
            productRepository.save( ((ErpBaseEndProduct) entity).stockOut(num));
        }  else if (entity instanceof ErpEndProductSupplier){
            //b - 供应商成品
            optLog.setProductId(waresId);
            optLog.setProductCode(waresBarcode);
            productSupplierRepository.save(((ErpEndProductSupplier) entity).stockOut(num));
        } else if (entity instanceof ErpBaseModelTool){
            //c - 不区分供应商的模具
            optLog.setMouldId(waresId);
            optLog.setMouldCode(waresBarcode);
            mouldRepository.save(((ErpBaseModelTool) entity).stockOut(num));
        } else if (entity instanceof ErpModelSupplier){
            //d - 供应商的模具
            optLog.setMouldId(waresId);
            optLog.setMouldCode(waresBarcode);
            mouldSupplierRepository.save(((ErpModelSupplier) entity).stockOut(num));
        } else if (entity instanceof ErpRawMaterialSupplier){
            //e - 原材料
            materialSupplierRepository.save(((ErpRawMaterialSupplier) entity).stockOut(num));
        }
        //3- 对应的单据
        if(!CollectionUtils.isEmpty(stockOut4Holders)){
            stockOut4Holders.forEach(service-> service.handleOut(holder, holderId, stockEntity, num));
        }
        return place;

    }

    /**
     * 查找成品的出入库记录
     * @param productId
     * @return
     */
    public List<ErpStockOptLog> findOptsByProductId(Long productId) {
        return stockOptLogRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }
}

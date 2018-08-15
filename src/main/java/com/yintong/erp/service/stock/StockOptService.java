package com.yintong.erp.service.stock;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.domain.stock.ErpStockOptLog;
import com.yintong.erp.domain.stock.ErpStockOptLogRepository;
import com.yintong.erp.domain.stock.ErpStockPlace;
import com.yintong.erp.domain.stock.ErpStockPlaceRepository;
import com.yintong.erp.service.sale.SaleOrderService;
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

    public static final String KEY_PREFIX = StockOptService.class.getName();

    @Autowired ErpStockOptLogRepository stockOptLogRepository;

    @Autowired ErpStockPlaceRepository stockPlaceRepository;

    @Autowired ErpBaseEndProductRepository productRepository;

    @Autowired SaleOrderService saleOrderService;//stockOuting TODO 销售出库

    @Autowired(required = false) List<StockInProduct4Holder> stockInProduct4Holders;

    @Autowired(required = false) List<StockOutProduct4Holder> stockOutProduct4Holders;


    /**
     * 成品入库
     * @param stockPlaceId - 仓位id
     * @param productId - 成品id
     * @param productCode - 成品条码
     * @param holder - 来源
     * @param holderId - 来源id ：holder为INIT时，holderId为null
     * @param holderBarCode 来源条码 ：holder为INIT时，holderBarCode为null
     * @param num - 数量 > 0
     * @return 仓位
     */
    @Transactional
    public ErpStockPlace stockInProduct(Long stockPlaceId, Long productId, String productCode, StockHolder holder, Long holderId, String holderBarCode, double num){
        Assert.notNull(stockPlaceId, "请选择仓位");
        Assert.isTrue(Objects.nonNull(productId) && StringUtils.hasText(productCode), "请选择成品");
        Assert.notNull(holder, "来源不能为空");
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
        //1.1 计算仓位空闲
        double remain = place.getUpperLimit() - place.getCurrentStorageNum();
        Assert.isTrue((remain - num) > 0, place.getName() + "剩余空间为" + remain + ",无法入库数量为" + num + "的成品");
        place.setCurrentStorageNum(place.getCurrentStorageNum() + num);
        stockPlaceRepository.save(place);
        //2- stockOptLog
        ErpStockOptLog optLog = ErpStockOptLog.builder()
                .stockPlaceId(stockPlaceId)
                .productId(productId)
                .productCode(productCode)
                .num(num)
                .operation(StockOpt.IN.name())
                .holder(holder.name())
                .holderId(holderId)
                .holderBarCode(holderBarCode)
                .build();
        stockOptLogRepository.save(optLog);
        //3- product num +
        ErpBaseEndProduct product = productRepository.findById(productId).orElse(null);
        Assert.notNull(product, "未找到成品");
        product.setTotalNum(product.getTotalNum() + num);
        productRepository.save(product);
        //4- 对应的单据
        final Long _holderId = holderId;
        if(holder != INIT && !CollectionUtils.isEmpty(stockInProduct4Holders)){
            stockInProduct4Holders.forEach(service-> service.handle(holder, _holderId, productId, num));
        }

        return place;
    }

    /**
     * 成品出库
     * @param stockPlaceId - 仓位id
     * @param productId - 成品id
     * @param productCode - 成品条码
     * @param holder - 目的
     * @param holderId - 目的id
     * @param holderBarCode 目的条码
     * @param num - 数量 > 0
     * @return 仓位
     */
    @Transactional
    public ErpStockPlace stockOutProduct(Long stockPlaceId, Long productId, String productCode, StockHolder holder, Long holderId, String holderBarCode, double num){

        Assert.notNull(stockPlaceId, "请选择仓位");
        Assert.isTrue(Objects.nonNull(productId) && StringUtils.hasText(productCode), "请选择成品");
        Assert.isTrue(Objects.nonNull(holder) && Objects.nonNull(holderId) && Objects.nonNull(holderBarCode), "来源不能为空");
        Assert.isTrue(num > 0, "出库数量需大于0");
        //1- stockPlace -
        ErpStockPlace place = stockPlaceRepository.findById(stockPlaceId).orElse(null);
        Assert.notNull(place, "未找到仓位");
        //1.1 计算仓位余量
        Assert.isTrue(num < place.getCurrentStorageNum(), place.getName() + "剩余库存为" + place.getCurrentStorageNum() + ",无法出库数量为" + num + "的成品");
        place.setCurrentStorageNum(place.getCurrentStorageNum() - num);
        stockPlaceRepository.save(place);
        //2- stockOptLog
        ErpStockOptLog optLog = ErpStockOptLog.builder()
                .stockPlaceId(stockPlaceId)
                .productId(productId)
                .productCode(productCode)
                .num(num)
                .operation(StockOpt.OUT.name())
                .holder(holder.name())
                .holderId(holderId)
                .holderBarCode(holderBarCode)
                .build();
        stockOptLogRepository.save(optLog);
        //3- product num -
        ErpBaseEndProduct product = productRepository.findById(productId).orElse(null);
        Assert.notNull(product, "未找到成品");
        product.setTotalNum(product.getTotalNum() - num);
        productRepository.save(product);
        //4- 对应的单据
        if(!CollectionUtils.isEmpty(stockOutProduct4Holders)){
            stockOutProduct4Holders.forEach(service-> service.handle(holder, holderId, productId, num));
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

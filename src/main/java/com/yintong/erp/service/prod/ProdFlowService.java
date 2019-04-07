package com.yintong.erp.service.prod;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.security.ErpEmployee;
import com.yintong.erp.domain.prod.ErpProdHalfFlowRecord;
import com.yintong.erp.domain.prod.ErpProdHalfFlowRecordRepository;
import com.yintong.erp.domain.prod.ErpProdOrder;
import com.yintong.erp.domain.prod.ErpProdOrderOptLog;
import com.yintong.erp.domain.prod.ErpProdOrderOptLogRepository;
import com.yintong.erp.domain.prod.ErpProdOrderRepository;
import com.yintong.erp.domain.stock.StockEntity;
import com.yintong.erp.service.stock.StockIn4Holder;
import com.yintong.erp.utils.common.Assert;
import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.utils.common.SessionUtil;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.yintong.erp.utils.common.CommonUtil.ifNotPresent;
import static com.yintong.erp.utils.common.Constants.ProdFlowStage.PROD_STAGE_1;
import static com.yintong.erp.utils.common.Constants.ProdFlowStage.PROD_STAGE_2;
import static com.yintong.erp.utils.common.Constants.ProdFlowStage.PROD_STAGE_3;
import static com.yintong.erp.utils.common.Constants.ProdFlowStage.PROD_STAGE_4;
import static com.yintong.erp.utils.common.Constants.StockHolder.FLOW;
import static com.yintong.erp.utils.common.Constants.StockHolder.PROD;
import static com.yintong.erp.utils.common.Constants.ProdFlowStage;
import static com.yintong.erp.utils.common.Constants.StockHolder;
import static com.yintong.erp.utils.common.Constants.WaresType;

/**
 * 工序卡服务
 *
 * @author lucifer.chan
 * @create 2019-04-07 下午8:49
 **/
@Service
public class ProdFlowService implements StockIn4Holder {

    @Autowired ErpProdOrderRepository orderRepository;

    @Autowired ErpProdOrderOptLogRepository orderOptLogRepository;

    @Autowired ErpProdHalfFlowRecordRepository flowRecordRepository;

    @Autowired ProdOrderService prodOrderService;

    /**
     * 保存完工序卡之后的操作
     * @param record
     * @param stage
     * @param oldKg 原先的重量：先减少后增加|针对包装
     * @param oldNum 原先的数量：先减少后增加|针对包装
     */
    @Transactional
    public void afterSaveFlow(ErpProdHalfFlowRecord record, ProdFlowStage stage, Double oldKg, Integer oldNum) {
        Long orderId = record.getProdOrderId();
        ErpProdOrder order = prodOrderService.findOneOrder(orderId);
        Double kg = record.getStage1Kg();
        Integer num = record.getStage1Num();
        if(PROD_STAGE_1 == stage){
            order.setFlowStart(true);
            orderRepository.save(order);
        } else if(PROD_STAGE_2 == stage){
            kg = record.getStage2Kg();
            num = record.getStage2Num();
        } else if (PROD_STAGE_3 == stage){
            kg = record.getStage3Kg();
            num = record.getStage3Num();
        } else if(PROD_STAGE_4 == stage){
            kg = record.getStage4Kg() - oldKg;
            num = record.getStage4Num() - oldNum;
            //已有的已包装数量
            Double lastNum = ifNotPresent(order.getPickNum(), 0d);
            Double packNum = "kg".equalsIgnoreCase(order.getUnit()) ? kg : num;
            order.setPickNum(lastNum + packNum);
            orderRepository.save(order);
        }
        //保存日志
        orderOptLogRepository.save(ErpProdOrderOptLog.builder()
                .orderId(orderId)
                .optType("flow")
                .content(stage.description + "【"+ record.getBarCode() + "】 重量【" + kg + "kg】 数量【" + num +"只】")
                .build());
    }

    /**
     * 保存完工序卡之后的操作
     * @param record
     * @param stage
     */
    @Transactional
    public void afterSaveFlow(ErpProdHalfFlowRecord record, ProdFlowStage stage){
        afterSaveFlow(record, stage, 0d, 0);
    }

    /**
     *  获取单个可入库的工序卡 成品
     * @param barcode
     * @return
     */
    public ErpProdHalfFlowRecord findFlow4In(String barcode) {
        return flowRecordRepository.findByBarCode(barcode).orElseThrow(() -> new IllegalArgumentException("未找到工序卡[".concat(barcode).concat("]")));
    }

    @Override
    public boolean matchesIn(StockHolder holder, StockEntity stockEntity) {
        return FLOW == holder && WaresType.P == stockEntity.waresType();
    }

    @Override
    public void stockIn(StockHolder holder, Long holderId, StockEntity stockEntity, double inKg) {
        ErpProdHalfFlowRecord record = flowRecordRepository.findById(holderId).orElseThrow(()-> new IllegalArgumentException("未找到工序卡[" + holderId + "]"));
        Assert.isTrue(record.getStage() == PROD_STAGE_4.stage, "工序未到".concat(PROD_STAGE_4.description).concat(",不能入库"));
        ErpProdOrder order = prodOrderService.findOneOrder(record.getProdOrderId());
        ErpBaseEndProduct product = order.getProduct();
        //1- 工序卡的入库
        //操作日志 - 暂无content
        ErpProdOrderOptLog flowOptLog = ErpProdOrderOptLog.builder()
                .optType("stock")
                .orderId(holderId)
                .waresId(stockEntity.templateId())
                .waresAssId(stockEntity.realityId())
                .waresType(stockEntity.waresType().name())
                .waresBarcode(stockEntity.entity().getBarCode())
            .build();
        Integer num = CommonUtil.kg2Num(product, inKg);
        double currentKg = inKg + record.getInKg();
        Integer currentNum = num + record.getInNum();
        String content = order.getProductName() + "入库【" + inKg + "kg】 累计入库 【" + currentKg + "/" + record.getStage4Kg() + "】kg";
        record.setInKg(currentKg);
        record.setInNum(currentNum);
        flowOptLog.setContent(content);
        flowRecordRepository.save(record);
        orderOptLogRepository.save(flowOptLog);
        //2 - 制令单的入库
        prodOrderService.stockIn(PROD, order.getId(), stockEntity, inKg);

    }

    /**
     * 包装
     * @param flowId
     * @param data
     * @return
     */
    public ErpProdHalfFlowRecord pack(Long flowId, ErpProdHalfFlowRecord data) {
        ErpProdHalfFlowRecord record = flowRecordRepository.findById(flowId).orElseThrow(()-> new IllegalArgumentException("未找到工序卡[" + flowId + "]"));
        int packCount = ifNotPresent(data.getPackCount(), 0);
        double oldKg = ifNotPresent(record.getStage4Kg(), 0d);
        int oldNum = ifNotPresent(record.getStage4Num(), 0);
        record.setSn(data.getSn());
        record.setAfterPickKg(data.getAfterPickKg());
        record.setPackCount(packCount);
        record.setPerPackNum(data.getPerPackNum());
        record.setPerPackKg(data.getPerPackKg());
        record.setRemnantKg(data.getRemnantKg());
        record.setRemnantNum(data.getRemnantNum());
        record.setTotalNum(data.getTotalNum());

        double stage4Kg = packCount * ifNotPresent(data.getPerPackKg(), 0d);
        int stage4Num = packCount * ifNotPresent(data.getPerPackNum(), 0);
        ErpEmployee employee = SessionUtil.getCurrentUser();

        record.setStage4Kg(stage4Kg);
        record.setStage4Num(stage4Num);
        record.setStage4UserId(employee.getId());
        record.setStage4UserName(employee.getName());
        record.setStage4Time(new Date());
        record.setStage(PROD_STAGE_4.stage);
        afterSaveFlow(record, PROD_STAGE_4, oldKg, oldNum);
        return flowRecordRepository.save(record);
    }
}

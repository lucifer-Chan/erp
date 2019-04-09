package com.yintong.erp.service.prod;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplier;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplierRepository;
import com.yintong.erp.domain.prod.ErpProdMould;
import com.yintong.erp.domain.prod.ErpProdMouldRepository;
import com.yintong.erp.domain.prod.ErpProdOrder;
import com.yintong.erp.domain.prod.ErpProdOrderOptLog;
import com.yintong.erp.domain.prod.ErpProdOrderOptLogRepository;
import com.yintong.erp.domain.prod.ErpProdOrderPickRecord;
import com.yintong.erp.domain.prod.ErpProdOrderPickRecordRepository;
import com.yintong.erp.domain.prod.ErpProdOrderRepository;
import com.yintong.erp.domain.prod.ErpProdPlan;
import com.yintong.erp.domain.prod.ErpProdPlanOptLog;
import com.yintong.erp.domain.prod.ErpProdPlanOptLogRepository;
import com.yintong.erp.domain.prod.ErpProdPlanRepository;
import com.yintong.erp.domain.prod.ErpProdProductBom;
import com.yintong.erp.domain.prod.ErpProdProductBomRepository;
import com.yintong.erp.domain.stock.ErpStockPlace;
import com.yintong.erp.domain.stock.StockEntity;
import com.yintong.erp.dto.BomDto;
import com.yintong.erp.dto.MouldDto;
import com.yintong.erp.dto.ProdOrderDto;
import com.yintong.erp.service.stock.StockIn4Holder;
import com.yintong.erp.service.stock.StockOut4Holder;
import com.yintong.erp.service.stock.StockPlaceService;
import com.yintong.erp.utils.common.Assert;
import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import com.yintong.erp.validator.OnDeleteSupplierMouldValidator;
import com.yintong.erp.validator.OnDeleteSupplierRawMaterialValidator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.util.StringUtils;

import static com.yintong.erp.utils.common.Constants.ProdBomHolder.ORDER;
import static com.yintong.erp.utils.common.Constants.ProdOrderStatus.S_002;
import static com.yintong.erp.utils.common.Constants.ProdOrderStatus.S_003;
import static com.yintong.erp.utils.common.Constants.StockHolder.PROD;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.equal;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.like;
import static javax.persistence.criteria.Predicate.BooleanOperator.AND;
import static javax.persistence.criteria.Predicate.BooleanOperator.OR;
import static com.yintong.erp.utils.common.Constants.StockHolder;
import static com.yintong.erp.utils.common.Constants.WaresType;

/**
 * @author lucifer.chan
 * @create 2018-09-03 下午4:24
 * 生产制令单服务
 **/
@Service
public class ProdOrderService implements StockOut4Holder, StockIn4Holder, OnDeleteSupplierRawMaterialValidator, OnDeleteSupplierMouldValidator {

    @Autowired ErpProdPlanRepository planRepository;

    @Autowired ErpProdOrderRepository orderRepository;

    @Autowired ErpProdPlanOptLogRepository planOptLogRepository;

    @Autowired ErpProdOrderOptLogRepository orderOptLogRepository;

    @Autowired ErpProdProductBomRepository prodProductBomRepository;

    @Autowired ErpProdMouldRepository prodMouldRepository;

    @Autowired ErpProdOrderPickRecordRepository pickRecordRepository;

    @Autowired ErpRawMaterialSupplierRepository materialSupplierRepository;

    @Autowired ProdPlanService planService;

    @Autowired StockPlaceService stockPlaceService;
    
    /**
     * 新增制令单
     * @param orderDto - order : employeeId prodNum ,planId, startDate, description 其余数据从计划单里拿
     *                 - bom - 只需要 id 和 num，其他的从计划单里获取 id为计划单bom的id
     *                 - mould id, num
     * @return
     */
    @Transactional
    public ErpProdOrder create(ProdOrderDto orderDto) {

        ErpProdPlan plan = planService.findOnePlan(orderDto.getOrder().getPlanId());
        //1-制令单
        ErpProdOrder order = orderDto.getOrder().copyFromPlan(plan);
        order.setId(null);
        ErpProdOrder ret = orderRepository.save(order);
        //2-bom
        List<BomDto> bomDtos = orderDto.getBoms();
        Assert.notEmpty(bomDtos, "物料清单不能为空");
        for(BomDto dto : bomDtos){
            Assert.notNull(dto.getId(), "物料id不能为空");
        }

        Map<Long, BomDto> bomDtoMap = bomDtos.stream().collect(Collectors.toMap(BomDto::getId, Function.identity()));

        List<ErpProdProductBom> bomList2Save = plan.getBoms().stream()
                .map(planBom -> {
                    BomDto dto = bomDtoMap.get(planBom.getId());
                    Assert.notNull(dto, "物料清单数据不匹配");
                    return ErpProdProductBom.copy4CreateOrder(ret.getId(), planBom, dto.getNum());
                })
                .collect(Collectors.toList());

        Assert.notEmpty(bomList2Save, "物料清单不能为空");
        prodProductBomRepository.saveAll(bomList2Save);
        //3-模具
        List<MouldDto> mouldDtos = orderDto.getMoulds();
        if(!CollectionUtils.isEmpty(mouldDtos)){
            for(MouldDto dto : mouldDtos){
                Assert.notNull(dto.getId(), "模具id不能为空");
            }
            Map<Long, MouldDto> mouldDtoMap = mouldDtos.stream().collect(Collectors.toMap(MouldDto::getId, Function.identity()));

            List<ErpProdMould> mouldList2Save = plan.getMoulds().stream()
                    .map(planMould -> {
                        MouldDto dto = mouldDtoMap.get(planMould.getId());
                        Assert.notNull(dto, "模具数据不匹配");
                        return ErpProdMould.copy(planMould, ORDER.name(), ret.getId(), dto.getNum());
                    })
                    .collect(Collectors.toList());

            prodMouldRepository.saveAll(mouldList2Save);
        }

        //4-计划单分配数量
        plan.setDistributedNum(plan.getDistributedNum() + order.getProdNum());
        planRepository.save(plan);
        //5-操作记录
        String content = "新建 数量：" + ret.getProdNum().intValue() + CommonUtil.ifNotPresent(order.getUnit(), "") +
                ", 开始时间：["  + DateUtil.getDateString(ret.getStartDate()) + "]";
        orderOptLogRepository.save(ErpProdOrderOptLog.builder().orderId(ret.getId()).content(content).optType("order").build());
        planOptLogRepository.save(ErpProdPlanOptLog.builder().planId(order.getPlanId()).content("新增制令单[" + ret.getBarCode() + "]").build());
        return ret;
    }

    /**
     * 修改制令单
     * @param orderDto - order :  employeeId prodNum ,planId 其余数据从计划单里拿
     *                 - bom - 只需要 id 和 num，id为制令单bom的id
     *                 - mould id, num
     * @return
     */
    @Transactional
    public ErpProdOrder update(ProdOrderDto orderDto) {
        ErpProdOrder order = orderDto.getOrder();
        Assert.notNull(order.getId(), "请选择制令单");
        //未打印出库单的允许修改
        ErpProdOrder oldOrder = orderRepository.findById(order.getId()).orElse(null);
        Assert.notNull(oldOrder, "请选择制令单");

        Assert.isTrue(0 == oldOrder.getPreStockIn(), "正在生产，不允许修改");

        String content = "更新 ";
        List<String> contents = new ArrayList<>();

        if(!oldOrder.getProdNum().equals(order.getProdNum())){
            //2-计划单的分配数量
            ErpProdPlan plan = planService.findOnePlan(oldOrder.getPlanId());
            plan.setDistributedNum(plan.getDistributedNum() - oldOrder.getProdNum() + order.getProdNum());
            contents.add("数量：" + order.getProdNum().intValue() + CommonUtil.ifNotPresent(oldOrder.getUnit(), ""));
            planRepository.save(plan);
            oldOrder.setProdNum(order.getProdNum());
        }

        if(!DateUtil.getDateString(oldOrder.getStartDate()).equals(DateUtil.getDateString(order.getStartDate()))) {
            contents.add("开始时间：[" + DateUtil.getDateString(oldOrder.getStartDate()) + " -> " + DateUtil.getDateString(order.getStartDate()) + "]");
            oldOrder.setStartDate(order.getStartDate());
        }

        if(!oldOrder.getEmployeeId().equals(order.getEmployeeId())){
            contents.add("工人：[" + oldOrder.getEmployeeName() + " -> " + order.getEmployeeName() + "]");
            oldOrder.setEmployeeId(order.getEmployeeId());
        }

        if(!ObjectUtils.equals(oldOrder.getRemark(), order.getRemark())){
            oldOrder.setRemark(order.getRemark());
        }

        if(!oldOrder.getDescription().equals(order.getDescription())){
            oldOrder.setDescription(order.getDescription());
        }

        oldOrder.setMachineCode(order.getMachineCode());

        //3-制令单单
        ErpProdOrder ret = orderRepository.save(oldOrder);

        //4-bom
        Map<Long, BomDto> bomDtoMap = orderDto.getBoms().stream().collect(Collectors.toMap(BomDto::getId, Function.identity()));
        List<ErpProdProductBom> bomList = prodProductBomRepository.findByIdIn(bomDtoMap.keySet()).stream()
                .map(bom -> {
                    BomDto dto = bomDtoMap.get(bom.getId());
                    Assert.notNull(dto, "物料清单数据不匹配");
                    if(!dto.getNum().equals(bom.getRealityMaterialNum())){
                        bom.setRealityMaterialNum(dto.getNum());
                        return bom;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(bomList)) {
            prodProductBomRepository.saveAll(bomList);
            contents.add("物料清单");
        }

        //5-模具
        List<MouldDto> mouldDtos = orderDto.getMoulds();
        if(!CollectionUtils.isEmpty(mouldDtos)){
            for(MouldDto dto : mouldDtos){
                Assert.notNull(dto.getId(), "模具id不能为空");
            }
            Map<Long, MouldDto> mouldDtoMap = mouldDtos.stream().collect(Collectors.toMap(MouldDto::getId, Function.identity()));

            List<ErpProdMould> mouldList2Save = oldOrder.getMoulds().stream()
                    .map(mould -> {
                        MouldDto dto = mouldDtoMap.get(mould.getId());
                        Assert.notNull(dto, "模具数据不匹配");
                        if(!dto.getNum().equals(mould.getRealityMouldNum())){
                            mould.setRealityMouldNum(dto.getNum());
                            return mould;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(mouldList2Save)) {
                prodMouldRepository.saveAll(mouldList2Save);
                contents.add("模具");
            }
        }

        //6-操作记录
        if(!CollectionUtils.isEmpty(contents)){
            content += StringUtils.collectionToCommaDelimitedString(contents);
            orderOptLogRepository.save(ErpProdOrderOptLog.builder().orderId(ret.getId()).content(content).optType("order").build());
            planOptLogRepository.save(ErpProdPlanOptLog.builder().planId(order.getPlanId()).content("修改制令单[" + oldOrder.getBarCode() + "]").build());
        }

        return ret;
    }

    /**
     * 删除制令单 - 已打印出库单[原材料|模具]的不允许删除
     * @param orderId
     */
    @Transactional
    public void delete(Long orderId) {
        ErpProdOrder order = findOneOrder(orderId);
        Assert.isTrue(0 == order.getPreStockOut(), "正在生产，不允许删除");
        ErpProdPlan plan = planRepository.findById(order.getPlanId()).orElse(null);
        Assert.notNull(plan, "未找到[" + order.getPlanId() + "]计划单");
        //计划单
        plan.setDistributedNum(plan.getDistributedNum() - order.getProdNum());
        planRepository.save(plan);
        //制令单
        orderRepository.deleteById(orderId);
        //bom
        prodProductBomRepository.deleteByHolderAndHolderId(ORDER.name(), orderId);
        //mould
        prodMouldRepository.deleteByHolderAndHolderId(ORDER.name(), orderId);
        //plan 操作记录
        planOptLogRepository.save(ErpProdPlanOptLog.builder().planId(order.getPlanId()).content("删除制令单[" + order.getBarCode() + "]").build());
        //order 操作记录
        orderOptLogRepository.deleteByOrderId(orderId);
    }

    /**
     * 获取单个order
     * @param orderId
     * @return
     */
    public ErpProdOrder findOneOrder(Long orderId) {
        ErpProdOrder order = orderRepository.findById(orderId).orElse(null);
        Assert.notNull(order, "未找到制令单[" + orderId + "]");
        return order;
    }

    /**
     * 组合查询
     * @param parameters
     * @return
     */
    public Page<ErpProdOrder> query(OrderParameterDto parameters){
        return orderRepository.findAll(parameters.specification(), parameters.pageable());
    }

    /**
     * 制令单的操作记录
     * @param orderId
     * @return
     */
    public List<ErpProdOrderOptLog> findOrderOptHistory(Long orderId){
        return orderOptLogRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    /**
     * 保存挑拣记录 - 新增|修改
     * @param record
     * @return
     */
    @Transactional
    public ErpProdOrderPickRecord saveRecord(ErpProdOrderPickRecord record) {
        record.requiredValidate();
        Long orderId = record.getOrderId();
        ErpProdOrder order = findOneOrder(orderId);
        Assert.isTrue(1 == order.getPreStockOut(), "尚未生产，请先打印物料出库单");
        if(Objects.isNull(record.getId())){
            record = pickRecordRepository.save(record);
            orderOptLogRepository.save(ErpProdOrderOptLog.builder()
                    .orderId(orderId)
                    .optType("pick")
                    .content("新增【"+ record.getBarCode() + "】 供挑拣重量【" + record.getTotalNum() + "kg】 合格重量【" + record.getValidNum() + "kg】 合格数【" + record.getValidOne() +"只】")
                    .build());
            //修改制令单的挑拣数量
            //order.setPickNum(order.getPickNum() + record.getValidNum());
            order.setPickNum(order.getPickNum() + calc(order, record));
            orderRepository.save(order);
            return record;
        }

        ErpProdOrderPickRecord old = findOneRecord(record.getId());
        List<String> contents = new ArrayList<String>(){{ add("修改【" + old.getBarCode() + "】"); }};

        if(!ObjectUtils.equals(record.getTotalNum(), old.getTotalNum())){
            contents.add("供挑拣重量【" + old.getTotalNum() + " -> " + record.getTotalNum() + "】");
        }

        if(!ObjectUtils.equals(record.getValidNum(), old.getValidNum())){
            contents.add("合格重量【" + old.getValidNum() + "kg -> " + record.getValidNum() + "kg】");
            contents.add("合格数【" + old.getValidOne() + "只 -> " + record.getValidOne() + "只】");
            //修改制令单的挑拣数量
            //order.setPickNum(order.getPickNum() + record.getValidNum() - old.getValidNum());
            order.setPickNum(order.getPickNum() + calc(order, record) - calc(order, old));
            orderRepository.save(order);
            return record;

        }
        record.copyBase(old);
        String content = StringUtils.collectionToDelimitedString(contents, " ");
        if(StringUtils.hasText(content)){
            orderOptLogRepository.save(ErpProdOrderOptLog.builder().orderId(orderId).optType("pick").content(content).build());
        }

        return pickRecordRepository.save(record);
    }


    /**
     * 获取单个挑拣记录
     * @param recordId
     * @return
     */
    public ErpProdOrderPickRecord findOneRecord(Long recordId){
        ErpProdOrderPickRecord record = pickRecordRepository.findById(recordId).orElse(null);
        Assert.notNull(record, "未找到挑拣记录[" + recordId + "]");
        return record;
    }

    /**
     * 准备出库-打印出库单[原材料|模具]之后调用
     * @param orderId
     * @return
     */
    public ErpProdOrder preStockOut(Long orderId) {
        ErpProdOrder order = findOneOrder(orderId);
        order.setPreStockOut(1);
        order.setStatusCode(S_002.name());
        return orderRepository.save(order);
    }

    /**
     * 准备入库-打印入库之后调用
     * @param orderId
     * @return
     */
    public ErpProdOrder preStockIn(Long orderId) {
        ErpProdOrder order = findOneOrder(orderId);
//        Assert.isTrue(1 == order.getPreStockOut(), "尚未生产，请先打印制令单");
        order.setPreStockIn(1);
        return orderRepository.save(order);
    }

    @Override
    public void onDeleteSupplierMould(Long id) {
        List<Long> orderIds = prodMouldRepository.findByHolderAndRealityMouldId(ORDER.name(), id).stream().map(ErpProdMould::getHolderId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(orderIds)) return;
        String codes = orderRepository.findByIdIn(orderIds).stream().map(ErpProdOrder::getBarCode).collect(Collectors.joining(","));
        Assert.isEmpty(codes, "请先删除生产制令单[" + codes + "]");
    }


    @Override
    public void onDeleteSupplierRawMaterial(Long id) {
        List<Long> orderIds = prodProductBomRepository.findByHolderAndRealityMaterialId(ORDER.name(), id).stream().map(ErpProdProductBom::getHolderId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(orderIds)) return;
        String codes = orderRepository.findByIdIn(orderIds).stream().map(ErpProdOrder::getBarCode).collect(Collectors.joining(","));
        Assert.isEmpty(codes, "请先删除生产制令单[" + codes + "]");
    }

    /**
     * 入库 [成品] 模具|原材料 - 用完回收
     * @param holder
     * @param stockEntity
     * @return
     */
    @Override
    public boolean matchesIn(StockHolder holder, StockEntity stockEntity) {
        return PROD == holder && Arrays.asList(WaresType.P, WaresType.M, WaresType.D).contains(stockEntity.waresType());
    }

    /**
     * 入库
     * @param holder
     * @param orderId
     * @param stockEntity
     * @param inNum
     */
    @Override
    public void stockIn(StockHolder holder, Long orderId, StockEntity stockEntity, double inNum) {
        //1.1 检查订单
        ErpProdOrder order = findOneOrder(orderId);
//        Assert.isTrue(1 == order.getPreStockIn(), "尚未生产，请先打印制令单");
//        String content = item.getWaresName() + " 完成入库,库存数量【" + currentInNum + "/" + item.getNum() + "】";
        //操作日志 - 暂无content
        ErpProdOrderOptLog prodOrderOptLog = ErpProdOrderOptLog.builder()
                .optType("stock")
                .orderId(orderId)
                .waresId(stockEntity.templateId())
                .waresAssId(stockEntity.realityId())
                .waresType(stockEntity.waresType().name())
                .waresBarcode(stockEntity.entity().getBarCode())
            .build();

        //2 根据类型做具体的操作
        if(WaresType.P == stockEntity.waresType()){
            //成品入库 - 挑拣后的成品 - 状态管理
//            Assert.isTrue(!S_003.name().equals(order.getStatusCode()), order.getProductName() + "已全部完成入库");//不做校验，可以超量生产
            //成品入库 - inNum为kg；根据制令单的单位（kg／只）计算出数量再入库
            inNum = CommonUtil.calcFromKg(order.getProduct(), order.getUnit(), inNum);
            double currentInNum = inNum + order.getFinishNum();
            String content = order.getProductName() + "入库【" + inNum + order.getUnit() + "】 累计入库 【" + currentInNum + "/" + order.getProdNum() + order.getUnit() + "】";
            if(currentInNum >= order.getProdNum()){
                order.setStatusCode(S_003.name());
                order.setFinishDate(new Date());
                content = order.getProductName() + "入库【" + inNum + order.getUnit() +  "】 全部完成入库 【" + currentInNum + "/" + order.getProdNum() + "】";
            }

            //计划单的达成数量计算
            planService.addFinish(order.getPlanId(), inNum);


            prodOrderOptLog.setContent(content);
            //保存订单
            order.setFinishNum(currentInNum);
            orderRepository.save(order);
        } else if (WaresType.M == stockEntity.waresType()){
            //原材料入库 - 回收
            List<ErpProdProductBom> bomList = order.getBoms().stream().filter(bom -> stockEntity.templateId().equals(bom.getMaterialId())).collect(Collectors.toList());
            ErpProdProductBom bom = CommonUtil.single(bomList, "制令单[" + order.getBarCode() + "]的物料清单存在脏数据");
            Assert.notNull(bom, "制令单[" + order.getBarCode() + "]的物料清单中未找到" + stockEntity.template().getSimpleName());
            double currentInNum = inNum + bom.getNumIn();
            Assert.isTrue(currentInNum <= bom.getNumOut(), "入库原材料数量不能大于总出库数");
            bom.setNumIn(currentInNum);
            prodOrderOptLog.setContent(bom.getMaterial().getRawName() + " 完成回收入库 【" + currentInNum + "kg】");
            //保存bom
            prodProductBomRepository.save(bom);

        } else {
            //模具入库 - 回收
            List<ErpProdMould> mouldList = order.getMoulds().stream().filter(mould -> stockEntity.templateId().equals(mould.getMouldId())).collect(Collectors.toList());
            ErpProdMould mould =  CommonUtil.single(mouldList, "制令单[" + order.getBarCode() + "]的模具清单存在脏数据");
            Assert.notNull(mould, "制令单[" + order.getBarCode() + "]的模具清单中未找到" + stockEntity.template().getSimpleName());
            double currentInNum = inNum + mould.getNumIn();
            Assert.isTrue(currentInNum <= mould.getNumOut(), "入库原材料量不能大于总出库量");
            mould.setNumIn(currentInNum);
            prodOrderOptLog.setContent(mould.getMould().getSimpleName() + " 完成回收入库 【" + currentInNum + "件】");
            //保存mould
            prodMouldRepository.save(mould);
        }
        //3 - 日志
        orderOptLogRepository.save(prodOrderOptLog);
    }

    /**
     * 只出库材料|模具
     * @param holder
     * @param stockEntity
     * @return
     */
    @Override
    public boolean matchesOut(StockHolder holder, StockEntity stockEntity) {
        return PROD == holder && Arrays.asList(WaresType.M, WaresType.D).contains(stockEntity.waresType());
    }

    @Override
    public void stockOut(StockHolder holder, Long orderId, StockEntity stockEntity, double outNum) {
        //1.1 检查订单
        ErpProdOrder order = findOneOrder(orderId);
        Assert.isTrue(1 == order.getPreStockOut(), "尚未生产，请先出库原材料");
        //操作日志 - 暂无content
        ErpProdOrderOptLog prodOrderOptLog = ErpProdOrderOptLog.builder()
                .optType("stock")
                .orderId(orderId)
                .waresId(stockEntity.templateId())
                .waresAssId(stockEntity.realityId())
                .waresType(stockEntity.waresType().name())
                .waresBarcode(stockEntity.entity().getBarCode())
                .build();
        if (WaresType.M == stockEntity.waresType()){
            //原材料出库 - 供生产使用
//            List<ErpProdProductBom> bomList = order.getBoms().stream().filter(bom -> stockEntity.templateId().equals(bom.getMaterialId())).collect(Collectors.toList());
            List<ErpProdProductBom> bomList = order.getBoms().stream().filter(bom -> stockEntity.realityId().equals(bom.getRealityMaterialId())).collect(Collectors.toList());
            ErpProdProductBom bom = CommonUtil.single(bomList, "制令单[" + order.getBarCode() + "]的物料清单存在脏数据");
            Assert.notNull(bom, "制令单[" + order.getBarCode() + "]的物料清单中未找到" + stockEntity.template().getSimpleName());
            double currentOutNum = outNum + bom.getNumOut();
            bom.setNumOut(currentOutNum);
            prodOrderOptLog.setContent(bom.getMaterial().getRawName() + " 完成出库，累计出库数量【" + currentOutNum + "kg】");
            //保存bom
            prodProductBomRepository.save(bom);

        } else {
            //模具出库 - 供生产使用
            List<ErpProdMould> mouldList = order.getMoulds().stream().filter(mould -> stockEntity.templateId().equals(mould.getMouldId())).collect(Collectors.toList());
            ErpProdMould mould =  CommonUtil.single(mouldList, "制令单[" + order.getBarCode() + "]的模具清单存在脏数据");
            Assert.notNull(mould, "制令单[" + order.getBarCode() + "]的模具清单中未找到" + stockEntity.template().getSimpleName());
            double currentOutNum = outNum + mould.getNumOut();
            mould.setNumOut(currentOutNum);
            prodOrderOptLog.setContent(mould.getMould().getSimpleName() + " 完成出库，累计出库数量【" + currentOutNum + "件】");
            //保存mould
            prodMouldRepository.save(mould);
        }
        //3 - 日志
        orderOptLogRepository.save(prodOrderOptLog);
    }

    /**
     * 获取单个可出库的制令单
     * @param barcode
     * @return
     */
    public ErpProdOrder findOrder4Out(String barcode){
        //1.1 检查订单
        ErpProdOrder order = orderRepository.findByBarCode(barcode).orElse(null);
        Assert.notNull(order, "未找到制令单[" + barcode + "]");
        Assert.isTrue(1 == order.getPreStockOut(), "尚未生产，请先打印制令单");
//        long bomCount = order.getBoms().stream().filter(bom -> bom.getNumOut() < bom.getNumIn()).count();
//        long mouldCount = order.getMoulds().stream().filter(mould -> mould.getNumOut() < mould.getNumIn()).count();
//        Assert.isTrue(bomCount + mouldCount > 0, "无可出库的模具或原材料");
        Assert.isTrue(!S_003.name().equals(order.getStatusCode()), "该制令单已完成生产，不能再出库模具或原材料");
        return order;
    }

    /**
     *  获取单个可入库的制令单 成品|模具|原材料
     * @param barcode
     * @return
     */
    public ErpProdOrder findOrder4In(String barcode){
        ErpProdOrder order = orderRepository.findByBarCode(barcode).orElse(null);
        Assert.notNull(order, "未找到制令单[" + barcode + "]");
//        Assert.isTrue(1 == order.getPreStockIn(), "尚未生产，请先打印制令单");
        return order;
    }

    /**
     * 强制结束
     * @param orderId
     * @return
     */
    public ErpProdOrder finish(Long orderId) {
        ErpProdOrder order = findOneOrder(orderId);
        order.setStatusCode(S_003.name());
        order.setStatusName(S_003.description());
        order.setFinishDate(new Date());
        orderOptLogRepository.save(ErpProdOrderOptLog.builder().orderId(orderId).content("强制结束").optType("order").build());
        return orderRepository.save(order);
    }

    /**
     * 更换原材料：
     *  1 - 新建一个bom,
     *  2 - 设置originalId为原bom的id，设置原材料数量为原bom数量
     *  3 - 原bom的原材料id和新的不能相同
     *  4 - 新的原材料大类要和原bom相同
     * @param orderId 制令单id
     * @param bomId 原始物料清单id
     * @param place 新的原材料仓位
     * @return 制令单
     */
    public ErpProdOrder replaceMaterial(Long orderId, Long bomId, ErpStockPlace place) {
        ErpProdOrder order = findOneOrder(orderId);
        Map<Long, ErpProdProductBom> boms = order.getBoms().stream().collect(Collectors.toMap(ErpProdProductBom::getId, Function.identity()));
        //原始物料
        ErpProdProductBom originalBom = boms.get(bomId);
        Assert.notNull(originalBom, "未找到原物料清单[" + bomId + "]");
        Assert.isTrue(Objects.isNull(originalBom.getOriginalId()), "该原材料为替换后的原材料,不能再次替换");
        //原始material_supplier_bar_code
        String originalMsBarCode = originalBom.getRealityMaterial().getBarCode();
        //新的仓位
        Assert.isTrue("M".equals(place.getStockPlaceType()), "请选择原材料仓位");
        //新的原材料条码
        String newMsBarCode = place.getMaterialSupplierBarCode();

        Assert.isTrue(!boms.values().stream().map(it ->it.getRealityMaterial().getBarCode()).collect(Collectors.toSet()).contains(newMsBarCode), "物料清单里已包含该原材料,请重新选择");

        Assert.isTrue(originalMsBarCode.substring(0, 2).equals(newMsBarCode.substring(0, 2)), "更换的原材料类型不正确,请重新选择");

        ErpRawMaterialSupplier materialSupplier = materialSupplierRepository.findByBarCode(newMsBarCode).orElseThrow(()-> new IllegalArgumentException("未找到原材料[" + newMsBarCode + "]"));

        ErpProdProductBom newBom =  ErpProdProductBom.copyFromOriginal(originalBom, materialSupplier);

        prodProductBomRepository.save(newBom);

        String content = "更换原材料为:" + place.getMaterialName();

        orderOptLogRepository.save(ErpProdOrderOptLog.builder().orderId(orderId).content(content).optType("order").build());

        return findOneOrder(orderId);

    }

    /**
     * 制令单查询入参dto
     */
    @Getter @Setter
    @OrderBy(fieldName = "startDate")
    public static class OrderParameterDto extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "description", "productName", "machineCode"}, compare = like, group = OR)
        String cause;
        @ParameterItem(mappingTo = {"flowStart"}, compare = equal, group = AND)
        Boolean flowStart;
    }

    /**
     * 出入库数量计算 ：根据订单的单位计算-只|Kg
     * @param order
     * @param record
     * @return
     */
    private Double calc(ErpProdOrder order, ErpProdOrderPickRecord record){
        ErpBaseEndProduct product = order.getProduct();
        if(Objects.isNull(product) || !StringUtils.hasText(product.getOnlyOrKg()) || !StringUtils.hasText(order.getUnit())){
            return record.getValidNum();
        }
        return "kg".equalsIgnoreCase(order.getUnit()) ? record.getValidNum() : record.getValidOne();
    }


}

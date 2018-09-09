package com.yintong.erp.service.prod;

import com.yintong.erp.domain.prod.ErpProdMould;
import com.yintong.erp.domain.prod.ErpProdMouldRepository;
import com.yintong.erp.domain.prod.ErpProdOrder;
import com.yintong.erp.domain.prod.ErpProdOrderOptLog;
import com.yintong.erp.domain.prod.ErpProdOrderOptLogRepository;
import com.yintong.erp.domain.prod.ErpProdOrderRepository;
import com.yintong.erp.domain.prod.ErpProdPlan;
import com.yintong.erp.domain.prod.ErpProdPlanOptLog;
import com.yintong.erp.domain.prod.ErpProdPlanOptLogRepository;
import com.yintong.erp.domain.prod.ErpProdPlanRepository;
import com.yintong.erp.domain.prod.ErpProdProductBom;
import com.yintong.erp.domain.prod.ErpProdProductBomRepository;
import com.yintong.erp.dto.BomDto;
import com.yintong.erp.dto.MouldDto;
import com.yintong.erp.dto.ProdOrderDto;
import com.yintong.erp.utils.common.Assert;
import com.yintong.erp.utils.common.DateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.util.StringUtils;

import static com.yintong.erp.utils.common.Constants.ProdBomHolder.ORDER;

/**
 * @author lucifer.chan
 * @create 2018-09-03 下午4:24
 * 生产制令单服务
 **/
@Service
public class ProdOrderService {

    @Autowired ErpProdPlanRepository planRepository;

    @Autowired ErpProdOrderRepository orderRepository;

    @Autowired ErpProdPlanOptLogRepository planOptLogRepository;

    @Autowired ErpProdOrderOptLogRepository orderOptLogRepository;

    @Autowired ErpProdProductBomRepository prodProductBomRepository;

    @Autowired ErpProdMouldRepository prodMouldRepository;

    @Autowired ProdPlanService planService;
    
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
        String content = "新建 数量：" + ret.getProdNum() +
                ", 开始时间：["  + DateUtil.getDateString(ret.getStartDate()) + "]";
        orderOptLogRepository.save(ErpProdOrderOptLog.builder().orderId(ret.getId()).content(content).build());
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
            contents.add("数量：" + order.getProdNum());
            //2-计划单的分配数量
            ErpProdPlan plan = planService.findOnePlan(oldOrder.getPlanId());
            plan.setDistributedNum(plan.getDistributedNum() - oldOrder.getProdNum() + order.getProdNum());
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
            orderOptLogRepository.save(ErpProdOrderOptLog.builder().orderId(ret.getId()).content(content).build());
            planOptLogRepository.save(ErpProdPlanOptLog.builder().planId(order.getPlanId()).content("修改制令单[" + oldOrder.getBarCode() + "]").build());
        }

        return ret;
    }

    /**
     * 删除制令单 - 已打印出库单的不允许删除
     * @param orderId
     */
    @Transactional
    public void delete(Long orderId) {
        ErpProdOrder order = findOneOrder(orderId);
        Assert.isTrue(0 == order.getPreStockIn(), "正在生产，不允许删除");
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
}

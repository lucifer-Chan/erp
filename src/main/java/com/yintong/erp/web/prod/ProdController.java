package com.yintong.erp.web.prod;

import com.yintong.erp.domain.prod.ErpProdHalfFlowRecord;
import com.yintong.erp.domain.prod.ErpProdMould;
import com.yintong.erp.domain.prod.ErpProdOrder;
import com.yintong.erp.domain.prod.ErpProdOrderPickRecord;
import com.yintong.erp.domain.prod.ErpProdPlan;
import com.yintong.erp.dto.ProdOrderDto;
import com.yintong.erp.dto.ProdPlanDto;
import com.yintong.erp.service.prod.ProdFlowService;
import com.yintong.erp.service.prod.ProdOrderService;
import com.yintong.erp.service.prod.ProdPlanService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

/**
 * @author lucifer.chan
 * @create 2018-09-03 下午4:22
 * 生产模块
 **/
@RestController
@RequestMapping("prod")
public class ProdController {

    @Autowired ProdPlanService planService;

    @Autowired ProdOrderService orderService;

    @Autowired ProdFlowService flowService;

    /**
     * 新增计划单 - 包括boms、不包括orders、moulds
     * @param planDto
     * @return
     */
    @PostMapping("plan")
    public BaseResult createPlan(@RequestBody ProdPlanDto planDto) {
        return new BaseResult().addPojo(planService.create(planDto));
    }

    /**
     * 组合查询
     * @param parameters
     * @return
     */
    @GetMapping("plan")
    public BaseResult findPlans(ProdPlanService.PlanParameterDto parameters) {
        Page<ErpProdPlan> page = planService.query(parameters);
        return page2BaseResult(page);
    }

    /**
     * 更新计划单 - 包括boms、不包括productId、orders、moulds
     * @param planDto
     * @return
     */
    @PutMapping("plan")
    public BaseResult updatePlan(@RequestBody ProdPlanDto planDto) {
        Assert.notNull(planDto.getPlan().getId(), "未选择计划单");
        return new BaseResult().addPojo(planService.update(planDto));
    }

    /**
     * 获取单个计划单
     * @param planId
     * @return
     */
    @GetMapping("plan/{planId}")
    public BaseResult findOnePlan(@PathVariable Long planId){
        return new BaseResult().addPojo(planService.findOnePlan(planId));
    }

    /**
     * 删除计划单
     * @param planId
     * @return
     */
    @DeleteMapping("plan/{planId}")
    public BaseResult deletePlan(@PathVariable Long planId) {
        planService.deletePlan(planId);
        return new BaseResult().setErrmsg("删除成功");
    }

    /**
     * 计划单的操作记录
     * @param planId
     * @return
     */
    @GetMapping("plan/{planId}/history/opt")
    public BaseResult findPlanOptHistory(@PathVariable Long planId) {
        return new BaseResult().addList(planService.findPlanOptHistory(planId));
    }

    /**
     * 新增／修改 模具
     * @param planId
     * @param mould realityMouldId realityMouldNum ; mouldId后台查询
     * @return
     */
    @PostMapping("plan/{planId}/mould")
    public BaseResult saveMould(@PathVariable Long planId, @RequestBody ErpProdMould mould){
        return new BaseResult().addPojo(planService.saveMould(planId, mould));
    }

    /**
     * 删除 模具
     * @param id
     * @return
     */
    @DeleteMapping("plan/mould/{id}")
    public BaseResult deleteMould(@PathVariable Long id){

        return new BaseResult().addPojo(planService.deleteMould(id));
    }

    /**
     * 根据计划单和计划单的成品获取物料清单
     * @param planId -1 时为新建
     * @param productId 成品id
     * @return List<BomDto>
     *         新建的计划单：清单的每项原材料实际数量为0
     *         更新的计划单：清单的每项原材料数量为计划单的物料清单里的数量
     *
     */
    @GetMapping("plan/{planId}/{productId}/boms")
    public BaseResult findBomListFromPlan(@PathVariable Long planId, @PathVariable Long productId){
        return new BaseResult().addList(planService.findBomList(planId, productId));
    }

    //========================== 以下为制令单内容 ==========================//

    /**
     * 新增制令单
     * @param orderDto - order : employeeId prodNum ,planId, startDate, description 其余数据从计划单里拿
     *                 - bom - 只需要 id 和 num，其他的从计划单里获取 id为计划单bom的id
     *                 - mould id, num
     * @return plan
     */
    @PostMapping("order")
    public BaseResult createOrder(@RequestBody ProdOrderDto orderDto) {
        return new BaseResult().addPojo(orderService.create(orderDto));
    }

    /**
     * 修改制令单
     * @param orderDto - order :  employeeId prodNum ,planId 其余数据从计划单里拿
     *                 - bom - 只需要 id 和 num，id为制令单bom的id
     *                 - mould id, num
     * @return plan
     */
    @PutMapping("order")
    public BaseResult updateOrder(@RequestBody ProdOrderDto orderDto) {
        return new BaseResult().addPojo(orderService.update(orderDto));
    }

    /**
     * 组合查询
     * @param parameters
     * @return
     */
    @GetMapping("order")
    public BaseResult findOrders(ProdOrderService.OrderParameterDto parameters) {
        Page<ErpProdOrder> page = orderService.query(parameters);
        return page2BaseResult(page);
    }

    /**
     * 删除制令单
     * @param orderId
     * @return
     */
    @DeleteMapping("order/{orderId}")
    public BaseResult deleteOrder(@PathVariable Long orderId){
        orderService.delete(orderId);
        return new BaseResult().setErrmsg("删除成功");
    }

    /**
     * 获取单个制令单
     * @param orderId
     * @return
     */
    @GetMapping("order/{orderId}")
    public BaseResult findOneOrder(@PathVariable Long orderId){
        return new BaseResult().addPojo(orderService.findOneOrder(orderId));
    }

    /**
     * 制令单的操作记录
     * @param orderId
     * @return
     */
    @GetMapping("order/{orderId}/history/opt")
    public BaseResult findOrderOptHistory(@PathVariable Long orderId) {
        return new BaseResult().addList(orderService.findOrderOptHistory(orderId));
    }

    /**
     * 准备出库-打印出库单[原材料|模具]之后调用
     * @param orderId
     * @return
     */
    @PatchMapping("order/{orderId}/preOut")
    public BaseResult preStockOut(@PathVariable Long orderId){
        return new BaseResult().addPojo(orderService.preStockOut(orderId));
    }

    /**
     * 准备入库-打印入库之后调用
     * @param orderId
     * @return
     */
    @PatchMapping("order/{orderId}/preIn")
    public BaseResult preStockIn(@PathVariable Long orderId){
        return new BaseResult().addPojo(orderService.preStockIn(orderId));
    }

    /**
     * 保存挑拣记录 - 新增|修改
     * @param record
     * @return
     */
    @PostMapping("order/pick")
    public BaseResult saveRecord(@RequestBody ErpProdOrderPickRecord record){
        return new BaseResult().addPojo(orderService.saveRecord(record));
    }

    // url : 'prod/order/pack/' + flowId,

    /**
     * 包装
     * @param flowId
     * @param data
     * @return
     */
    @PatchMapping("order/pack/{flowId}")
    public BaseResult pack(@PathVariable Long flowId, @RequestBody ErpProdHalfFlowRecord data){
        ErpProdHalfFlowRecord record = flowService.pack(flowId, data);
        return new BaseResult().addPojo(orderService.findOneOrder(record.getProdOrderId()));
    }

    @PatchMapping("order/finish/{orderId}")
    public BaseResult finish(@PathVariable Long orderId){
        return new BaseResult().addPojo(orderService.finish(orderId));
    }
}

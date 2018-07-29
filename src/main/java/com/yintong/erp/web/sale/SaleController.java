package com.yintong.erp.web.sale;

import com.yintong.erp.domain.sale.ErpSaleOrder;
import com.yintong.erp.domain.sale.ErpSaleOrderItem;
import com.yintong.erp.domain.sale.ErpSalePlan;
import com.yintong.erp.service.sale.SaleOrderService;
import com.yintong.erp.service.sale.SalePlanService;
import com.yintong.erp.utils.base.BaseResult;
import com.yintong.erp.utils.common.Constants;
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

import static com.yintong.erp.service.sale.SalePlanService.PlanParameterDto;
import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

/**
 * @author lucifer.chan
 * @create 2018-07-21 下午5:06
 * 销售模块入库
 **/
@RestController
@RequestMapping("sale")
public class SaleController {

    @Autowired SalePlanService salePlanService;

    @Autowired SaleOrderService orderService;


    /**
     * 新增计划单
     * @param plan
     * @return
     */
    @PostMapping("plan")
    public BaseResult createPlan(@RequestBody ErpSalePlan plan){
        Assert.notNull(plan.getStartDate(), "开始时间不能为空");
        Assert.notNull(plan.getEndDate(), "结束时间不能为空");
        Assert.notNull(plan.getPlanMoney(), "计划金额不能为空");
        Assert.notNull(plan.getProductId(), "未选择成品！");
        return new BaseResult().addPojo(salePlanService.create(plan));
    }

    /**
     * 更新计划单
     * @param plan
     * @return
     */
    @PutMapping("plan")
    public BaseResult updatePlan(@RequestBody ErpSalePlan plan){
        Assert.notNull(plan.getId(), "未选择计划单");
        Assert.notNull(plan.getStartDate(), "开始时间不能为空");
        Assert.notNull(plan.getEndDate(), "结束时间不能为空");
        Assert.notNull(plan.getPlanMoney(), "计划金额不能为空");
        return new BaseResult().addPojo(salePlanService.update(plan));
    }

    /**
     * 删除计划单
     * @param planId
     * @return
     */
    @DeleteMapping("plan/{planId}")
    public BaseResult deletePlan(@PathVariable Long planId){
        salePlanService.deletePlan(planId);
        return new BaseResult().setErrmsg("删除成功");
    }

    /**
     * 组合查询
     * @param parameters
     * @return
     */
    @GetMapping("plan")
    public BaseResult findPlans(PlanParameterDto parameters){
        Page<ErpSalePlan> page = salePlanService.query(parameters);
        return page2BaseResult(page);
    }

    /**
     * 获取单个计划单
     * @param planId
     * @return
     */
    @GetMapping("plan/{planId}")
    public BaseResult findPlan(@PathVariable Long planId){
        return new BaseResult().addPojo(salePlanService.findOnePlan(planId));
    }

    /**
     *  获取单个计划单的历史记录
     * @param planId
     * @return
     */
    @GetMapping("plan/{planId}/history/opt")
    public BaseResult findPlanOptHistory(@PathVariable Long planId){
        return new BaseResult().addList(salePlanService.findPlanOptHistory(planId));
    }


    /*===========================以下为销售订单===========================*/
    /**
     * 新增销售订单-可包含明细列表
     * @param order
     * @return
     */
    @PostMapping("order")
    public BaseResult createOrder(@RequestBody ErpSaleOrder order){
        Assert.notNull(order.getOrderDate(), "订单时间不能为空");
        Assert.notNull(order.getCustomerId(), "未选择客户");
        return new BaseResult().addPojo(orderService.create(order));
    }

    /**
     * 更新销售订单-不包含明细
     * 更新内容： 时间、总额、客户、订单日期
     * 约束条件：状态为未发布、审核退回
     * @param order
     * @return
     */
    @PutMapping("order")
    public BaseResult updateOrder(@RequestBody ErpSaleOrder order){
        Assert.notNull(order.getId(), "订单id不能为空");
        Assert.notNull(order.getOrderDate(), "订单时间不能为空");
        Assert.notNull(order.getCustomerId(), "未选择客户");
//        Assert.isTrue(STATUS_001.name().equals(status) || STATUS_004.name().equals(status)
//                ,"只有未发布或待审核的订单可以修改");
        return new BaseResult().addPojo(orderService.update(order));
    }

    /**
     * 删除销售订单-级联删除明细
     * 约束条件：状态为未发布、审核退回
     * @param orderId
     * @return
     */
    @DeleteMapping("order/{orderId}")
    public BaseResult deleteOrder(@PathVariable Long orderId){
        orderService.deleteOrder(orderId);
        return new BaseResult().setErrmsg("删除成功");
    }

    /**
     * 单个销售订单查询
     * @param orderId
     * @return
     */
    @GetMapping("order/{orderId}")
    public BaseResult findOrder(@PathVariable Long orderId){
        return new BaseResult().addPojo(orderService.findOrder(orderId));
    }

    /**
     * 组合查询-销售订单
     * eg：codes -> a,b,c
     * @param parameters
     * @return history,items
     */
    @GetMapping("order")
    public BaseResult findOrders(SaleOrderService.OrderParameterDto parameters){
        Page<ErpSaleOrder> page = orderService.query(parameters);
        return page2BaseResult(page);
    }

    /**
     * 更新订单状态：待审核、审核通过、审核退回、客户退货、已出库、已完成
     * 约束条件：未发布 ->待审核
     *          待审核 ->审核通过、审核退回
     *          审核退回 ->待审核
     *          审核通过 ->已出库
     *          已出库 ->客户退货、已完成
     * @param orderId
     * @param status 修改后的状态
     * @param remark 备注信息
     * @return
     */
    @PatchMapping("order/{orderId}/{status}")
    public BaseResult updateOrderStatus(@PathVariable Long orderId, @PathVariable Constants.SaleOrderStatus status, String remark){
        return new BaseResult().addPojo(orderService.updateOrderStatus(orderId, status, remark));
    }

    /**
     * 新增销售订单明细-已有订单的情况下
     * 约束条件：订单状态为未发布、审核退回
     * ps：订单id、code校验，自身属性在service里校验
     * @param item
     * @return
     */
    @PostMapping("orderItem")
    public BaseResult createOrderItem(@RequestBody ErpSaleOrderItem item){
        Assert.notNull(item.getOrderId(), "销售订单id不能为空");
        Assert.notNull(item.getOrderCode(), "销售订单编号不能为空");
        return new BaseResult().addPojo(orderService.createOrderItem(item));
    }

    /**
     * 更新销售订单明细-已有订单的情况下
     * 约束条件：订单状态为未发布、审核退回
     * ps：订单id、code校验，自身属性在service里校验
     * @param item
     * @return
     */
    @PutMapping("orderItem")
    public BaseResult updateOrderItem(@RequestBody ErpSaleOrderItem item){
        Assert.notNull(item.getOrderId(), "销售订单id不能为空");
        Assert.notNull(item.getOrderCode(), "销售订单编号不能为空");
        Assert.notNull(item.getId(), "销售订单明细id不能为空");
        return new BaseResult().addPojo(orderService.updateOrderItem(item));
    }

    /**
     * 删除销售订单明细-已有订单的情况下
     * 约束条件：订单状态为未发布、审核退回
     * @param orderId
     * @param orderItemId
     * @return
     */
    @DeleteMapping("orderItem/{orderId}/{orderItemId}")
    public BaseResult deleteOrderItem(@PathVariable Long orderId, @PathVariable Long orderItemId){
        orderService.deleteOrderItem(orderId, orderItemId);
        return new BaseResult().setErrmsg("删除成功");
    }

    /**
     *  获取单个销售订单的历史记录
     * @param orderId
     * @return
     */
    @GetMapping("order/{orderId}/history/opt")
    public BaseResult findOrderOptHistory(@PathVariable Long orderId){
        return new BaseResult().addList(orderService.findOrderOptHistory(orderId));
    }

    /**
     * 获取单个销售订单的明细
     * @param orderId
     * @return
     */
    @GetMapping("order/{orderId}/items")
    public BaseResult findOrderItems(@PathVariable Long orderId){
        return new BaseResult().addList(orderService.findOrderItems(orderId));
    }

}

package com.yintong.erp.web.purchase;

import com.yintong.erp.domain.purchase.ErpPurchaseOrder;
import com.yintong.erp.domain.purchase.ErpPurchaseOrderItem;
import com.yintong.erp.domain.purchase.ErpPurchasePlan;
import com.yintong.erp.service.purchase.PurchaseOrderService;
import com.yintong.erp.service.purchase.PurchasePlanService;
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
import static com.yintong.erp.utils.common.Constants.*;

/**
 * @author lucifer.chan
 * @create 2018-08-22 上午11:07
 * 采购controller
 **/
@RestController
@RequestMapping("purchase")
public class PurchaseController {

    @Autowired PurchasePlanService planService;

    @Autowired PurchaseOrderService orderService;

    /**
     * 新增计划单
     *
     * @param plan
     * @return
     */
    @PostMapping("plan")
    public BaseResult createPlan(@RequestBody ErpPurchasePlan plan) {
        return new BaseResult().addPojo(planService.create(plan));
    }

    /**
     * 更新计划单
     *
     * @param plan
     * @return
     */
    @PutMapping("plan")
    public BaseResult updatePlan(@RequestBody ErpPurchasePlan plan) {
        Assert.notNull(plan.getId(), "未选择计划单");
        return new BaseResult().addPojo(planService.update(plan));
    }

    /**
     * 删除计划单
     *
     * @param planId
     * @return
     */
    @DeleteMapping("plan/{planId}")
    public BaseResult deletePlan(@PathVariable Long planId) {
        planService.deletePlan(planId);
        return new BaseResult().setErrmsg("删除成功");
    }

    /**
     * 组合查询
     *
     * @param parameters
     * @return
     */
    @GetMapping("plan")
    public BaseResult findPlans(PurchasePlanService.PlanParameterDto parameters) {
        Page<ErpPurchasePlan> page = planService.query(parameters);
        return page2BaseResult(page);
    }

    /**
     * 获取单个计划单
     *
     * @param planId
     * @return
     */
    @GetMapping("plan/{planId}")
    public BaseResult findPlan(@PathVariable Long planId) {
        return new BaseResult().addPojo(planService.findOnePlan(planId));
    }

    /**
     * 获取单个计划单的历史记录
     *
     * @param planId
     * @return
     */
    @GetMapping("plan/{planId}/history/opt")
    public BaseResult findPlanOptHistory(@PathVariable Long planId) {
        return new BaseResult().addList(planService.findPlanOptHistory(planId));
    }

    /*===========================以下为采购订单===========================*/

    /**
     * 新增采购订单-可包含明细列表
     * @param order
     * @return
     */
    @PostMapping("order")
    public BaseResult createOrder(@RequestBody ErpPurchaseOrder order){
        Assert.notNull(order.getOrderDate(), "订单时间不能为空");
        Assert.notNull(order.getSupplierId(), "未选择客户");
        return new BaseResult().addPojo(orderService.create(order));
    }

    /**
     * 更新采购订单-不包含明细
     * 更新内容： 时间、总额、客户、订单日期
     * 约束条件：状态为未发布、审核退回
     * @param order
     * @return
     */
    @PutMapping("order")
    public BaseResult updateOrder(@RequestBody ErpPurchaseOrder order){
        return new BaseResult().addPojo(orderService.update(order));
    }

    /**
     * 退货-包含明细
     * 更新内容：明细的退货金额、shouldRtNum & 订单的状态->STATUS_009("正在退货")
     * 约束条件：状态为STATUS_005【已入库】
     * @param order
     * @return
     */
    @PutMapping("order/refunds")
    public BaseResult refunds(@RequestBody ErpPurchaseOrder order){
        return new BaseResult().addPojo(orderService.refunds(order));
    }

    //purchase/order/refunds

    /**
     * 删除采购订单-级联删除明细
     * 约束条件：状态为未发布,待审核,审核退回
     * @param orderId
     * @return
     */
    @DeleteMapping("order/{orderId}")
    public BaseResult deleteOrder(@PathVariable Long orderId){
        orderService.deleteOrder(orderId);
        return new BaseResult().setErrmsg("删除成功");
    }

    /**
     * 单个采购订单查询
     * @param orderId
     * @return
     */
    @GetMapping("order/{orderId}")
    public BaseResult findOrder(@PathVariable Long orderId){
        return new BaseResult().addPojo(orderService.findOrder(orderId));
    }

    /**
     * 组合查询-采购订单
     * eg：codes -> a,b,c
     * @param parameters
     * @return history,items
     */
    @GetMapping("order")
    public BaseResult findOrders(PurchaseOrderService.OrderParameterDto parameters){
        Page<ErpPurchaseOrder> page = orderService.query(parameters);
        return page2BaseResult(page);
    }

    /**
     * 更新订单状态：待审核、审核通过、审核退回、客户退货、已入库、已完成
     * 约束条件：未发布 ->待审核.
     *          待审核 ->审核通过、审核退回.
     *          审核退回 ->待审核.
     *          审核通过 ->已入库.
     *          已入库 ->客户退货、已完成.
     * @param orderId
     * @param status 修改后的状态
     * @param remark 备注信息
     * @return
     */
    @PatchMapping("order/{orderId}/{status}")
    public BaseResult updateOrderStatus(@PathVariable Long orderId, @PathVariable PurchaseOrderStatus status, String remark){
        return new BaseResult().addPojo(orderService.updateOrderStatus(orderId, status, remark));
    }

    /**
     * 准备入库-打印入库单之后调用
     * @param orderId
     * @return
     */
    @PatchMapping("order/{orderId}")
    public BaseResult preStockIn(@PathVariable Long orderId){
        return new BaseResult().addPojo(orderService.preStockIn(orderId));
    }

    /**
     * 新增采购订单明细-已有订单的情况下
     * 约束条件：订单状态为未发布、审核退回
     * ps：订单id、code校验，自身属性在service里校验
     * @param item
     * @return
     */
    @PostMapping("orderItem")
    public BaseResult createOrderItem(@RequestBody ErpPurchaseOrderItem item){
        Assert.notNull(item.getOrderId(), "采购订单id不能为空");
        Assert.notNull(item.getOrderCode(), "采购订单编号不能为空");
        return new BaseResult().addPojo(orderService.createOrderItem(item));
    }

    /**
     * 更新采购订单明细-已有订单的情况下
     * 约束条件：订单状态为未发布、审核退回
     * ps：订单id、code校验，自身属性在service里校验
     * @param item
     * @return
     */
    @PutMapping("orderItem")
    public BaseResult updateOrderItem(@RequestBody ErpPurchaseOrderItem item){
        Assert.notNull(item.getOrderId(), "采购订单id不能为空");
        Assert.notNull(item.getOrderCode(), "采购订单编号不能为空");
        Assert.notNull(item.getId(), "采购订单明细id不能为空");
        return new BaseResult().addPojo(orderService.updateOrderItem(item));
    }

    /**
     * 删除采购订单明细-已有订单的情况下
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
     *  获取单个采购订单的历史记录
     * @param orderId
     * @return
     */
    @GetMapping("order/{orderId}/history/opt")
    public BaseResult findOrderOptHistory(@PathVariable Long orderId){
        return new BaseResult().addList(orderService.findOrderOptHistory(orderId));
    }

    /**
     * 获取单个采购订单的明细
     * @param orderId
     * @return
     */
    @GetMapping("order/{orderId}/items")
    public BaseResult findOrderItems(@PathVariable Long orderId){
        return new BaseResult().addList(orderService.findOrderItems(orderId));
    }

    /**
     * 根据供应商和货物类型获取货物下拉列表
     * @param supplierId
     * @param type
     * @return
     */
    @GetMapping("order/lookup/{supplierId}/{type}")
    public BaseResult lookup(@PathVariable Long supplierId, @PathVariable String type){
        return new BaseResult().addList(orderService.findWares(supplierId, WaresType.valueOf(type)));
    }
}

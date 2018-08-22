package com.yintong.erp.web.purchase;

import com.yintong.erp.domain.purchase.ErpPurchasePlan;
import com.yintong.erp.service.purchase.PurchaseOrderService;
import com.yintong.erp.service.purchase.PurchasePlanService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

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

    /*===========================以下为销售订单===========================*/

}

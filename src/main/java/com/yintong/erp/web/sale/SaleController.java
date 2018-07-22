package com.yintong.erp.web.sale;

import com.yintong.erp.domain.sale.ErpSalePlan;
import com.yintong.erp.service.sale.SaleService;
import static com.yintong.erp.service.sale.SaleService.PlanParameterDto;
import com.yintong.erp.utils.base.BaseResult;
import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;
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

/**
 * @author lucifer.chan
 * @create 2018-07-21 下午5:06
 * 销售模块入库
 **/
@RestController
@RequestMapping("sale")
public class SaleController {

    @Autowired SaleService saleService;


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
        return new BaseResult().addPojo(saleService.create(plan));
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
        return new BaseResult().addPojo(saleService.update(plan));
    }

    /**
     * 删除计划单
     * @param planId
     * @return
     */
    @DeleteMapping("plan/{planId}")
    public BaseResult deletePlan(@PathVariable Long planId){
        saleService.deletePlan(planId);
        return new BaseResult().setErrmsg("删除成功");
    }

    /**
     * 组合查询
     * @param parameters
     * @return
     */
    @GetMapping("plan")
    public BaseResult findPlans(PlanParameterDto parameters){
        Page<ErpSalePlan> page = saleService.query(parameters);
        return page2BaseResult(page);
    }

    /**
     * 获取单个计划单
     * @param planId
     * @return
     */
    @GetMapping("plan/{planId}")
    public BaseResult findPlan(@PathVariable Long planId){
        return new BaseResult().addPojo(saleService.findOnePlan(planId));
    }

    /**
     *  获取单个计划单的历史记录
     * @param planId
     * @return
     */
    @GetMapping("plan/{planId}/history/opt")
    public BaseResult findPlanOptHistory(@PathVariable Long planId){
        return new BaseResult().addList(saleService.findPlanOptHistory(planId));
    }

}

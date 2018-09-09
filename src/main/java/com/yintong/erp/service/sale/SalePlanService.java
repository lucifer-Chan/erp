package com.yintong.erp.service.sale;

import com.yintong.erp.domain.sale.ErpSalePlan;
import com.yintong.erp.domain.sale.ErpSalePlanOptLog;
import com.yintong.erp.domain.sale.ErpSalePlanOptLogRepository;
import com.yintong.erp.domain.sale.ErpSalePlanRepository;
import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import com.yintong.erp.validator.OnDeleteProductValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import static com.yintong.erp.utils.query.ParameterItem.COMPARES.like;
import static com.yintong.erp.utils.query.OrderBy.METHOD.asc;
import static javax.persistence.criteria.Predicate.BooleanOperator.OR;

/**
 * @author lucifer.chan
 * @create 2018-07-21 下午4:47
 * 销售计划单服务
 **/
@Service
public class SalePlanService implements OnDeleteProductValidator{

    @Autowired ErpSalePlanOptLogRepository salePlanOptLogRepository;

    @Autowired ErpSalePlanRepository salePlanRepository;

    /**
     * 新增计划单
     * @param plan
     * @return
     */
    public ErpSalePlan create(ErpSalePlan plan) {
        validatePlan(plan);
        ErpSalePlan ret = salePlanRepository.save(plan);

        String content = "新建 金额：" + ret.getPlanMoney() +
                ", 时间：["  + DateUtil.getDateString(plan.getStartDate()) + " 至 " + DateUtil.getDateString(plan.getEndDate()) + "]";
        salePlanOptLogRepository.save(ErpSalePlanOptLog.builder().planId(ret.getId()).content(content).build());
        return ret;
    }

    /**
     * 更新计划单
     * @param plan
     * @return
     */
    public ErpSalePlan update(ErpSalePlan plan) {
        validatePlan(plan);
        ErpSalePlan old = salePlanRepository.findById(plan.getId()).orElse(null);
        Assert.notNull(old, "未找到id为" + plan.getId() + "的销售计划单");
        String content = "更新 ";
        List<String> contents = new ArrayList<>();

        if(!old.getPlanMoney().equals(plan.getPlanMoney())){
            contents.add("金额：¥" + plan.getPlanMoney());
            old.setPlanMoney(plan.getPlanMoney());
        }

        if(!DateUtil.getDateString(old.getStartDate()).equals(DateUtil.getDateString(plan.getStartDate()))
                || !DateUtil.getDateString(old.getEndDate()).equals(DateUtil.getDateString(plan.getEndDate()))) {
            contents.add("时间：[" + DateUtil.getDateString(plan.getStartDate()) + " 至 " + DateUtil.getDateString(plan.getEndDate()) + "]");
            old.setStartDate(plan.getStartDate());
            old.setEndDate(plan.getEndDate());
        }

        if(!old.getRemark().equals(plan.getRemark())){
            old.setRemark(plan.getRemark());
        }

        if(!old.getDescription().equals(plan.getDescription())){
            old.setDescription(plan.getDescription());
        }

        if(!CollectionUtils.isEmpty(contents)){
            content += StringUtils.collectionToDelimitedString(contents, ", ");
        }

        ErpSalePlan ret = salePlanRepository.save(old);
        if(!"更新 ".equals(content)){
            salePlanOptLogRepository.save(ErpSalePlanOptLog.builder().planId(ret.getId()).content(content).build());
        }
        return ret;
    }

    /**
     * 删除计划单
     * @param planId
     */
    @Transactional
    public void deletePlan(Long planId) {
        salePlanRepository.deleteById(planId);
        salePlanOptLogRepository.deleteByPlanId(planId);
    }

    /**
     * 查询单个计划单
     * @param planId
     * @return
     */
    public ErpSalePlan findOnePlan(Long planId){
        ErpSalePlan plan = salePlanRepository.findById(planId).orElse(null);
        Assert.notNull(plan, "未找到销售计划单[" + planId + "]");
        return plan;
    }

    /**
     * 组合查询
     * @param parameters
     * @return
     */
    public Page<ErpSalePlan> query(PlanParameterDto parameters){
        return salePlanRepository.findAll(parameters.specification(), parameters.pageable());
    }

    /**
     * 获取一个销售计划单的历史修改记录
     * @param planId
     * @return
     */
    public List<ErpSalePlanOptLog> findPlanOptHistory(Long planId){
        return salePlanOptLogRepository.findByPlanIdOrderByCreatedAtDesc(planId);
    }

    @Override
    public void onDeleteProduct(Long productId) {
        String codes = salePlanRepository.findByProductId(productId)
                .stream().map(ErpSalePlan::getBarCode).collect(Collectors.joining(","));
        if(StringUtils.hasText(codes)){
            throw new IllegalArgumentException("请先删除采购计划单[" + codes + "]");
        }
    }

    /**
     * 计划单查询入参dto
     */
    @Getter @Setter
    @OrderBy(fieldName = "startDate", method = asc)
    public static class PlanParameterDto extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "description"}, compare = like, group = OR)
        String cause;
    }

    /**
     * 新增或更新时验证计划单有效性
     * @param toSave
     */
    private void validatePlan(ErpSalePlan toSave){
        List<ErpSalePlan> plans = salePlanRepository.findByProductId(toSave.getProductId());
        if(CollectionUtils.isEmpty(plans)) return;
        Long planId = null == toSave.getId() ? -100L : toSave.getId();
        for(ErpSalePlan plan : plans){
            if(!plan.getId().equals(planId)){
                boolean isNotCross =
                        DateUtil.isNotCross(plan.getStartDate(), plan.getEndDate(), toSave.getStartDate(), toSave.getEndDate());
                Assert.isTrue(isNotCross, "时间范围有交集！");

            }
        }
    }
}

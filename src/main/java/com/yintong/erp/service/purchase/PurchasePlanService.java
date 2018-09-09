package com.yintong.erp.service.purchase;

import com.yintong.erp.domain.purchase.ErpPurchasePlan;
import com.yintong.erp.domain.purchase.ErpPurchasePlanOptLog;
import com.yintong.erp.domain.purchase.ErpPurchasePlanOptLogRepository;
import com.yintong.erp.domain.purchase.ErpPurchasePlanRepository;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;

import com.yintong.erp.utils.query.QueryParameterBuilder;
import com.yintong.erp.validator.OnDeleteRawMaterialValidator;
import com.yintong.erp.validator.OnDeleteProductValidator;
import com.yintong.erp.validator.OnDeleteMouldValidator;
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

import static com.yintong.erp.utils.query.OrderBy.METHOD.asc;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.equal;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.like;
import static javax.persistence.criteria.Predicate.BooleanOperator.OR;

/**
 * @author lucifer.chan
 * @create 2018-08-20 上午10:10
 * 采购计划单服务
 **/
@Service
public class PurchasePlanService implements OnDeleteProductValidator
        , OnDeleteRawMaterialValidator, OnDeleteMouldValidator {

    @Autowired ErpPurchasePlanRepository planRepository;

    @Autowired ErpPurchasePlanOptLogRepository planOptLogRepository;

    /**
     * 新增计划单
     * @param plan
     * @return
     */
    @Transactional
    public ErpPurchasePlan create(ErpPurchasePlan plan){
        validatePlan(plan);
        ErpPurchasePlan ret = planRepository.save(plan);
        String content = "新建 数量：" + ret.getPlanNum() +
                ", 预算：¥" + ret.getPlanMoney() +
                ", 时间：["  + DateUtil.getDateString(plan.getStartDate()) + " 至 " + DateUtil.getDateString(plan.getEndDate()) + "]";
        planOptLogRepository.save(ErpPurchasePlanOptLog.builder().planId(ret.getId()).content(content).build());
        return ret;
    }


    /**
     * 更新计划单 货物类型不作修改
     * @param plan
     * @return
     */
    @Transactional
    public ErpPurchasePlan update(ErpPurchasePlan plan) {
        validatePlan(plan);
        ErpPurchasePlan old = planRepository.findById(plan.getId()).orElse(null);
        Assert.notNull(old, "未找到id为" + plan.getId() + "的采购计划单");
        String content = "更新 ";

        List<String> contents = new ArrayList<>();

        if(!old.getPlanNum().equals(plan.getPlanNum())){
            contents.add("数量：" + plan.getPlanNum());
            old.setPlanNum(plan.getPlanNum());
        }

        if(!old.getPlanMoney().equals(plan.getPlanMoney())){
            contents.add("预算：¥" + plan.getPlanMoney());
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

        ErpPurchasePlan ret = planRepository.save(old);
        if(!"更新 ".equals(content)){
            planOptLogRepository.save(ErpPurchasePlanOptLog.builder().planId(ret.getId()).content(content).build());
        }
        return ret;
    }

    /**
     * 删除计划单
     * @param planId
     */
    @Transactional
    public void deletePlan(Long planId) {
        planRepository.deleteById(planId);
        planOptLogRepository.deleteByPlanId(planId);
    }

    /**
     * 查询单个计划单
     * @param planId
     * @return
     */
    public ErpPurchasePlan findOnePlan(Long planId){
        ErpPurchasePlan plan = planRepository.findById(planId).orElse(null);
        Assert.notNull(plan, "未找到采购计划单[" + planId + "]");
        return plan;
    }

    /**
     * 组合查询
     * @param parameters
     * @return
     */
    public Page<ErpPurchasePlan> query(PlanParameterDto parameters){
        return planRepository.findAll(parameters.specification(), parameters.pageable());
    }

    /**
     * 获取一个销售计划单的历史修改记录
     * @param planId
     * @return
     */
    public List<ErpPurchasePlanOptLog> findPlanOptHistory(Long planId){
        return planOptLogRepository.findByPlanIdOrderByCreatedAtDesc(planId);
    }

    /**
     * 计划单查询入参dto
     */
    @Getter @Setter
    @OrderBy(fieldName = "startDate", method = asc)
    public static class PlanParameterDto extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "description", "waresName"}, compare = like, group = OR)
        String cause;
        @ParameterItem(mappingTo = "waresType", compare = equal)
        String type;
    }

    @Override
    public void onDeleteMould(Long mouldId) {
        onDeleteWares(mouldId, Constants.WaresType.D);
    }

    @Override
    public void onDeleteProduct(Long productId) {
        onDeleteWares(productId, Constants.WaresType.P);
    }

    @Override
    public void onDeleteMaterial(Long materialId) {
        onDeleteWares(materialId, Constants.WaresType.M);
    }


    /**
     * 删除货物时的校验
     * @param waresId
     * @param type
     */
    private void onDeleteWares(Long waresId, Constants.WaresType type){
        String codes = planRepository.findByWaresIdAndWaresType(waresId, type.name())
                .stream().map(ErpPurchasePlan::getBarCode).collect(Collectors.joining(","));
        if(StringUtils.hasText(codes)){
            throw new IllegalArgumentException("请先删除采购计划单[" + codes + "]");
        }
    }


    /**
     * 新增或更新时验证计划单有效性
     * @param toSave
     */
    private void validatePlan(ErpPurchasePlan toSave){
        Assert.notNull(toSave.getWaresId(), "物品id不能为空");
        Assert.hasText(toSave.getDescription(), "计划内容不能为空");
        Assert.hasText(toSave.getWaresName(), "货物名称不能为空");
        Assert.hasText(toSave.getWaresType(), "货物类型不能为空");
        Assert.notNull(toSave.getPlanNum(), "数量不能为空");
        Assert.notNull(toSave.getPlanMoney(), "预算不能为空");
        try{
            Constants.WaresType.valueOf(toSave.getWaresType());
        } catch (RuntimeException e){
            throw new IllegalArgumentException("物品类型非法");
        }
        List<ErpPurchasePlan> plans = planRepository.findByWaresIdAndWaresTypeAndCreatedAtIsBetween(toSave.getWaresId(), toSave.getWaresType(), toSave.getStartDate(), toSave.getEndDate());
        if(CollectionUtils.isEmpty(plans)) return;
        Long planId = null == toSave.getId() ? -100L : toSave.getId();
        boolean crossed = plans.stream().anyMatch(plan -> !plan.getId().equals(planId));
        Assert.isTrue(!crossed, "时间范围有交集！");
    }
}

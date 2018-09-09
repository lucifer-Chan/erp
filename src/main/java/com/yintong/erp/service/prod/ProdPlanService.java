package com.yintong.erp.service.prod;

import com.yintong.erp.domain.basis.ErpBaseModelTool;
import com.yintong.erp.domain.basis.ErpBaseModelToolRepository;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplier;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplierRepository;
import com.yintong.erp.domain.prod.ErpProdMould;
import com.yintong.erp.domain.prod.ErpProdMouldRepository;
import com.yintong.erp.domain.prod.ErpProdOrderRepository;
import com.yintong.erp.domain.prod.ErpProdPlan;
import com.yintong.erp.domain.prod.ErpProdPlanOptLog;
import com.yintong.erp.domain.prod.ErpProdPlanOptLogRepository;
import com.yintong.erp.domain.prod.ErpProdPlanRepository;
import com.yintong.erp.domain.prod.ErpProdProductBom;
import com.yintong.erp.domain.prod.ErpProdProductBomRepository;
import com.yintong.erp.dto.BomDto;
import com.yintong.erp.dto.ProdPlanDto;
import com.yintong.erp.service.basis.associator.ProductBomService;
import com.yintong.erp.utils.common.Assert;
import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import com.yintong.erp.validator.OnDeleteSupplierMouldValidator;
import com.yintong.erp.validator.OnDeleteSupplierRawMaterialValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import static com.yintong.erp.utils.common.Constants.ProdBomHolder.PLAN;
import static com.yintong.erp.utils.query.OrderBy.METHOD.asc;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.like;
import static javax.persistence.criteria.Predicate.BooleanOperator.OR;

/**
 * @author lucifer.chan
 * @create 2018-09-03 下午4:24
 * 生产计划单服务
 **/
@Service
public class ProdPlanService implements OnDeleteSupplierRawMaterialValidator, OnDeleteSupplierMouldValidator {

    @Autowired ErpProdPlanRepository planRepository;

    @Autowired ErpProdOrderRepository orderRepository;

    @Autowired ErpProdPlanOptLogRepository planOptLogRepository;

    @Autowired ErpRawMaterialSupplierRepository materialSupplierRepository;

    @Autowired ErpProdProductBomRepository prodProductBomRepository;

    @Autowired ErpProdMouldRepository prodMouldRepository;

    @Autowired ErpBaseModelToolRepository mouldRepository;

    @Autowired ProductBomService productBomService;

    /**
     * 新增计划单
     * @param planDto
     * @return
     */
    @Transactional
    public ErpProdPlan create(ProdPlanDto planDto) {
        ErpProdPlan plan = planDto.getPlan();
        plan.setId(null);
        //计划单
        ErpProdPlan ret = planRepository.save(plan);
        //bom
        List<ErpProdProductBom> bomList = planDto.getBoms().stream()
                .map(source -> ErpProdProductBom.build4Plan(plan, source, this::findMaterialAssId))
                .collect(Collectors.toList());
        Assert.notEmpty(bomList, "物料清单不能为空");
        prodProductBomRepository.saveAll(bomList);
        //操作记录
        String content = "新建计划单 成品数量：" + ret.getPlanNum() +
                ", 时间：["  + DateUtil.getDateString(plan.getStartDate()) + " 至 " + DateUtil.getDateString(plan.getEndDate()) + "]";
        planOptLogRepository.save(ErpProdPlanOptLog.builder().planId(ret.getId()).content(content).build());
        return ret;
    }

    /**
     * 修改计划单 - 不修改成品
     * @param planDto boms包含id
     * @return
     */
    @Transactional
    public ErpProdPlan update(ProdPlanDto planDto) {
        ErpProdPlan plan = planDto.getPlan();
        Assert.notNull(plan.getId(), "请选择生产计划单");
        ErpProdPlan oldPlan = planRepository.findById(plan.getId()).orElse(null);
        Assert.notNull(oldPlan, "请选择生产计划单");

        String content = "更新";
        List<String> contents = new ArrayList<>();
        if(!oldPlan.getPlanNum().equals(plan.getPlanNum())){
            contents.add("数量：¥" + plan.getPlanNum());
            oldPlan.setPlanNum(plan.getPlanNum());
        }

        if(!DateUtil.getDateString(oldPlan.getStartDate()).equals(DateUtil.getDateString(plan.getStartDate()))
                || !DateUtil.getDateString(oldPlan.getEndDate()).equals(DateUtil.getDateString(plan.getEndDate()))) {
            contents.add("时间：[" + DateUtil.getDateString(plan.getStartDate()) + " 至 " + DateUtil.getDateString(plan.getEndDate()) + "]");
            oldPlan.setStartDate(plan.getStartDate());
            oldPlan.setEndDate(plan.getEndDate());
        }

        if(!oldPlan.getRemark().equals(plan.getRemark())){
            oldPlan.setRemark(plan.getRemark());
        }

        if(!oldPlan.getDescription().equals(plan.getDescription())){
            oldPlan.setDescription(plan.getDescription());
        }

        //计划单
        ErpProdPlan ret = planRepository.save(oldPlan);

        //bom - 制令单存在的情况下不允许修改bom
        if(CollectionUtils.isEmpty(plan.getProdOrders())) {
            Map<Long, BomDto> bomDtoMap = planDto.getBoms().stream().collect(Collectors.toMap(BomDto::getId, Function.identity()));
            List<ErpProdProductBom> bomList = prodProductBomRepository.findByIdIn(bomDtoMap.keySet()).stream()
                    .map(bom -> bom.copy4UpdatePlan(bomDtoMap.get(bom.getId()), this::findMaterialAssId))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(bomList)) {
                prodProductBomRepository.saveAll(bomList);
                contents.add("物料清单");
            }
        }
        //操作记录
        if(!CollectionUtils.isEmpty(contents)){
            content += StringUtils.collectionToCommaDelimitedString(contents);
            planOptLogRepository.save(ErpProdPlanOptLog.builder().planId(ret.getId()).content(content).build());
        }

        return ret;
    }

    /**
     * 删除计划单 - 有制令单的不允许删除
     * @param planId
     */
    @Transactional
    public void deletePlan(Long planId) {
        ErpProdPlan plan = planRepository.findById(planId).orElse(null);
        Assert.notNull(plan, "未找到[" + planId + "]计划单");
        Assert.isEmpty(plan.getProdOrders(), "该计划单已有制令单，不能删除");
        //计划单
        planRepository.deleteById(planId);
        //bom
        prodProductBomRepository.deleteByHolderAndHolderId(PLAN.name(), planId);
        //mould
        prodMouldRepository.deleteByHolderAndHolderId(PLAN.name(), planId);
        //操作记录
        planOptLogRepository.deleteByPlanId(planId);
    }

    /**
     * 新增／修改 模具
     * @param planId
     * @param mould
     * @return
     */
    @Transactional
    public ErpProdPlan saveMould(Long planId, ErpProdMould mould) {
        ErpProdPlan plan = findOnePlan(planId);
        //1-已有制令单的不允许修改或新增
        Assert.isEmpty(plan.getProdOrders(), "该计划单已有制令单，不能修改或新增模具");

        mould.setHolder(PLAN.name());
        mould.setHolderId(planId);
        mould.setProductId(plan.getProductId());
        mould.validate();
        Long realityMouldId = mould.getRealityMouldId();
        String mouldName = mouldRepository.findById(mould.getMouldId()).orElse(new ErpBaseModelTool()).getSimpleName();
        Assert.hasText(mouldName, "未找到模具[" + mould.getMouldId() + "]");
        List<ErpProdMould> existMoulds = prodMouldRepository.findByHolderAndHolderIdAndRealityMouldId(PLAN.name(), planId, realityMouldId);
        //新增
        if(Objects.isNull(mould.getId())){
            Assert.isEmpty(existMoulds, "已存在该模具，请重新选择");
            planOptLogRepository.save(ErpProdPlanOptLog.builder().planId(planId).content("新增模具：" + mouldName + ",数量：" + mould.getRealityMouldNum()).build());

        } else {
            //更新
            ErpProdMould old = prodMouldRepository.findById(mould.getId()).orElse(null);
            Assert.notNull(old, "未找到计划单模具[" + mould.getId() + "]");
            mould.copyBase(old);
            Assert.isTrue(existMoulds.stream().map(ErpProdMould::getId).filter(id -> !id.equals(mould.getId())).collect(Collectors.toList()).isEmpty(), "已存在该模具，请重新选择");
            List<String> contents = new ArrayList<>();

            if(!ObjectUtils.equals(old.getMouldId(), mould.getMouldId())){
                String oldMouldName = mouldRepository.findById(old.getMouldId()).orElse(new ErpBaseModelTool()).getSimpleName();
                contents.add(oldMouldName + "->" + mouldName);
            }

            if(!old.getRealityMouldNum().equals(mould.getRealityMouldNum())){
                contents.add(mouldName + "数量：" + old.getRealityMouldNum() + "->" + mould.getRealityMouldNum());
            }

            if(!CollectionUtils.isEmpty(contents)){
                planOptLogRepository.save(ErpProdPlanOptLog.builder().planId(planId).content("更新模具: " + contents.stream().collect(Collectors.joining(","))).build());
            }
        }

        prodMouldRepository.save(mould);
        return findOnePlan(planId);
    }

    /**
     * 删除 模具
     * @param id
     */
    @Transactional
    public ErpProdPlan deleteMould(Long id) {
        ErpProdMould mould = prodMouldRepository.findById(id).orElse(null);
        Assert.notNull(mould, "未找到计划单模具[" + id + "]");
        ErpProdPlan plan = findOnePlan(mould.getHolderId());
        //1-已有制令单的不允许修改或新增
        Assert.isEmpty(plan.getProdOrders(), "该计划单已有制令单，不能删除模具");
        String mouldName = mouldRepository.findById(mould.getMouldId()).orElse(new ErpBaseModelTool()).getSimpleName();
        planOptLogRepository.save(ErpProdPlanOptLog.builder().planId(mould.getHolderId()).content("删除模具：" + mouldName).build());
        prodMouldRepository.deleteById(id);
        return findOnePlan(mould.getHolderId());
    }

    @Override
    public void onDeleteSupplierMould(Long id) {
        List<Long> planIds = prodMouldRepository.findByHolderAndRealityMouldId(PLAN.name(), id).stream().map(ErpProdMould::getHolderId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(planIds)) return;
        String codes = planRepository.findByIdIn(planIds).stream().map(ErpProdPlan::getBarCode).collect(Collectors.joining(","));
        Assert.isEmpty(codes, "请先删除生产计划单[" + codes + "]");
    }


    @Override
    public void onDeleteSupplierRawMaterial(Long id) {
        List<Long> planIds = prodProductBomRepository.findByHolderAndRealityMaterialId(PLAN.name(), id).stream().map(ErpProdProductBom::getHolderId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(planIds)) return;
        String codes = planRepository.findByIdIn(planIds).stream().map(ErpProdPlan::getBarCode).collect(Collectors.joining(","));
        Assert.isEmpty(codes, "请先删除生产计划单[" + codes + "]的物料清单关联");
    }

    /**
     * 组合查询
     * @param parameters
     * @return
     */
    public Page<ErpProdPlan> query(PlanParameterDto parameters){
        return planRepository.findAll(parameters.specification(), parameters.pageable());
    }

    /**
     * 查询单个计划单
     * @param planId
     * @return
     */
    public ErpProdPlan findOnePlan(Long planId){
        ErpProdPlan plan = planRepository.findById(planId).orElse(null);
        Assert.notNull(plan, "未找到生产计划单[" + planId + "]");
        return plan;
    }

    /**
     * 计划单的操作记录
     * @param planId
     * @return
     */
    public List<ErpProdPlanOptLog> findPlanOptHistory(Long planId){
        return planOptLogRepository.findByPlanIdOrderByCreatedAtDesc(planId);
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
    public List<BomDto> findBomList(Long planId, Long productId) {
        //新建计划单的情况
        if(-1 == planId){
            return productBomService.findBomList(productId).stream().map(pb ->
                BomDto.builder()
                        .material(pb.getMaterial())
                        .materialId(pb.getMaterialId())
                        .num(0D)
                        .materialNum(pb.getMaterialNum())
                    .build()
            ).collect(Collectors.toList());
        }
        //更新
        return findOnePlan(planId).getBoms().stream().map(b->
                BomDto.builder()
                        .id(b.getId())
                        .supplierId(b.getSupplierId())
                        .supplierName(b.getSupplierName())
                        .material(b.getMaterial())
                        .materialId(b.getMaterialId())
                        .num(b.getRealityMaterialNum())
                        .materialNum(b.getMaterialNum())
                    .build()
        ).collect(Collectors.toList());
    }

    /**
     * 查找供应商和原材料相关的关联id
     * @param supplierId
     * @param materialId
     * @return
     */
    public Long findMaterialAssId(Long supplierId, Long materialId){
        ErpRawMaterialSupplier ass = materialSupplierRepository.findByRawMaterIdAndSupplierId(materialId, supplierId).orElse(null);
        return Objects.nonNull(ass) ? ass.getId() : null;
    }

    /**
     * 计划单查询入参dto
     */
    @Getter @Setter
    @OrderBy(fieldName = "startDate", method = asc)
    public static class PlanParameterDto extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "description", "productName"}, compare = like, group = OR)
        String cause;
    }

}

package com.yintong.erp.service.basis.associator;

import com.yintong.erp.domain.basis.ErpBaseModelTool;
import com.yintong.erp.domain.basis.ErpBaseModelToolRepository;
import com.yintong.erp.domain.basis.ErpBaseSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpModelSupplier;
import com.yintong.erp.domain.basis.associator.ErpModelSupplierRepository;
import com.yintong.erp.dto.TreeNode;
import com.yintong.erp.service.basis.CategoryService;
import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.validator.OnDeleteMouldValidator;
import com.yintong.erp.validator.OnDeleteSupplierMouldValidator;
import com.yintong.erp.validator.OnDeleteSupplierValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by jianqiang on 2018/6/17.
 */
@Service
public class SupplierMouldService implements OnDeleteMouldValidator , OnDeleteSupplierValidator {

    @Autowired ErpBaseSupplierRepository supplierRepository;
    @Autowired ErpBaseModelToolRepository modelToolRepository;
    @Autowired ErpModelSupplierRepository modelSupplierRepository;
    @Autowired CategoryService categoryService;
    @Autowired(required = false) List<OnDeleteSupplierMouldValidator> onDeleteSupplierMouldValidator;

    /**
     * 建立模具和供应商的关联
     * @param association
     */
    public void save(ErpModelSupplier association){
        ErpModelSupplier shouldBeNull = modelSupplierRepository
                .findByModelIdAndSupplierId(association.getModelId(), association.getSupplierId())
                .orElse(null);
        if(Objects.isNull(shouldBeNull))
            modelSupplierRepository.save(association);
    }

    /**
     * 根据barcode查找供应商和模具的关联
     * @param barcode
     * @return
     */
    public ErpModelSupplier findByBarcode(String barcode){
        ErpModelSupplier association = modelSupplierRepository.findByBarCode(barcode).orElse(null);
        Assert.notNull(association, "未找到模具和供应商的关联");
        return association;
    }

    /**
     * 批量保存
     * @param associations
     */
    @Transactional
    public void batchSave(List<ErpModelSupplier> associations) {
        associations.forEach(this::save);
    }

    /**
     * 批量保存
     * @param supplierId
     * @param modelIds
     */
    @Transactional
    public void batchSave(Long supplierId, List<Long> modelIds) {
        String supplierTypeCode3 = supplierRepository.getOne(supplierId).getSupplierTypeCode().substring(2,3);
        batchSave(
                modelIds.stream()
                        .map(modelId ->
                                ErpModelSupplier.builder()
                                        .modelType(modelToolRepository.getOne(modelId).getModelToolTypeCode())
                                        .modelId(modelId)
                                        .supplierType(supplierTypeCode3)
                                        .supplierId(supplierId)
                                        .associateAt(new Date())
                                        .build()
                        ).collect(Collectors.toList())
        );
    }

    /**
     * 删除关联
     * @param modelId
     * @param supplierId
     */
    public void delete(Long modelId, Long supplierId){
        ErpModelSupplier one = modelSupplierRepository.findByModelIdAndSupplierId(modelId, supplierId).orElse(null);
        Assert.notNull(one, "未找到关联");
        if(!org.apache.commons.collections4.CollectionUtils.isEmpty(onDeleteSupplierMouldValidator))
            onDeleteSupplierMouldValidator
                    .forEach(validator -> validator.onDeleteSupplierMould(one.getId()));
        modelSupplierRepository.delete(one);
    }

    /**
     * 根据模具id删除
     * @param modelId
     */
    public void deleteByModelId(Long modelId){
        modelSupplierRepository.deleteByModelId(modelId);
    }

    /**
     * 根据供应商id删除
     * @param supplierId
     */
    public void deleteBySupplierId(Long supplierId){
        modelSupplierRepository.deleteBySupplierId(supplierId);
    }




    @Override
    public void onDeleteMould(Long mouldId) {
        Assert.isTrue(
                CollectionUtils.isEmpty(modelSupplierRepository.findByModelId(mouldId)),
                "请先删除模具和供应商之间的关联。"
        );
    }

    @Override
    public void onDeleteSupplier(Long supplierId) {
        Assert.isTrue(
                CollectionUtils.isEmpty(modelSupplierRepository.findBySupplierId(supplierId)),
                "请先删除供应商和模具之间的关联."
        );
    }
    /**
     * 所有模具的节点-包括枝节点
     * @return
     */
    public List<TreeNode> mouldTreeNodes(){
        return nodes(null);
    }

    /**
     * 根据供应商id获取所有的未关联的树
     * @param supplierId
     * @return
     */
    public List<TreeNode> unAssociatedNodes(Long supplierId){
        return nodes(node -> !associatedMouldIds(supplierId).contains(node.getCode()));
    }

    /**
     * 根据供应商id获取所有的已关联的树
     * @param supplierId
     * @return
     * TODO 修改为只显示拥有的类别，而非全类别
     */
    public List<TreeNode> associatedNodes(Long supplierId){

        List<TreeNode> leaves = modelSupplierRepository.findBySupplierId(supplierId)
                .stream()
                .map(ass -> {
                    String parentCode = ass.getModelType();
                    Long modelId = ass.getModelId();
                    ErpBaseModelTool model = modelToolRepository.getOne(modelId);
                    TreeNode treeNode = new TreeNode(ass.getModelId() + "", model.getDescription(), parentCode, false)
                            .setSource(ass.filter("alertUpper", "alertLower", "associateAt", "totalNum"));
                    return treeNode
                            .setFullName(treeNode.getName())
                            .setName(treeNode.getName() + "[" + CommonUtil.ifNotPresent(ass.getAlertLower(), 0) + "," + CommonUtil.ifNotPresent(ass.getAlertUpper(),0) + "]");
                })
                .collect(Collectors.toList());
        List<TreeNode> branch = branch().stream()
                .filter(node -> leaves.stream().map(TreeNode::getParentCode).collect(Collectors.toSet()).contains(node.getCode()))
                .collect(Collectors.toList());
        branch.addAll(leaves);
        return branch;
    }

    /**
     * 获取供应商所有的模具关联->mouldId
     * @param supplierId
     * @return
     */
    private List<String> associatedMouldIds(Long supplierId){
        return modelSupplierRepository.findBySupplierId(supplierId)
                .stream()
                .map(ErpModelSupplier::getModelId)
                .map(l -> l + "")
                .collect(Collectors.toList());
    }

    private List<TreeNode> nodes(Predicate<TreeNode> filter){
        Stream<TreeNode> leafStream = modelToolRepository.findAll()
                .stream()
                .map(mould ->
                        new TreeNode(mould.getId() + "", mould.getDescription(), mould.getModelToolTypeCode(), false)
                );
        List<TreeNode> leaves =  Objects.isNull(filter) ?
                leafStream.collect(Collectors.toList()) :
                leafStream.filter(filter).collect(Collectors.toList());
        List<TreeNode> branch = branch();
        branch.addAll(leaves);
        return branch;
    }

    private List<TreeNode> branch(){
        return categoryService.append("D", new ArrayList<>())
                .stream()
                .map(category -> new TreeNode(category.getCode(), category.getName(), category.getParentCode(), true))
                .collect(Collectors.toList());
    }

    /**
     * 余量
     * @param mouldAssId
     * @return
     */
    public double stockRemain(Long mouldAssId) {
        ErpModelSupplier ass = modelSupplierRepository.findById(mouldAssId).orElse(null);
        Assert.notNull(ass, "未找到模具");
        return ass.getTotalNum();
    }
}

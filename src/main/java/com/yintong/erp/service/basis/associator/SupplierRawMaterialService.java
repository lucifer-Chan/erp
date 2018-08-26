package com.yintong.erp.service.basis.associator;

import com.yintong.erp.domain.basis.ErpBaseRawMaterial;
import com.yintong.erp.domain.basis.ErpBaseRawMaterialRepository;
import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.domain.basis.ErpBaseSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplier;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplierRepository;
import com.yintong.erp.dto.TreeNode;
import com.yintong.erp.service.basis.CategoryService;
import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.validator.OnDeleteRawMaterialValidator;
import com.yintong.erp.validator.OnDeleteSupplierRawMaterialValidator;
import com.yintong.erp.validator.OnDeleteSupplierValidator;
import java.util.Comparator;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.apache.commons.collections4.KeyValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.util.StringUtils;

/**
 * Created by jianqiang on 2018/6/16.
 * 供应商原材料关联的服务
 */
@Service
public class SupplierRawMaterialService implements OnDeleteRawMaterialValidator, OnDeleteSupplierValidator {

    @Autowired
    ErpBaseSupplierRepository supplierRepository;
    @Autowired
    ErpBaseRawMaterialRepository rawMaterialRepository;
    @Autowired
    ErpRawMaterialSupplierRepository rawMaterialSupplierRepository;
    @Autowired
    CategoryService categoryService;
    @Autowired(required = false)
    List<OnDeleteSupplierRawMaterialValidator> onDeleteSupplierRawMeterials;

    /**
     * 建立供应商和原材料的关联
     * @param materialId
     * @param supplierId
     * @param up
     * @param low
     * @return
     */
    public boolean save(Long materialId, Long supplierId, Integer up, Integer low) {

        String supplierTypeCode3 = supplierRepository.getOne(supplierId).getSupplierTypeCode().substring(2,3);

        ErpRawMaterialSupplier association = ErpRawMaterialSupplier.builder()
                .rawMaterType(rawMaterialRepository.getOne(materialId).getRawTypeCode())
                .rawMaterId(materialId)
                .supplierType(supplierTypeCode3)
                .supplierId(supplierId)
                .associateAt(new Date())
                .alertLower(low)
                .alertUpper(up)
                .build();
        return save(association);
    }

    /**
     * 一个关联后的原材料的描述
     * @param assId
     * @return 供应商-名称-规格
     */
    public String description(Long assId){
        try {
            ErpRawMaterialSupplier ass = rawMaterialSupplierRepository.getOne(assId);
            ErpBaseSupplier supplier = supplierRepository.getOne(ass.getSupplierId());
            ErpBaseRawMaterial material = rawMaterialRepository.getOne(ass.getRawMaterId());
            return supplier.getSupplierName() + "【" + material.getDescription() + "】";
        } catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 所有关联的原材料-厂家
     * @return assId：供应商-名称-规格
     */
    public List<KeyValue<Long, String>> descriptions(){

        Map<Long, ErpBaseSupplier> supplierMap = supplierRepository.findAll().stream().collect(Collectors.toMap(ErpBaseSupplier::getId, supplier -> supplier));
        Map<Long, ErpBaseRawMaterial> materialMap = rawMaterialRepository.findAll().stream().collect(Collectors.toMap(ErpBaseRawMaterial::getId, material -> material));

        return rawMaterialSupplierRepository.findAll().stream()
                .sorted(Comparator.comparing(ErpRawMaterialSupplier::getSupplierId))
                .map(ass -> new KeyValue<Long, String>() {

                    Long key; String value;

                    @Override
                    public Long getKey() {
                        return ass.getId();
                    }

                    @Override
                    public String getValue() {
                        ErpBaseSupplier supplier = supplierMap.get(ass.getSupplierId());
                        ErpBaseRawMaterial material = materialMap.get(ass.getRawMaterId());
                        return (Objects.isNull(supplier) || Objects.isNull(material)) ? ""
                                : supplier.getSupplierName() + "【" + material.getDescription() + "】";
                    }
                })
                .filter(keyValue -> StringUtils.hasText(keyValue.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 批量保存
     * @param associations
     */
    @Transactional
    public void batchSave(List<ErpRawMaterialSupplier> associations) {
        associations.forEach(this::save);
    }

    /**
     * 批量保存
     * @param supplierId
     * @param rawMaterIds
     */
    @Transactional
    public void batchSave(Long supplierId, List<Long> rawMaterIds) {
        String supplierTypeCode3 = supplierRepository.getOne(supplierId).getSupplierTypeCode().substring(2,3);
        batchSave(
                rawMaterIds.stream()
                        .map(rawMaterId ->
                                ErpRawMaterialSupplier.builder()
                                        .rawMaterType(rawMaterialRepository.getOne(rawMaterId).getRawTypeCode())
                                        .rawMaterId(rawMaterId)
                                        .supplierType(supplierTypeCode3)
                                        .supplierId(supplierId)
                                        .associateAt(new Date())
                                        .build()
                        ).collect(Collectors.toList())
        );
    }

    /**
     * 删除关联
     * @param rawMaterId
     * @param supplierId
     */
    public void delete(Long rawMaterId, Long supplierId){
        ErpRawMaterialSupplier one = rawMaterialSupplierRepository.findByRawMaterIdAndSupplierId(rawMaterId, supplierId).orElse(null);
        Assert.notNull(one, "未找到关联");
        if(!org.apache.commons.collections4.CollectionUtils.isEmpty(onDeleteSupplierRawMeterials))
            onDeleteSupplierRawMeterials
                    .forEach(validator -> validator.onDeleteSupplierRawMaterial(one.getId()));
        rawMaterialSupplierRepository.delete(one);
    }

    /**
     * 根据原材料id删除
     * @param rawMaterId
     */
    public void deleteByRawMaterId(Long rawMaterId){
        rawMaterialSupplierRepository.deleteByRawMaterId(rawMaterId);
    }

    /**
     * 根据供应商id删除
     * @param supplierId
     */
    public void deleteBySupplierId(Long supplierId){
        rawMaterialSupplierRepository.deleteBySupplierId(supplierId);
    }


    @Override
    public void onDeleteMaterial(Long rawMaterialId) {
        Assert.isTrue(
                CollectionUtils.isEmpty(rawMaterialSupplierRepository.findByRawMaterId(rawMaterialId)),
                "请先删除原材料和供应商之间的关联。"
        );
    }

    @Override
    public void onDeleteSupplier(Long supplierId) {
        Assert.isTrue(
                CollectionUtils.isEmpty(rawMaterialSupplierRepository.findBySupplierId(supplierId)),
                "请先删除供应商和原材料之间的关联."
        );
    }

    /**
     * 所有原材料的节点-包括枝节点
     * @return
     */
    public List<TreeNode> rawMaterTreeNodes(){
        return nodes(null);
    }

    /**
     * 根据供应商id获取所有的未关联的树
     * @param supplierId
     * @return
     */
    public List<TreeNode> unAssociatedNodes(Long supplierId){
        return nodes(node -> !associatedRawMaterIds(supplierId).contains(node.getCode()));
    }

    /**
     * 根据供应商id获取所有的已关联的树，只显示拥有的类别，而非全类别
     * @param supplierId
     * @return
     */
    public List<TreeNode> associatedNodes(Long supplierId){

        List<TreeNode> leaves = rawMaterialSupplierRepository.findBySupplierId(supplierId)
                .stream()
                .map(ass -> {
                    String parentCode = ass.getRawMaterType();
                    Long rawMaterId = ass.getRawMaterId();
                    ErpBaseRawMaterial rawMaterial = rawMaterialRepository.getOne(rawMaterId);
                    TreeNode treeNode = new TreeNode(ass.getRawMaterId() + "", rawMaterial.getDescription(), parentCode, false)
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
     * 获取供应商所有的原材料关联->RawMaterId
     * @param supplierId
     * @return
     */
    private List<String> associatedRawMaterIds(Long supplierId){
        return rawMaterialSupplierRepository.findBySupplierId(supplierId)
                .stream()
                .map(ErpRawMaterialSupplier::getRawMaterId)
                .map(l -> l + "")
                .collect(Collectors.toList());
    }

    private List<TreeNode> nodes(Predicate<TreeNode> filter){
        Stream<TreeNode> leafStream = rawMaterialRepository.findAll()
                .stream()
                .map(rawMaterial ->
                        new TreeNode(rawMaterial.getId() + "", rawMaterial.getDescription(), rawMaterial.getRawTypeCode(), false)
                );
        List<TreeNode> leaves =  Objects.isNull(filter) ?
                leafStream.collect(Collectors.toList()) :
                leafStream.filter(filter).collect(Collectors.toList());
        List<TreeNode> branch = branch();
        branch.addAll(leaves);
        return branch;
    }

    private List<TreeNode> branch(){
        return categoryService.append("M", new ArrayList<>())
                .stream()
                .map(category -> new TreeNode(category.getCode(), category.getName(), category.getParentCode(), true))
                .collect(Collectors.toList());
    }

    /**
     * 建立供应商和原材料的关联
     * @param association
     * @return
     */
    private boolean save(ErpRawMaterialSupplier association){
        ErpRawMaterialSupplier shouldBeNull = rawMaterialSupplierRepository
                .findByRawMaterIdAndSupplierId(association.getRawMaterId(), association.getSupplierId())
                .orElse(null);
        if(Objects.nonNull(shouldBeNull)){
            return false;
        }
        rawMaterialSupplierRepository.save(association);
        return true;
    }

    /**
     * 查询余量
     * @param materialAssId
     * @return safe,total
     */
    public double stockRemain(Long materialAssId) {
        ErpRawMaterialSupplier ass = rawMaterialSupplierRepository.findById(materialAssId).orElse(null);
        Assert.notNull(ass, "未找到原材料");
        return ass.getTotalNum();
    }
}

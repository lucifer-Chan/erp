package com.yintong.erp.service.basis.associator;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.domain.basis.ErpBaseSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpEndProductSupplier;
import com.yintong.erp.domain.basis.associator.ErpEndProductSupplierRepository;
import com.yintong.erp.dto.TreeNode;
import com.yintong.erp.service.basis.CategoryService;
import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.validator.OnDeleteProductValidator;
import com.yintong.erp.validator.OnDeleteSupplierProductValidator;
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
 * @author lucifer.chan
 * @create 2018-06-03 下午7:02
 * 供应商成品关联的服务
 **/
@Service
public class SupplierProductService implements OnDeleteProductValidator, OnDeleteSupplierValidator {

    @Autowired ErpBaseSupplierRepository supplierRepository;

    @Autowired ErpBaseEndProductRepository productRepository;

    @Autowired ErpEndProductSupplierRepository productSupplierRepository;

    @Autowired CategoryService categoryService;

    @Autowired(required = false) List<OnDeleteSupplierProductValidator> onDeleteSupplierProductValidators;

    /**
     * 建立供应商和成品的关联
     * @param association
     */
    public void save(ErpEndProductSupplier association){
        ErpEndProductSupplier shouldBeNull = productSupplierRepository
                .findByEndProductIdAndSupplierId(association.getEndProductId(), association.getSupplierId())
                .orElse(null);
        if(Objects.isNull(shouldBeNull))
            productSupplierRepository.save(association);
    }

    /**
     * 根据barcode查找供应商和成品的关联
     * @param barcode
     * @return
     */
    public ErpEndProductSupplier findByBarcode(String barcode){
        ErpEndProductSupplier association = productSupplierRepository.findByBarCode(barcode).orElse(null);
        Assert.notNull(association, "未找到成品和供应商的关联");
        return association;
    }

    /**
     * 批量保存
     * @param associations
     */
    @Transactional
    public void batchSave(List<ErpEndProductSupplier> associations) {
        associations.forEach(this::save);
    }

    /**
     * 批量保存
     * @param supplierId
     * @param productIds
     */
    @Transactional
    public void batchSave(Long supplierId, List<Long> productIds) {
        String supplierTypeCode3 = supplierRepository.getOne(supplierId).getSupplierTypeCode().substring(2,3);
        batchSave(
                productIds.stream()
                        .map(productId ->
                                ErpEndProductSupplier.builder()
                                        .endProductType(productRepository.getOne(productId).getEndProductTypeCode())
                                        .endProductId(productId)
                                        .supplierType(supplierTypeCode3)
                                        .supplierId(supplierId)
                                        .associateAt(new Date())
                                        .build()
                        ).collect(Collectors.toList())
        );
    }

    /**
     * 删除关联
     * @param productId
     * @param supplierId
     */
    public void delete(Long productId, Long supplierId){
        ErpEndProductSupplier one = productSupplierRepository.findByEndProductIdAndSupplierId(productId, supplierId).orElse(null);
        Assert.notNull(one, "未找到关联");
        if(!CollectionUtils.isEmpty(onDeleteSupplierProductValidators))
            onDeleteSupplierProductValidators
                    .forEach(validator -> validator.onDeleteSupplierProduct(one.getId()));
        productSupplierRepository.delete(one);
    }

    /**
     * 根据成品id删除
     * @param productId
     */
    public void deleteByProductId(Long productId){
        productSupplierRepository.deleteByEndProductId(productId);
    }

    /**
     * 根据供应商id删除
     * @param supplierId
     */
    public void deleteBySupplierId(Long supplierId){
        productSupplierRepository.deleteBySupplierId(supplierId);
    }

    @Override
    public void onDeleteProduct(Long productId) {
        Assert.isTrue(
                CollectionUtils.isEmpty(productSupplierRepository.findByEndProductId(productId)),
                "请先删除成品和供应商之间的关联。"
        );
    }

    @Override
    public void onDeleteSupplier(Long supplierId) {
        Assert.isTrue(
                CollectionUtils.isEmpty(productSupplierRepository.findBySupplierId(supplierId)),
                "请先删除供应商和成品之间的关联."
        );
    }

    /**
     * 所有成品的节点-包括枝节点
     * @return
     */
    public List<TreeNode> productTreeNodes(){
        return nodes(null);
    }

    /**
     * 根据供应商id获取所有的未关联的树
     * @param supplierId
     * @return
     */
    public List<TreeNode> unAssociatedNodes(Long supplierId){
        return nodes(node -> !associatedProductIds(supplierId).contains(node.getCode()));
    }

    /**
     * 根据供应商id获取所有的已关联的树
     * @param supplierId
     * @return
     *
     */
    public List<TreeNode> associatedNodes(Long supplierId){

        List<TreeNode> leaves = productSupplierRepository.findBySupplierId(supplierId)
                .stream()
                .map(ass -> {
                    String parentCode = ass.getEndProductType();
                    Long productId = ass.getEndProductId();
                    ErpBaseEndProduct product = productRepository.getOne(productId);
                    TreeNode treeNode = new TreeNode(ass.getEndProductId() + "", product.getDescription(), parentCode, false)
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
     * 获取供应商所有的成品关联->productId
     * @param supplierId
     * @return
     */
    private List<String> associatedProductIds(Long supplierId){
        return productSupplierRepository.findBySupplierId(supplierId)
                .stream()
                .map(ErpEndProductSupplier::getEndProductId)
                .map(l -> l + "")
                .collect(Collectors.toList());
    }

    private List<TreeNode> nodes(Predicate<TreeNode> filter){
        Stream<TreeNode> leafStream = productRepository.findAll()
                .stream()
                .map(product ->
                        new TreeNode(product.getId() + "", product.getDescription(), product.getEndProductTypeCode(), false)
                );
        List<TreeNode> leaves =  Objects.isNull(filter) ?
                leafStream.collect(Collectors.toList()) :
                leafStream.filter(filter).collect(Collectors.toList());
        List<TreeNode> branch = branch();
        branch.addAll(leaves);
        return branch;
    }

    private List<TreeNode> branch(){
        return categoryService.append("P", new ArrayList<>())
                .stream()
                .map(category -> new TreeNode(category.getCode(), category.getName(), category.getParentCode(), true))
                .collect(Collectors.toList());
    }
}

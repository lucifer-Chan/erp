package com.yintong.erp.service.purchase;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.basis.ErpBaseEndProductRepository;
import com.yintong.erp.domain.basis.ErpBaseModelTool;
import com.yintong.erp.domain.basis.ErpBaseModelToolRepository;
import com.yintong.erp.domain.basis.ErpBaseRawMaterial;
import com.yintong.erp.domain.basis.ErpBaseRawMaterialRepository;
import com.yintong.erp.domain.basis.associator.ErpEndProductSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpModelSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplierRepository;
import com.yintong.erp.domain.purchase.ErpPurchaseOrder;
import com.yintong.erp.domain.purchase.ErpPurchaseOrderItem;
import com.yintong.erp.domain.purchase.ErpPurchaseOrderItemRepository;
import com.yintong.erp.domain.purchase.ErpPurchaseOrderOptLog;
import com.yintong.erp.domain.purchase.ErpPurchaseOrderOptLogRepository;
import com.yintong.erp.domain.purchase.ErpPurchaseOrderRepository;
import com.yintong.erp.domain.stock.ErpStockInOrder;
import com.yintong.erp.domain.stock.ErpStockInOrderRepository;
import com.yintong.erp.domain.stock.StockEntity;
import com.yintong.erp.service.stock.StockIn4Holder;
import com.yintong.erp.utils.base.JsonWrapper;
import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.utils.common.Constants.PurchaseOrderStatus;
import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import com.yintong.erp.validator.OnDeleteSupplierMouldValidator;
import com.yintong.erp.validator.OnDeleteSupplierProductValidator;
import com.yintong.erp.validator.OnDeleteSupplierRawMaterialValidator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONObject;
import org.apache.commons.collections4.KeyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import static com.yintong.erp.utils.common.Constants.*;
import static com.yintong.erp.utils.common.Constants.PurchaseOrderStatus.*;
import static com.yintong.erp.utils.common.Constants.StockHolder.BUY;
import static com.yintong.erp.utils.common.Constants.WaresType;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.equal;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.in;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.like;
import static javax.persistence.criteria.Predicate.BooleanOperator.OR;

/**
 * @author lucifer.chan
 * @create 2018-08-20 上午9:41
 * 采购订单服务
 **/
@Service
public class PurchaseOrderService implements StockIn4Holder,
        OnDeleteSupplierRawMaterialValidator, OnDeleteSupplierMouldValidator, OnDeleteSupplierProductValidator {

    @Autowired ErpPurchaseOrderRepository orderRepository;

    @Autowired ErpPurchaseOrderItemRepository orderItemRepository;

    @Autowired ErpPurchaseOrderOptLogRepository orderOptLogRepository;

    @Autowired ErpStockInOrderRepository stockInOrderRepository;

    @Autowired ErpEndProductSupplierRepository productSupplierRepository;

    @Autowired ErpRawMaterialSupplierRepository materialSupplierRepository;

    @Autowired ErpModelSupplierRepository mouldSupplierRepository;

    @Autowired ErpBaseEndProductRepository productRepository;

    @Autowired ErpBaseRawMaterialRepository materialRepository;

    @Autowired ErpBaseModelToolRepository mouldRepository;

    /**
     * 组合查询
     * @param parameters
     * @return
     */
    public Page<ErpPurchaseOrder> query(OrderParameterDto parameters){
        return orderRepository.findAll(parameters.specification(), parameters.pageable());
    }

    /**
     * 单个采购订单查询
     * @param orderId
     * @return
     */
    public ErpPurchaseOrder findOrder(Long orderId) {
        ErpPurchaseOrder ret = orderRepository.findById(orderId).orElse(null);
        Assert.notNull(ret, "未找到采购订单[" + orderId + "]");
        //添加明细
        ret.setItems(orderItemRepository.findByOrderIdOrderByMoneyDesc(orderId));
        //添加操作记录
        ret.setOpts(orderOptLogRepository.findByOrderIdOrderByCreatedAtDesc(orderId));
        return ret;
    }

    /**
     * 获取单个可入库的采购订单
     * @param barcode
     * @return
     */
    public ErpPurchaseOrder findOrder4In(String barcode){
        ErpPurchaseOrder order = CommonUtil.single(orderRepository.findByBarCode(barcode));
        Assert.notNull(order, "未找到采购订单[" + barcode + "]");
        Assert.isTrue(1 == order.getPreStockIn(), "该采购订单尚未打印入库单");
        //添加明细
        List<ErpPurchaseOrderItem> items = orderItemRepository.findByOrderIdOrderByMoneyDesc(order.getId())
                .stream().filter(item -> item.getInNum() < item.getNum())
                .collect(Collectors.toList());
        Assert.notEmpty(items, "无可入库的明细");
        return order;
    }

    /**
     * 新增采购订单
     * @param order
     * @return
     */
    @Transactional
    public ErpPurchaseOrder create(ErpPurchaseOrder order) {
        order.setId(null);
        List<ErpPurchaseOrderItem> items = order.getItems();
        double totalMoney = Objects.isNull(items) || items.isEmpty() ? 0.00d : items.stream().filter(item -> Objects.nonNull(item.getMoney())).mapToDouble(ErpPurchaseOrderItem::getMoney).sum();
        order.setMoney(totalMoney);
        //1-主信息
        final ErpPurchaseOrder _order = orderRepository.save(order.setStatusCode(STATUS_001));
        //2-明细列表
        if(!CollectionUtils.isEmpty(items)){
            orderItemRepository.saveAll(items.stream().map(item -> item.copy(_order).validateRequired()).collect(Collectors.toList()));
        }
        //3-日志
        String content = STATUS_001.toLog();
        if( totalMoney != 0.00d){
            content += " 总额：¥" + totalMoney;
        }
        if(StringUtils.hasText(order.getRemark())){
            content += " 备注：" + order.getRemark();
        }
        orderOptLogRepository.save(ErpPurchaseOrderOptLog.builder().orderId(_order.getId()).content(content).optType("status").statusCode(_order.getStatusCode()).build());
        return _order;
    }

    /**
     * 更新采购订单-不包含明细，不修改状态
     * 更新内容： 描述、备注、订单日期、[供应商-尚无明细的情况]
     * 约束条件：状态为未发布、审核退回
     * @param order
     * @return
     */
    @Transactional
    public ErpPurchaseOrder update(ErpPurchaseOrder order) {
        ErpPurchaseOrder inDb = orderRepository.findById(order.getId()).orElse(null);
        Assert.notNull(inDb, "未找到采购订单[" + order.getId() + "]");
        Assert.isTrue(Arrays.asList(STATUS_001, STATUS_004).contains(PurchaseOrderStatus.valueOf(inDb.getStatusCode())), "只有状态为" + STATUS_001 + "或" + STATUS_004 +"的采购订单可以更改");
        String content = "更新";
        //描述
        inDb.setDescription(order.getDescription());
        //备注
        inDb.setRemark(order.getRemark());
        //供应商
        if(!inDb.getSupplierId().equals(order.getSupplierId())){
            Assert.isTrue(CollectionUtils.isEmpty(orderItemRepository.findByOrderId(order.getId())), "已有明细的情况下不能修改供应商");
            content += " 供应商：" + order.getSupplierName();
            inDb.setSupplierId(order.getSupplierId());
            inDb.setSupplierName(order.getSupplierName());
        }
        //订单日期
        if(!DateUtil.getDateString(inDb.getOrderDate()).equals(DateUtil.getDateString(order.getOrderDate()))) {
            content += " 订单日期：" + DateUtil.getDateString(order.getOrderDate());
            inDb.setOrderDate(order.getOrderDate());
        }
        orderOptLogRepository.save(ErpPurchaseOrderOptLog.builder().statusCode(order.getStatusCode()).content(content).optType("order").orderId(order.getId()).build());
        return orderRepository.save(inDb);
    }

    /**
     * 删除采购订单-级联删除明细
     * 约束条件：未发布,待审核,审核退回
     * @param orderId
     * @return
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        ErpPurchaseOrder order = orderRepository.findById(orderId).orElse(null);
        Assert.notNull(order, "未找到采购订单[" + orderId + "]");
        Assert.isTrue(Arrays.asList(STATUS_001, STATUS_002, STATUS_004).contains(PurchaseOrderStatus.valueOf(order.getStatusCode())), "订单状态:" + PurchaseOrderStatus.valueOf(order.getStatusCode()) +",无法删除");
        orderRepository.deleteById(orderId);
        orderItemRepository.deleteByOrderId(orderId);
        orderOptLogRepository.deleteByOrderId(orderId);
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
    @Transactional
    public ErpPurchaseOrder updateOrderStatus(Long orderId, PurchaseOrderStatus status, String remark) {
        remark = StringUtils.hasLength(remark) ? " 备注：" + remark : "";
        ErpPurchaseOrder old = orderRepository.findById(orderId).orElse(null);
        Assert.notNull(old, "未找到采购订单[" + orderId + "]");
        Assert.isTrue(STATUS_001 != status , "状态不能变更为：" + STATUS_001.description());
        Assert.isTrue(!STATUS_007.name().equals(old.getStatusCode()), STATUS_007 + "的订单不能修改状态");
        String oldStatus = old.getStatusCode();
        //->待审核
        if(status == STATUS_002){
            Assert.isTrue(Arrays.asList(STATUS_001.name(), STATUS_004.name()).contains(oldStatus)
                    , "只有" + STATUS_001.description() + "或" + STATUS_004.description() + "的订单可以变更为" + STATUS_002.description());
            List<ErpPurchaseOrderItem> oldOrderItems = orderItemRepository.findByOrderIdOrderByMoneyDesc(orderId);
            Assert.notEmpty(oldOrderItems, "请先维护订单明细再提交");
        }
        //->审核通过、审核退回
        if(status == STATUS_003 || status == STATUS_004){
            Assert.isTrue(STATUS_002.name().equals(oldStatus), "只有" + STATUS_002.description() + "的订单可以变更为" + status.description());
        }
        

        //->作废
        if(status == STATUS_008){
            Assert.isTrue(STATUS_003.name().equals(oldStatus), "只有" + STATUS_003.description() + "的订单可以" + status.description());
        }

        ErpPurchaseOrder order = orderRepository.save(old.setStatusCode(status));

        //修改明细的状态
        List<ErpPurchaseOrderItem> items = orderItemRepository.findByOrderIdOrderByMoneyDesc(orderId);
        items.forEach(item -> item.setStatusCode(status.name()));
        orderItemRepository.saveAll(items);

        orderOptLogRepository.save(ErpPurchaseOrderOptLog.builder().statusCode(status.name()).content(status.toLog() + remark).optType("status").orderId(order.getId()).build());
        return order;
    }

    /**
     * 准备入库-打印入库单之后调用
     * 1-设置采购订单的preStockIn
     * 2-生成入库单
     * @param orderId
     * @return
     */
    @Transactional
    public ErpPurchaseOrder preStockIn(Long orderId){
        ErpPurchaseOrder order = orderRepository.findById(orderId).orElse(null);
        Assert.notNull(order, "未找到采购订单[" + orderId + "]");
        order.setPreStockIn(1);
        //入库单
        ErpStockInOrder inOrder = CommonUtil.single(stockInOrderRepository.findByHolderAndHolderId(BUY.name(), orderId), "采购订单[" + orderId + "]对应的入库单存在脏数据");
        if(Objects.isNull(inOrder)){
            Map<WaresType, List<ErpPurchaseOrderItem>> orderItemsMap =
                    orderItemRepository.findByOrderId(orderId).stream().collect(Collectors.groupingBy(item -> WaresType.valueOf(item.getWaresType())));
            //成品 id & name
            KeyValue<String, String> products = collectIdAndName(orderItemsMap.getOrDefault(WaresType.P, new ArrayList<>()));
            //原材料 id & name
            KeyValue<String, String> materials = collectIdAndName(orderItemsMap.getOrDefault(WaresType.M, new ArrayList<>()));
            //模具 id & name
            KeyValue<String, String> moulds = collectIdAndName(orderItemsMap.getOrDefault(WaresType.D, new ArrayList<>()));

            stockInOrderRepository.save(
                    ErpStockInOrder.builder()
                            .holder(BUY.name())
                            .holderId(orderId)
                            .holderBarCode(order.getBarCode())
                            .productIds(products.getKey())
                            .productNames(products.getValue())
                            .materialIds(materials.getKey())
                            .materialNames(materials.getValue())
                            .mouldIds(moulds.getKey())
                            .mouldNames(moulds.getValue())
                        .build()
            );
        }

        return orderRepository.save(order);
    }

    /**
     * 新增采购订单明细-已有订单的情况下
     * 约束条件：订单状态为未发布、审核退回
     * ps：订单id、code校验，自身属性在service里校验
     * @param item
     * @return
     */
    @Transactional
    public ErpPurchaseOrderItem createOrderItem(ErpPurchaseOrderItem item) {
        ErpPurchaseOrder oldOrder = orderRepository.findById(item.getOrderId()).orElse(null);
        Assert.notNull(oldOrder, "未找到采购订单[" + item.getOrderId() + "]");
        Assert.isTrue(Arrays.asList(STATUS_001, STATUS_004).contains(PurchaseOrderStatus.valueOf(item.getStatusCode())), "只有" + STATUS_001 + "或" + STATUS_004 + "的订单可以新增采购明细");

        item.setStatusCode(oldOrder.getStatusCode());
        item.validateRequired();
        item.setId(null);
        boolean exist = orderItemRepository.findByOrderIdAndWaresAssIdAndWaresType(item.getOrderId(), item.getWaresAssId(), item.getWaresType()).size() > 0;
        Assert.isTrue(!exist, "已存在货物[" + item.getWaresName() + "]的明细，请重新选择");
        ErpPurchaseOrderItem ret = orderItemRepository.save(item);
        Double totalMoney = oldOrder.getMoney() + item.getMoney();
        oldOrder.setMoney(totalMoney);
        orderRepository.save(oldOrder);
        orderOptLogRepository.save(
                ErpPurchaseOrderOptLog.builder()
                        .statusCode(item.getStatusCode())
                        .content("新增明细：" + item.getWaresName() + ", 总金额变化为：¥" + totalMoney)
                        .optType("item")
                        .orderId(item.getOrderId())
                    .build()
        );
        return ret;
    }

    /**
     * 更新采购订单明细-已有订单的情况下
     * 约束条件：订单状态为未发布、审核退回
     * ps：订单id、code校验，自身属性在service里校验
     * @param item
     * @return
     */
    @Transactional
    public ErpPurchaseOrderItem updateOrderItem(ErpPurchaseOrderItem item) {
        ErpPurchaseOrderItem oldItem = orderItemRepository.findById(item.getId()).orElse(null);
        Assert.notNull(oldItem, "未找到采购订单明细[" + item.getId() + "]");
        Assert.isTrue(Arrays.asList(STATUS_001, STATUS_004).contains(PurchaseOrderStatus.valueOf(item.getStatusCode())), "只有" + STATUS_001 + "或" + STATUS_004 + "的订单可以修改采购明细");
        item.setStatusCode(oldItem.getStatusCode());
        item.validateRequired();
        ErpPurchaseOrder oldOrder = orderRepository.findById(item.getOrderId()).orElse(null);
        Assert.notNull(oldOrder, "未找到采购订单[" + item.getOrderId() + "]");
        boolean exist = orderItemRepository.findByOrderIdAndWaresAssIdAndWaresType(item.getOrderId(), item.getWaresAssId(), item.getWaresType()).stream()
                .anyMatch(obj-> !item.getId().equals(obj.getId()));
        Assert.isTrue(!exist, "已存在货物[" + item.getWaresName() + "]的明细，请重新选择");

        String content = "更新明细";
        Double totalMoney = oldOrder.getMoney() + item.getMoney() - oldItem.getMoney();
        if(!totalMoney.equals(oldOrder.getMoney())){
            oldOrder.setMoney(totalMoney);
            orderRepository.save(oldOrder);
            content += ", 总金额变化为：¥" + totalMoney;
        }
        if(!item.getWaresId().equals(oldItem.getWaresId())){
            content += ", 货物调整为：" + item.getWaresName();
        }
        orderOptLogRepository.save(
                ErpPurchaseOrderOptLog.builder()
                        .statusCode(item.getStatusCode())
                        .content(content)
                        .optType("item")
                        .orderId(item.getOrderId()).
                    build()
        );
        item.copyBase(oldItem);
        return orderItemRepository.save(item);
    }

    /**
     * 删除采购订单明细-已有订单的情况下
     * 约束条件：订单状态为未发布、审核退回
     * @param orderId
     * @param orderItemId
     * @return
     */
    @Transactional
    public void deleteOrderItem(Long orderId, Long orderItemId) {
        ErpPurchaseOrderItem oldItem = orderItemRepository.findById(orderItemId).orElse(null);
        Assert.notNull(oldItem, "未找到采购订单明细[" + orderItemId + "]");
        Assert.isTrue(Arrays.asList(STATUS_001, STATUS_004).contains(PurchaseOrderStatus.valueOf(oldItem.getStatusCode())), "只有" + STATUS_001 + "或" + STATUS_004 + "的订单可以删除采购明细");
        ErpPurchaseOrder oldOrder = orderRepository.findById(orderId).orElse(null);
        Assert.notNull(oldOrder, "未找到采购订单[" + orderId + "]");
        Assert.isTrue(orderId.equals(oldItem.getOrderId()), "查询参数订单id和明细实际对应的订单id不相符");
        List<ErpPurchaseOrderItem> oldOrderItems = orderItemRepository.findByOrderIdOrderByMoneyDesc(orderId);
        Double totalMoney = oldOrderItems.size() <=1 ? 0.00d : (oldOrder.getMoney() - oldItem.getMoney());
        oldOrder.setMoney(totalMoney);
        orderRepository.save(oldOrder);
        orderItemRepository.deleteById(orderItemId);
        orderOptLogRepository.save(
                ErpPurchaseOrderOptLog.builder()
                        .statusCode(oldOrder.getStatusCode())
                        .content("删除明细["+ oldItem.getWaresName() + "], 总金额变化为：¥" + totalMoney)
                        .optType("item").orderId(orderId)
                    .build()
        );
    }

    /**
     * 获取单个采购订单的历史记录
     * @param orderId
     * @return
     */
    public List<ErpPurchaseOrderOptLog> findOrderOptHistory(Long orderId) {
        return orderOptLogRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    /**
     * 获取单个采购订单的明细
     * @param orderId
     * @return
     */
    public List<ErpPurchaseOrderItem> findOrderItems(Long orderId){
        return  orderItemRepository.findByOrderIdOrderByMoneyDesc(orderId);
    }

    /**
     * 采购入库 成品|原材料|模具
     * @param holder
     * @param stockEntity
     * @return
     */
    @Override
    public boolean matchesIn(StockHolder holder, StockEntity stockEntity) {
        WaresType waresType = stockEntity.waresType();
        return BUY == holder && Arrays.asList(WaresType.P, WaresType.M, WaresType.D).contains(waresType);
    }

    /**
     * 对订单明细进行入库：采购入库
     * @param holder
     * @param purchaseOrderId - 采购单id
     * @param stockEntity
     * @param inNum 入库数量
     */
    @Override
    @Transactional
    public void stockIn(StockHolder holder, Long purchaseOrderId, StockEntity stockEntity, double inNum) {

        //1-检查订单
        ErpPurchaseOrder order = orderRepository.findById(purchaseOrderId).orElse(null);
        Assert.notNull(order, "未找到采购订单[" + purchaseOrderId + "]");
        Assert.isTrue( 1 == order.getPreStockIn(), "尚未打印入库单");

        List<ErpPurchaseOrderItem> items = orderItemRepository.findByOrderIdAndWaresAssIdAndWaresType(purchaseOrderId, stockEntity.realityId(), stockEntity.waresType().name());

        ErpPurchaseOrderItem item = CommonUtil.single(items, "采购订单[" + purchaseOrderId + "存在脏数据");
        if(Objects.isNull(item)) return;

        Assert.isTrue(!STATUS_005.name().equals(item.getStatusCode()), item.getWaresName() + "已完成入库");
        PurchaseOrderStatus status = STATUS_049;//入库中
        double currentInNum = inNum + item.getInNum();
        String content = item.getWaresName() + " 完成入库,入库数量【" + currentInNum + "/" + item.getNum() + "】";
        if(currentInNum >= item.getNum()){
            status = STATUS_005;
            content = item.getWaresName() + " 全部完成入库,入库数量【" + currentInNum + "/" + item.getNum() + "】";
        }
        //2-保存订单明细
        item.setStatusCode(status.name());
        item.setInNum(currentInNum);
        orderItemRepository.save(item);
        //3-明细日志
        orderOptLogRepository.save(
                ErpPurchaseOrderOptLog.builder()
                        .statusCode(status.name())
                        .content(content)
                        .optType("item")
                        .orderId(purchaseOrderId)
                        .waresId(stockEntity.templateId())
                        .waresAssId(stockEntity.realityId())
                        .waresType(stockEntity.waresType().name())
                        .waresBarcode(stockEntity.entity().getBarCode())
                    .build()
        );

        //4 未完成入库的订单明细
        List<ErpPurchaseOrderItem> unOutItems = orderItemRepository.findByOrderIdAndStatusCodeNot(purchaseOrderId, STATUS_005.name());
        if(CollectionUtils.isEmpty(unOutItems)){
            //全部入库成功->修改订单状态为：已入库
            orderRepository.save(order.setStatusCode(STATUS_005));
            orderOptLogRepository.save(ErpPurchaseOrderOptLog.builder().statusCode(STATUS_005.name()).content(STATUS_005.toLog()).optType("status").orderId(purchaseOrderId).build());
            //入库单-完成
            ErpStockInOrder inOrder = CommonUtil.single(stockInOrderRepository.findByHolderAndHolderId(BUY.name(), purchaseOrderId));
            if(Objects.nonNull(inOrder)){
                inOrder.setComplete(1);
                stockInOrderRepository.save(inOrder);
            }
        } else if(!STATUS_049.name().equals(order.getStatusCode())){
            //存在未完成入库的明细，且自身状态不为：入库中
            orderRepository.save(order.setStatusCode(STATUS_049));
            orderOptLogRepository.save(ErpPurchaseOrderOptLog.builder().statusCode(STATUS_049.name()).content(STATUS_049.toLog()).optType("status").orderId(purchaseOrderId).build());
        }
    }

    @Override
    public void onDeleteSupplierMould(Long id) {
        onDeleteAss(id, WaresType.D);
    }

    @Override
    public void onDeleteSupplierProduct(Long id) {
        onDeleteAss(id, WaresType.P);
    }

    @Override
    public void onDeleteSupplierRawMaterial(Long id) {
        onDeleteAss(id, WaresType.M);
    }

    /**
     * 根据供应商id和货物类型查找货物
     * @param supplierId
     * @param type
     * @return code-id & name-name
     */
    public List<JSONObject> findWares(Long supplierId, WaresType type){
        if(WaresType.P == type){
            return productSupplierRepository.findBySupplierId(supplierId)
                    .stream()
                    .map(ass -> {
                        ErpBaseEndProduct product = productRepository.findById(ass.getEndProductId()).orElse(null);

                        return Objects.isNull(product) ?  null :
                                JsonWrapper.builder()
                                        .add("code", ass.getId())
                                        .add("name",product.getDescription())
                                        .add("waresId", product.getId())
                                    .build();
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        if(WaresType.M == type){
            return materialSupplierRepository.findBySupplierId(supplierId)
                    .stream()
                    .map(ass -> {
                        ErpBaseRawMaterial material = materialRepository.findById(ass.getRawMaterId()).orElse(null);

                        return Objects.isNull(material) ? null :
                                JsonWrapper.builder()
                                        .add("code", ass.getId())
                                        .add("name", material.getDescription())
                                        .add("waresId", material.getId())
                                    .build();
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        if(WaresType.D == type){
            return mouldSupplierRepository.findBySupplierId(supplierId)
                    .stream()
                    .map(ass -> {
                        ErpBaseModelTool mould = mouldRepository.findById(ass.getModelId()).orElse(null);

                        return Objects.isNull(mould) ? null :
                                JsonWrapper.builder()
                                        .add("code", ass.getId())
                                        .add("name", mould.getDescription())
                                        .add("waresId", mould.getId())
                                    .build();
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 其他地方删除关联时的校验
     * @param assId
     * @param type
     */
    private void onDeleteAss(Long assId, WaresType type){
        String codes =  orderItemRepository.findByWaresAssIdAndWaresType(assId, type.name())
                .stream().map(ErpPurchaseOrderItem::getOrderCode).collect(Collectors.joining(","));
        if(StringUtils.hasText(codes)){
            throw new IllegalArgumentException("请先删除采购单[" + codes + "]对应的明细");
        }
    }

    /**
     * 采购订单查询入参dto
     */
    @Getter @Setter
    @OrderBy(fieldName = "lastUpdatedAt")
    public static class OrderParameterDto extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "description", "supplierName"}, compare = like, group = OR)
        String cause;

        @ParameterItem( mappingTo = "statusCode", compare = equal)
        String code;

        @ParameterItem( mappingTo = "statusCode", compare = in)
        Collection<String> codes;//statusCode范围
    }

    /**
     * 从list中获取WaresAssId和WaresAssName，分别用英文逗号隔开， key - ids | value - names
     * @param orderItems
     * @return
     */
    private KeyValue<String, String> collectIdAndName(List<ErpPurchaseOrderItem> orderItems){
        if(CollectionUtils.isEmpty(orderItems)){
            return new KeyValue<String, String>() {
                @Override
                public String getKey() {
                    return "";
                }

                @Override
                public String getValue() {
                    return "";
                }
            };
        }

        String ids = orderItems.stream().map(ErpPurchaseOrderItem::getWaresAssId).filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(","));
        String names = orderItems.stream().map(ErpPurchaseOrderItem::getWaresName).filter(StringUtils::hasText).collect(Collectors.joining(","));
        return new KeyValue<String, String>() {
            @Override
            public String getKey() {
                return ids;
            }

            @Override
            public String getValue() {
                return names;
            }
        };
    }
}

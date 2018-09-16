package com.yintong.erp.service.sale;

import com.yintong.erp.domain.basis.ErpBaseEndProduct;
import com.yintong.erp.domain.sale.ErpSaleOrder;
import com.yintong.erp.domain.sale.ErpSaleOrderItem;
import com.yintong.erp.domain.sale.ErpSaleOrderItemRepository;
import com.yintong.erp.domain.sale.ErpSaleOrderOptLog;
import com.yintong.erp.domain.sale.ErpSaleOrderOptLogRepository;
import com.yintong.erp.domain.sale.ErpSaleOrderRepository;

import com.yintong.erp.domain.stock.ErpStockInOrder;
import com.yintong.erp.domain.stock.ErpStockInOrderRepository;
import com.yintong.erp.domain.stock.ErpStockOutOrder;
import com.yintong.erp.domain.stock.ErpStockOutOrderRepository;
import com.yintong.erp.domain.stock.StockEntity;
import com.yintong.erp.service.stock.StockIn4Holder;
import com.yintong.erp.service.stock.StockOut4Holder;
import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.validator.OnDeleteCustomerValidator;
import com.yintong.erp.validator.OnDeleteProductValidator;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.equal;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.like;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.in;
import static javax.persistence.criteria.Predicate.BooleanOperator.OR;
import static com.yintong.erp.utils.common.Constants.*;
import static com.yintong.erp.utils.common.Constants.SaleOrderStatus.*;

import static com.yintong.erp.utils.common.Constants.StockHolder.SALE;


/**
 * @author lucifer.chan
 * @create 2018-07-23 下午9:34
 * 销售订单服务
 **/
@Service
public class SaleOrderService implements StockOut4Holder, StockIn4Holder, OnDeleteCustomerValidator, OnDeleteProductValidator {

    @Autowired ErpSaleOrderRepository saleOrderRepository;

    @Autowired ErpSaleOrderItemRepository orderItemRepository;

    @Autowired ErpSaleOrderOptLogRepository orderOptLogRepository;

    @Autowired ErpStockOutOrderRepository stockOutOrderRepository;

    @Autowired ErpStockInOrderRepository stockInOrderRepository;

    /**
     * 组合查询
     * @param parameters
     * @return
     */
    public Page<ErpSaleOrder> query(OrderParameterDto parameters){
        return saleOrderRepository.findAll(parameters.specification(), parameters.pageable());
    }

    /**
     * 单个销售订单查询
     * @param orderId
     * @return
     */
    public ErpSaleOrder findOrder(Long orderId) {
        ErpSaleOrder ret = saleOrderRepository.findById(orderId).orElse(null);
        Assert.notNull(ret, "未找到销售订单[" + orderId + "]");
        //添加明细
        ret.setItems(orderItemRepository.findByOrderIdOrderByMoneyDesc(orderId));
        //添加操作记录
        ret.setOpts(orderOptLogRepository.findByOrderIdOrderByCreatedAtDesc(orderId));
        return ret;
    }

    /**
     * 获取单个可出库的销售订单
     * @param barcode
     * @return
     */
    public ErpSaleOrder findOrder4Out(String barcode){
        ErpSaleOrder order = CommonUtil.single(saleOrderRepository.findByBarCode(barcode));
        Assert.notNull(order, "未找到销售订单[" + barcode + "]");
        Assert.isTrue(1 == order.getPreStockOut(), "该销售订单尚不可出库");
        //添加明细
        List<ErpSaleOrderItem> items = orderItemRepository.findByOrderIdOrderByMoneyDesc(order.getId())
                .stream().filter(item -> item.getOutedNum() < item.getNum())
                .collect(Collectors.toList());
        Assert.notEmpty(items, "无可出库的明细");
        order.setItems(items);
        return order;
    }

    /**
     *  获取单个可入库的销售订单【退货单】
     * @param barcode
     * @return
     */
    public ErpSaleOrder findOrder4In(String barcode){
        ErpSaleOrder order = CommonUtil.single(saleOrderRepository.findByBarCode(barcode));
        Assert.notNull(order, "未找到销售订单[" + barcode + "]");
        Assert.isTrue(1 == order.getPreStockIn(), "该销售订单尚不可入库");
        SaleOrderStatus currentStatus = SaleOrderStatus.valueOf(order.getStatusCode());
        Assert.isTrue(STATUS_006 == currentStatus, "当前订单状态为" + currentStatus.description() + "，不可入库");
        //添加明细
        List<ErpSaleOrderItem> items = orderItemRepository.findByOrderIdOrderByMoneyDesc(order.getId())
                .stream().filter(item -> item.getInNum() < item.getNum())
                .collect(Collectors.toList());
        Assert.notEmpty(items, "无可入库的明细");
        order.setItems(orderItemRepository.findByOrderIdOrderByMoneyDesc(order.getId()));
        return order;
    }

    /**
     * 新增销售订单
     * @param order
     * @return
     */
    @Transactional
    public ErpSaleOrder create(ErpSaleOrder order) {
        order.setId(null);
        List<ErpSaleOrderItem> items = order.getItems();
        double totalMoney = Objects.isNull(items) || items.isEmpty() ? 0.00d : items.stream().filter(item -> Objects.nonNull(item.getMoney())).mapToDouble(ErpSaleOrderItem::getMoney).sum();
        order.setMoney(totalMoney);
        //1-主信息
        final ErpSaleOrder _order = saleOrderRepository.save(order.setStatusCode(STATUS_001));
        if(Objects.nonNull(items) && !items.isEmpty()){
            //2-明细列表
            items.forEach(item -> {
                item.setStatusCode(_order.getStatusCode());
                item.setOrderId(_order.getId());
                item.setOrderCode(_order.getBarCode());
                item.validateRequired();
            });
            orderItemRepository.saveAll(items);
        }

        //3-日志
        String content = STATUS_001.toLog();
        if( totalMoney != 0.00d){
            content += " 总额：¥" + totalMoney;
        }
        if(StringUtils.hasText(order.getRemark())){
            content += " 备注：" + order.getRemark();
        }
        orderOptLogRepository.save(ErpSaleOrderOptLog.builder().orderId(_order.getId()).content(content).optType("status").statusCode(_order.getStatusCode()).build());
        return _order;
    }

    /**
     * 修更新销售订单-不包含明细，不修改状态
     * 更新内容： 客户、订单日期
     * 约束条件：状态为未发布、审核退回
     * @param order
     * @return
     */
    @Transactional
    public ErpSaleOrder update(ErpSaleOrder order) {
        ErpSaleOrder old = saleOrderRepository.findById(order.getId()).orElse(null);
        Assert.notNull(old, "未找到销售订单[" + order.getId() + "]");
        Assert.isTrue(Arrays.asList(STATUS_001, STATUS_004).contains(SaleOrderStatus.valueOf(old.getStatusCode())), "只有状态为" + STATUS_001 + "或" + STATUS_004 +"的销售订单可以更改");
        //不修改状态
        order.setStatusCode(old.getStatusCode());
        //不修改金额
        order.setMoney(old.getMoney());

        String content = "更新";
        if(!DateUtil.getDateString(old.getOrderDate()).equals(DateUtil.getDateString(order.getOrderDate()))) {
            content += " 订单日期："+ DateUtil.getDateString(order.getOrderDate());
        }

        if(!old.getCustomerName().equals(order.getCustomerName())){
            content += " 客户：" + order.getCustomerName();
        }
        order.copyBase(old);
        ErpSaleOrder _order = saleOrderRepository.save(order);
        orderOptLogRepository.save(ErpSaleOrderOptLog.builder().statusCode(order.getStatusCode()).content(content).optType("order").orderId(order.getId()).build());
        return _order;
    }

    /**
     * 删除销售订单-级联删除明细
     * 约束条件：未发布,待审核,审核退回
     * @param orderId
     * @return
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        ErpSaleOrder old = saleOrderRepository.findById(orderId).orElse(null);
        Assert.notNull(old, "未找到销售订单[" + orderId + "]");
        Assert.isTrue(Arrays.asList(STATUS_001, STATUS_002, STATUS_004).contains(SaleOrderStatus.valueOf(old.getStatusCode())), "订单状态:" + SaleOrderStatus.valueOf(old.getStatusCode()) +",无法删除");
        saleOrderRepository.deleteById(orderId);
        orderItemRepository.deleteByOrderId(orderId);
        orderOptLogRepository.deleteByOrderId(orderId);
    }

    /**
     * 更新订单状态：待审核、审核通过、审核退回、客户退货、已出库、已完成
     * 约束条件：未发布 ->待审核.
     *          待审核 ->审核通过、审核退回.
     *          审核退回 ->待审核.
     *          审核通过 ->已出库.
     *          已出库 ->客户退货、已完成.
     * @param orderId
     * @param status 修改后的状态
     * @param remark 备注信息
     * @return
     */
    @Transactional
    public ErpSaleOrder updateOrderStatus(Long orderId, SaleOrderStatus status, String remark) {
        remark = StringUtils.hasLength(remark) ? " 备注：" + remark : "";
        ErpSaleOrder old = saleOrderRepository.findById(orderId).orElse(null);
        Assert.notNull(old, "未找到销售订单[" + orderId + "]");
        Assert.isTrue(STATUS_001 != status , "状态不能变更为：" + STATUS_001.description());
        Assert.isTrue(!STATUS_007.name().equals(old.getStatusCode()), STATUS_007 + "的订单不能修改状态");
        String oldStatus = old.getStatusCode();
        //->待审核
        if(status == STATUS_002){
            Assert.isTrue(Arrays.asList(STATUS_001.name(), STATUS_004.name()).contains(oldStatus)
                    , "只有" + STATUS_001.description() + "或" + STATUS_004.description() + "的订单可以变更为" + STATUS_002.description());
            List<ErpSaleOrderItem> oldOrderItems = orderItemRepository.findByOrderIdOrderByMoneyDesc(orderId);
            Assert.notEmpty(oldOrderItems, "请先维护订单明细再提交");
        }
        //->审核通过、审核退回
        if(status == STATUS_003 || status == STATUS_004){
            Assert.isTrue(STATUS_002.name().equals(oldStatus), "只有" + STATUS_002.description() + "的订单可以变更为" + status.description());
        }
//        //->已出库
//        if(status == STATUS_005){
//            Assert.isTrue(STATUS_049.name().equals(oldStatus), "只有" + STATUS_049.description() + "的订单可以变更为" + status.description());
//        }
        //->客户退货、已完成
        if(status == STATUS_006 || status == STATUS_007){
            Assert.isTrue(STATUS_005.name().equals(oldStatus), "只有" + STATUS_005.description() + "的订单可以变更为" + status.description());
        }

        //->作废
        if(status == STATUS_008){
            Assert.isTrue(STATUS_003.name().equals(oldStatus), "只有" + STATUS_003.description() + "的订单可以" + status.description());
            old.setPreStockOut(0);
            old.setPreStockIn(1);
        }

        // 客户退货 修改preStockOut属性为0 即不允许再出库，允许入库
        if(status == STATUS_006){
            old.setPreStockOut(0);
            old.setPreStockIn(1);
            findOrCreateStockInOrder(old);
        }

        ErpSaleOrder order = saleOrderRepository.save(old.setStatusCode(status));

        //修改明细的状态
        List<ErpSaleOrderItem> items = orderItemRepository.findByOrderIdOrderByMoneyDesc(orderId);
        items.forEach(item -> item.setStatusCode(status.name()));
        orderItemRepository.saveAll(items);

        orderOptLogRepository.save(ErpSaleOrderOptLog.builder().statusCode(status.name()).content(status.toLog() + remark).optType("status").orderId(order.getId()).build());
        return order;
    }

    /**
     * 准备出库-打印出库单之后调用
     * 1-设置销售订单的preStockOut
     * 2-生成出库单
     * @param orderId
     * @return
     */
    @Transactional
    public ErpSaleOrder preStockOut(Long orderId){
        ErpSaleOrder order = saleOrderRepository.findById(orderId).orElse(null);
        Assert.notNull(order, "未找到销售订单[" + orderId + "]");
        order.setPreStockOut(1);
        //出库单
        ErpStockOutOrder outOrder = CommonUtil.single(stockOutOrderRepository.findByHolderAndHolderId(SALE.name(), orderId), "销售订单[" + orderId + "]对应的出库单存在脏数据");
        if(Objects.isNull(outOrder)){
            List<ErpSaleOrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            List<ErpBaseEndProduct> products =  orderItems.stream().map(ErpSaleOrderItem::getProduct).filter(Objects::nonNull).collect(Collectors.toList());

            String productIds = products.stream().map(product -> product.realityId().toString()).collect(Collectors.joining(","));
            String productNames = products.stream().map(ErpBaseEndProduct::getEndProductName).collect(Collectors.joining(","));
            stockOutOrderRepository.save(
                    ErpStockOutOrder.builder()
                        .holder(SALE.name())
                        .holderId(orderId)
                        .holderBarCode(order.getBarCode())
                        .productIds(productIds)
                        .productNames(productNames)
                        .complete(0)
                    .build()
            );
        }
        return saleOrderRepository.save(order);
    }

    /**
     * 新增销售订单明细-已有订单的情况下
     * 约束条件：订单状态为未发布、审核退回
     * ps：订单id、code校验，自身属性在service里校验
     * @param item
     * @return
     */
    @Transactional
    public ErpSaleOrderItem createOrderItem(ErpSaleOrderItem item) {
        ErpSaleOrder oldOrder = saleOrderRepository.findById(item.getOrderId()).orElse(null);
        Assert.notNull(oldOrder, "未找到销售订单[" + item.getOrderId() + "]");
        Assert.isTrue(Arrays.asList(STATUS_001, STATUS_004).contains(SaleOrderStatus.valueOf(item.getStatusCode())), "只有" + STATUS_001 + "或" + STATUS_004 + "的订单可以新增销售明细");

        item.setStatusCode(oldOrder.getStatusCode());
        item.validateRequired();
        item.setId(null);
        boolean exist = orderItemRepository.findByOrderIdOrderByMoneyDesc(item.getOrderId()).stream()
                .anyMatch(obj-> item.getProductId().equals(obj.getProductId()));
        Assert.isTrue(!exist, "已存在成品[" + item.getProductName() + "]的明细，请重新选择");
        ErpSaleOrderItem ret = orderItemRepository.save(item);
        Double totalMoney = oldOrder.getMoney() + item.getMoney();
        oldOrder.setMoney(totalMoney);
        saleOrderRepository.save(oldOrder);
        orderOptLogRepository.save(ErpSaleOrderOptLog.builder().statusCode(item.getStatusCode()).content("新增明细, 总金额变化为：¥" + totalMoney).optType("item").orderId(item.getOrderId()).build());
        return ret;
    }

    /**
     * 更新销售订单明细-已有订单的情况下
     * 约束条件：订单状态为未发布、审核退回
     * ps：订单id、code校验，自身属性在service里校验
     * @param item
     * @return
     */
    @Transactional
    public ErpSaleOrderItem updateOrderItem(ErpSaleOrderItem item) {
        ErpSaleOrderItem oldItem = orderItemRepository.findById(item.getId()).orElse(null);
        Assert.notNull(oldItem, "未找到销售订单明细[" + item.getId() + "]");
        Assert.isTrue(Arrays.asList(STATUS_001, STATUS_004).contains(SaleOrderStatus.valueOf(item.getStatusCode())), "只有" + STATUS_001 + "或" + STATUS_004 + "的订单可以修改销售明细");
        item.setStatusCode(oldItem.getStatusCode());
        item.validateRequired();
        ErpSaleOrder oldOrder = saleOrderRepository.findById(item.getOrderId()).orElse(null);
        Assert.notNull(oldOrder, "未找到销售订单[" + item.getOrderId() + "]");
        boolean exist = orderItemRepository.findByOrderIdOrderByMoneyDesc(item.getOrderId()).stream()
                .anyMatch(obj-> item.getProductId().equals(obj.getProductId()) && !item.getId().equals(obj.getId()));
        Assert.isTrue(!exist, "已存在成品[" + item.getProductName() + "]的明细，请重新选择");

        String content = "更新明细";
        Double totalMoney = oldOrder.getMoney() + item.getMoney() - oldItem.getMoney();
        if(!totalMoney.equals(oldOrder.getMoney())){
            oldOrder.setMoney(totalMoney);
            saleOrderRepository.save(oldOrder);
            content += ", 总金额变化为：¥" + totalMoney;
        }
        if(!item.getProductId().equals(oldItem.getProductId())){
            content += ", 成品调整为：" + item.getProductName();
        }
        orderOptLogRepository.save(ErpSaleOrderOptLog.builder().statusCode(item.getStatusCode()).content(content).optType("item").orderId(item.getOrderId()).build());
        item.copyBase(oldItem);
        return orderItemRepository.save(item);
    }

    /**
     * 删除销售订单明细-已有订单的情况下
     * 约束条件：订单状态为未发布、审核退回
     * @param orderId
     * @param orderItemId
     * @return
     */
    @Transactional
    public void deleteOrderItem(Long orderId, Long orderItemId) {
        ErpSaleOrderItem oldItem = orderItemRepository.findById(orderItemId).orElse(null);
        Assert.notNull(oldItem, "未找到销售订单明细[" + orderItemId + "]");
        Assert.isTrue(Arrays.asList(STATUS_001, STATUS_004).contains(SaleOrderStatus.valueOf(oldItem.getStatusCode())), "只有" + STATUS_001 + "或" + STATUS_004 + "的订单可以删除销售明细");
        ErpSaleOrder oldOrder = saleOrderRepository.findById(orderId).orElse(null);
        Assert.notNull(oldOrder, "未找到销售订单[" + orderId + "]");
        Assert.isTrue(orderId.equals(oldItem.getOrderId()), "查询参数订单id和明细实际对应的订单id不相符");
        List<ErpSaleOrderItem> oldOrderItems = orderItemRepository.findByOrderIdOrderByMoneyDesc(orderId);
        Double totalMoney = oldOrderItems.size() <=1 ? 0.00d : (oldOrder.getMoney() - oldItem.getMoney());
        oldOrder.setMoney(totalMoney);
        saleOrderRepository.save(oldOrder);
        orderItemRepository.deleteById(orderItemId);
        orderOptLogRepository.save(ErpSaleOrderOptLog.builder().statusCode(oldOrder.getStatusCode()).content("删除明细[" + oldItem.getProductName() + "], 总金额变化为：¥" + totalMoney).optType("item").orderId(orderId).build());
    }

    /**
     * 获取单个销售订单的历史记录
     * @param orderId
     * @return
     */
    public List<ErpSaleOrderOptLog> findOrderOptHistory(Long orderId) {
        return orderOptLogRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    /**
     * 获取单个销售订单的明细
     * @param orderId
     * @return
     */
    public List<ErpSaleOrderItem> findOrderItems(Long orderId){
        return  orderItemRepository.findByOrderIdOrderByMoneyDesc(orderId);
    }

    @Override
    public void onDeleteCustomer(Long employeeId) {
        ErpSaleOrder order = saleOrderRepository.findByCustomerId(employeeId)
                .stream().findAny().orElse(null);
        if(null != order){
           throw new IllegalArgumentException("请先删除销售单[" + order.getBarCode() + "]");
        }
    }

    @Override
    public void onDeleteProduct(Long productId) {
        ErpSaleOrderItem item = orderItemRepository.findByProductId(productId)
                .stream().findAny().orElse(null);
        if(null != item){
            throw new IllegalArgumentException("请先删除销售单[" + item.getOrderCode() + "]");
        }
    }

    @Override
    public boolean matchesOut(StockHolder holder, StockEntity stockEntity) {
        return SALE == holder && WaresType.P == stockEntity.waresType();
    }

    /**
     * 对订单明细进行出库 计算出库情况，当明细全部出库完成，修改订单状态为"已出库"
     * @param holder
     * @param saleOrderId 销售订单id
     * @param stockEntity 出库实例
     * @param outNum 数量
     */
    @Override
    @Transactional
    public void stockOut(StockHolder holder, Long saleOrderId, StockEntity stockEntity, double outNum) {
        //1-检查订单
        ErpSaleOrder order = saleOrderRepository.findById(saleOrderId).orElse(null);
        Assert.notNull(order, "未找到销售订单[" + saleOrderId + "]");
        Assert.isTrue( 1 == order.getPreStockOut(), "尚未打印出库单");

        List<ErpSaleOrderItem> items = orderItemRepository.findByOrderIdAndProductId(saleOrderId, stockEntity.templateId());
        ErpSaleOrderItem item = CommonUtil.single(items, "销售订单[" + order.getBarCode() + "存在脏数据");
        if(Objects.isNull(item)) return;

        Assert.isTrue(!STATUS_005.name().equals(item.getStatusCode()), item.getProductName() + "已完成出库");
        SaleOrderStatus status = STATUS_049;//出库中
        double currentOutedNum = outNum + item.getOutedNum();
        String content = item.getProductName() + " 完成出库,出库数量【" + currentOutedNum + "/" + item.getNum() + "】";
        if(currentOutedNum >= item.getNum()){
            status = STATUS_005;
            content = item.getProductName() + " 全部完成出库,出库数量【" + currentOutedNum + "/" + item.getNum() + "】";
        }
        //2-保存订单明细
        item.setStatusCode(status.name());
        item.setOutedNum(currentOutedNum);
        orderItemRepository.save(item);
        //3-明细日志
        orderOptLogRepository.save(
                ErpSaleOrderOptLog.builder()
                        .statusCode(status.name())
                        .content(content)
                        .optType("item")
                        .orderId(saleOrderId)
                        .productId(stockEntity.templateId())
                        .productCode(stockEntity.entity().getBarCode())
                    .build()
        );

        //4 未完成出库的订单明细
        List<ErpSaleOrderItem> unOutItems = orderItemRepository.findByOrderIdAndStatusCodeNot(saleOrderId, STATUS_005.name());
        if(CollectionUtils.isEmpty(unOutItems)){
            //全部出库成功->修改订单状态为：已出库
            saleOrderRepository.save(order.setStatusCode(STATUS_005));
            orderOptLogRepository.save(ErpSaleOrderOptLog.builder().statusCode(STATUS_005.name()).content(STATUS_005.toLog()).optType("status").orderId(saleOrderId).build());
            //出库单-完成
            ErpStockOutOrder outOrder = CommonUtil.single(stockOutOrderRepository.findByHolderAndHolderId(SALE.name(), saleOrderId));
            if(Objects.nonNull(outOrder)){
                outOrder.setComplete(1);
                stockOutOrderRepository.save(outOrder);
            }
        } else if(!STATUS_049.name().equals(order.getStatusCode())){
            //存在未完成出库的明细，且自身状态不为：出库中
            saleOrderRepository.save(order.setStatusCode(STATUS_049));
            orderOptLogRepository.save(ErpSaleOrderOptLog.builder().statusCode(STATUS_049.name()).content(STATUS_049.toLog()).optType("status").orderId(saleOrderId).build());
        }
    }

    /**
     * 退货入库
     * @param holder
     * @param stockEntity
     * @return
     */
    @Override
    public boolean matchesIn(StockHolder holder, StockEntity stockEntity) {
        return  SALE == holder && WaresType.P == stockEntity.waresType();
    }

    /**
     * 对订单明细进行出库：退货入库
     * @param holder
     * @param saleOrderId
     * @param stockEntity
     * @param inNum 入库数量
     */
    @Override
    public void stockIn(StockHolder holder, Long saleOrderId, StockEntity stockEntity, double inNum) {
        //1-检查订单
        ErpSaleOrder order = saleOrderRepository.findById(saleOrderId).orElse(null);
        Assert.notNull(order, "未找到销售订单[" + saleOrderId + "]");
        Assert.isTrue( 1 == order.getPreStockIn(), "客户未退货");
        Assert.isTrue(STATUS_006.name().equals(order.getStatusCode())
                , "状态为" + STATUS_006.description() + "的销售订单方可入库");
        List<ErpSaleOrderItem> items = orderItemRepository.findByOrderIdAndProductId(saleOrderId, stockEntity.templateId());
        ErpSaleOrderItem item = CommonUtil.single(items, "销售订单[" + order.getBarCode() + "存在脏数据");
        if(Objects.isNull(item)) return;

        Assert.isTrue(!STATUS_061.name().equals(item.getStatusCode()), item.getProductName() + "已完成退货");
        SaleOrderStatus status = STATUS_006;//客户退货
        double currentInNum = inNum + item.getInNum();
        String content = item.getProductName() + " 完成入库,库存数量【" + currentInNum + "/" + item.getNum() + "】";
        if(currentInNum >= item.getNum()){
            status = STATUS_061;
            content = item.getProductName() + " 全部完成入库,库存数量【" + currentInNum + "/" + item.getNum() + "】";
        }
        //2-保存订单明细
        item.setStatusCode(status.name());
        item.setOutedNum(currentInNum);
        orderItemRepository.save(item);
        //3-明细日志
        orderOptLogRepository.save(
                ErpSaleOrderOptLog.builder()
                        .statusCode(status.name())
                        .content(content)
                        .optType("item")
                        .orderId(saleOrderId)
                        .productId(stockEntity.templateId())
                        .productCode(stockEntity.entity().getBarCode())
                    .build()
        );

        //4 未完成入库的订单明细
        List<ErpSaleOrderItem> unInItems = orderItemRepository.findByOrderIdAndStatusCodeNot(saleOrderId, STATUS_061.name());
        if(CollectionUtils.isEmpty(unInItems)){
            //全部入库成功->修改订单状态为：完成退货
            saleOrderRepository.save(order.setStatusCode(STATUS_061));
            orderOptLogRepository.save(ErpSaleOrderOptLog.builder().statusCode(STATUS_061.name()).content(STATUS_061.toLog()).optType("status").orderId(saleOrderId).build());
            //入库单-完成
            ErpStockInOrder inOrder = CommonUtil.single(stockInOrderRepository.findByHolderAndHolderId(SALE.name(), saleOrderId));

            if(Objects.nonNull(inOrder)){
                inOrder.setComplete(1);
                stockInOrderRepository.save(inOrder);
            }
        } else if(!STATUS_006.name().equals(order.getStatusCode())){
            //存在未完成入库的明细，且自身状态不为：客户退货
            saleOrderRepository.save(order.setStatusCode(STATUS_006));
            orderOptLogRepository.save(ErpSaleOrderOptLog.builder().statusCode(STATUS_006.name()).content(STATUS_006.toLog()).optType("status").orderId(saleOrderId).build());
        }
    }


    /**
     * 新建入库单 - 没有就新建
     * @param order
     * @return
     */
    private ErpStockInOrder findOrCreateStockInOrder(ErpSaleOrder order){
        Long saleOrderId = order.getId();
        //入库单
        ErpStockInOrder inOrder = CommonUtil.single(stockInOrderRepository.findByHolderAndHolderId(SALE.name(), saleOrderId), "销售订单[" + saleOrderId + "]对应的入库单存在脏数据");
        if(Objects.isNull(inOrder)){
            List<ErpBaseEndProduct> products = orderItemRepository.findByOrderId(saleOrderId).stream().map(ErpSaleOrderItem::getProduct).collect(Collectors.toList());
            String productIds = products.stream().map(product -> product.getId().toString()).collect(Collectors.joining(","));
            String productNames = products.stream().map(ErpBaseEndProduct::getEndProductName).collect(Collectors.joining(","));
            return stockInOrderRepository.save(
                    ErpStockInOrder.builder()
                            .holder(SALE.name())
                            .holderId(saleOrderId)
                            .holderBarCode(order.getBarCode())
                            .productIds(productIds)
                            .productNames(productNames)
                            .build()
            );
        }
        return inOrder;
    }

    /**
     * 销售订单查询入参dto
     */
    @Getter @Setter
    @OrderBy(fieldName = "lastUpdatedAt")
    public static class OrderParameterDto extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "description", "customerName"}, compare = like, group = OR)
        String cause;

        @ParameterItem( mappingTo = "statusCode", compare = equal)
        String code;

        @ParameterItem( mappingTo = "statusCode", compare = in)
        Collection<String> codes;//statusCode范围
    }
}

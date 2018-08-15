package com.yintong.erp.service.sale;

import com.yintong.erp.domain.sale.ErpSaleOrder;
import com.yintong.erp.domain.sale.ErpSaleOrderItem;
import com.yintong.erp.domain.sale.ErpSaleOrderItemRepository;
import com.yintong.erp.domain.sale.ErpSaleOrderOptLog;
import com.yintong.erp.domain.sale.ErpSaleOrderOptLogRepository;
import com.yintong.erp.domain.sale.ErpSaleOrderRepository;

import com.yintong.erp.domain.stock.ErpStockOutOrder;
import com.yintong.erp.domain.stock.ErpStockOutOrderRepository;
import com.yintong.erp.service.stock.StockOutProduct4Holder;
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
public class SaleOrderService implements OnDeleteCustomerValidator, OnDeleteProductValidator, StockOutProduct4Holder {

    @Autowired ErpSaleOrderRepository saleOrderRepository;

    @Autowired ErpSaleOrderItemRepository orderItemRepository;

    @Autowired ErpSaleOrderOptLogRepository orderOptLogRepository;

    @Autowired ErpStockOutOrderRepository stockOutOrderRepository;
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
            content += ",总额：¥" + totalMoney;
        }
        if(StringUtils.hasText(order.getRemark())){
            content += "," + order.getRemark();
        }
        orderOptLogRepository.save(ErpSaleOrderOptLog.builder().orderId(_order.getId()).content(content).optType("status").statusCode(_order.getStatusCode()).build());
        return _order;
    }

    /**
     * 修更新销售订单-不包含明细，不修改状态
     * 更新内容： 时间、总额、客户、订单日期
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
        //不修改创建时间
        order.setCreatedAt(old.getCreatedAt());

        String content = "更新";

        if(!DateUtil.getDateString(old.getOrderDate()).equals(DateUtil.getDateString(order.getOrderDate()))) {
            content += "订单时间："+ DateUtil.getDateString(order.getOrderDate()) + ";";
        }

        if(!old.getCustomerName().equals(order.getCustomerName())){
            content += "客户：" + order.getCustomerName();
        }
        ErpSaleOrder _order = saleOrderRepository.save(order);
        orderOptLogRepository.save(ErpSaleOrderOptLog.builder().statusCode(order.getStatusCode()).content(content).optType("order").orderId(order.getId()).build());
        return _order;
    }

    /**
     * 删除销售订单-级联删除明细
     * 约束条件：出库之前
     * @param orderId
     * @return
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        ErpSaleOrder old = saleOrderRepository.findById(orderId).orElse(null);
        Assert.notNull(old, "未找到销售订单[" + orderId + "]");
        Assert.isTrue(Arrays.asList(STATUS_001, STATUS_002, STATUS_003, STATUS_004).contains(SaleOrderStatus.valueOf(old.getStatusCode())), "订单状态:" + SaleOrderStatus.valueOf(old.getStatusCode()) +",无法删除");
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
        remark = StringUtils.hasLength(remark) ? ",备注：" + remark : "";
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
//            Assert.isTrue(STATUS_003.name().equals(oldStatus), "只有" + STATUS_003.description() + "的订单可以变更为" + status.description());
//        }
        //->客户退货、已完成
        if(status == STATUS_006 || status == STATUS_007){
            Assert.isTrue(STATUS_005.name().equals(oldStatus), "只有" + STATUS_005.description() + "的订单可以变更为" + status.description());
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
            List<Long> productIds = orderItems.stream().map(ErpSaleOrderItem::getProductId).filter(Objects::nonNull).collect(Collectors.toList());
            List<String> productNames = orderItems.stream().map(ErpSaleOrderItem::getProductName).filter(Objects::nonNull).collect(Collectors.toList());
            stockOutOrderRepository.save(
                    ErpStockOutOrder.builder()
                        .holder(SALE.name())
                        .holderId(orderId)
                        .holderBarCode(order.getBarCode())
                        .productIds(StringUtils.collectionToCommaDelimitedString(productIds))
                        .productNames(StringUtils.collectionToCommaDelimitedString(productNames))
                    .build()
            );
        }
        return  saleOrderRepository.save(order);
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

        Assert.isTrue(!CollectionUtils.isEmpty(oldOrderItems) && oldOrderItems.size() > 1, "总明细不能少于" + oldOrderItems.size() + "条,无法删除");
        Double totalMoney = oldOrder.getMoney() - oldItem.getMoney();
        oldOrder.setMoney(totalMoney);
        saleOrderRepository.save(oldOrder);
        orderItemRepository.deleteById(orderItemId);
        orderOptLogRepository.save(ErpSaleOrderOptLog.builder().statusCode(oldOrder.getStatusCode()).content("删除明细, 总金额变化为：¥" + totalMoney).optType("item").orderId(orderId).build());
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
        Assert.isNull(order, "请先删除销售单[" + order.getBarCode() + "]");
    }

    @Override
    public void onDeleteProduct(Long productId) {
        ErpSaleOrderItem item = orderItemRepository.findByProductId(productId)
                .stream().findAny().orElse(null);
        Assert.isNull(item, "请先删除销售单[" + item.getOrderCode() + "]中的明细");
    }

    @Override
    public boolean matchesOutProductHolder(StockHolder holder) {
        return SALE == holder;
    }

    /**
     * 对订单明细进行出库
     * @param holder
     * @param saleOrderId 销售订单id
     * @param productId 产品id
     * @param outNum 数量
     */
    @Override
    @Transactional
    public void stockOutProduct(StockHolder holder, Long saleOrderId, Long productId, double outNum) {
        //1-检查订单
        ErpSaleOrder order = saleOrderRepository.findById(saleOrderId).orElse(null);
        Assert.notNull(order, "未找到销售订单[" + saleOrderId + "]");
        Assert.isTrue( 1 == order.getPreStockOut(), "尚未打印出库单");

        List<ErpSaleOrderItem> items = orderItemRepository.findByOrderIdAndProductId(saleOrderId, productId);
        ErpSaleOrderItem item = CommonUtil.single(items, "销售订单[" + saleOrderId + "存在脏数据");
        if(Objects.isNull(item)) return;

        Assert.isTrue(!STATUS_005.name().equals(item.getStatusCode()), item.getProductName() + "已完成出库");
        SaleOrderStatus status = STATUS_049;//出库中
        if((outNum + item.getOutedNum()) >= item.getNum()){
            status = STATUS_005;
        }
        //2-保存订单明细
        item.setStatusCode(status.name());
        item.setOutedNum(outNum + item.getOutedNum());
        orderItemRepository.save(item);
        //3-明细日志
        String content = item.getProductName() + "完成出库,出库数量【" + item.getOutedNum() + "/" + item.getNum() + "】";
        orderOptLogRepository.save(ErpSaleOrderOptLog.builder().statusCode(status.name()).content(content).optType("item").orderId(saleOrderId).build());

        //4 未完成出库的订单明细
        List<ErpSaleOrderItem> unOutItems = orderItemRepository.findByOrderIdAndStatusCodeNot(saleOrderId, STATUS_005.name());
        if(CollectionUtils.isEmpty(unOutItems)){
            //不存在未完成出库的明细->修改订单状态为：已出库
            saleOrderRepository.save(order.setStatusCode(STATUS_005));
            orderOptLogRepository.save(ErpSaleOrderOptLog.builder().statusCode(STATUS_005.name()).content(STATUS_005.toLog()).optType("status").orderId(saleOrderId).build());
        } else if(!STATUS_049.name().equals(order.getStatusCode())){
            //存在未完成出库的明细，且自身状态不为：出库中
            saleOrderRepository.save(order.setStatusCode(STATUS_049));
            orderOptLogRepository.save(ErpSaleOrderOptLog.builder().statusCode(STATUS_049.name()).content(STATUS_049.toLog()).optType("status").orderId(saleOrderId).build());
        }
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

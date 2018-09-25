package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseCustomer;
import com.yintong.erp.domain.basis.ErpBaseCustomerRepository;
import com.yintong.erp.domain.sale.ErpSaleOrder;
import com.yintong.erp.domain.sale.ErpSaleOrderItemRepository;
import com.yintong.erp.domain.sale.ErpSaleOrderRepository;
import com.yintong.erp.dto.TreeNode;
import com.yintong.erp.utils.bar.BarCodeConstants;
import com.yintong.erp.utils.common.CommonUtil;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import com.yintong.erp.validator.OnDeleteCustomerValidator;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.*;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.equal;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.like;
import static javax.persistence.criteria.Predicate.BooleanOperator.OR;

/**
 * Created by jianqiang on 2018/6/3 0003.
 * 客户
 */
@Service
public class CustomerService {

    @Autowired ErpBaseCustomerRepository erpBaseCustomerRepository;


    @Autowired ErpSaleOrderRepository saleOrderRepository;

    @Autowired ErpSaleOrderItemRepository saleOrderItemRepository;



    @Autowired(required = false)
    List<OnDeleteCustomerValidator> onDeleteCustomerValidator;

    /**
     * 动态查询
     * @param parameter
     * @return
     */
    public Page<ErpBaseCustomer> query(CustomerService.CustomerParameterBuilder parameter){
        return erpBaseCustomerRepository.findAll(parameter.specification(), parameter.pageable());
    }

    /**
     * 创建客户
     * @param customer
     * @return
     */
    public ErpBaseCustomer create(ErpBaseCustomer customer){
        customer.setId(null);//防止假数据
        validateCustomer(customer);
        return erpBaseCustomerRepository.save(customer);
    }

    /**
     * 更新供应商
     * @param customer
     * @return
     */
    public ErpBaseCustomer update(ErpBaseCustomer customer){
        Assert.notNull(customer.getId(), "客户id不能为空");
        Assert.notNull(erpBaseCustomerRepository.findById(customer.getId()).orElse(null), "未找到客户");
        validateCustomer(customer);
        return erpBaseCustomerRepository.save(customer);
    }

    /**
     * 根据客户id查找客户
     * @param customerId
     * @return
     */
    public ErpBaseCustomer one(Long customerId){
        ErpBaseCustomer customer = erpBaseCustomerRepository.findById(customerId).orElse(null);
        Assert.notNull(customer, "未找到客户");
        return customer;
    }

    /**
     * 根据客户id查找销售订单
     * @param customerId
     * @return
     */
    public List<TreeNode> findSaleOrders(Long customerId){

        List<ErpSaleOrder> orders = saleOrderRepository.findByCustomerId(customerId);
        if(CollectionUtils.isEmpty(orders)) return new ArrayList<>();
        Map<Long, ErpSaleOrder> orderMap = orders.stream().collect(Collectors.toMap(ErpSaleOrder::getId, Function.identity()));


        List<TreeNode> branch = orders.stream()
                .map(order -> new TreeNode(order.getId().toString(), order.getBarCode() + "[" + Constants.SaleOrderStatus.valueOf(order.getStatusCode()).description() + "]" , null, true))
                .collect(Collectors.toList());
        List<TreeNode> leaves = saleOrderItemRepository.findByOrderIdInOrderByMoney(orders.stream().map(ErpSaleOrder::getId).collect(Collectors.toList()))
                .stream()
                .map(item->{
                    ErpSaleOrder order = orderMap.get(item.getOrderId());
                    if(Objects.isNull(order)) return null;
                    return new TreeNode(item.getId().toString(),
                            item.getProductName() + "[ ¥" + item.getUnitPrice() + "*" + item.getNum() + " = ¥" + item.getMoney() + "]", order.getId().toString(),
                            false);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        branch.addAll(leaves);
        return branch;
    }




    /**
     * 删除客户
     * @param customerId
     */
    @Transactional
    public void delete(Long customerId){
        if(!CollectionUtils.isEmpty(onDeleteCustomerValidator))
            onDeleteCustomerValidator.forEach(validator -> validator.onDeleteCustomer(customerId));
        erpBaseCustomerRepository.deleteById(customerId);
    }

    /**
     * 查询客户
     * @return
     */
    public List<ErpBaseCustomer> findSupplierAll(){
        return erpBaseCustomerRepository.findAll();
    }

    /**
     * 验证客户
     * @param customer
     */
    private void validateCustomer(ErpBaseCustomer customer){
        Assert.notNull(customer, "客户不能为null");
        String type = customer.getCustomerTypeCode();
        Assert.hasLength(type, "类型不能为空");
        Assert.isTrue(Arrays.asList(UCC0, UCS0, UCG0).contains(BarCodeConstants.BAR_CODE_PREFIX.valueOf(type)), "客户类型不正确");
        Assert.hasLength(customer.getCustomerName(), "客户名称不能为空");
    }


    @Getter
    @Setter
    @OrderBy(fieldName = "barCode")
    public static class CustomerParameterBuilder extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "customerName", "contactName", "contactMobile", "contactPhone"}, compare = like, group = OR)
        String cause;
        @ParameterItem(mappingTo = "customerTypeCode", compare = equal)
        String type;
        @ParameterItem(mappingTo = "rank", compare = equal)
        String rank;
    }
}

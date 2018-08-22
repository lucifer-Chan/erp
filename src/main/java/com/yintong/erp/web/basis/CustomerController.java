package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseCustomer;
import com.yintong.erp.domain.basis.ErpBaseCustomerRepository;
import com.yintong.erp.service.basis.CustomerService;
import com.yintong.erp.utils.base.BaseResult;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

/**
 * Created by jianqiang on 2018/6/3 0003.
 * 客户
 */
@RestController
@RequestMapping("basis/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired ErpBaseCustomerRepository customerRepository;

    /**
     * 组合查询
     * @param parameter
     * @return
     */
    @GetMapping
    public BaseResult query(CustomerService.CustomerParameterBuilder parameter){
        Page<ErpBaseCustomer> page = customerService.query(parameter);
        return page2BaseResult(page);
    }

    /**
     * 查找全部
     * @return
     */
    @GetMapping("all")
    public BaseResult findAll(){
        return new BaseResult().addList(customerRepository.findAll());
    }

    /**
     * 新增客户
     * @param customer
     * @return
     */
    @PostMapping
    public BaseResult create(@RequestBody ErpBaseCustomer customer){
        return new BaseResult().addPojo(customerService.create(customer));
    }

    /**
     * 批量新增
     * @param customers
     * @return
     */
    @PostMapping("batch/save")
    public BaseResult batchCreate(@RequestBody List<ErpBaseCustomer> customers){
        int result = 0;
        List<String> exceptions = new ArrayList<>();
        for(ErpBaseCustomer customer : customers){
            try{
                customerService.create(customer);
                result ++;
            } catch (Exception e){
                exceptions.add(e.getMessage());
            }
        }

        return new BaseResult().addList("exceptions", exceptions).put("result", result);
    }

    /**
     * 更新客户
     * @param customer
     * @return
     */
    @PutMapping
    public BaseResult update(@RequestBody ErpBaseCustomer customer){
        return new BaseResult().addPojo(customerService.update(customer));
    }

    /**
     * 根据id删除
     * @param customerId
     * @return
     */
    @DeleteMapping("{customerId}")
    public BaseResult delete(@PathVariable Long customerId){
        customerService.delete(customerId);
        return new BaseResult().setErrmsg("删除成功");
    }

    /**
     * 根据供应商id查找客户
     * @param customerId
     * @return
     */
    @GetMapping("{customerId}")
    public BaseResult one(@PathVariable Long customerId){
        return new BaseResult().addPojo(customerService.one(customerId));
    }

    /**
     * 销售订单tree
     * @param customerId
     * @return
     */
    @GetMapping("{customerId}/orders")
    public BaseResult orders(@PathVariable Long customerId){
        return new BaseResult().addList(customerService.findSaleOrders(customerId));
    }
}

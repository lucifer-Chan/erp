package com.yintong.erp.service;

import com.yintong.erp.domain.basis.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午10:09
 * 预置数据服务
 **/

@Component
public class PreDataService {

    @Autowired ErpMenuRepository menuRepository;

    @Autowired ErpEmployeeRepository employeeRepository;

    @Autowired ErpEmployeeMenuRepository erpEmployeeMenuRepository;

    @PostConstruct
    void init(){
        initMenus();
        initEmployees();
    }


    /**
     * 基础数据 10
     *  -人员管理   1001
     *  -成品管理   1002
     *  -原材料管理 1003
     *  -模具管理   1004
     *  -设备管理   1005
     *  -客户管理   1006
     *  -供应商管理 1007
     * 销售模块 20
     *  -销售计划单 2001
     *  -销售订单   2002
     *  -销售审核   2003
     *  -退货单管理 2004
     * TODO 待完善
     */
    private void initMenus(){
        menuRepository.deleteAll();
        List<ErpMenu> menus = Arrays.asList(
                ErpMenu.builder().code("10").name("基础数据").build()
                    , ErpMenu.builder().code("1001").name("人员管理").matches("/employee/**").uri("/tpl/employee.html").parentCode("10").build()
                    , ErpMenu.builder().code("1002").name("成品管理").matches("/product/**").uri("/tpl/product.html").parentCode("10").build()
                    , ErpMenu.builder().code("1003").name("原材料管理").matches("/material/**").uri("/tpl/material.html").parentCode("10").build()
                    , ErpMenu.builder().code("1004").name("模具管理").matches("/mould/**").uri("/tpl/mould.html").parentCode("10").build()
                    , ErpMenu.builder().code("1005").name("设备管理").matches("/equipment;/**").uri("/tpl/equipment;.html").parentCode("10").build()
                    , ErpMenu.builder().code("1006").name("客户管理").matches("/customer/**").uri("/tpl/customer.html").parentCode("10").build()
                    , ErpMenu.builder().code("1007").name("供应商管理").matches("/supplier/**").uri("/tpl/supplier.html").parentCode("10").build()
                , ErpMenu.builder().code("20").name("销售模块").build()
                    , ErpMenu.builder().code("2001").name("销售计划单").matches("/sale/plan/**").uri("/tpl/sale/plan.html").parentCode("20").build()
                    , ErpMenu.builder().code("2002").name("销售订单").matches("/sale/order/**").uri("/tpl/sale/order.html").parentCode("20").build()
                    , ErpMenu.builder().code("2003").name("销售审核").matches("/sale/approval/**").uri("/tpl/sale/approval.html").parentCode("20").build()
                    , ErpMenu.builder().code("2004").name("退货单管理").matches("/sale/refunds/**").uri("/tpl/sale/refunds.html").parentCode("20").build()
        );
        menuRepository.saveAll(menus);
    }

    /**
     * 初始化人员
     */
    private void initEmployees(){
        employeeRepository.deleteAll();
        erpEmployeeMenuRepository.deleteAll();
        //管理员
        ErpEmployee admin = employeeRepository.save(ErpEmployee.builder().loginName("admin").password("123").name("管理员").build());
        erpEmployeeMenuRepository.save(ErpEmployeeMenu.builder().employeeId(admin.getId()).menuCode("99").build());
        //测试员工
        ErpEmployee employee = employeeRepository.save(ErpEmployee.builder().loginName("test").password("123").name("测试人员").build());
        erpEmployeeMenuRepository.saveAll(
                Arrays.asList(
                        ErpEmployeeMenu.builder().employeeId(employee.getId()).menuCode("1006").build()
                        , ErpEmployeeMenu.builder().employeeId(employee.getId()).menuCode("1007").build()
                        , ErpEmployeeMenu.builder().employeeId(employee.getId()).menuCode("2004").build()
                )
        );
    }
}

package com.yintong.erp.service;

import com.yintong.erp.domain.basis.ErpBaseCategory;
import com.yintong.erp.domain.basis.ErpBaseCategoryRepository;
import com.yintong.erp.domain.basis.ErpBaseDepartment;
import com.yintong.erp.domain.basis.ErpBaseDepartmentRepository;
import com.yintong.erp.domain.basis.security.*;
import com.yintong.erp.utils.bar.BarCodeConstants.*;
import org.apache.commons.collections4.KeyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.LinkedHashMap;
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

    @Autowired ErpBaseCategoryRepository categoryRepository;

    @Autowired ErpBaseDepartmentRepository departmentRepository;

    @Value("${yintong.erp.model.debug}")
    private boolean debug;

    @PostConstruct
    void init(){
        if(!debug) return;
        initMenus();
        initEmployees();
        initCategories();
        initDepartments();
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

    /**
     * 初始化类别
     */
    private void initCategories(){
        categoryRepository.deleteAll();

        LinkedHashMap<String, ErpBaseCategory> map = new LinkedHashMap<>();

        for (BAR_CODE_PREFIX prefix : BAR_CODE_PREFIX.values()){
            KeyValue<String, String> first = prefix.first();
            KeyValue<String, String> second = prefix.second();
            KeyValue<String, String> third = prefix.third();
            KeyValue<String, String> fourth = prefix.fourth();

            //一级分类
            ErpBaseCategory _1 = ErpBaseCategory.builder()
                    .code("0".equals(second.getKey()) ? first.getKey() + "000" : first.getKey())
                    .name(first.getValue())
                    .fullName(first.getValue()).build();
            map.putIfAbsent(_1.getCode(), _1);
            //二级分类
            if(!"0".equals(second.getKey())){
                String code = first.getKey() + second.getKey();
                //没有三级的情况
                if("0".equals(third.getKey()))
                    code = code + "00";
                ErpBaseCategory _2 = ErpBaseCategory.builder()
                                .name(second.getValue())
                                .code(code)
                                .fullName(first.getValue() + "-" + second.getValue())
                                .parentCode(first.getKey())
                                .build();
                map.putIfAbsent(_2.getCode(), _2);
            }
            //三级分类
            if(!"0".equals(third.getKey())){
                String code = first.getKey() + second.getKey() + third.getKey();
                //没有四级的情况
                if("0".equals(fourth.getKey()))
                    code = code + "0";
                ErpBaseCategory _3 = ErpBaseCategory.builder()
                        .name(third.getValue())
                        .code(code)
                        .fullName(first.getValue() + "-" + second.getValue() + "-" + third.getValue())
                        .parentCode(first.getKey() + second.getKey())
                        .build();
                map.putIfAbsent(_3.getCode(), _3);
            }

            //四级分类
            if(!"0".equals(fourth.getKey())){
                ErpBaseCategory _4 = ErpBaseCategory.builder()
                        .name(fourth.getValue())
                        .code(prefix.name())
                        .fullName(prefix.description())
                        .parentCode(first.getKey() + second.getKey() + third.getKey())
                        .build();
                map.putIfAbsent(_4.getCode(), _4);
            }
            categoryRepository.saveAll(map.values());
        }
    }

    /**
     * 初始化部门
     */
    private void initDepartments(){
        departmentRepository.deleteAll();
        departmentRepository.saveAll(
                Arrays.asList(
                        ErpBaseDepartment.builder().code("D001").name("综合办公室").build()
                        , ErpBaseDepartment.builder().code("D002").name("销售部").build()
                        , ErpBaseDepartment.builder().code("D003").name("采购部").build()
                        , ErpBaseDepartment.builder().code("D012").name("总经理室").build()
                )
        );

        //仓储部
        ErpBaseDepartment _1 = departmentRepository.save(ErpBaseDepartment.builder().code("D004").name("仓储部").build());
        departmentRepository.saveAll(
                Arrays.asList(
                        ErpBaseDepartment.builder().code("D00401").name("原材料仓储部").parentId(_1.getId()).build()
                        , ErpBaseDepartment.builder().code("D00402").name("成品仓储部").parentId(_1.getId()).build()
                        , ErpBaseDepartment.builder().code("D00403").name("运输部").parentId(_1.getId()).build()
                        , ErpBaseDepartment.builder().code("D00404").name("废品仓储部").parentId(_1.getId()).build()
                )
        );
        //生产部
        ErpBaseDepartment _2 = departmentRepository.save(ErpBaseDepartment.builder().code("D005").name("生产部").build());
        departmentRepository.saveAll(
                Arrays.asList(
                        ErpBaseDepartment.builder().code("D00501").name("一号车间").parentId(_2.getId()).build()
                        , ErpBaseDepartment.builder().code("D00502").name("二号车间").parentId(_2.getId()).build()
                )
        );

        //技术部
        ErpBaseDepartment _3 = departmentRepository.save(ErpBaseDepartment.builder().code("D006").name("技术部").build());
        departmentRepository.saveAll(
                Arrays.asList(
                        ErpBaseDepartment.builder().code("D00601").name("开发部").parentId(_3.getId()).build()
                        , ErpBaseDepartment.builder().code("D00602").name("理化室").parentId(_3.getId()).build()
                )
        );
    }
}
package com.yintong.erp.service;

import com.yintong.erp.domain.basis.*;
import com.yintong.erp.domain.basis.security.*;
import com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX;
import com.yintong.erp.utils.common.Constants;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.KeyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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

    @Autowired ErpBaseLookupRepository lookupRepository;


    @Autowired ErpMiniRoleRepository miniRoleRepository;

    @Value("${yintong.erp.model.debug}")
    private boolean debug;

    @PostConstruct
    void init(){
//        if(!debug) return;
//        initMenus();
//        initEmployees();
//        initCategories();
//        initDepartments();
//        initLookup();
        initMiniRoles();
    }

    public void initMiniRoles(){
        miniRoleRepository.deleteAll();

        List<ErpMiniRole> roles = Arrays.asList(
                ErpMiniRole.builder().code("IN_INIT").name("初始入库").build()
                , ErpMiniRole.builder().code("IN_BUY").name("采购入库").build()
                , ErpMiniRole.builder().code("OUT_BUY").name("采购退货").build()
                , ErpMiniRole.builder().code("OUT_SALE").name("销售出库").build()
                , ErpMiniRole.builder().code("IN_REFUNDS").name("销售退货").build()
                , ErpMiniRole.builder().code("OUT_PROD").name("生产物料出库").build()
                , ErpMiniRole.builder().code("IN_PROD_P").name("生产成品入库").build()
                , ErpMiniRole.builder().code("IN_PROD_W").name("原材料退回").build()
                , ErpMiniRole.builder().code("inventory").name("仓库盘点").build()
                , ErpMiniRole.builder().code("employee").name("扫码领料").build()
                , ErpMiniRole.builder().code("garbage").name("废料入库").build()


                , ErpMiniRole.builder().code("PROD_STAGE_1").name("半成品流转").build()
                , ErpMiniRole.builder().code("PROD_STAGE_2").name("半成品后处理").build()
                , ErpMiniRole.builder().code("PROD_STAGE_3").name("半成品挑拣").build()
                , ErpMiniRole.builder().code("PROD_STAGE_4").name("成品包装").build()
        );

        miniRoleRepository.saveAll(roles);

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
//        if(! CollectionUtils.isEmpty(menuRepository.findAll())) return;
        menuRepository.deleteAll();
        List<ErpMenu> menus = Arrays.asList(
                ErpMenu.builder().code("10").name("基础数据").build()
                    , ErpMenu.builder().code("1001").name("人员组织管理").matches("basis/employee/**").uri("basis/employee.html").parentCode("10").build()
                    , ErpMenu.builder().code("1002").name("成品管理").matches("basis/product/**").uri("basis/product.html").parentCode("10").build()
                    , ErpMenu.builder().code("1003").name("原材料管理").matches("basis/material/**").uri("basis/material.html").parentCode("10").build()
                    , ErpMenu.builder().code("1004").name("模具管理").matches("basis/mould/**").uri("basis/mould.html").parentCode("10").build()
                    , ErpMenu.builder().code("1005").name("设备管理").matches("basis/equipment/**").uri("basis/equipment.html").parentCode("10").build()
                    , ErpMenu.builder().code("1006").name("客户管理").matches("basis/customer/**").uri("basis/customer.html").parentCode("10").build()
                    , ErpMenu.builder().code("1007").name("供应商管理").matches("basis/supplier/**").uri("basis/supplier.html").parentCode("10").build()
                , ErpMenu.builder().code("20").name("销售模块").build()
                    , ErpMenu.builder().code("2001").name("销售计划单").matches("/sale/plan/**").uri("sale/plan.html").parentCode("20").build()
                    , ErpMenu.builder().code("2002").name("销售订单").matches("/sale/order/**").uri("sale/order.html").parentCode("20").build()
                    , ErpMenu.builder().code("2003").name("销售审核").matches("/sale/order/**").uri("sale/approval.html").parentCode("20").build()
//                    , ErpMenu.builder().code("2004").name("测试打印").matches("/sale/order/**").uri("sale/refunds.html").parentCode("20").build()
                , ErpMenu.builder().code("30").name("库存管理").build()
                    , ErpMenu.builder().code("3001").name("仓位维护").matches("/stock/place/**").uri("stock/place.html").parentCode("30").build()


                , ErpMenu.builder().code("40").name("采购管理").build()
                    , ErpMenu.builder().code("4001").name("采购计划单").matches("/purchase/plan/**").uri("purchase/plan.html").parentCode("40").build()
                    , ErpMenu.builder().code("4002").name("采购订单").matches("/purchase/order/**").uri("purchase/order.html").parentCode("40").build()
                    , ErpMenu.builder().code("4003").name("采购审核").matches("/purchase/order/**").uri("purchase/approval.html").parentCode("40").build()

                , ErpMenu.builder().code("50").name("生产管理").build()
                    , ErpMenu.builder().code("5001").name("生产计划单").matches("/prod/plan/**").uri("prod/plan.html").parentCode("50").build()
                    , ErpMenu.builder().code("5002").name("生产制令单").matches("/prod/order/**").uri("prod/order.html").parentCode("50").build()

        );
        menuRepository.saveAll(menus);
    }

    /**
     * 初始化人员
     */
    private void initEmployees(){
        if(! CollectionUtils.isEmpty(employeeRepository.findAll())) return;
        employeeRepository.deleteAll();
        erpEmployeeMenuRepository.deleteAll();
        //管理员
        ErpEmployee admin = employeeRepository.save(ErpEmployee.builder().loginName("admin").password("123").name("管理员").build());
        erpEmployeeMenuRepository.save(ErpEmployeeMenu.builder().employeeId(admin.getId()).menuCode("99").build());
        //测试员工
//        employeeRepository.save(ErpEmployee.builder().loginName("test").password("123").name("测试人员").build());
    }

    /**
     * 初始化类别
     */
    private void initCategories(){
        if(! CollectionUtils.isEmpty(categoryRepository.findAll())) return;
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
        if(! CollectionUtils.isEmpty(departmentRepository.findAll())) return;
        departmentRepository.deleteAll();
        departmentRepository.saveAll(
                Arrays.asList(
                        ErpBaseDepartment.builder().code("D001").name("综合办公室").parentId(-1L).build()
                        , ErpBaseDepartment.builder().code("D002").name("销售部").parentId(-1L).build()
                        , ErpBaseDepartment.builder().code("D003").name("采购部").parentId(-1L).build()
                        , ErpBaseDepartment.builder().code("D012").name("总经理室").parentId(-1L).build()
                )
        );

        //仓储部
        ErpBaseDepartment _1 = departmentRepository.save(ErpBaseDepartment.builder().code("D004").name("仓储部").parentId(-1L).build());
        departmentRepository.saveAll(
                Arrays.asList(
                        ErpBaseDepartment.builder().code("D00401").name("原材料仓储部").parentId(_1.getId()).build()
                        , ErpBaseDepartment.builder().code("D00402").name("成品仓储部").parentId(_1.getId()).build()
                        , ErpBaseDepartment.builder().code("D00403").name("运输部").parentId(_1.getId()).build()
                        , ErpBaseDepartment.builder().code("D00404").name("废品仓储部").parentId(_1.getId()).build()
                )
        );
        //生产部
        ErpBaseDepartment _2 = departmentRepository.save(ErpBaseDepartment.builder().code("D005").name("生产部").parentId(-1L).build());
        departmentRepository.saveAll(
                Arrays.asList(
                        ErpBaseDepartment.builder().code("D00501").name("一号车间").parentId(_2.getId()).build()
                        , ErpBaseDepartment.builder().code("D00502").name("二号车间").parentId(_2.getId()).build()
                )
        );

        //技术部
        ErpBaseDepartment _3 = departmentRepository.save(ErpBaseDepartment.builder().code("D006").name("技术部").parentId(-1L).build());
        departmentRepository.saveAll(
                Arrays.asList(
                        ErpBaseDepartment.builder().code("D00601").name("开发部").parentId(_3.getId()).build()
                        , ErpBaseDepartment.builder().code("D00602").name("理化室").parentId(_3.getId()).build()
                )
        );
    }

    private void initLookup(){
//        if(! CollectionUtils.isEmpty(lookupRepository.findAll())) return;
        lookupRepository.deleteAll();
        lookupRepository.saveAll(
                Arrays.asList(
                        //供应商等级
                        ErpBaseLookup.builder().code("000").name("【无】").type("supplier").tag(0L).description("供应商等级").build()
                        , ErpBaseLookup.builder().code("001").name("一级").type("supplier").tag(1L).description("供应商等级").build()
                        , ErpBaseLookup.builder().code("002").name("二级").type("supplier").tag(2L).description("供应商等级").build()
                        , ErpBaseLookup.builder().code("003").name("三级").type("supplier").tag(3L).description("供应商等级").build()
                        //客户等级
                        ,ErpBaseLookup.builder().code("000").name("【无】").type("customer").tag(0L).description("客户等级").build()
                        , ErpBaseLookup.builder().code("001").name("一级").type("customer").tag(1L).description("客户等级").build()
                        , ErpBaseLookup.builder().code("002").name("二级").type("customer").tag(2L).description("客户等级").build()
                        , ErpBaseLookup.builder().code("003").name("三级").type("customer").tag(3L).description("客户等级").build()
                )
        );

        //销售订单状态
        lookupRepository.saveAll(
                Stream.of(Constants.SaleOrderStatus.values())
                        .map(status ->
                                ErpBaseLookup.builder()
                                        .code(status.name())
                                        .name(status.description())
                                        .type("sale_order")
                                        .description("销售订单状态")
                                        .tag(Integer.valueOf(status.ordinal()).longValue())
                                        .build()
                        )
                .collect(Collectors.toList())
        );

        //采购订单状态
        lookupRepository.saveAll(
                Stream.of(Constants.PurchaseOrderStatus.values())
                        .map(status ->
                                ErpBaseLookup.builder()
                                        .code(status.name())
                                        .name(status.description())
                                        .type("purchase_order")
                                        .description("采购订单状态")
                                        .tag(Integer.valueOf(status.ordinal()).longValue())
                                        .build()
                        )
                        .collect(Collectors.toList())
        );

        //仓库类型
        lookupRepository.saveAll(
                Stream.of(Constants.StockPlaceType.values())
                        .map(status ->
                                ErpBaseLookup.builder()
                                        .code(status.name())
                                        .name(status.description())
                                        .type("sp_type")
                                        .description("仓位类型")
                                        .tag(Integer.valueOf(status.ordinal()).longValue())
                                        .build()
                        )
                        .collect(Collectors.toList())
        );
        //仓位状态
        lookupRepository.saveAll(
                Stream.of(Constants.StockPlaceStatus.values())
                    .map(status ->
                            ErpBaseLookup.builder()
                                    .code(status.name())
                                    .name(status.description())
                                    .type("sp_status")
                                    .description("仓位状态")
                                    .tag(Integer.valueOf(status.ordinal()).longValue())
                                    .build()
                    )
                .collect(Collectors.toList())
        );
    }
}
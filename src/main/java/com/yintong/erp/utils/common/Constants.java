package com.yintong.erp.utils.common;

import org.springframework.util.StringUtils;

public interface Constants {

    interface Roles{
        /**
         * 管理员的权限
         */
        String ADMIN_ROLE_CODE = "99";
        /**
         * 管理员的权限匹配
         */
        String ADMIN_ROLE_MATCHES = "/**";
        /**
         * 任何登陆用户的权限
         */
        String PROFILE_ROLE_CODE = "98";
        /**
         * 任何登陆用户的权限匹配
         */
        String PROFILE_ROLE_MATCHES = "/profile/**" +
                //当前菜单树
                ",basis/menus/current/tree" +
                ",basis/common/*"
                ;

        //不登陆也可以访问
        String OPEN_MATCHES = "/open/**";
//        String OPEN_MATCHES = "/open/**,/js/**" +
//                ",/**";
    }

    /**
     * 销售订单状态
     */
    enum SaleOrderStatus {
        STATUS_001("未发布", "新建"),
        STATUS_002("待审核", "提交"),
        STATUS_003("审核通过", "审核"),
        STATUS_004("审核退回", "审核"),
        STATUS_049("正在出库", ""),
        STATUS_005("已出库", ""),//全部出库完成
        STATUS_006("客户退货", ""),
        STATUS_061("完成退货", "入库"),//全部入库完成
        STATUS_007("已完成", ""),
        STATUS_008("作废", "审核");

        SaleOrderStatus(String description, String operation) {
            this.description = description;
            this.operation = operation;
        }
        
        private String description;

        private String operation;

        public String description() {
            return description;
        }

        public String operation() {
            return operation;
        }

        public String toLog(){
            String prefix = StringUtils.hasLength(operation()) ? operation() + "-" : "";
            return "【" + prefix + description() + "】";
        }
    }

    /**
     * 仓位状态
     */
    enum StockPlaceStatus {
        ON("在役"), STOP("停役");

        StockPlaceStatus(String description){
            this.description = description;
        }
        private String description;
        public String description() {
            return description;
        }
    }

    /**
     * 制令单状态
     */
    enum ProdOrderStatus {
        S_001("新建"),
        S_002("生产中"),//打印了出库单-全部入库
        S_003("已完成");//全部入库

        ProdOrderStatus(String description){
            this.description = description;
        }
        private String description;
        public String description() {
            return description;
        }
    }

    /**
     * 仓位类型
     */
    enum StockPlaceType {
        P("成品仓位" ,"成品"),
        M("原材料仓位" ,"原材料"),
        R("废品仓位" ,"废品"),
        D("模具仓位" ,"模具");

        StockPlaceType(String description, String content){
            this.description = description;
            this.content = content;
        }
        private String description;
        private String content;
        public String description() {
            return description;
        }
        public String content() {
            return content;
        }
    }

    /**
     * 库存动作
     */
    enum StockOpt {
        IN("入库"), OUT("出库");

        StockOpt(String description){
            this.description = description;
        }
        private String description;
        public String description() {
            return description;
        }
    }

    /**
     * 出入库宿主
     */
    enum StockHolder {
        SALE("销售订单"), //出库
        REFUNDS("退货单"), //入库
        PROD("制令单"),  //出库 入库
        BUY("采购单"),  //入库
        INIT("初始化");//入库

        StockHolder(String description){
            this.description = description;
        }
        private String description;
        public String description() {
            return description;
        }
    }

    /**
     * 生产环节，物料清单的宿主
     */
    enum ProdBomHolder {
        PLAN("生产计划单"), ORDER("生产制令单");
        ProdBomHolder(String description){
            this.description = description;
        }
        private String description;
        public String description() {
            return description;
        }
    }

    /**
     * 货物类型：成品|原材料|模具
     */
    enum WaresType {
        P("成品"), M("原材料"), D("模具"), R("废品");
        WaresType(String description){
            this.description = description;
        }
        private String description;
        public String description() {
            return description;
        }
    }

    /**
     * 采购订单状态
     */
    enum PurchaseOrderStatus {
        STATUS_001("未发布", "新建"),
        STATUS_002("待审核", "提交"),
        STATUS_003("审核通过", "审核"),
        STATUS_004("审核退回", "审核"),
        STATUS_049("正在入库", ""),
        STATUS_005("已入库", ""),
        STATUS_007("已完成", ""),
        STATUS_008("作废", "审核");

        PurchaseOrderStatus(String description, String operation) {
            this.description = description;
            this.operation = operation;
        }

        private String description;

        private String operation;

        public String description() {
            return description;
        }

        public String operation() {
            return operation;
        }

        public String toLog(){
            String prefix = StringUtils.hasLength(operation()) ? operation() + "-" : "";
            return "【" + prefix + description() + "】";
        }
    }

}
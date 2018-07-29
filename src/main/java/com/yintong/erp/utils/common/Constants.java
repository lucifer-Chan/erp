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
        STATUS_005("已出库", ""),
        STATUS_006("客户退货", ""),
        STATUS_007("已完成", "");

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
}
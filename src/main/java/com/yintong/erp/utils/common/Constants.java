package com.yintong.erp.utils.common;

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
        STATUS_001("未发布"),
        STATUS_002("待审核"),
        STATUS_003("审核通过"),
        STATUS_004("审核退回"),
        STATUS_005("已出库"),
        STATUS_006("客户退货"),
        STATUS_007("已完成");

        SaleOrderStatus(String description) {
            this.description = description;
        }
        
        private String description;

        public String description() {
            return description;
        }
    }
}
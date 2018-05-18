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
        String PROFILE_ROLE_MATCHES = "/profile/**," +
                "basis/menus/current/tree"//当前菜单树
                ;

        //不登陆也可以访问
        String OPEN_MATCHES = "/open/**,/js/**" +
                ",/**";
    }
}

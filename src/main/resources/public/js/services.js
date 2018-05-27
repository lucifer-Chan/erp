define('services',['utils'],function (utils) {
    var account = {
        //登陆
        login : function (loginInfo) {
            return $.http.post({
                url : 'login',
                data : loginInfo
            }).then(function (ret) {
                $.local(GLOBALS.localKeys.accountInfo, ret);
                return ret;
            });
        },
        //退出登陆
        logout : function () {
            return $.http.post('logout');
        },

        //修改密码-self
        modifyPwd : function (old, newed) {
            return $.http.patch({
                url : 'profile/password',
                data : {old: old, newed: newed}
            });
        },

        //修改电话号码-self
        modifyMobile : function (mobile) {
            return $.http.patch({
                url : 'profile/mobile',
                data : {mobile: mobile}
            }).then(function (ret) {
                $.local(GLOBALS.localKeys.accountInfo, ret);
                return ret;
            });
        },

        /**
         * 根据id查找一个员工
         * @param employeeId
         */
        one : function (employeeId) {
            return $.http.get('basis/employee/' + employeeId);
        },

        //删除员工操作
        delete : function (employeeId) {
            return $.http.delete('basis/employee/' + employeeId);
        },

        /**
         * 新增用户
         * @param data - {name, loginName, password, mobile}
         * @returns {*}
         */
        create : function (data) {
            return $.http.post({
                url : 'basis/employee',
                data : data,
                contentType : $.contentType.json
            });
        },

        /**
         * 修改用户
         * @param data - {id, name, loginName, mobile}
         */
        update : function (data) {
            return $.http.put({
                url : 'basis/employee',
                data : data,
                contentType : $.contentType.json
            });
        },

        /**
         * 保存用户-部门
         * @param id
         * @param list
         */
        saveDepartments : function (id, list) {
            return $.http.post({
                url : 'basis/employee/' + id + '/departments',
                data : list,
                contentType : $.contentType.json
            })
        },

        /**
         * 保存用户-菜单[权限]
         * @param id
         * @param list
         */
        saveMenus : function (id, list) {
            return $.http.post({
                url : 'basis/employee/' + id + '/menus',
                data : list,
                contentType : $.contentType.json
            })
        },

        /**
         * 更新用户的密码
         * @param id
         * @param password
         */
        updatePassword : function (id, password) {
            return $.http.patch({
                url : 'basis/employee/' + id,
                data : {
                    password : password
                }
            });
        },

        /**
         * 组合查询
         * @param data - {cause|departmentId}
         * @returns {*}
         */
        query : function (data) {
            return $.http.get({
                url : 'basis/employee',
                data : $.extend({cause : '', departmentId: ''}, data)
            });
        }
    };

    var menus = {
        //获取当前登陆用户菜单
        current : function () {
            return $.http.get('basis/menus/current/tree');
        },

        //可操作的所有菜单
        children : function () {
            return $.http.get('basis/menus/all/operation');
        }
    };

    var department = {
        //获取所有组织
        all : function () {
            return $.http.get('basis/department/all');
        },

        //修改名称
        modify : function (id, name) {
            return $.http.patch({
                url : 'basis/department/' + id,
                data : {
                    name : name
                }
            });
        },

        //删除部门
        delete : function (id) {
            return $.http.delete('basis/department/' + id);
        },

        //新增部门
        create : function (data) {
            return $.http.post({
                url : 'basis/department',
                data : data,
                contentType : $.contentType.json
            })
        }
    };

    var supplier = {
        //分类
        types : function () {
            return $.http.get('basis/common/categories/children/direct?code=US');
        },
        //查询
        query : function (params) {
            return $.http.get({
                url : 'basis/supplier',
                data : params
            });
        },
        //查找
        one : function (id) {
            return $.http.get('basis/supplier/' + id);
        },

        //新建
        create : function (data) {
            return $.http.post({
                url : 'basis/supplier',
                data : data,
                contentType : $.contentType.json
            });
        },
        //更新
        update : function (data) {
            return $.http.put({
                url : 'basis/supplier',
                data : data,
                contentType : $.contentType.json
            });
        },
        //删除
        delete : function (id) {
            return $.http.delete('basis/supplier/' + id);
        }
    };

    var mould = {
        //分类
        types : function () {
            return $.http.get('basis/common/categories/children/direct?code=D');
        },
        //模块对应的供应商
        findSupplierAll : function () {
            return $.http.get('basis/mould/findSupplierAll');
        },
        //查询
        query : function (params) {
            return $.http.get({
                url : 'basis/mould',
                data : params
            });
        },
        //更新
        update : function (data) {
            return $.http.put({
                url : 'basis/mould',
                data : data,
                contentType : $.contentType.json
            });
        },
        //查找
        one : function (id) {
            return $.http.get('basis/mould/' + id);
        },
        //新增模具
        create:function(data){
            return $.http.post({
                url : 'basis/mould',
                data : data,
                contentType : $.contentType.json
            });
        },
        //删除
        delete : function (id) {
            return $.http.delete('basis/mould/' + id);
        }
    };

    var product = {
        //分类
        //分类
        getTypeAll : function () {
            return $.http.get('basis/common/categories/tree?code=P');
        },
        getTypeP : function () {
            return $.http.get('basis/common/categories/children/direct?code=P');
        },
        //细类
        getTypeC : function (code) {
            return $.http.get('basis/common/categories/children/direct?code='+code);
        },
        //模块对应的供应商
        findSupplierAll : function () {
            return $.http.get('basis/mould/findSupplierAll');
        },
        //查询
        query : function (params) {
            return $.http.get({
                url : 'basis/product',
                data : params
            });
        },
        //更新
        update : function (data) {
            return $.http.put({
                url : 'basis/product',
                data : data,
                contentType : $.contentType.json
            });
        },
        //查找
        one : function (id) {
            return $.http.get('basis/product/' + id);
        },
        //新增模具
        create:function(data){
            return $.http.post({
                url : 'basis/product',
                data : data,
                contentType : $.contentType.json
            });
        },
        //删除
        delete : function (id) {
            return $.http.delete('basis/product/' + id);
        }
    };

    /**
     * 下拉
     * @param type
     * @returns {*}
     */
    var lookup = function (type) {
        return $.http.get('basis/common/lookup/' + type);
    }

    return {
         account: account
        , menus : menus
        , department : department
        , supplier : supplier
        , mould : mould
        , product : product
        , lookup : lookup
    }
});
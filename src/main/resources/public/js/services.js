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

        //修改密码
        modifyPwd : function (old, newed) {
            return $.http.patch({
                url : '',
                data : {old: old, newed: newed}
            });
        },

        //删除员工操作
        delete: function () {
            return $.http.delete();
        }

    };

    var menus = {
        //获取当前登陆用户菜单
        current : function () {
            return $.http.get('menus/current/tree');
        }
    };

    var department = {
        //获取所有组织
        all : function () {
            return $.http.get('department/all');
        },

        //修改名称
        modify : function (id, name) {
            return $.http.patch({
                url : 'department/' + id,
                data : {
                    name : name
                }
            });
        },

        //删除部门
        delete : function (id) {
            return $.http.delete('department/' + id);
        },

        //新增部门
        create : function (data) {
            return $.http.post({
                url : 'department',
                data : data,
                contentType : $.contentType.json
            })
        }
    };

    return {
         account: account
        , menus : menus
        , department : department
    }
});
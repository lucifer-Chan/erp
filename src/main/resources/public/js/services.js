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
        },
        //导入的查询
        imported : function () {
            return $.http.get('basis/product/group');
        },
        //批量删除
        batchDelete : function (importedAt) {
            return $.http.delete('basis/product/batch/' + importedAt);
        },
        //所有成品
        all : function () {
            return $.http.get('basis/product/all');
        },
        //保存上下限 {supplierId}/product/{productId}
        saveWarning : function(id, alertLower, alertUpper){
            return $.http.patch({
                url : 'basis/product/'+ id,
                data : {
                    alertLower : alertLower,
                    alertUpper : alertUpper
                }
            })
        }
    };

    var equipment = {
        //分类
        types : function () {
            return $.http.get('basis/common/categories/children/direct?code=E');
        },

        //查询
        query : function (params) {
            return $.http.get({
                url : 'basis/equipment',
                data : params
            });
        },
        //更新
        update : function (data) {
            return $.http.put({
                url : 'basis/equipment',
                data : data,
                contentType : $.contentType.json
            });
        },
        //查找
        one : function (id) {
            return $.http.get('basis/equipment/' + id);
        },
        //新增设备
        create:function(data){
            return $.http.post({
                url : 'basis/equipment',
                data : data,
                contentType : $.contentType.json
            });
        },
        //删除
        delete : function (id) {
            return $.http.delete('basis/equipment/' + id);
        }
    };

    var rawMaterial = {
        //分类
        getTypeAll : function () {
            return $.http.get('basis/common/categories/tree?code=M');
        },
        getTypeP : function () {
            return $.http.get('basis/common/categories/children/direct?code=M');
        },
        //细类
        getTypeC : function (code) {
            return $.http.get('basis/common/categories/children/direct?code='+code);
        },

        //查询
        query : function (params) {
            return $.http.get({
                url : 'basis/rawMaterial',
                data : params
            });
        },
        //更新
        update : function (data) {
            return $.http.put({
                url : 'basis/rawMaterial',
                data : data,
                contentType : $.contentType.json
            });
        },
        //查找
        one : function (id) {
            return $.http.get('basis/rawMaterial/' + id);
        },
        //新增模具
        create:function(data){
            return $.http.post({
                url : 'basis/rawMaterial',
                data : data,
                contentType : $.contentType.json
            });
        },
        //删除
        delete : function (id) {
            return $.http.delete('basis/rawMaterial/' + id);
        },
        //导入的查询
        imported : function () {
            return $.http.get('basis/rawMaterial/group');
        },
        //批量删除
        batchDelete : function (importedAt) {
            return $.http.delete('basis/rawMaterial/batch/' + importedAt);
        }
    };

    var customer = {
        //分类
        types : function () {
            return $.http.get('basis/common/categories/children/direct?code=UC');
        },
        //查询
        query : function (params) {
            return $.http.get({
                url : 'basis/customer',
                data : params
            });
        },
        //查找
        one : function (id) {
            return $.http.get('basis/customer/' + id);
        },
        //新建
        create : function (data) {
            return $.http.post({
                url : 'basis/customer',
                data : data,
                contentType : $.contentType.json
            });
        },
        //更新
        update : function (data) {
            return $.http.put({
                url : 'basis/customer',
                data : data,
                contentType : $.contentType.json
            });
        },
        //删除
        delete : function (id) {
            return $.http.delete('basis/customer/' + id);
        }
    };

    /**
     * 下拉
     * @param type
     * @returns {*}
     */
    var lookup = function (type) {
        return $.http.get('basis/common/lookup/' + type);
    };

    /**
     * 关联
     * @type {{}}
     */
    var association = {
        //供应商
        supplier : function (supplierId) {
            return {
                //供应商-成品树[未关联]
                unProductTree : function () {
                    return $.http.get({
                        url : 'basis/supplier/product/nodes/unassociated',
                        data : {
                            supplierId : supplierId
                        }
                    })
                }
                //保存关联
                , saveProducts : function (productIds) {
                    return $.http.post({
                        url : 'basis/supplier/' + supplierId + '/product',
                        data : productIds,
                        contentType : $.contentType.json
                    })
                }
                //删除关联//@DeleteMapping("{supplierId}/product/{productId}")
                , delete : function (productId) {
                    return $.http.delete('basis/supplier/'+ supplierId+'/product/' + productId);
                }
                //供应商-成品树[已关联]
                , edProductTree : function () {
                    return $.http.get({
                        url : 'basis/supplier/product/nodes/associated',
                        data : {
                            supplierId : supplierId
                        }
                    })
                }
                //保存上下限 {supplierId}/product/{productId}
                , saveWarning : function(productId, alertLower, alertUpper){
                    return $.http.patch({
                        url : 'basis/supplier/'+ supplierId +'/product/' + productId,
                        data : {
                            alertLower : alertLower,
                            alertUpper : alertUpper
                        }
                    })
                }

            }
        },
        rawMaterial : function (supplierId) {
            return {
                //供应商-原材料树[未关联]
                unRawMaterialTree : function () {
                    return $.http.get({
                        url : 'basis/supplier/rawMaterial/nodes/unassociated',
                        data : {
                            supplierId : supplierId
                        }
                    })
                }
                //保存关联
                , saveRawMaterials : function (rawMaterialIds) {
                    return $.http.post({
                        url : 'basis/supplier/' + supplierId + '/rawMaterial',
                        data : rawMaterialIds,
                        contentType : $.contentType.json
                    })
                }
                //删除关联//@DeleteMapping("{supplierId}/rawMaterial/{rawMaterialId}")
                , delete : function (rawMaterialId) {
                    return $.http.delete('basis/supplier/'+ supplierId+'/rawMaterial/' + rawMaterialId);
                }
                //供应商-原材料树[已关联]
                , rawMaterialTree : function () {
                    return $.http.get({
                        url : 'basis/supplier/rawMaterial/nodes/associated',
                        data : {
                            supplierId : supplierId
                        }
                    })
                }
                //保存上下限 {supplierId}/rawMaterial/{rawMaterialId}
                , saveWarning : function(rawMaterialId, alertLower, alertUpper){
                    return $.http.patch({
                        url : 'basis/supplier/'+ supplierId +'/rawMaterial/' + rawMaterialId,
                        data : {
                            alertLower : alertLower,
                            alertUpper : alertUpper
                        }
                    })
                }

            }
        },
        model : function (supplierId) {
            return {
                //供应商-模具树[未关联]
                unModelTree : function () {
                    return $.http.get({
                        url : 'basis/supplier/mould/nodes/unassociated',
                        data : {
                            supplierId : supplierId
                        }
                    })
                }
                //保存关联
                , saveMould : function (mouldIds) {
                    return $.http.post({
                        url : 'basis/supplier/' + supplierId + '/mould',
                        data : mouldIds,
                        contentType : $.contentType.json
                    })
                }
                //删除关联//@DeleteMapping("{supplierId}/mould/{mouldId}")
                , delete : function (mouldId) {
                    return $.http.delete('basis/supplier/'+ supplierId+'/mould/' + mouldId);
                }
                //供应商-原材料树[已关联]
                , modelTree : function () {
                    return $.http.get({
                        url : 'basis/supplier/mould/nodes/associated',
                        data : {
                            supplierId : supplierId
                        }
                    })
                }
                //保存上下限 {supplierId}/mould/{mouldId}
                , saveWarning : function(mouldId, alertLower, alertUpper){
                    return $.http.patch({
                        url : 'basis/supplier/'+ supplierId +'/mould/' + mouldId,
                        data : {
                            alertLower : alertLower,
                            alertUpper : alertUpper
                        }
                    })
                }

            }
        }
    };

    /**
     * 销售计划
     * @type {{}}
     */
    var salePlan = {
        //新增
        create : function (data) {
            return $.http.post({
                url : 'sale/plan',
                data : data,
                contentType : $.contentType.json
            })
        },
        //新增
        update : function (data) {
            var plan = {};
            return $.http.put({
                url : 'sale/plan',
                data : data,
                contentType : $.contentType.json
            }).then(function (ret) {
                plan = ret;
                return $.http.get('sale/plan/' + ret.id + '/history/opt');
            }).then(function (opts) {
                //操作记录
                plan.opts = opts.list;
                return plan;
            });
        },

        delete : function (planId) {
            return $.http.delete('sale/plan/' + planId);
        },

        //查询
        query : function (params) {
            return $.http.get({
                url : 'sale/plan',
                data : params
            });
        },

        //单个获取
        one : function (planId) {
            return $.http.get('sale/plan/' + planId);
        },

        //计划单的操作记录
        optHistory : function (planId) {
            return $.http.get('sale/plan/' + planId + '/history/opt');
        }
    };

    return {
         account: account
        , menus : menus
        , department : department
        , supplier : supplier
        , mould : mould
        , product : product
        , equipment:equipment
        , rawMaterial:rawMaterial
        , customer:customer
        , lookup : lookup
        , association : association
        , salePlan : salePlan
    }
});
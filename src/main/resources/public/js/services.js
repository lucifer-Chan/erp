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
            return $.http.post('logout').then(function () {
                $.session();
            });
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
         * 保存用户-小程序权限
         * @param id
         * @param list
         * @returns {*}
         */
        saveMiniRoles : function (id, list) {
            return $.http.post({
                url : 'basis/employee/' + id + '/miniRoles',
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
        },

        all : function () {
            return $.http.get('basis/employee/all');
        },

        /**
         * 用户创建的订单
         * @param employeeId
         * @returns {*}
         */
        orders : function (employeeId) {
            return $.http.get('basis/employee/' + employeeId +'/orders')
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
        sessionKey : '_services_supplier_all_',

        //全部
        all : function () {
            var $this = this;
            var value = $.session($this.sessionKey);
            if (!!value){
                return $.Promise.resolve().then(function () {
                    return value;
                })
            }
            return $.http.get('basis/supplier/all').then(function (ret) {
                $.session($this.sessionKey, ret);
                return ret;
            });
        },
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
            var $this = this;
            return $.http.post({
                url : 'basis/supplier',
                data : data,
                contentType : $.contentType.json
            }).then(function (value) {
                $.session($this.sessionKey, null);
                return value;
            });
        },
        //更新
        update : function (data) {
            var $this = this;
            return $.http.put({
                url : 'basis/supplier',
                data : data,
                contentType : $.contentType.json
            }).then(function (value) {
                $.session($this.sessionKey, null);
                return value;
            });
        },
        //删除
        delete : function (id) {
            var $this = this;
            return $.http.delete('basis/supplier/' + id).then(function (value) {
                $.session($this.sessionKey, null);
                return value;
            });
        }
    };

    var mould = {
        sessionKey : '_services_mould_all_',
        sessionKey2 : '_services_mould_lookup_',

        //库存剩余： total
        stockRemain : function (id) {
            return $.http.get('basis/mould/' + id + '/stockRemain');
        },

        //所有原材料
        all : function () {
            var $this = this;
            var value = $.session($this.sessionKey);
            if (!!value){
                return $.Promise.resolve().then(function () {
                    return value;
                })
            }
            return $.http.get('basis/mould/all')
                .then(function (ret) {
                    $.session($this.sessionKey, ret);
                    return ret;
                });
        },

        //所有原材料 code : name
        lookup : function () {
            var $this = this;
            var value = $.session($this.sessionKey2);
            if (!!value){
                return $.Promise.resolve().then(function () {
                    return value;
                })
            }
            return $.http.get('basis/mould/lookup')
                .then(function (ret) {
                    $.session($this.sessionKey2, ret);
                    return ret;
                });
        },

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
            var $this = this;
            return $.http.put({
                url : 'basis/mould',
                data : data,
                contentType : $.contentType.json
            }).then(function (value) {
                $.session($this.sessionKey, null);
                $.session($this.sessionKey2, null);
                return value;
            });
        },
        //查找
        one : function (id) {
            return $.http.get('basis/mould/' + id);
        },
        //新增模具
        create:function(data){
            var $this = this;
            return $.http.post({
                url : 'basis/mould',
                data : data,
                contentType : $.contentType.json
            }).then(function (value) {
                $.session($this.sessionKey, null);
                $.session($this.sessionKey2, null);
                return value;
            });
        },
        //删除
        delete : function (id) {
            var $this = this;
            return $.http.delete('basis/mould/' + id)
                .then(function (value) {
                    $.session($this.sessionKey, null);
                    $.session($this.sessionKey2, null);
                    return value;
                });
        },

        //批量删除
        batchDelete : function (importedAt) {
            var $this = this;
            return $.http.delete('basis/mould/batch/' + importedAt)
                .then(function (value) {
                    $.session($this.sessionKey, null);
                    $.session($this.sessionKey2, null);
                    return value;
                });
        },

        //导入的查询
        imported : function () {
            var $this = this;
            return $.http.get('basis/mould/group')
                .then(function (value) {
                    $.session($this.sessionKey, null);
                    $.session($this.sessionKey2, null);
                    return value;
                });
        },
    };

    var product = {
        sessionKey : '_services_product_all_',

        //找到成品里之前有过的数据，然后把客户代码（新）以及客户名称自动填充上
        cust : function (oldCode) {
            return $.http.get('basis/product/cust?oldCode=' + oldCode);
        },

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
            var $this = this;
            return $.http.put({
                url : 'basis/product',
                data : data,
                contentType : $.contentType.json
            }).then(function (value) {
                $.session($this.sessionKey, null);
                return value;
            });
        },
        //查找
        one : function (id) {
            return $.http.get('basis/product/' + id);
        },
        //查找库存记录
        stockHistory : function (id) {
            return $.http.get('basis/product/' + id + '/stock');
        },

        //库存剩余： {safe,total}
        stockRemain : function (id) {
            return $.http.get('basis/product/' + id + '/stockRemain');
        },

        //新增成品
        create:function(data){
            var $this = this;
            return $.http.post({
                url : 'basis/product',
                data : data,
                contentType : $.contentType.json
            }).then(function (value) {
                $.session($this.sessionKey, null);
                return value;
            });
        },
        //删除
        delete : function (id) {
            var $this = this;
            return $.http.delete('basis/product/' + id)
                .then(function (value) {
                    $.session($this.sessionKey, null);
                    return value;
                });
        },
        //导入的查询
        imported : function () {
            var $this = this;
            return $.http.get('basis/product/group')
                .then(function (value) {
                    $.session($this.sessionKey, null);
                    return value;
                });
        },
        //批量删除
        batchDelete : function (importedAt) {
            var $this = this;
            return $.http.delete('basis/product/batch/' + importedAt)
                .then(function (value) {
                    $.session($this.sessionKey, null);
                    return value;
                });
        },
        //所有成品
        all : function () {
            var $this = this;
            var value = $.session($this.sessionKey);
            if (!!value){
                return $.Promise.resolve().then(function () {
                    return value;
                })
            }
            return $.http.get('basis/product/all')
                .then(function (ret) {
                    $.session($this.sessionKey, ret);
                    return ret;
                });
        },
        //所有成品 - 有bom关联的
        allWithBom : function () {
            return $.http.get('basis/product/allWithBom');
        },

        //保存上下限 {supplierId}/product/{productId}
        saveWarning : function(id, alertLower, alertUpper){
            var $this = this;
            return $.http.patch({
                url : 'basis/product/'+ id,
                data : {
                    alertLower : alertLower,
                    alertUpper : alertUpper
                }
            }).then(function (value) {
                $.session($this.sessionKey, null);
                return value;
            });
        },
        //物料清单相关
        bom : {
            //列表
            list : function (productId) {
                return $.http.get('basis/product/' + productId + '/bom');
            },
            //单个
            one : function (id) {
                return $.http.get('basis/product/bom/' + id);
            },
            //供选的原材料
            materials : function (productId) {
                return $.http.get('basis/product/' + productId + '/materials');
            },

            /**
             * 创建
             * @param data - productId, materialId, materialNum
             */
            create : function (data) {
                return $.http.post({
                    url : 'basis/product/bom',
                    data : data,
                    contentType : $.contentType.json
                });
            },
            /**
             * 更新
             * @param data id, materialNum
             */
            update : function (data) {
                return $.http.patch({
                    url : 'basis/product/bom/' + data.id,
                    data : {materialNum : data.materialNum}
                })
            },
            //删除
            delete : function (id) {
                return $.http.delete('basis/product/bom/' + id);
            }
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
        sessionKey : '_services_material_all_',
        //所有原材料
        all : function () {
            var $this = this;
            var value = $.session($this.sessionKey);
            if (!!value){
                return $.Promise.resolve().then(function () {
                    return value;
                })
            }
            return $.http.get('basis/rawMaterial/all')
                .then(function (ret) {
                    $.session($this.sessionKey, ret);
                    return ret;
                });
        },

        //库存剩余： total
        stockRemain : function (id) {
            return $.http.get('basis/rawMaterial/' + id + '/stockRemain');
        },

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
            var $this = this;
            return $.http.put({
                url : 'basis/rawMaterial',
                data : data,
                contentType : $.contentType.json
            }).then(function (value) {
                $.session($this.sessionKey, null);
                return value;
            });
        },
        //查找
        one : function (id) {
            return $.http.get('basis/rawMaterial/' + id);
        },
        //新增原材料
        create:function(data){
            var $this = this;
            return $.http.post({
                url : 'basis/rawMaterial',
                data : data,
                contentType : $.contentType.json
            }).then(function (value) {
                $.session($this.sessionKey, null);
                return value;
            });
        },
        //删除
        delete : function (id) {
            var $this = this;
            return $.http.delete('basis/rawMaterial/' + id)
                .then(function (value) {
                    $.session($this.sessionKey, null);
                    return value;
                });
        },
        //导入的查询
        imported : function () {
            var $this = this;
            return $.http.get('basis/rawMaterial/group')
                .then(function (value) {
                    $.session($this.sessionKey, null);
                    return value;
                });
        },
        //批量删除
        batchDelete : function (importedAt) {
            var $this = this;
            return $.http.delete('basis/rawMaterial/batch/' + importedAt)
                .then(function (value) {
                    $.session($this.sessionKey, null);
                    return value;
                });
        }
    };

    var customer = {
        sessionKey : '_services_customer_all_',
        //全部
        all : function () {
            var $this = this;
            var value = $.session($this.sessionKey);
            if (!!value){
                return $.Promise.resolve().then(function () {
                    return value;
                })
            }

            return $.http.get('basis/customer/all').then(function (ret) {
                $.session($this.sessionKey, ret);
                return ret;
            });
        },

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
            var $this = this;
            return $.http.post({
                url : 'basis/customer',
                data : data,
                contentType : $.contentType.json
            }).then(function (value) {
                $.session($this.sessionKey, null);
                return value;
            });
        },
        //更新
        update : function (data) {
            var $this = this;
            return $.http.put({
                url : 'basis/customer',
                data : data,
                contentType : $.contentType.json
            }).then(function (value) {
                $.session($this.sessionKey, null);
                return value;
            });
        },
        //删除
        delete : function (id) {
            var $this = this;
            return $.http.delete('basis/customer/' + id).then(function (value) {
                $.session($this.sessionKey, null);
                return value;
            });
        },
        //查找销售订单
        orders : function (id) {
            return $.http.get('basis/customer/' + id + '/orders');
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
                //保存关联  @PostMapping("{materialId}/supplier/{supplierId}")
                , saveAss : function (materialId, low, up) {
                    return $.http.post({
                        url : 'basis/rawMaterial/' + materialId + '/supplier/' + supplierId,
                        data : { low : low,  up : up }
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
        },
        //根据原材料-找已关联的供应商
        materialSupplier : function (materialId) {
            return $.http.get('basis/rawMaterial/'+ materialId+'/supplier');
        },
        //找原材料未关联的供应商
        unassociatedSuppliers : function (materialId) {
            return $.http.get('basis/rawMaterial/'+ materialId+'/supplier/unassociated');
        },
        //根据成品-找已关联的供应商
        productSupplier : function (productId) {
            return $.http.get('basis/product/' + productId + '/supplier');
        },
        //找成品未关联的供应商
        unProductSupplier : function (productId) {
            return $.http.get('basis/product/'+ productId+'/supplier/unassociated');
        },
        //根据模具-找已关联的供应商
        mouldSupplier : function (mouldId) {
            return $.http.get('basis/mould/' + mouldId + '/supplier');
        },
        //找模具未关联的供应商
        unMouldSupplier : function (mouldId) {
            return $.http.get('basis/mould/'+ mouldId+'/supplier/unassociated');
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
        //更新
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
        //删除
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

    /**
     * 采购计划
     * @type {{}}
     */
    var purchasePlan = {
        //新增
        create : function (data) {
            return $.http.post({
                url : 'purchase/plan',
                data : data,
                contentType : $.contentType.json
            })
        },
        //更新
        update : function (data) {
            var plan = {};
            return $.http.put({
                url : 'purchase/plan',
                data : data,
                contentType : $.contentType.json
            }).then(function (ret) {
                plan = ret;
                return $.http.get('purchase/plan/' + ret.id + '/history/opt');
            }).then(function (opts) {
                //操作记录
                plan.opts = opts.list;
                return plan;
            });
        },
        //删除
        delete : function (planId) {
            return $.http.delete('purchase/plan/' + planId);
        },
        //查询
        query : function (params) {
            return $.http.get({
                url : 'purchase/plan',
                data : params
            });
        },
        //单个获取
        one : function (planId) {
            return $.http.get('purchase/plan/' + planId);
        },
        //计划单的操作记录
        optHistory : function (planId) {
            return $.http.get('purchase/plan/' + planId + '/history/opt');
        }
    };

    /**
     * 生产计划
     * @type {{}}
     */
    var prodPlan = {
        //新增
        create : function (data) {
            return $.http.post({
                url : 'prod/plan',
                data : data,
                contentType : $.contentType.json
            })
        },
        //更新
        update : function (data) {
            var plan = {};
            return $.http.put({
                url : 'prod/plan',
                data : data,
                contentType : $.contentType.json
            }).then(function (ret) {
                plan = ret;
                return $.http.get('prod/plan/' + ret.id + '/history/opt');
            }).then(function (opts) {
                //操作记录
                plan.opts = opts.list;
                return plan;
            });
        },
        //删除
        delete : function (planId) {
            return $.http.delete('prod/plan/' + planId);
        },
        //查询
        query : function (params) {
            return $.http.get({
                url : 'prod/plan',
                data : params
            });
        },
        //单个获取
        one : function (planId) {
            return $.http.get('prod/plan/' + planId);
        },
        //计划单的操作记录
        optHistory : function (planId) {
            return $.http.get('prod/plan/' + planId + '/history/opt');
        },
        //获取物料清单
        boms : function (planId, productId) {
            return $.http.get('prod/plan/' + planId + '/' + productId +'/boms')
        },

        /**
         * 保存模具[新增|更新]
         * @param planId
         * @param data {id, mouldId, supplierId[可选], realityMouldNum}
         * @returns {*}
         */
        saveMould : function (planId, data) {
            return $.http.post({
                url : 'prod/plan/' + planId + '/mould',
                data : data,
                contentType : $.contentType.json
            });
        },

        deleteMould : function (id) {
            return $.http.delete('prod/plan/mould/' + id);
        }
    };

    /**
     * 销售订单
     * @type {{}}
     */
    var saleOrder = {
        //新增
        create : function (data) {
            return $.http.post({
                url : 'sale/order',
                data :data,
                contentType : $.contentType.json
            });
        },
        //更新
        update : function (data) {
            var order = {};
            return $.http.put({
                url : 'sale/order',
                data :data,
                contentType : $.contentType.json
            }).then(function (ret) {
                order = ret;
                return $.http.get('sale/order/'+ ret.id +'/history/opt');
            }).then(function (opts) {
                //操作记录
                order.opts = opts.list;
                return $.http.get('sale/order/' + order.id + '/items');
            }).then(function (items) {
                order.items = items.list;
                return order;
            });
        },
        //打印回调
        afterPrint : function (orderId) {
            return $.http.patch('sale/order/' + orderId);
        },
        //更新状态
        updateStatus : function (orderId, status, remark) {
            return $.http.patch({
                url : 'sale/order/' + orderId + '/' + status,
                data : {
                    remark : remark
                }
            });
        },
        //单个获取
        one : function (orderId) {
            return $.http.get('sale/order/' + orderId);
        },
        //组合查询
        query : function (params) {
            return $.http.get({
                url : 'sale/order',
                data : params
            });
        },
        //删除
        delete : function (orderId) {
            return $.http.delete('sale/order/' + orderId);
        },
        //添加明细
        addItem : function (item) {
            return $.http.post({
                url : 'sale/orderItem',
                data : item,
                contentType : $.contentType.json
            });
        },
        //修改明细
        updateItem : function (item) {
            return $.http.put({
                url : 'sale/orderItem',
                data : item,
                contentType : $.contentType.json
            });
        },
        //删除明细
        deleteItem : function (orderId, itemId) {
            return $.http.delete('sale/orderItem/'+ orderId +'/' + itemId);
        }
    };

    /**
     * 采购订单
     * @type {{}}
     */
    var purchaseOrder = {
        //新增
        create : function (data) {
            return $.http.post({
                url : 'purchase/order',
                data :data,
                contentType : $.contentType.json
            });
        },
        //更新
        update : function (data) {
            var order = {};
            return $.http.put({
                url : 'purchase/order',
                data :data,
                contentType : $.contentType.json
            }).then(function (ret) {
                order = ret;
                return $.http.get('purchase/order/'+ ret.id +'/history/opt');
            }).then(function (opts) {
                //操作记录
                order.opts = opts.list;
                return $.http.get('purchase/order/' + order.id + '/items');
            }).then(function (items) {
                order.items = items.list;
                return order;
            });
        },
        //打印回调
        afterPrint : function (orderId) {
            return $.http.patch('purchase/order/' + orderId);
        },
        //更新状态
        updateStatus : function (orderId, status, remark) {
            return $.http.patch({
                url : 'purchase/order/' + orderId + '/' + status,
                data : {
                    remark : remark
                }
            });
        },
        //单个获取
        one : function (orderId) {
            return $.http.get('purchase/order/' + orderId);
        },
        //组合查询
        query : function (params) {
            return $.http.get({
                url : 'purchase/order',
                data : params
            });
        },
        //删除
        delete : function (orderId) {
            return $.http.delete('purchase/order/' + orderId);
        },
        //添加明细
        addItem : function (item) {
            return $.http.post({
                url : 'purchase/orderItem',
                data : item,
                contentType : $.contentType.json
            });
        },
        //修改明细
        updateItem : function (item) {
            return $.http.put({
                url : 'purchase/orderItem',
                data : item,
                contentType : $.contentType.json
            });
        },
        //删除明细
        deleteItem : function (orderId, itemId) {
            return $.http.delete('purchase/orderItem/'+ orderId +'/' + itemId);
        },

        //货物的下拉列表
        lookup : function (supplierId, type) {
            return $.http.get('purchase/order/lookup/'+ supplierId + '/' + type);
        },
        //退货
        refunds : function (order) {
            return $.http.put({
                url : 'purchase/order/refunds',
                data :order,
                contentType : $.contentType.json
            });
        }
    };

    /**
     * 生产制令单
     * @type {{}}
     */
    var prodOrder = {
        //新增
        create : function (data) {
            return $.http.post({
                url : 'prod/order',
                data : data,
                contentType : $.contentType.json
            });
        },
        //修改
        update : function (data) {
            return $.http.put({
                url : 'prod/order',
                data : data,
                contentType : $.contentType.json
            })
        },
        //删除
        delete : function (orderId) {
            return $.http.delete('prod/order/' + orderId);
        },
        //find one
        one : function (orderId) {
            return $.http.get('prod/order/' + orderId);
        },
        //操作记录
        opt : function (orderId) {
            return $.http.get('prod/order/' + orderId + '/history/opt');
        },
        //打印出库单之后
        afterPrintOut : function (orderId) {
            return $.http.patch('prod/order/' + orderId + '/preOut');
        },
        //打印入库单之后
        afterPrintIn : function (orderId) {
            return $.http.patch('prod/order/' + orderId + '/preIn');
        },
        //挑拣-新增|修改
        pick : function (record) {
            return $.http.post({
                url : 'prod/order/pick',
                data : record,
                contentType : $.contentType.json
            })
        },
        //组合查询
        query : function (params) {
            return $.http.get({
                url : 'prod/order',
                data : params
            });
        }
    };

    /**
     * 仓库管理
     * @type {{place: {}}}
     */
    var stock = {
        //1-仓位
        place : {
            //新增仓位
            create : function (data) {
                return $.http.post({
                    url : 'stock/place',
                    data : data,
                    contentType : $.contentType.json
                })
            },

            /**
             * 修改仓位
             * @param data {id, upperLimit, description}
             * @returns {*}
             */
            update : function (data) {
                return $.http.put({
                    url : 'stock/place',
                    data : data,
                    contentType : $.contentType.json
                })
            },
            //查询
            query : function (params) {
                return $.http.get({
                    url : 'stock/place',
                    data : params
                });
            },
            //删除
            delete : function (id) {
                return $.http.delete('stock/place/' + id);
            },
            //获取单个
            one : function (id) {
                return $.http.get('stock/place/' + id);
            },
            //停役
            stop : function (id) {
                return $.http.patch('stock/place/stop/' + id);
            },
            //出入库记录和现存
            ext : function (id) {
                return $.http.get('stock/place/ext/' + id);
            }
        }
    };

    //常用
    var common = {
        ass : {
            //供应商和原材料的所有关联
            supplierMaterial : function () {
                return $.http.get('basis/common/ass/supplier/material')
            },
            //获取供应商货物条形码文本
            barcode : function (waresType, waresAssId) {
                return $.http.get('basis/common/barcode/'+ waresType + '/' + waresAssId);
            }
        },
        /**
         *
         * @param barcode
         * @returns {*} base64 直接放到img的src里
         */
        barcode : function (barcode) {
            return $.http.get('basis/common/barcode/'+ barcode);
        },

        /**
         * 小程序的权限列表
         * @returns {*}
         */
        miniRoles : function () {
            return $.http.get('basis/common/mini/roles');
        }
    };

    return {
         account : account
        , menus : menus
        , department : department
        , supplier : supplier
        , mould : mould
        , product : product
        , equipment : equipment
        , rawMaterial : rawMaterial
        , customer : customer
        , lookup : lookup
        , association : association
        , salePlan : salePlan
        , saleOrder : saleOrder
        , purchasePlan : purchasePlan
        , purchaseOrder : purchaseOrder
        , prodPlan : prodPlan
        , prodOrder : prodOrder
        , stock : stock
        , common : common
    }
});
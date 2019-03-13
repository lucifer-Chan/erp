define('order',['ztree','utils','services'],function(ztree, utils, services){

    var consts = {};

    //预置数据
    function prepareData() {
        return $.Promise.resolve()
            .then(function () {
                if(consts.config.initProducts && !consts.products.length){
                    return services.product.all().then(function (ret) {
                        consts.products = [];
                        consts.productCache = {};
                        consts.products.push({code : -1, name : '【请选择成品】'});
                        ret.list.forEach(function (value) {
                            consts.products.push({code : value.id , name : value.description});
                            consts.productCache[value.id] = value.onlyOrKg;
                        });

                    });
                }
                return [];
            })
            .then(function () {
                if(consts.config.initCustomers && !consts.customers.length){
                    return services.customer.all().then(function (ret) {
                        consts.customers = [];
                        consts.customers.push({code : -1, name : '【请选择客户】'});
                        ret.list.forEach(function (value) {
                            consts.customers.push({code : value.id , name : value.customerName});
                        })
                    });
                }
                return [];
            })
            .catch(function (reason) {
                console.log('reason', reason);
            });
    }

    /**
     * 加载列表
     */
    function loadDataTable(oParams) {
        //缓存查询条件
        if (oParams === true){
            oParams = consts.oQueryParam;
        } else {
            consts.oQueryParam = oParams ||{};
        }

        /**
         * 加载数据的实际方法
         * @param params
         */
        function dataProvider(params){
            return services.saleOrder.query(params);
        }

        /**
         * 选中行事件
         * @param $tr 选中的行事件
         */
        function fClickTr($tr){
            var $rightPage = consts.$rightPage;
            var id = $tr.attr('id');
            if (id === $rightPage.attr('data-id')) return;
            $rightPage.attr('data-id', id);
            services.saleOrder.one(id).then(function (tr) {
                console.log('点击tr', tr);
                initTrDetail(tr);
                $rightPage.show().attr('class', 'rightInfoPage animated fadeInRightBig');
                $rightPage.find('.fa-angle-double-down').click();
            }).catch(function (reason) {
                layer.msg(reason.caught ? reason.message : '请求失败！');
            });
        }

        function convertMoney(array){
            return '¥' + (array.length ? array[0] : '0.00');
        }

        function convertDate(array) {
            return (array.length ? array[0] : '').substr(0,10);
        }

        function convertStatus(array) {
            return _convertStatus(array[0]);
        }

        utils.dataTable({
            holder: '#' + consts.config.dataTable.id
            , $dataProvider : dataProvider
            , oParams : $.extend({}, oParams, { codes : (consts.config.dataTable.codes || []).join(',')} )
            , fClickTr : fClickTr
            , aConverts : [convertMoney, convertDate, convertStatus]
        });
    }

    function _convertStatus(code) {
        var css = 'label-default';
        if ('STATUS_002' === code || 'STATUS_049' === code) css = 'label-warning';
        if ('STATUS_003' === code || 'STATUS_005' === code) css = 'label-success';
        if ('STATUS_004' === code || 'STATUS_006' === code || 'STATUS_061' === code || 'STATUS_008' === code) css = 'label-danger';
        if ('STATUS_007' === code) css = 'label-primary';
        return '<span class="label '+ css +'">'+ consts.status[code] +'</span>';
    }

    //搜索的状态下拉
    function init_search_dropdown() {
        if (!consts.$statusDropDown || !consts.$statusDropDown.length) return;

        var dropdownData = [];
        (consts.config.dataTable.codes || []).forEach(function (code) {
            dropdownData.push({ code : code, name : consts.status[code]});
        });
        utils.dropdown({
            $holder : consts.$statusDropDown,
            data : dropdownData,
            prefix : '状态',
            callback : function (value) {
                consts.oQueryParam.code = value;
                console.log('oQueryParam', consts.oQueryParam);
                loadDataTable(true);
            }
        });
    }

    //按钮事件
    function init_buttons_event() {
        //新增订单按钮
        if(consts.$addSaleOrderBt && consts.$addSaleOrderBt.length){
            consts.$addSaleOrderBt.bind('click', function(){
                consts.currentOrder = {};
            });
        }
        //删除订单按钮
        if(consts.$deleteOrderBt && consts.$deleteOrderBt.length){
            consts.$deleteOrderBt.unbind('click').click(function () {
                layer.confirm('删除操作不可恢复，确定删除吗？',{title:'提示',move:false},function(){
                    services.saleOrder.delete(consts.currentOrder.id)
                        .then(function (ret) {
                            layer.msg(ret._message);
                            loadDataTable(true);
                            consts.currentOrder = {};
                            consts.$rightPage.attr('data-id', '').hide();
                        })
                        .catch(function (reason) {
                            layer.msg(reason.caught ? reason.message : '请求失败！');
                        });
                });
            });
        }
        //订单保存按钮事件
        if(consts.$saveOrderBt && consts.$saveOrderBt.length){
            consts.$saveOrderBt.unbind('click').click(function (e) {
                e.preventDefault();
                var data = consts.$addOrderModal.find('form').serializeObject();
                data.customerId = $('#_addCustomerId').attr('data-value');
                data.customerName = consts.$addOrderModal.find('.dk_label').html();
                if(data.customerId === '-1'){
                    layer.msg('请选择客户！');
                    return;
                }
                if(!data.orderDate ){
                    layer.msg('请选择订单时间！');
                    return;
                }
                data.id = consts.currentOrder.id || '';
                var _function = !!consts.currentOrder.id ? services.saleOrder.update : services.saleOrder.create;
                console.log('save order Data', data);
                _function(data).then(function (order) {
                    consts.$addOrderModal.modal('hide');
                    if (consts.currentOrder.id){
                        //更新的情况
                        loadDataTable(true);
                        initTrDetail(order);
                    } else if(consts.$editOrderItemModal && consts.$editOrderItemModal.length){
                        //新增的情况-跳转到明细编辑modal，刷新列表和右侧在明细modal隐藏时触发
                        consts.currentOrder = order;
                        consts.currentOrderItem = {};
                        consts.$editOrderItemModal.modal('show');
                    }
                    layer.msg('保存销售定单成功');
                }).catch(function (reason) {
                    console.log('保存销售定单', reason);
                    layer.msg(reason.caught ? reason.message : '请求失败！');
                });
            });
        }
        //新增明细按钮
        if(consts.$addOrderItemBt && consts.$addOrderItemBt.length){
            consts.$addOrderItemBt.bind('click', function () {
                consts.currentOrderItem = {};
            })
        }
        //保存订单明细按钮事件
        if (consts.$saveOrderItemBt && consts.$saveOrderItemBt.length){
            consts.$saveOrderItemBt.unbind('click').click(function (e) {
                e.preventDefault();
                var data = consts.$editOrderItemModal.find('form').serializeObject();
                data.productId = $('#_editProductId').attr('data-value');
                data.orderId = consts.currentOrder.id;
                data.orderCode = consts.currentOrder.barCode;
                data.statusCode = consts.currentOrder.statusCode;
                data.id = (consts.currentOrderItem || {}).id;
                data.unit = consts.currentOrderItem.unit;
                if(!data.unit){
                    layer.msg('请选择单位！');
                    return;
                }

                if(data.productId === '-1'){
                    layer.msg('请选择成品！');
                    return;
                }
                if(!data.money ){
                    layer.msg('请输入金额');
                    return;
                }
                if(!data.num ){
                    layer.msg('请输入数量');
                    return;
                }
                if(!data.unitPrice ){
                    layer.msg('请输入单价');
                    return;
                }
                var _function = !!consts.currentOrderItem.id ? services.saleOrder.updateItem : services.saleOrder.addItem;
                console.log('save orderItem Data', data);
                _function(data).then(function () {
                    if (!!consts.currentOrderItem.id){
                        consts.$editOrderItemModal.modal('hide');
                        layer.msg('更新明细成功');
                    }  else {
                        consts.$editOrderItemModal.find('input[type="text"], textarea, input[type="number"]').val('');
                        //供选成品
                        utils.dropkick({
                            $holder : $('#_editProductId'),
                            data : consts.products,
                            callback : init_select_product_tip
                        });
                        layer.msg('新增明细成功');
                    }
                }).catch(function (reason) {
                    layer.msg(reason.caught ? reason.message : '请求失败！');
                });
            });
        }

        //销售出／入库通知单
        if (consts.$printToOutBt && consts.$printToOutBt.length && consts.template.$printToStock){
            consts.$printToOutBt.bind('click', function(){
                var $printHolder = $('#printHolder').clone();
                var data = buildPrintData('STATUS_003', '销售出库通知单');
                services.common.barcode(data.barCode)
                    .then(function (ret) {
                        data.base64 = ret.base64;
                    })
                    .then(function () {
                        var html = template('sale_stock_print_tpl', data);
                        $printHolder.html(html).show().print({
                            deferred: $.Deferred().done(function() {
                                services.saleOrder.afterPrint(order.id);
                            })
                        });
                    })
                    .catch(function () {
                        layer.msg('生成条形码失败！');
                    })
            });
        }

        //销售退货通知单 暂定审核状态为003
        if (consts.$printToInBt && consts.$printToInBt.length && consts.template.$printToStock){
            consts.$printToInBt.bind('click', function(){
                var $printHolder = $('#printHolder').clone();
                var data = buildPrintData('STATUS_003', '销售退货通知单');
                services.common.barcode(data.barCode)
                    .then(function (ret) {
                        data.base64 = ret.base64;
                    })
                    .then(function () {
                        var html = template('sale_stock_print_tpl', data);
                        $printHolder.html(html).show().print();
                    })
                    .catch(function () {
                        layer.msg('生成条形码失败！');
                    })
            });
        }


        /**
         * 构建打印数据
         * @param approvalStatus 审核状态
         * @param title
         */
        function buildPrintData(approvalStatus, title) {
            var order = consts.currentOrder;
            var statusLogs = order.statusLogs || [];
            var approvalPass = '';
            for(var i = 0; i < statusLogs.length; i ++){
                if (approvalStatus === statusLogs[i].statusCode){
                    approvalPass = statusLogs[i];
                    break;
                }
            }

            var empty = [];
            var max = (order.items || []).length;
            for(var index = max+1; index <= 6; index ++){
                empty.push(index);
            }
            return {
                title: title,
                now: new Date().format("yyyy-MM-dd"),
                currentUser: $.local(GLOBALS.localKeys.accountInfo).name,
                barCode: order.barCode,
                createdName: order.createdName,
                approvalName : approvalPass.createdName,
                items: order.items,
                empty: empty
            }
        }




        //状态变更按钮事件
        $('#_toSubmitOrderBt').bind('click', function () {
            consts.toNewStatus = {
                code : 'STATUS_002',
                header : '提交审核',
                bt : '提交'
            };
        });
        $('#_toRefundsOrderBt').bind('click', function () {
            consts.toNewStatus = {
                code : 'STATUS_006',
                header : '客户退货',
                bt : '保存'
            };
        });
        $('#_toFinishOrderBt').bind('click', function () {
            consts.toNewStatus = {
                code : 'STATUS_007',
                header : '订单归档',
                bt : '保存'
            };
        });
        $('#_toInvalidOrderBt').bind('click', function () {
            consts.toNewStatus = {
                code : 'STATUS_008',
                header : '订单作废',
                bt : '保存'
            };
        });

        $('#_toApprovalPassBt').bind('click', function () {
            consts.toNewStatus = {
                code : 'STATUS_003',
                header : '审核通过',
                bt : '保存'
            };
        });
        $('#_toApprovalRefuseBt').bind('click', function () {
            consts.toNewStatus = {
                code : 'STATUS_004',
                header : '审核退回',
                bt : '保存'
            };
        });

    }

    //输入框事件
    function init_input_event() {
        function calc() {
            var unitPrice = $('#_editItemUnitPrice').val() || 0;
            var num = $('#_editItemNum').val() || 0;
            var ret = parseFloat(unitPrice) * parseFloat(num);
            $('#_editItemMoney').val(ret.toFixed(2));
            calc2Kg();
        }

        $('#_editItemNum, #_editItemUnitPrice').bind('input propertychange', function () {
            calc();
        });
    }

    function calc2Kg(productId) {
        var unit = consts.currentOrderItem.unit;
        if (!unit || unit === 'kg') {
            clearRemark();
            return;
        }
        var _productId = productId || $('#_editProductId').attr('data-value');
        var onlyOrKg = _productId === '-1' ? 0 : consts.productCache[_productId];
        var num = $('#_editItemNum').val();
        if(!!onlyOrKg && !!num){
            var kg = (num/ onlyOrKg).toFixed(2);
            $('#_editItemRemark').val('约' + kg + 'kg');
        } else {
            clearRemark();
        }

        function clearRemark() {
            $('#_editItemRemark').val('');
        }
    }

    //编辑明细modal
    function init_edit_item_modal() {
        if(!consts.$editOrderItemModal || !consts.$editOrderItemModal.length) return;
        consts.$editOrderItemModal.on('show.bs.modal', function () {
            var $this = $(this);
            // 初始数据
            $.each(consts.currentOrderItem, function (name, value) {
                $this.find('form [name="'+ name +'"]').val(value);
            });
            //供选成品
            utils.dropkick({
                $holder : $('#_editProductId'),
                data : consts.products,
                current : consts.currentOrderItem.productId,
                callback : init_select_product_tip
            });

            //初始化总量单位
            utils.dropdown({
                $holder : $('#units_li'),
                data : [{code : 'kg', name : 'kg'}, {code : '只', name : '只'}],
                unselected : '-选择单位-',
                current : consts.currentOrderItem || '',
                callback : function (value) {
                    console.log("单位切换", value);
                    consts.currentOrderItem.unit = value;
                    calc2Kg();
                }
            });

            consts.$saveOrderItemBt.text(!!consts.currentOrderItem.id ? '保存' : '继续添加');

        }).on('hidden.bs.modal', function () {
            //清理数据
            $(this).find('input[type="text"], textarea, input[type="number"]').val('');
            //刷新列表
            loadDataTable(true);
            //右侧-有consts.currentOrder.id说明是右侧进来
            if(consts.currentOrder.id){
                services.saleOrder.one(consts.currentOrder.id).then(function (tr) {
                    initTrDetail(tr);
                }).catch(function (reason) {
                    layer.msg(reason.caught ? reason.message : '请求失败！');
                });
            }
        })

    }

    //新增订单modal
    function init_add_sale_modal() {
        console.log('init_add_sale_modal');
        if(!consts.$addOrderModal || !consts.$addOrderModal.length) return;

        //数据初始化
        consts.$addOrderModal.on('shown.bs.modal', function () {
            console.log('consts.$addOrderModal is shown');
            var $this = $(this);
            // 初始数据
            $.each(consts.currentOrder, function (name, value) {
                if (name === 'orderDate'){
                    value = (value ||'').substr(0,10);
                }
                $this.find('form [name="'+ name +'"]').val(value);
            });
            // 时间控件
            $('#_addOrderDate').datepicker(utils.datepickerConfig);
            // 供选客户
            utils.dropkick({
                $holder : $('#_addCustomerId'),
                data : consts.customers,
                current : consts.currentOrder.customerId
            })
        }).on('hidden.bs.modal', function () {
            //清理数据
            $(this).find('input[type="text"], textarea').val('');
        });
    }

    //状态变更modal
    function init_change_status_modal() {
        if(!consts.$changeOrderStatusModal || !consts.$changeOrderStatusModal.length) return;
        var $modal = consts.$changeOrderStatusModal;
        $modal.on('show.bs.modal', function () {
            $('#changeStatusRemark').val('');
            if(consts.toNewStatus && consts.toNewStatus.code){
                $modal.find('.modal-title span').text(consts.toNewStatus.header);
                $('#_changeOrderStatusBt').text(consts.toNewStatus.bt);
            }
        }).on('hidden.bs.modal', function () {
            consts.toNewStatus = {};
        });

        consts.$changeOrderStatusBt.unbind('click').bind('click', function (e) {
            e.preventDefault();
            services.saleOrder.updateStatus(consts.currentOrder.id, consts.toNewStatus.code,  $('#changeStatusRemark').val())
                .then(function () {
                    loadDataTable(true);
                    layer.msg('提交成功！');
                    consts.currentOrder = {};
                    consts.$rightPage.attr('data-id', '').hide();
                    consts.$changeOrderStatusModal.modal('hide');
                })
                .catch(function (reason) {
                    layer.msg(reason.caught ? reason.message : '请求失败！');
                })
        });
    }

    /**
     * 初始化右侧
     */
    function initTrDetail(tr) {
        if (!tr) return;
        console.log('点击列表', tr);
        consts.currentOrder = tr;
        var $detail = $('.initSaleOrderDetail');
        var $baseInfo = $detail.find('div[data-value="baseInfo"]');
        //初始化页面数据-基本信息
        $baseInfo.find('div[data-name]').each(function () {
            var value = tr[$(this).attr('data-name')] || '';
            var convert = $(this).attr('data-convert');
            if(convert === 'convertDate'){
                value = value.substr(0,10);
            } else if(convert === 'convertMoney'){
                value = '¥' + (value || 0).toFixed(2);
            } else if(convert === 'convertStatus'){
                value = _convertStatus(value)
            } else if(convert === 'false'){
                value = value || '无';
            }
            $(this).html(value);
        });
        //历史记录
        var $statusHolder = consts.$statusHistory.find('.bugInfoHistories');
        var $orderHolder = consts.$orderHistory.find('.bugInfoHistories');
        var $itemHolder = consts.$itemHistory.find('.bugInfoHistories');

        if(!tr.opts || !tr.opts.length){
            $statusHolder.html('<p>暂无</p>');
            $orderHolder.html('<p>暂无</p>');
            $itemHolder.html('<p>暂无</p>');
        } else {
            function initHistory($holder, logs) {
                if (!logs.length){
                    $holder.html('<p>暂无</p>');
                } else {
                    var html = '';
                    logs.forEach(function (log) {
                        html += consts.template.optLog.replace('{name}', log.createdName)
                            .replace('{content}', log.content)
                            .replace('{time}', log.createdAt);
                    });
                    $holder.html(html);
                }
            }
            var statusLogs = [], orderLogs = [], itemLogs = [];
            tr.opts.forEach(function (opt) {
                if(opt.optType === 'status'){
                    statusLogs.push(opt);
                } else if(opt.optType === 'order'){
                    orderLogs.push(opt);
                } else if(opt.optType === 'item'){
                    itemLogs.push(opt);
                }
            });
            initHistory($statusHolder, statusLogs);
            initHistory($orderHolder, orderLogs);
            initHistory($itemHolder, itemLogs);
            tr.statusLogs = statusLogs;
        }
        //订单明细
        var $itemTemplate = consts.template.$orderItem;
        var $orderItems = consts.$items.empty();
        if(!tr.items || !tr.items.length){
            $orderItems.html('<p>暂无</p>');
        } else {
            $.each(tr.items, function (i, item) {
                if(typeof consts.itemCache === 'object'){
                    consts.itemCache[item.id] = item;
                }
                var clone = $itemTemplate.clone();
                $(clone).find('.panel-title').text(item.productName);
                $(clone).find('.tools').find('.fa-angle-double-up, .fa-angle-double-down, ._print, ._update, ._delete').attr('data-value', item.id);
                var body = $(clone).find('.panel-body');
                body.attr('data-value', (item.id + 'Info'));
                if(item.statusCode !== 'STATUS_049' && item.statusCode !== 'STATUS_005'){
                    $(body).find('div[data-name="status"]').parent().hide();
                }
                $(body).find('div[data-name="status"]').html(_convertStatus(item.statusCode));
                $(body).find('div[data-name="money"]').text('¥' + (item.money || 0).toFixed(2));
                $(body).find('div[data-name="unitPrice"]').text('¥' + (item.unitPrice||0).toFixed(2));
                $(body).find('div[data-name="num"]').text(item.num + item.unit);
                $(body).find('div[data-name="remark"]').text(item.remark || '无');
                $orderItems.append($(clone).show());
            })
        }
        utils.initRightAngleEvent();
        //未发布|审核退回 可操作: 修改订单、新增明细、提交审核、删除、更新明细、删除明细
        if(tr.statusCode === 'STATUS_001' || tr.statusCode === 'STATUS_004'){
            $('#rightInfoPage').find('#_addSaleOrderBt, #_addSaleOrderItemBt, #_toSubmitOrderBt, ._update, ._delete').show();
        } else {
            $('#rightInfoPage').find('#_addSaleOrderBt, #_addSaleOrderItemBt, #_toSubmitOrderBt, ._update, ._delete').hide();
        }
        //待审核 可操作：审核通过、审核退回、删除
        if(tr.statusCode === 'STATUS_002'){
            $('#rightInfoPage').find('#_toApprovalPassBt, #_toApprovalRefuseBt').show();
        } else {
            $('#rightInfoPage').find('#_toApprovalPassBt, #_toApprovalRefuseBt').hide();
        }
        //审核通过 可操作：打印出库单、作废
        if(tr.statusCode === 'STATUS_003'){
            $('#rightInfoPage').find('#_printToOutBt, #_toInvalidOrderBt').show();
        } else {
            $('#rightInfoPage').find('#_printToOutBt, #_toInvalidOrderBt').hide();
        }
        //已出库 可操作: 客户退回、完成
        if(tr.statusCode === 'STATUS_005'){
            $('#rightInfoPage').find('#_toRefundsOrderBt, #_toFinishOrderBt').show();
        } else {
            $('#rightInfoPage').find('#_toRefundsOrderBt, #_toFinishOrderBt').hide();
        }
        //客户退货 可操作: 打印退货单
        if(tr.statusCode === 'STATUS_006'){
            $('#rightInfoPage').find('#_printToInBt').show();
        } else {
            $('#rightInfoPage').find('#_printToInBt').hide();
        }

        //删除判断
        if(tr.statusCode === 'STATUS_001' || tr.statusCode === 'STATUS_002' || tr.statusCode === 'STATUS_004'){
            $('#rightInfoPage').find('#_deleteSaleOrderBt').show();
        } else {
            $('#rightInfoPage').find('#_deleteSaleOrderBt').hide();
        }

        //明细操作按钮
        $('#rightInfoPage').find('._print, ._update, ._delete').click(function () {
            var $bt = $(this);
            var item = consts.itemCache[$bt.attr('data-value')];
            if(!item) return;
            consts.currentOrderItem = item;
            if ($bt.hasClass('_delete')){
                layer.confirm('删除操作不可恢复，确定删除吗？',{title:'提示',move:false},function(){
                    services.saleOrder.deleteItem(item.orderId, item.id)
                        .then(function (ret) {
                            layer.msg(ret._message);
                            loadDataTable(true);
                            if(item.orderId){
                                services.saleOrder.one(item.orderId).then(function (_tr) {
                                    initTrDetail(_tr);
                                }).catch(function (reason) {
                                    layer.msg(reason.caught ? reason.message : '请求失败！');
                                });
                            }
                            initTrDetail(tr);
                        })
                        .catch(function (reason) {
                            layer.msg(reason.caught ? reason.message : '请求失败！');
                        });
                });
            } else if($bt.hasClass('_print')){
                //条形码模版
                var $printTemplate = $('#print_to_wares');
                console.log(consts.currentOrderItem);
                var product = consts.currentOrderItem.product;
                var $clone = $printTemplate.clone();
                //基本信息
                $clone.find('label[data-name="label_1"]').html('名称：');
                $clone.find('span[data-name="content_1"]').html(product.description);
                //条形码
                services.common.barcode(product.barCode)
                    .then(function (ret) {
                        $clone.find('.print-barcode').attr('src', ret.base64);
                    })
                    .then(function () {
                        $clone.show().print();
                    })
                    .catch(function () {
                        layer.msg('生成条形码失败！');
                    });
            }
        })
    }

    //初始化选择成品的tip
    function init_select_product_tip(productId) {
        var $tip = $('#selectProductTip');
        if(!$tip || !$tip.length) return;
        if(!productId || productId === '-1'){
            $tip.html('').hide();
            return;
        }

        calc2Kg(productId);

        services.product.stockRemain(productId)
            .then(function (map) {
                $tip.html('总库存量：' + map.total + "(kg)，安全库存量：" + map.safe + '(kg)').show();
            });
    }

    /**
     * 初始化事件
     */
    function initEvent() {
        init_search_dropdown();
        init_buttons_event();
        init_add_sale_modal();
        init_edit_item_modal();
        init_change_status_modal();
        init_input_event();

        //2- 搜索
        utils.bindQuery(function (value) {
            consts.oQueryParam.cause = value;
            loadDataTable(true);
        });
    }

    function init(options) {
        console.log('in order.js');
        consts = options;
        prepareData().then(function () {
            initEvent();
            loadDataTable();
        });
    }

    return {
        init : init
    }
});
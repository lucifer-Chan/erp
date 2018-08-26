/**
 * 采购订单
 */

define('purchase',['ztree','utils','services'],function(ztree, utils, services){

    var consts = {};

    var waresData = {};//货物数据 key:{waresType}{assId}, value : {waresId}

    var waresService = {
        D : services.mould,
        P : services.product,
        M : services.rawMaterial
    };

    //预置数据
    function prepareData() {
        return $.Promise.resolve()
            .then(function () {
                if(consts.config.initSuppliers && !consts.suppliers.length){
                    return services.supplier.all().then(function (ret) {
                        consts.suppliers = [];
                        consts.suppliers.push({code : -1, name : '【请选择供应商】'});
                        ret.list.forEach(function (value) {
                            consts.suppliers.push({code : value.id , name : value.supplierName});
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
            return services.purchaseOrder.query(params);
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
            services.purchaseOrder.one(id).then(function (tr) {
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
        if ('STATUS_004' === code || 'STATUS_006' === code) css = 'label-danger';
        if ('STATUS_007' === code) css = 'label-primary';
        return '<span class="label '+ css +'">'+ consts.status[code] +'</span>';
    }

    //搜索的状态下拉
    function init_search() {
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

        //2- 搜索图标
        $('#searchIcon').unbind('click').click(function(){
            consts.oQueryParam.cause = $('#fastKeyword').val();
            loadDataTable(true);
        });
        $("#fastKeyword").keydown(function(e){
            if(e.keyCode === 13){
                consts.oQueryParam.cause = $('#fastKeyword').val();
                loadDataTable(true);
            }
        });
    }

    //按钮事件
    function init_buttons_event() {
        //新增订单按钮
        if(consts.$addPurchaseOrderBt && consts.$addPurchaseOrderBt.length){
            consts.$addPurchaseOrderBt.bind('click', function(){
                consts.currentOrder = {};
            });
        }
        //删除订单按钮
        if(consts.$deleteOrderBt && consts.$deleteOrderBt.length){
            consts.$deleteOrderBt.unbind('click').click(function () {
                layer.confirm('删除操作不可恢复，确定删除吗？',{title:'提示',move:false},function(){
                    services.purchaseOrder.delete(consts.currentOrder.id)
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
                data.supplierId = $('#_addSupplierId').attr('data-value');
                data.supplierName = consts.$addOrderModal.find('.dk_label').html();
                if(data.supplierId === '-1'){
                    layer.msg('请选择供应商！');
                    return;
                }
                if(!data.orderDate ){
                    layer.msg('请选择订单时间！');
                    return;
                }
                data.id = consts.currentOrder.id || '';
                var _function = !!consts.currentOrder.id ? services.purchaseOrder.update : services.purchaseOrder.create;
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
                data.waresType = $('#_editWaresType').attr('data-value');
                data.waresAssId = $('#_editWaresAssId').attr('data-value');
                data.orderId = consts.currentOrder.id;
                data.orderCode = consts.currentOrder.barCode;
                data.statusCode = consts.currentOrder.statusCode;
                data.id = (consts.currentOrderItem || {}).id;


                if(data.waresType === '-1'){
                    layer.msg('请选择货物类型！');
                    return;
                }
                if(data.waresAssId === '-1'){
                    layer.msg('请选择货物！');
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
                data.waresId = waresData[data.waresType  + data.waresAssId];
                data.waresName = $('#_editWaresAssId').find('.dk_label').html();
                var _function = !!consts.currentOrderItem.id ? services.purchaseOrder.updateItem : services.purchaseOrder.addItem;
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
                            data : consts.products
                        });
                        layer.msg('新增明细成功');
                    }
                }).catch(function (reason) {
                    layer.msg(reason.caught ? reason.message : '请求失败！');
                });
            });
        }

        //打印入库单
        if(consts.$printToOutBt && consts.$printToOutBt.length && consts.template.$printToOut && consts.template.$printToOut.length){
            consts.$printToOutBt.bind('click', function(){
                var $clone = consts.template.$printToOut.clone();
                //打印数据构建 - 表头
                $clone.find('div[data-name]').each(function () {
                    var value = consts.currentOrder[$(this).attr('data-name')] || '';
                    var convert = $(this).attr('data-convert');
                    if(convert === 'convertDate'){
                        value = value.substr(0,10);
                    }
                    $(this).html(value);
                });
                //打印数据构建 - 条码
                if(consts.currentOrder.barCode){
                    $clone.find('.print-barcode').attr('src', window.GLOBALS.ctxPath + 'basis/common/barcode/' + consts.currentOrder.barCode);
                }
                //打印数据构建 - 列表
                if (consts.currentOrder.items && consts.currentOrder.items.length){
                    var tbody = $clone.find('tbody').empty();
                    var html = '';
                    var calcTotalMoney = 0;
                    consts.currentOrder.items.forEach(function (item) {
                        var product = item.product;
                        if(product){
                            var calcMoney = ((item.unitPrice||0) * (item.num || 0)).toFixed(2);
                            html += '<tr>' +
                                '<td>'+ (product.endProductName || '') +'</td>' +
                                '<td>'+ (product.specification || '')  +'</td>' +
                                '<td>¥'+ (item.unitPrice||0).toFixed(2) +'</td>' +
                                '<td>'+ (item.num || 0)  +'</td>' +
                                '<td>'+ (product.unit || '') +'</td>' +
                                '<td>¥'+ calcMoney +'</td>' +
                                '<td>¥'+ (item.money || 0).toFixed(2)  +'</td></tr>';
                            calcTotalMoney += parseFloat(calcMoney);
                        }

                    });
                    if (html !== ''){
                        var chineseMoney = utils.convertCurrency(calcTotalMoney.toFixed(2));
                        html += '<tr>' +
                            '<th colspan="5">' +
                            '   <span class="pd44 pd22">合计：</span>' +
                            chineseMoney +
                            '</th>' +
                            '<td>¥' + calcTotalMoney.toFixed(2) + '</td>' +
                            '<td>¥' + (consts.currentOrder.money || 0).toFixed(2) + '</td>' +
                            '</tr>';
                    }
                    tbody.html(html);
                }
                //打印数据构建 - 表尾
                var statusLogs = consts.currentOrder.statusLogs || [];
                var approvalPass = null;
                for(var i = 0; i < statusLogs.length; i ++){
                    if ('STATUS_003' === statusLogs[i].statusCode){
                        approvalPass = statusLogs[i];
                        break;
                    }
                }
                if(approvalPass){
                    $clone.find('.approvalName').text(approvalPass.createdName);
                    $clone.find('.approvalDate').text((approvalPass.createdAt||'').substr(0,10));
                }

                $clone.show().print({
                    deferred: $.Deferred().done(function() {
                        services.purchaseOrder.afterPrint(consts.currentOrder.id);
                    })
                });
            });
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
                header : '供应商退货',
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
        }

        $('#_editItemNum, #_editItemUnitPrice').bind('input propertychange', function () {
            calc();
        });
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

            //供选货物类型
            utils.dropkick({
                $holder : $('#_editWaresType'),
                data : consts.waresTypes,
                current : consts.currentOrderItem.waresType,
                callback : init_wares
            });
            consts.$saveOrderItemBt.text(!!consts.currentOrderItem.id ? '保存' : '继续添加');

        }).on('hidden.bs.modal', function () {
            //清理数据
            $(this).find('input[type="text"], textarea, input[type="number"]').val('');
            //刷新列表
            loadDataTable(true);
            //右侧-有consts.currentOrder.id说明是右侧进来
            if(consts.currentOrder.id){
                services.purchaseOrder.one(consts.currentOrder.id).then(function (tr) {
                    initTrDetail(tr);
                }).catch(function (reason) {
                    layer.msg(reason.caught ? reason.message : '请求失败！');
                });
            }
        })

    }

    //新增订单modal
    function init_add_purchase_modal() {
        console.log('init_add_purchase_modal');
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
            // 供选供应商
            utils.dropkick({
                $holder : $('#_addSupplierId'),
                data : consts.suppliers,
                current : consts.currentOrder.supplierId
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
            services.purchaseOrder.updateStatus(consts.currentOrder.id, consts.toNewStatus.code,  $('#changeStatusRemark').val())
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
        var $detail = $('.initPurchaseOrderDetail');
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
                $(clone).find('.panel-title').text(item.waresName);
                $(clone).find('.tools').find('.fa-angle-double-up, .fa-angle-double-down, ._update, ._delete').attr('data-value', item.id);
                var body = $(clone).find('.panel-body');
                body.attr('data-value', (item.id + 'Info'));
                if(item.statusCode !== 'STATUS_049'){
                    $(body).find('div[data-name="status"]').parent().hide();
                }
                $(body).find('div[data-name="status"]').html(_convertStatus(item.statusCode));
                $(body).find('div[data-name="money"]').text('¥' + (item.money || 0).toFixed(2));
                $(body).find('div[data-name="unitPrice"]').text('¥' + (item.unitPrice||0).toFixed(2));
                $(body).find('div[data-name="num"]').text(item.num);
                $(body).find('div[data-name="remark"]').text(item.remark || '无');
                $orderItems.append($(clone).show());
            })
        }
        utils.initRightAngleEvent();
        //未发布|审核退回 可操作: 修改订单、新增明细、提交审核、删除、更新明细、删除明细
        if(tr.statusCode === 'STATUS_001' || tr.statusCode === 'STATUS_004'){
            $('#rightInfoPage').find('#_addPurchaseOrderBt, #_addPurchaseOrderItemBt, #_toSubmitOrderBt, ._update, ._delete').show();
        } else {
            $('#rightInfoPage').find('#_addPurchaseOrderBt, #_addPurchaseOrderItemBt, #_toSubmitOrderBt, ._update, ._delete').hide();
        }
        //待审核 可操作：审核通过、审核退回、删除
        if(tr.statusCode === 'STATUS_002'){
            $('#rightInfoPage').find('#_toApprovalPassBt, #_toApprovalRefuseBt').show();
        } else {
            $('#rightInfoPage').find('#_toApprovalPassBt, #_toApprovalRefuseBt').hide();
        }
        //审核通过 可操作：打印入库单、作废
        if(tr.statusCode === 'STATUS_003'){
            $('#rightInfoPage').find('#_printToInBt, #_toInvalidOrderBt').show();
        } else {
            $('#rightInfoPage').find('#_printToInBt, #_toInvalidOrderBt').hide();
        }
        //已出库 可操作: 供应商退回、完成
        if(tr.statusCode === 'STATUS_005'){
            $('#rightInfoPage').find('#_toRefundsOrderBt, #_toFinishOrderBt').show();
        } else {
            $('#rightInfoPage').find('#_toRefundsOrderBt, #_toFinishOrderBt').hide();
        }

        //删除判断
        if(tr.statusCode === 'STATUS_001' || tr.statusCode === 'STATUS_002' || tr.statusCode === 'STATUS_004'){
            $('#rightInfoPage').find('#_deletePurchaseOrderBt').show();
        } else {
            $('#rightInfoPage').find('#_deletePurchaseOrderBt').hide();
        }


        //明细操作按钮
        $('#rightInfoPage').find('._update, ._delete').click(function () {
            var $bt = $(this);
            var item = consts.itemCache[$bt.attr('data-value')];
            if(!item) return;
            consts.currentOrderItem = item;
            if ($bt.hasClass('_delete')){
                layer.confirm('删除操作不可恢复，确定删除吗？',{title:'提示',move:false},function(){
                    services.purchaseOrder.deleteItem(item.orderId, item.id)
                        .then(function (ret) {
                            layer.msg(ret._message);
                            loadDataTable(true);
                            if(item.orderId){
                                services.purchaseOrder.one(item.orderId).then(function (_tr) {
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
            }
        })
    }

    //根据货物类型初始化货物列表
    function init_wares(waresType) {
        var label = consts.waresTypes.find(function (type) {
            return type.code === waresType;
        }).name;
        $('#wareAssLabel').html(label);

        services.purchaseOrder.lookup(consts.currentOrder.supplierId, waresType)
            .then(function (ret) {
                var data = [{code : '-1', name : '【请选择' + label + '】'}];
                ret.list.forEach(function (item) {
                    data.push(item);
                    waresData[waresType + item.code] = item.waresId;
                });
                //供选货物类型
                utils.dropkick({
                    $holder : $('#_editWaresAssId'),
                    data : data,
                    current : consts.currentOrderItem.waresAssId,
                    callback : function (id) {
                        console.log('当前货物类型', waresType);
                        var $tip = $('#selectWaresTip');
                        if(!id || id === '-1') {
                            $tip.html('').hide();
                            return;
                        }
                        //如果是成品，查找成品模版库存，否则查找当前供应商的货物库存
                        var _id = 'P' === waresType ? waresData[waresType + id] : id;

                        waresService[waresType].stockRemain(_id)
                            .then(function (map) {
                                $tip.html('总库存量：' + map.total).show();
                            })
                            .catch(function (reason) {
                                console.log('reason', reason);
                            })
                        ;
                    }
                });
            })
    }

    /**
     * 初始化事件
     */
    function initEvent() {
        init_search();
        init_buttons_event();
        init_add_purchase_modal();
        init_edit_item_modal();
        init_change_status_modal();
        init_input_event();

        
    }

    function init(options) {
        console.log('in purchase/order.js');
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
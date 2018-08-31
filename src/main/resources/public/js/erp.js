define('erp',[],function(){


    // var printOut = function ($template, oOrder, aItems) {
    //
    // }
    //
    //
    //
    // if(consts.$printToOutBt && consts.$printToOutBt.length && consts.template.$printToOut && consts.template.$printToOut.length){
    //     consts.$printToOutBt.bind('click', function(){
    //         var $clone = consts.template.$printToOut.clone();
    //         //打印数据构建 - 表头
    //         $clone.find('div[data-name]').each(function () {
    //             var value = consts.currentOrder[$(this).attr('data-name')] || '';
    //             var convert = $(this).attr('data-convert');
    //             if(convert === 'convertDate'){
    //                 value = value.substr(0,10);
    //             }
    //             $(this).html(value);
    //         });
    //         //打印数据构建 - 条形码
    //         if(consts.currentOrder.barCode){
    //             $clone.find('.print-barcode').attr('src', window.GLOBALS.ctxPath + 'basis/common/barcode/' + consts.currentOrder.barCode);
    //         }
    //         //打印数据构建 - 列表
    //         if (consts.currentOrder.items && consts.currentOrder.items.length){
    //             var tbody = $clone.find('tbody').empty();
    //             var html = '';
    //             var calcTotalMoney = 0;
    //             consts.currentOrder.items.forEach(function (item) {
    //                 var product = item.product;
    //                 if(product){
    //                     var calcMoney = ((item.unitPrice||0) * (item.num || 0)).toFixed(2);
    //                     html += '<tr>' +
    //                         '<td>'+ (product.endProductName || '') +'</td>' +
    //                         '<td>'+ (product.specification || '')  +'</td>' +
    //                         '<td style="text-align: center">'+ (product.unit || '') +'</td>' +
    //                         '<td style="text-align: right">'+ (item.num || 0)  +'</td>' +
    //                         '<td style="text-align: right">'+ (item.unitPrice||0).toFixed(2) +'</td>' +
    //                         //'<td>¥'+ calcMoney +'</td>' +
    //                         '<td style="text-align: right">'+ (item.money || 0).toFixed(2)  +'</td>' +
    //                         '<td>'+ (item.remark || '') +'</td>' +
    //                         '</tr>';
    //                     calcTotalMoney += parseFloat(calcMoney);
    //                 }
    //
    //             });
    //             if (html !== ''){
    //                 //不足5行的补足5行
    //                 var emptyLines = 5 - consts.currentOrder.items.length;
    //                 for(var index = 0; emptyLines > 0 && index < emptyLines; index ++){
    //                     html += '<tr style="height: 37px"><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>';
    //                 }
    //                 //合计
    //                 var chineseMoney = utils.convertCurrency(calcTotalMoney.toFixed(2));
    //                 html += '<tr>' +
    //                     '<th colspan="3">' +
    //                     '   <span class="pd44 pd22">合计金额（大写）：</span>' +
    //                     chineseMoney +
    //                     '</th>' +
    //                     //'<td>¥' + calcTotalMoney.toFixed(2) + '</td>' +
    //                     //'<td>¥' + (consts.currentOrder.money || 0).toFixed(2) + '</td>' +
    //                     '<th colspan="3" style="text-align: center">（小写）¥' + (consts.currentOrder.money || 0).toFixed(2) + '</th>' +
    //                     '<td></td>' +
    //                     '</tr>';
    //             }
    //             tbody.html(html);
    //         }
    //
    //         //打印数据构建 - 表尾
    //         var statusLogs = consts.currentOrder.statusLogs || [];
    //         var approvalPass = null;
    //         for(var i = 0; i < statusLogs.length; i ++){
    //             if ('STATUS_003' === statusLogs[i].statusCode){
    //                 approvalPass = statusLogs[i];
    //                 break;
    //             }
    //         }
    //         if(approvalPass){
    //             $clone.find('.approvalName').text(approvalPass.createdName);
    //             $clone.find('.approvalDate').text((approvalPass.createdAt||'').substr(0,10));
    //         }
    //
    //         $clone.show().print({
    //             deferred: $.Deferred().done(function() {
    //                 services.saleOrder.afterPrint(consts.currentOrder.id);
    //             })
    //         });
    //     });
    // }
    //
    //
    //
    // return {
    //     printOut : printOut //打印出库单
    //     , printIn : printIn //打印入库单
    //     , printWares : printWares //打印入库单
    //     , printPlace : printPlace // 打印仓位条码
    //
    // }
});
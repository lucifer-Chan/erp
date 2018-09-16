/**
 * 全局变量的定义
 */
;window.GLOBALS = {
    // ctxPath : '/erp'//生产环境可能需要替换TODO
    ctxPath : '/'//生产环境可能需要替换TODO
    , debug : false
    , time : 500
    , localKeys : {
        accountInfo : 'accountInfo'//账户信息
    }
    , defaultHeadUrl : '/img/head_default.png'//默认头像
    , ImageNumber : 5
    , attachmentNumber : 2
    , attachmentSize : 5
};
window.checkStrNum = function(th,num){
    $(th).parent().addClass('onFocus');
    var val = $(th).val();
    if(val.length > num){
        layer.msg('最多输入'+num+'个字符');
        $(th).val($(th).attr('data-value'));
        return false;
    }else{
        $(th).attr('data-value',val);
    }
};

Date.prototype.format = function (fmt) { //author: meizz
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "h+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

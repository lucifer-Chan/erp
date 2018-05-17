/**
 * 全局变量的定义
 */
;window.GLOBALS = {
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
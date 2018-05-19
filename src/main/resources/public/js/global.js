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
    //全局的回调函数，在模版页面加载完成后调用
    , callBack : function () {
        //右侧页面收起打开操作
        $('#rightInfoPage .fa-angle-double-up, #rightInfoPage .fa-angle-double-down').click(function () {
            var value = $(this).data("value") + "Info";
            $(this).parents('.row').find('div[data-value="'+ value+'"]').toggle();
            if($(this).hasClass('fa-angle-double-up'))
                $(this).removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
            else if($(this).hasClass('fa-angle-double-down'))
                $(this).removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        });
        //隐藏右侧页面事件
        $('.rightInfoBack').click(function () {
            $('#rightInfoPage').hide();
        });
    }
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

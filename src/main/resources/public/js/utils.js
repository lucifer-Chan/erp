/**
 * Created by lucifer.chan on 2018/03/19.
 */

define('utils',[],function(){
    /**
     * ajax加载页面
     * @param div
     * @param opts
     * @param callback
     */
    function loadPage(opts) {
        var _default = {
            url : '',
            div : $('body'),
            async : true
        };
        var setting = $.extend({}, _default, (opts || {}));
        //设置loading
        var loadIngTips = 0;
        var index = '';
        loadIngTips = setTimeout(function(){
            index = layer.msg('<i class="fa fa-spinner fa-spin"></i> 正在加载',{time:10*60*1000});
        },500);
        //加载模板
        return $.http.get({
            url : 'tpl/' + setting.url,
            dataType : 'html',
            async : setting.async
        }).then(function (data) {
            setting.div.html(data);
            clearTimeout(loadIngTips);
            layer.close(index);
            return setting.div;
        }).then(function (div) {
            //右侧页面收起打开操作
            $(div).find('#rightInfoPage .fa-angle-double-up, #rightInfoPage .fa-angle-double-down').click(function () {
                var value = $(this).data("value") + "Info";
                $(this).parents('.row').find('div[data-value="'+ value+'"]').toggle();
                if($(this).hasClass('fa-angle-double-up'))
                    $(this).removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
                else if($(this).hasClass('fa-angle-double-down'))
                    $(this).removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
            });
            //隐藏右侧页面事件
            $(div).find('.rightInfoBack').click(function () {
                $('#rightInfoPage').attr('data-id', '').hide();
            });
        }).catch(function (reason) {
            console.log('loadPage', reason);
            clearTimeout(loadIngTips);
            setting.div.html('<h4>页面' + setting.url +'尚未建设！</h4>')
            // layer.msg('请求'+ setting.url + '失败');
        });
    }

    function getRandom(num){
        var random = '';
        for(var n=0;n<num;n++)
            random += Math.floor(Math.random()*10);
        return random;
    }

    function setPageTitle(name){
        document.title = '工作台-'+name;
    }

    /**
     * 字符转义
     */
    function strRegExp(str){
        if(typeof str == 'undefined')
            str = '';
        str = str.replace(/\</g,'&lt;');
        str = str.replace(/\>/g,'&gt;');
        str = str.replace(/\n/g,'<br/>');
        //str = str.replace(/\&/g,'&amp;');
        str = str.replace(/\"/g,'&quot;');
        str = str.replace(/\'/g,'&#39;');
        str = str.replace(/\ /g,'&nbsp;');
        str = str.replace(/\	/g,'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
        return str;
    }

    /**
     * 获取地址栏字符串
     */
    function getUrlString(str){
        var reg = new RegExp("(^|&)"+ str +"=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        return !!r ? decodeURIComponent(r[2]): null;
    }

    /**
     * 添加链接
     * */
    function strRegExpAddUrl(str){
        if(typeof str == 'undefined')
            str = '';
        var exp = /((http[s]{0,1}|ftp):\/\/[a-zA-Z0-9\.\-]+\.([a-zA-Z0-9]{1,4})(:\d+)?(\/[a-zA-Z0-9\.\-~!@#$%&*+?:_\/=]*)?)/g;
        str = str.replace(/\</g,' &lt; ');
        str = str.replace(/\>/g,' &gt; ');
        str = str.replace(/\n/g,' <br/> ');
        str = str.replace(/\"/g,' &quot; ');
        str = str.replace(/\'/g,' &#39; ');

        str = str.replace(exp,"<a href='$1' target='_blank' style='color:#00aeef'>$1</a>");
        str = str.replace(/ &lt; /g, '&lt;');
        str = str.replace(/ &gt; /g, '&gt;');
        str = str.replace(/ <br\/> /g, '<br/>');
        str = str.replace(/ &quot; /g, '&quot;');
        str = str.replace(/ &#39; /g, '&#39;');
        var ina = false;
        var newStr = '';
        for(var i = 0;i<str.length;i++) {
            var c = str[i];
            if(c == '<') {
                ina = true;
            }
            else if(c == '>') {
                ina = false;
            }
            if(c == ' ' || c == '	') {
                if (!ina) {
                    if(c == ' ') {
                        newStr += '&nbsp;';
                    }
                    else {
                        newStr += '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
                    }
                }
                else {
                    newStr += c;
                }
            }
            else {
                newStr += c;
            }
        }
        return newStr;
    }

    /**
     * 自定义 checkbox 和 radios
     */
    function setupLabel() {
        // Checkbox
        var checkBox = ".checkbox";
        var checkBoxInput = checkBox + " input[type='checkbox']";
        var checkBoxChecked = "checked";
        var checkBoxDisabled = "disabled";

        // Radio
        var radio = ".radio";
        var radioInput = radio + " input[type='radio']";
        var radioOn = "checked";
        var radioDisabled = "disabled";

        // Checkboxes
        if ($(checkBoxInput).length) {
            $(checkBox).each(function(){
                $(this).removeClass(checkBoxChecked);
            });
            $(checkBoxInput + ":checked").each(function(){
                $(this).parent(checkBox).addClass(checkBoxChecked);
            });
            $(checkBoxInput + ":disabled").each(function(){
                $(this).parent(checkBox).addClass(checkBoxDisabled);
            });
        }

        // Radios
        if ($(radioInput).length) {
            $(radio).each(function(){
                $(this).removeClass(radioOn);
            });
            $(radioInput + ":checked").each(function(){
                $(this).parent(radio).addClass(radioOn);
            });
            $(radioInput + ":disabled").each(function(){
                $(this).parent(radio).addClass(radioDisabled);
            });
        };
    }


    /**
     * utf-8 字符转换
     * */
    function utf16to8(str) {
        var out, i, len, c;
        out = "";
        len = str.length;
        for(i = 0; i < len; i++) {
            c = str.charCodeAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                out += str.charAt(i);
            } else if (c > 0x07FF) {
                out += String.fromCharCode(0xE0 | ((c >> 12) & 0x0F));
                out += String.fromCharCode(0x80 | ((c >>  6) & 0x3F));
                out += String.fromCharCode(0x80 | ((c >>  0) & 0x3F));
            } else {
                out += String.fromCharCode(0xC0 | ((c >>  6) & 0x1F));
                out += String.fromCharCode(0x80 | ((c >>  0) & 0x3F));
            }
        }
        return out;
    }

    /**
     * 设置主题
     */
    function initTheme(){
        function changeTheme(theme) {
            theme = theme || $.local('theme') || 'default';
            $('html').removeClass('theme1 theme2 theme3 theme4 theme5 theme6 theme7');
            if(theme !== 'default'){
                $('html').addClass(theme);
            }
            $.local('theme',theme);
        }

        function _el($el, change) {
            change = undefined === change ? !!0 : change;
            var val = $el.attr('data-value');
            var _current = $.local('theme');
            if(_current === val || change){
                $('#appBrush span i').removeClass('fa-dot-circle-o').addClass('fa-circle-o');
                $el.parents('.erp_theme').find('i').addClass('fa-dot-circle-o').removeClass('fa-circle-o');
                changeTheme(val);
            }
        }

        $('#appBrush span').each(function(index,el) {
            _el($(el));
            $(el).click(function(){
                _el($(el), !!1);
            });
        });
    }

    /**
     * 个人设置事件
     */
    function initUserSettingEvent() {
        $('.setting .username').unbind('click').click(function () {
            $(this).hasClass('actived') && $(this).removeClass('actived') || $(this).addClass('actived');
        });
        $(document).bind('click', function (e) {
            $(e.target).closest('.setting .username').length ||  $('.setting .username').removeClass('actived');
        });
    }

    /**
     * 加载个人信息
     */
    function loadAccountInfo() {
        var account = $.local(GLOBALS.localKeys.accountInfo);
        var account_wrap = '<i class="fa fa-user"></i>\n' +
                '<span class="username-txt textOverHiden" alt="'+account.name+'" title="'+account.name+'">'+strRegExp(account.name)+'</span>\n' +
            '<i class="fa fa-caret-down"></i>';
        $('.username-warp').html(account_wrap);
        initUserSettingEvent();
    }

    //装dataTable的map
    var dataTableMap = new Map();

    /**
     * 封装dataTable
     * @param options
     *
     * @param holderId table节点的id-带#
     * @param $dataProvider - promise
     * @param oParams 外置查询参数
     * @param fClickTr 点击tr的回调方法
     * @param aConverts 包装方法-用来处理复杂的数据包装
     */
    function dataTable(options) {

        options = options || {};

        var holderId = options.holder || '';
        var $dataProvider = options.$dataProvider || '';
        var oParams = options.oParams || {};
        var fClickTr = options.fClickTr || function(){};
        var aConverts = options.aConverts;
        if(typeof $dataProvider !== 'function' ){
            console.log(holderId, 'dataTable没有数据供应的promise方法');
            throw new TypeError($dataProvider + ' is not a function');
        }

        var _oTable = dataTableMap.get(holderId);
        var pageInfo = {};
        if(_oTable){
            _oTable.api().destroy();
            // console.log(_oTable.api());
        }
        //以<th/>为模版
        var ths = $(holderId).find('th');
        //<th>父节点<tr>,用来存id
        var tr = $(holderId).find('tr');
        //每行的tds数据模版
        var aTr = [];
        //每行tr，用来存id
        var oTr = null;
        $.each($(ths), function(idx,th){
            //原内容
            var text = $(th).attr('erp-data') || '';
            //多个{content}
            var _array = text.match(/\{(.+?)\}/g);
            var array = [];
            var map = new Map();
            for(var index in _array){
                var temp = /\{(.+?)\}/.exec(_array[index])
                map.set(temp[0], temp[1]);
            }
            map.forEach(function (value, key, map) {
                array.push({replace : key, field : value});
            });
            aTr.push({source : text, array : array, _function : $(th).attr('erp-convert') || ''});
        });

        //每行tr的id占位
        if($(tr).attr("erp-id")!==undefined){
            var _text = $(tr).attr('erp-id') || '';
            var _temp = /\{(.+?)\}/.exec(_text);
            if(_temp){
                oTr = {source : _text, replace : _temp[0], field : _temp[1]};
            }
        }

        _oTable = $(holderId).dataTable({
            "bProcessing": false,
            "bLengthChange": false,
            "bServerSide": true,
            "bAutoWidth" : false,
            "bFilter":false,
            "bInfo": false,
            "bStateSave": false,
            "iDisplayLength": 20,//当前每页显示多少
            "ordering": false, // 禁止排序
            "fnServerData": function ( sSource, aoData, fnCallback ) {
                var params = $.extend({},oParams, {pageNum : aoData.start / aoData.length + 1, perPageNum : aoData.length});
                $dataProvider(params)
                    .then(function (ret) {
                        pageInfo.iTotalRecords = ret.totalElements;
                        pageInfo.iTotalDisplayRecords = ret.totalElements;
                        var aaData = [];
                        ret.list.forEach(function (data) {
                            var aaa = convert(data);
                            aaData.push(aaa);
                        });

                        /**
                         * data - 后端返回list里的元素{}
                         * return []
                         */
                        function convert(data){
                            var aData = [];
                            if (oTr){
                                aData.push(oTr.source.replace(new RegExp(oTr.replace,'g'), data[oTr.field]));
                            }
                            //td - {source : text, array : [{replace, content}], _function : '')
                            aTr.forEach(function (oTd){
                                //td
                                var text = oTd.source;
                                if(text){
                                    var _function = oTd._function;
                                    //有convert方法就使用convert方法
                                    if(_function){
                                        var converted = false;
                                        var params = [];
                                        oTd.array.forEach(function(r){
                                            params.push(data[r.field]);
                                        });
                                        if(!$.isArray(aConverts)) throw new TypeError((aConverts||'末尾参数：aConverts') + ' is not a Array');
                                        for(var i = 0; i < aConverts.length; i ++){
                                            if(typeof aConverts[i] !== 'function' )
                                                throw new TypeError(aConverts[i]  + ' is not a function');
                                            if(aConverts[i].name === _function){
                                                text = aConverts[i](params);
                                                converted = true;
                                            }
                                        }
                                        if(!converted)
                                            throw new ReferenceError('aConverts中未找到' + _function + '方法');
                                    } else {
                                        //没有convert方法就正则替换
                                        oTd.array.forEach(function(r){
                                            text = text.replace(new RegExp(r.replace,'g'), data[r.field]);
                                        });
                                    }
                                }
                                aData.push(text);

                            });
                            return aData;
                        }
                        fnCallback($.extend({},{aaData:aaData},pageInfo));
                    })
                    .then(function () {
                        if (fClickTr) {
                            _oTable.$('tr').unbind('click').click(function () {
                                fClickTr($(this));
                            });
                        }
                        $('#dataTables_info').parent().hide();
                        if(pageInfo.iTotalRecords >20){
                            $('#dataTables_paginate').parent().attr('class','col-sm-12');
                        }else{
                            $('#dataTables_paginate').parent().hide();
                        }
                    })
                    .catch(function (reason) {
                        console.log(reason);
                    })
                ;
            },

            "fnCreatedRow": function (nRow, aData, iDataIndex) {
                if(oTr){
                    //设置id
                    $(nRow).attr('id', aData[0]);
                    //删除第一个td
                    $('td:eq(0)', nRow).remove();
                    //追加一个
                    $(nRow).append('<td>' + aData[aData.length-1] + '</td>');
                }
            }
        });
        dataTableMap.set(holderId, _oTable);
    }





    return {
        loadPage: loadPage
        , getRandom: getRandom
        , strRegExp: strRegExp
        , strRegExpAddUrl: strRegExpAddUrl
        , getUrlString: getUrlString
        , setupLabel: setupLabel
        , utf16to8: utf16to8
        , loadAccountInfo: loadAccountInfo
        , initTheme: initTheme
        , setPageTitle: setPageTitle
        , dataTable : dataTable
    }
});
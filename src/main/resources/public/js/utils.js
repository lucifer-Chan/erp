/**
 * Created by lucifer.chan on 2018/03/19.
 */

define('utils',['timeObjectUtil'],function(timeObjectUtil){
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
            initRightAngleEvent(div);
            //隐藏右侧页面事件
            $(div).find('.rightInfoBack').click(function () {
                $('#rightInfoPage').attr('data-id', '').hide();
                $('.customTable').find('tr').removeClass('hover');
            });
        }).catch(function (reason) {
            console.log('loadPage', reason);
            clearTimeout(loadIngTips);
            setting.div.html('<h4>页面' + setting.url +'尚未建设！</h4>')
            // layer.msg('请求'+ setting.url + '失败');
        });
    }

    //右侧下收起-打开事件，动态生成的页面，需要在生成之后手动调用
    function initRightAngleEvent(div) {
        div = !!div ? div : $('body');
        $(div).find('#rightInfoPage .fa-angle-double-up, #rightInfoPage .fa-angle-double-down').click(function () {
            var value = $(this).data("value") + "Info";
            $(this).parents('.row').find('div[data-value="'+ value+'"]').toggle();
            if($(this).hasClass('fa-angle-double-up'))
                $(this).removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
            else if($(this).hasClass('fa-angle-double-down'))
                $(this).removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
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

        var unSorting = options.unSorting || []; //不参与排序的index eg:4,5,6

        var sorting = [0, 'desc'];
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
            "iDisplayLength": 15,//当前每页显示多少
            "order": [sorting],
            "aoColumnDefs":[
                { "bSortable": false,"aTargets": unSorting }
            ],
            // "ordering": !sorting.length, // 禁止排序
            "fnServerData": function ( sSource, aoData, fnCallback ) {
                var params = $.extend({},oParams, {pageNum : aoData.start / aoData.length + 1, perPageNum : aoData.length});

                if (!!aoData.order && !$.isEmptyObject(aoData) && ths.length >= aoData.order[0].column){
                    var attrs = $(ths[aoData.order[0].column]).attr('erp-data');///\{(.+?)\}/.exec($(ths[aoData.order[0].column]))[1];
                    var attr = (attrs.match(/\{(.+?)\}/g) || [''])[0];
                    var sortAttr = (/\{(.+?)\}/.exec(attr) || ['', ''])[1];
                    var sortBy = !!sortAttr ? sortAttr + '@' + aoData.order[0].dir : false;
                    if (sortBy !== false){
                        params.sortBy = sortBy;
                    }
                    console.log('列表排序：', sortBy);
                }
                $dataProvider(params)
                    .then(function (ret) {
                        $('#allCount').text('共'+ret.totalElements+'条记录');
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
                        _oTable.$('tr').unbind('click').click(function () {
                            $(this).addClass('hover').siblings().removeClass('hover');
                            fClickTr($(this));
                        });
                        $(holderId+ '_info').parent().hide();
                        if(pageInfo.iTotalRecords >15){
                            $(holderId + '_paginate').parent().attr('class','col-sm-12');
                        }else{
                            $(holderId +'_paginate').parent().hide();
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

    /**
     * @param setting -{$holder, -- dom
     *                  data, -- 数据 [{code, name}] or {a:b,c:d,e:f}
     *                  unselected, -未选择时的展现字
     *                  prefix, -- 前缀
     *                  callback, -- 点击a标签的回调函数，参数为a的data-value
     *                  init, --初始时候调用callback
     *                  current -- 当前值
     *                  }
     */
    function dropdown(setting) {
        setting = $.extend({$holder : $('<div/>'), data : [], prefix:'', current : ''}, setting);
        var callback = setting.callback || function (value) {console.log(value)};
        var unselected = setting.unselected || '全部';
        var prefix = !!setting.prefix ? (setting.prefix + ':') : '';

        setting.$holder.html('<a class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">'+ prefix + unselected +' <span class="caret"></span></a>');
        var ul = $('<ul class="dropdown-menu">');

        ul.append('<li><a data-value="">'+ unselected +'</a></li>');
        var dataType = $.isArray(setting.data) ? 'array' : $.isObject(setting.data) ? 'object' : undefined;
        if(dataType === 'object'){
            $.each(setting.data, function (code, name) {
                ul.append('<li><a data-value="'+ code +'">'+ name + '</a></li>');
            });
        } else if(dataType === 'array'){
            $.each(setting.data, function () {
                ul.append('<li><a data-value="'+ this.code +'">'+ this.name + '</a></li>');
            });
        }
        setting.$holder.append(ul);
        ul.find('li>a').click(function () {
            var $this = $(this);
            $(this).parents('li.dropdown').children().eq(0).html(prefix + $this.text()+' <span class="caret"></span>');
            callback($(this).attr('data-value'));
        });
        (setting.init === true) && ul.find('li>a').first().click();
        (setting.current !== '') && ul.find('li>a[data-value="'+  setting.current +'"]').click();
    }

    /**
     * @param setting -{$holder, -- dom
     *                  data, -- 数据 [{code, name}] or {a:b,c:d,e:f}
     *                  current, -- 当前数据
     *                  callback -- 选择option的回调函数，参数为option的value
     *                  }
     */
    function dropkick(setting) {
        setting = $.extend({$holder : $('<div/>'), data : [], current : ''}, setting);
        var callback = setting.callback || function (value) {console.log(value)};
        var holderId = setting.$holder.attr('id');
        var $select = $('<select id="'+holderId+'_Select">');
        var dataType = $.isArray(setting.data) ? 'array' : $.isObject(setting.data) ? 'object' : undefined;
        var count = 0;

        if(dataType === 'object'){
            $.each(setting.data, function (code, name) {
                var selected = (setting.current && setting.current == code) ? ' selected' : '';
                $select.append('<option value="'+code+ '"' + selected + '>' + name + '</option>');
                count ++;
            });
        } else if(dataType === 'array'){
            $.each(setting.data, function () {
                var selected = (setting.current && setting.current == this.code) ? ' selected' : '';
                $select.append('<option value="'+ this.code+ '"' + selected + '>' + this.name + '</option>');
                count ++;
            });
        }
        setting.$holder.empty().append($select);
        var value = setting.current || $select.find('option').eq(0).val() || '';
        setting.$holder.attr('data-value', value);
        callback(value);
        var $$ = $('#' + holderId+'_Select').dropkick({
            change : function(value){
                setting.$holder.attr('data-value', value);
                callback(value);
                setTimeout(function(){
                    setting.$holder.find('.search i').hide();
                    setting.$holder.find('input').val('');
                    setting.$holder.find('ul li').each(function(index,el){
                        $(el).show();
                    });
                },500);
            }
        });
        if(count > 8){
            setting.$holder.find('ul').before('<div class="search"><input type="text" class="form-control" placeholder="快速搜索"><i class="fa fa-times-circle"></i></div>');
            setting.$holder.find('input').focus(function(){
                var $this = $(this);
                setting.$holder.children().addClass('dk_open dk_focus');
                $this.unbind('input').on('input',function(){
                    var input_val = $this.val().toLowerCase();
                    var li_val = setting.$holder.find('ul li');
                    if(!!input_val){
                        setting.$holder.find('.search i').show();
                    }else{
                        setting.$holder.find('.search i').hide();
                    }
                    li_val.each(function(index,el){
                        var text = $(el).find('a').text();
                        console.log(text);
                        if(text && text.toLowerCase().indexOf(input_val) > -1){
                            $(el).show();
                        } else {
                            $(el).hide();
                        }
                    });
                });
                return false;
            });
            setting.$holder.find('.search i').click(function(){
                $(this).hide();
                setting.$holder.find('input').val('');
                setting.$holder.find('ul li').each(function(index,el){
                    $(el).show();
                });
            });
        }
        setting.$holder.find('.input-group').click(function(){return false});
    }

    /**
     * 文件上传
     * @param oSettings - {$input, uploadUrl, type, $modal, onSuccess, onFailed}
     */
    function uploadFile(oSettings) {
        var $input = oSettings.$input;
        var uploadUrl = oSettings.uploadUrl;
        var type = oSettings.type || '';
        var $modal = oSettings.$modal;
        var onSuccess = oSettings.onSuccess || function () {}
        var onFailed = oSettings.onFailed || function (){}
        //$input, uploadUrl, type
        $input.fileinput({
            language: 'zh', //设置语言
            uploadUrl: uploadUrl, //上传的地址
            uploadAsync: true, //默认异步上传
            showCaption: true,//是否显示标题
            showUpload: true, //是否显示上传按钮
            browseClass: "btn btn-primary", //按钮样式
            allowedFileExtensions: ["xlsx"], //接收的文件后缀
            maxFileCount: 1,//最大上传文件数限制
            showPreview: false, //是否显示预览
            uploadExtraData: function () {
                return {type : type};
            }
        });

        $input.on("fileuploaded", function (event, data, previewId, index) {
            console.log(data);
            var fileName = data.files[index].name;
            var response = data.response;
            if(response.success === true){
                onSuccess(fileName, response.ret);
                //关闭
                $modal && $modal.modal('hide');
            } else{
                onFailed(fileName, response.ret || response);
                $input.fileinput("clear").fileinput("reset").fileinput('refresh').fileinput('enable');
            }
        });

        $modal && $modal.on('hidden.bs.modal', function () {
            //清理数据
            $input.fileinput("clear").fileinput("reset").fileinput('refresh').fileinput('enable');
        });
    }

    //日期选择器的默认配置
    var datepickerConfig = {
        keyboardNavigation: false,
        weekBeginMonday: false,
        forceParse: true,
        autoclose: true,
        clearBtn : true,
        todayHighlight: true,
        language : 'zh-CN',
        format : 'yyyy-mm-dd',
        weekStart: 0,
        startDate : new Date('2000-01-01'),
        endDate : new Date('2300-01-01')
    };

    /**
     * 日期选择
     * @param json
     */
    function datepicker(json){
        json = $.extend({weekBeginMonday:false},json);
        json.begin.datepicker(datepickerConfig).on('changeDate',function(ev){
            if(typeof ev.date != 'undefined'){
                var value = ev.date.valueOf();
                var endDate = json.end.val();
                if(new Date(endDate) < value){
                    json.end.datepicker('setDate',timeObjectUtil.longMsTimeConvertToDate3(value));
                }
            }
        });
        json.end.datepicker(datepickerConfig).on('changeDate',function(ev){
            if(typeof ev.date != 'undefined'){
                var value = ev.date.valueOf();
                var beginDate = json.begin.val();
                if(new Date(beginDate) > value){
                    json.begin.datepicker('setDate',timeObjectUtil.longMsTimeConvertToDate3(value));
                }
            }
        });
    }

    function convertCurrency(money) {
        //汉字的数字
        var cnNums = new Array('零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖');
        //基本单位
        var cnIntRadice = new Array('', '拾', '佰', '仟');
        //对应整数部分扩展单位
        var cnIntUnits = new Array('', '万', '亿', '兆');
        //对应小数部分单位
        var cnDecUnits = new Array('角', '分', '毫', '厘');
        //整数金额时后面跟的字符
        var cnInteger = '整';
        //整型完以后的单位
        var cnIntLast = '元';
        //最大处理的数字
        var maxNum = 999999999999999.9999;
        //金额整数部分
        var integerNum;
        //金额小数部分
        var decimalNum;
        //输出的中文金额字符串
        var chineseStr = '';
        //分离金额后用的数组，预定义
        var parts;
        if (money == '') { return ''; }
        money = parseFloat(money);
        if (money >= maxNum) {
            //超出最大处理数字
            return '';
        }
        if (money == 0) {
            chineseStr = cnNums[0] + cnIntLast + cnInteger;
            return chineseStr;
        }
        //转换为字符串
        money = money.toString();
        if (money.indexOf('.') == -1) {
            integerNum = money;
            decimalNum = '';
        } else {
            parts = money.split('.');
            integerNum = parts[0];
            decimalNum = parts[1].substr(0, 4);
        }
        //获取整型部分转换
        if (parseInt(integerNum, 10) > 0) {
            var zeroCount = 0;
            var IntLen = integerNum.length;
            for (var i = 0; i < IntLen; i++) {
                var n = integerNum.substr(i, 1);
                var p = IntLen - i - 1;
                var q = p / 4;
                var m = p % 4;
                if (n == '0') {
                    zeroCount++;
                } else {
                    if (zeroCount > 0) {
                        chineseStr += cnNums[0];
                    }
                    //归零
                    zeroCount = 0;
                    chineseStr += cnNums[parseInt(n)] + cnIntRadice[m];
                }
                if (m == 0 && zeroCount < 4) {
                    chineseStr += cnIntUnits[q];
                }
            }
            chineseStr += cnIntLast;
        }
        //小数部分
        if (decimalNum != '') {
            var decLen = decimalNum.length;
            for (var i = 0; i < decLen; i++) {
                var n = decimalNum.substr(i, 1);
                if (n != '0') {
                    chineseStr += cnNums[Number(n)] + cnDecUnits[i];
                }
            }
        }
        if (chineseStr == '') {
            chineseStr += cnNums[0] + cnIntLast + cnInteger;
        } else if (decimalNum == '') {
            chineseStr += cnInteger;
        }
        return chineseStr;
    }

    function bindQuery(callback) {
        callback = callback || function (val) {
            console.log('当前输入:' + val);
        }
        //搜索图标
        $('#searchIcon').unbind('click').click(function(){
            callback($('#fastKeyword').val());
        });
        //回车搜索
        $("#fastKeyword").keydown(function(e){
            if(e.keyCode === 13){
                callback($('#fastKeyword').val());
            }
        });
        //输入完成搜索
        $("#fastKeyword").bind('input propertychange', function () {
            callback($('#fastKeyword').val());
        });
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
        , dropdown : dropdown
        , dropkick : dropkick
        , uploadFile : uploadFile
        , datepickerConfig : datepickerConfig
        , datepicker : datepicker
        , initRightAngleEvent : initRightAngleEvent
        , convertCurrency : convertCurrency
        , bindQuery : bindQuery
    }
});
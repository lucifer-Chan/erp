requirejs.config({
    baseUrl: '/js',
    urlArgs: "v1",
    waitSeconds: 15,
    paths: {
        dropkick : '/vendor/jquery/jquery.dropkick-1.0.0',
        slimScroll : '/vendor/jquery/jquery.slimscroll.min.1.3.8',
        metisMenu : '/vendor/jquery/jquery.metisMenu',
        treeview : '/vendor/bootstrap/bootstrap-treeview',
        ztree : '/vendor/jquery/ztree/jquery.ztree.all'
    }
});

requirejs(['validate','utils','services','dropkick','slimScroll','metisMenu', 'modals', 'ztree'],function (val, utils, services, dropkick, slimScroll, metisMenu, modals, ztree) {

    var menuTemplate = {
        first : '<li><a>{name}<span class="fa arrow"></span></a></li>'
        , group : '<ul class="nav nav-second-level"></ul>'
        , second : '<li data-url="{url}" for="main" alt="{name}" title="{name}" id="{code}_li"><a>{name}</a></li>'
    };
    init();


    /**
     * 加载依赖信息 - 页面初始化时调用
     */
    function loadDependency() {
        //1-加载模版页面
        return $.Promise.all([
            utils.loadPage({ url : 'modals.html', div : $('#modals')}),
            utils.loadPage({ url : 'profile.html', div : $('#profile')}),
            utils.loadPage({url : 'head.html', div : $('#header')})
        ])

        .then(function () {
            //2-加载登陆信息
            utils.initTheme();
            return utils.loadAccountInfo();
        })
        .catch(function (reason) {
            layer.msg(reason.caught ? reason.message : '获取个人信息失败！');
        });
    }

    function loadMenus() {
        var $menusHolder = $('#side-menu');
        services.menus.current()
            .then(function (ret) {
                $menusHolder.empty();

                $.each(ret.menus, function (index, item) {
                    var $first = $(menuTemplate.first.replace(new RegExp('{name}','g'), item.name));
                    if(item.children){
                        var $seconds = $(menuTemplate.group);
                        $.each(item.children, function (idx, itm) {
                            $seconds.append(menuTemplate.second
                                .replace(new RegExp('{url}','g'), itm.uri)
                                .replace(new RegExp('{name}','g'), itm.name)
                                .replace(new RegExp('{code}', 'g'), itm.code));
                        });
                        $first.append($seconds);
                    }
                    $menusHolder.append($first);
                });

                $menusHolder.metisMenu({utils : utils});
                //导航收起与展开
                $('.sidebar-menus-icon').unbind('click').click(function () {
                    $("body").toggleClass("mini-navbar");
                    if (!$('body').hasClass('mini-navbar') || $('body').hasClass('body-small')) {
                        $('#side-menu').hide();
                        setTimeout(
                            function () {
                                $('#side-menu').fadeIn(500);
                            }, 100);
                    } else if ($('body').hasClass('fixed-sidebar')){
                        $('#side-menu').hide();
                    } else {
                        $('#side-menu').removeAttr('style');
                    }
                    return false;
                });

                $('.sidebar-collapse').slimScroll({
                    position:'left',
                    height: '100%',
                    railOpacity: 0.9
                });
                $('.erp-main').slimScroll({
                    height: '100%',
                    railOpacity: 0.9
                });

                layer.closeAll();
                return ret.menus;
            })
            .catch(function (reason) {
                layer.msg(reason.caught ? reason.message : '初始化菜单失败！');
            });
    }

    function init(){
        loadDependency()
            .then(function () {
                loadMenus();
            });
    }
});
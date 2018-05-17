requirejs.config({
    baseUrl: '/js',
    urlArgs: "v256",
    waitSeconds: 15,
});

requirejs(['validate', 'utils', 'services'],function (val, utils, services) {

    var username_tips = $('.username_inputTips');
    var pwd_tips = $('.pwd_inputTips');

    var $loginName = $('input[name="username"]');
    var $password = $('input[name="password"]');

    //验证账号
    var vLoginName = function () {
        if (!$loginName.val().trim()){
            username_tips.html('<i class="fa fa-times-circle"></i> 用户名不能为空');
            return !!0;
        }
        username_tips.html('');
        return !!1;
    };

    //验证密码
    function vPassword() {
        if($password.val().trim() === ''){
            pwd_tips.html('<i class="fa fa-times-circle"></i> 密码不能为空');
            return !!0;
        }
        pwd_tips.html('');
        return !!1;
    }
    
    $loginName.blur(function () {
        vLoginName();
    });

    $password.blur(function () {
        vPassword();
    });

    //登陆操作
    $('#loginBtn').unbind('click').click(function () {
        if(!vLoginName() || !vPassword())
            return;
        return services.account.login({
            username: $loginName.val().trim(),
            password: $password.val().trim()
        }).then(function (res) {
            console.log(res);
            window.location.href = GLOBALS.ctxPath + 'app.html';
        }).catch(function (reason) {
            layer.msg(reason.caught ? reason.message : '请求失败！');
        });
    });
    
    $.browser.isInternetExplorer();
});
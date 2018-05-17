/**
 * Created by MAJIANGTAO on 2015/11/10.
 */

define('validate',[],function(){
    var phone = /^1[3|4|5|7|8][0-9]\d{4,8}$/;
    var validate_phone = function(value){
        return phone.test(value.trim());
    }

    //var email_reg = /^[a-z0-9A-Z]+([._\\-]*[a-z0-9A-Z])*@([a-z0-9A-Z]+[-a-z0-9A-Z]*[a-z0-9A-Z]+.){1,63}[a-z0-9A-Z]+$/;
    var email_reg = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
    var validate_email = function(value){
        return email_reg.test(value.trim());
    };

    var pwd_reg = /^(?!\D+$)(?![^a-z]+$)[a-zA-Z\d]{6,20}$/;
    var validate_pwd = function (value) {
        return pwd_reg.test(value);
    };

    return {
        phone : validate_phone,
        email : validate_email,
        pwd : validate_pwd
    }
})
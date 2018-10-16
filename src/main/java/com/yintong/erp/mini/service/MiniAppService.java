package com.yintong.erp.mini.service;

import com.yintong.erp.domain.basis.security.ErpEmployee;
import com.yintong.erp.domain.basis.security.ErpEmployeeRepository;
import com.yintong.erp.mini.domain.WxMiniUser;
import com.yintong.erp.mini.domain.WxMiniUserRepository;
import com.yintong.erp.utils.common.AESUtil;
import com.yintong.erp.utils.common.CommonUtil;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author lucifer.chan
 * @create 2018-08-16 上午11:13
 * 微信小程序
 **/
@Service
public class MiniAppService {

    @Autowired WxMiniUserRepository wxMiniUserRepository;

    @Autowired ErpEmployeeRepository employeeRepository;

    private AESUtil aesUtil = AESUtil.getInstance();

    public static final String UNBIND_OPENID_PREFIX = "unbind_";

    /**
     * 根据用户信息生成token
     * @param params 包含了openid
     * @return 有绑定则返回employeeId[加密], 未绑定则返回 unbind_{openId[加密]}
     */
    public String makeToken(WxMiniUser params) {
        String openId = params.getOpenid();

        WxMiniUser miniUser = wxMiniUserRepository.findByOpenid(openId);
        if(Objects.nonNull(miniUser)){
            params.setCreatedAt(miniUser.getCreatedAt());
            params.setCreatedBy(miniUser.getCreatedBy());
            params.setCreatedName(miniUser.getCreatedName());
        }
        wxMiniUserRepository.save(params);
        ErpEmployee employee = CommonUtil.single(employeeRepository.findByOpenId(openId));

        return Objects.isNull(employee) ?
                (UNBIND_OPENID_PREFIX + aesUtil.encrypt(openId)) : aesUtil.encrypt(employee.getId().toString());

    }

    /**
     * 登录
     * @param encryptedOpenId
     * @param loginName
     * @param password
     * @return
     */
    public String login(String encryptedOpenId, String loginName, String password) {
        ErpEmployee employee = CommonUtil.single(employeeRepository.findByLoginName(loginName));
        Assert.notNull(employee, "用户名\"" + loginName + "\"不存在");
        Assert.isTrue(new BCryptPasswordEncoder().matches(password, employee.getPassword()), "密码错误");
        String openId = decryptOpenId(encryptedOpenId);
        Assert.notNull(openId, "openId解密失败");
        Assert.isTrue(StringUtils.isEmpty(employee.getOpenId()) || openId.equals(employee.getOpenId()), loginName + "已被其他人绑定！");
        employee.setOpenId(openId);
        employeeRepository.save(employee);
        return aesUtil.encrypt(employee.getId().toString());
    }

    /**
     * 解密openId
     * @param encryptedOpenId
     * @return
     */
    private String decryptOpenId(String encryptedOpenId){
        String encrypt = encryptedOpenId.substring(UNBIND_OPENID_PREFIX.length(), encryptedOpenId.length());
        try{
            return aesUtil.decrypt(encrypt);
        } catch (Exception e){
            return null;
        }
    }
}

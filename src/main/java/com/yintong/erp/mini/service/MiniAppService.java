package com.yintong.erp.mini.service;

import com.yintong.erp.domain.basis.associator.ErpEmployeeMiniRole;
import com.yintong.erp.domain.basis.associator.ErpEmployeeMiniRoleRepository;
import com.yintong.erp.domain.basis.security.ErpEmployee;
import com.yintong.erp.domain.basis.security.ErpEmployeeMenuRepository;
import com.yintong.erp.domain.basis.security.ErpEmployeeRepository;
import com.yintong.erp.domain.basis.security.ErpMiniRole;
import com.yintong.erp.domain.basis.security.ErpMiniRoleRepository;
import com.yintong.erp.mini.domain.WxMiniUser;
import com.yintong.erp.mini.domain.WxMiniUserRepository;
import com.yintong.erp.service.basis.MenuService;
import com.yintong.erp.utils.common.AESUtil;
import com.yintong.erp.utils.common.CommonUtil;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    @Autowired ErpEmployeeMenuRepository employeeMenuRepository;

    @Autowired ErpMiniRoleRepository miniRoleRepository;

    @Autowired ErpEmployeeMiniRoleRepository erpEmployeeMiniRoleRepository;

    @Autowired MenuService menuService;

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

//        //非管理员时，校验是否有库存权限
//        if(!menuService.isAdmin(employee.getId())){
//            Set<String> roleGroups = employeeMenuRepository.findByEmployeeId(employee.getId())
//                    .stream()
//                    .map(ErpEmployeeMenu::getMenuCode)
//                    .map(code -> code.substring(0,2))
//                    .collect(Collectors.toSet());
//            Assert.isTrue(roleGroups.contains(STOCK_ROLE_GROUP), "您没有库存管理的权限！");
//        }

        employee.setOpenId(openId);
        employeeRepository.save(employee);
        return aesUtil.encrypt(employee.getId().toString());
    }

    /**
     * 角色code & name
     * @param employeeId
     * @return
     */
    public Map<String, String> miniRoles(Long employeeId){

        if(menuService.isAdmin(employeeId)){
            return miniRoleRepository.findAll().stream()
                    .collect(Collectors.toMap(ErpMiniRole::getCode, ErpMiniRole::getName));
        }

        return erpEmployeeMiniRoleRepository.findByUserId(employeeId).stream()
                .map(ErpEmployeeMiniRole::getMiniRoleCode)
                .collect(Collectors.toMap(Function.identity()
                        , it -> miniRoleRepository.findById(it).map(ErpMiniRole::getName).orElse(""))
                );
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

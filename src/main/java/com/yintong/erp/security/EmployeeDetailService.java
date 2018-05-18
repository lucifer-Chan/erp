package com.yintong.erp.security;

import com.yintong.erp.domain.basis.security.*;
import com.yintong.erp.utils.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午3:53
 **/
public class EmployeeDetailService implements UserDetailsService, Constants.Roles {

    @Autowired ErpEmployeeRepository employeeRepository;

    @Autowired ErpMenuRepository menuRepository;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        List<ErpEmployee> employees = employeeRepository.findByLoginName(name);
        if (employees.isEmpty())
            throw new RuntimeException("用户名\"" + name + "\"不存在");
        return new EmployeeDetails(employees.get(0));
    }

    /**
     * 加载权限表中的所有权限
     * @return
     */
    public List<Map.Entry<String, String []>> matches(){
        Map<String, String []> ret =  menuRepository.findAll().stream()
                .filter(menu->StringUtils.hasLength(menu.getMatches()))
                .collect(
                        Collectors.toMap(ErpMenu::getCode, menu -> menu.getMatches().split(","))
                );


        ret.putIfAbsent(PROFILE_ROLE_CODE, StringUtils.tokenizeToStringArray(PROFILE_ROLE_MATCHES, ","));

        return ret.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).collect(Collectors.toList());

    }
}

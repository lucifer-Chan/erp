package com.yintong.erp.security;

import com.yintong.erp.utils.base.BaseResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午3:53
 * 权限认证成功的处理
 **/
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        BaseResult ret = new BaseResult();
        Object employeeDetail = authentication.getPrincipal();
        if(employeeDetail instanceof EmployeeDetails){
            ret.addPojo(((EmployeeDetails) employeeDetail).getEmployee().exclude("id", "password", "createdAt", "createdBy", "isDel", "lastUpdatedAt"));
        }

        response.getWriter().write(ret.toString());
    }
}
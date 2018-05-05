package com.yintong.erp.security;

import com.yintong.erp.exception.GlobalExceptionHandler;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.yintong.erp.exception.GlobalExceptionHandler.ExtensionStatus.AUTHENTICATION_FAILURE;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午3:53
 * 验证失败的处理
 **/
@Component
public class CustomAuthenticationFailureHandler extends GlobalExceptionHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        if(response.isCommitted()) return;
        Exception ex = e instanceof BadCredentialsException ? new RuntimeException("密码错误") : e;
        BaseResult ret = convertException(request, response, new Body(AUTHENTICATION_FAILURE, ex.getMessage()), AUTHENTICATION_FAILURE.value(), ex);
        response.getWriter().write(ret.toString());
    }
}

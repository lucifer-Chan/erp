package com.yintong.erp.security;

import com.yintong.erp.exception.GlobalExceptionHandler;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午3:53
 * 权限未通过的处理
 **/
@Component
public class CustomAccessDeniedHandler extends GlobalExceptionHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
        if(response.isCommitted()) return;
        HttpStatus status = HttpStatus.FORBIDDEN;
        response.setStatus(status.value());
        BaseResult ret = convertException(request.getParameterMap(), response.getHeaderNames().stream().collect(Collectors.toMap(Function.identity(), response::getHeader)), status, e);
        response.getWriter().write(ret.toString());
    }
}

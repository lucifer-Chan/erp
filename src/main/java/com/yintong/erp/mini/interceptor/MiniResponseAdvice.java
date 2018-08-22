package com.yintong.erp.mini.interceptor;

import com.yintong.erp.utils.base.BaseResult;
import com.yintong.erp.utils.common.SpringUtil;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import static com.yintong.erp.utils.common.SessionUtil.*;

/**
 * @author lucifer.chan
 * @create 2018-08-17 下午11:29
 * 微信小程序的出参包装
 **/
@RestControllerAdvice
public class MiniResponseAdvice implements ResponseBodyAdvice {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return converterType != StringHttpMessageConverter.class;
    }

    /**
     * 将入参的token塞回去
     * @param o
     * @param returnType
     * @param mediaType
     * @param converterType
     * @param request
     * @param response
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object o, MethodParameter returnType
            , MediaType mediaType, Class converterType, ServerHttpRequest request, ServerHttpResponse response) {
        if(!(o instanceof BaseResult)) return o;

        try {
            String token = SpringUtil.getRequest().getParameter(TOKEN_KEY);
            return StringUtils.isEmpty(token) ? o :  ((BaseResult) o).put(TOKEN_KEY, token);
        } catch (NullPointerException e){
            return o;
        }
    }
}


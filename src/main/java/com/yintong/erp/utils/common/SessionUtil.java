package com.yintong.erp.utils.common;

import com.yintong.erp.domain.basis.security.ErpEmployee;
import com.yintong.erp.exception.SessionExpiryException;
import com.yintong.erp.security.EmployeeDetails;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午3:15
 * session辅助类
 **/
public class SessionUtil {
    private static final String KEY_PREFIX = SessionUtil.class.getName()+ "_";

    public static <T> T get(String key){
        return get(null, key);
    }

    public static <T> void set(String key, T value){
        set(null, key, value);
    }

    public static <T> T remove(String key){
        return remove(null, key);
    }

    public static Long getCurrentUserId(){
        ErpEmployee currentUser = getCurrentUser();
        SessionExpiryException.notNull(currentUser, "会话过期！未获取到当前用户");
        return currentUser.getId();
    }

    private static ErpEmployee getCurrentUser(){
        EmployeeDetails employeeDetails = getEmployeeDetails();
        SessionExpiryException.notNull(employeeDetails, "会话过期！未获取到当前用户明细");
        return employeeDetails.getEmployee();
    }

    private static EmployeeDetails getEmployeeDetails(){
        SecurityContext securityContext =
                (SecurityContext)SpringUtil.getRequest().getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        SessionExpiryException.notNull(securityContext, "会话过期！");
        return (EmployeeDetails)securityContext.getAuthentication().getPrincipal();
    }

    @SuppressWarnings(value = "unchecked")
    public static <T> T get(HttpServletRequest request, String key){
        request = request(request);
        if(null == request)
            return null;
        return (T)request.getSession().getAttribute(KEY_PREFIX + key);
    }

    public static <T> void set(HttpServletRequest request, String key, T value){
        request = request(request);
        if(null != request)
            request.getSession().setAttribute(KEY_PREFIX + key, value);
    }

    public static <T> T remove(HttpServletRequest request, String key){
        request = request(request);
        if(null == request)
            return null;
        T t = get(request, key);
        request.getSession().removeAttribute(key);
        return t;
    }

    private static HttpServletRequest request(HttpServletRequest request){
        return null != request ? request : SpringUtil.getRequest();
    }
}

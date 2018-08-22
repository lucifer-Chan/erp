package com.yintong.erp.utils.common;

import com.yintong.erp.domain.basis.security.ErpEmployee;
import com.yintong.erp.domain.basis.security.ErpEmployeeRepository;
import com.yintong.erp.exception.SessionExpiryException;
import com.yintong.erp.security.EmployeeDetails;
import java.util.Objects;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午3:15
 * session辅助类
 **/
public class SessionUtil {
    private static final String KEY_PREFIX = SessionUtil.class.getName()+ "_";

    public static final String TOKEN_KEY = "token";

    private static SimpleCache<EmployeeDetails> employeeDetailsSimpleCache = new SimpleCache<>();

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

    public static ErpEmployee getCurrentUser(){
        EmployeeDetails employeeDetails = getEmployeeDetails();
        SessionExpiryException.notNull(employeeDetails, "会话过期！未获取到当前用户明细");
        return employeeDetails.getEmployee();
    }

    public static EmployeeDetails getEmployeeDetails(){
        EmployeeDetails ret = getEmployeeDetailsByToken();
        if(Objects.nonNull(ret)) return ret;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(Objects.nonNull(principal) && principal instanceof EmployeeDetails){
            return (EmployeeDetails)principal;
        }

        throw new SessionExpiryException("会话过期");
    }

    private static EmployeeDetails getEmployeeDetailsByToken(){
        HttpServletRequest request = SpringUtil.getRequest();
        if(null == request) return null;
        String token = request.getParameter(TOKEN_KEY);
        if(StringUtils.isEmpty(token)) return null;

        return employeeDetailsSimpleCache.getDataFromCache(KEY_PREFIX + token, value -> {
            try{
                Long currentUserId = Long.parseLong(AESUtil.getInstance().decrypt(token));
                ErpEmployee employee = SpringUtil.getBean(ErpEmployeeRepository.class).findById(currentUserId).orElse(null);
                return Objects.isNull(employee) ? null : new EmployeeDetails(employee);
            }catch (Exception e){
                return null;
            }
        });
    }

//    private static EmployeeDetails getEmployeeDetailsByToken(){
//        HttpServletRequest request = SpringUtil.getRequest();
//        if(null == request) return null;
//        String accessToken = request.getParameter(TOKEN_KEY);
//        if(StringUtils.isEmpty(accessToken)) return null;
//
//        EmployeeDetails ret = employeeDetailsSimpleCache.getDataFromCache(KEY_PREFIX + accessToken, value -> {
//            try{
//                Long currentUserId = Long.parseLong(AESUtil.getInstance().decrypt(accessToken));
//                ErpEmployee employee = SpringUtil.getBean(ErpEmployeeRepository.class).findById(currentUserId).orElse(null);
//                return Objects.isNull(employee) ? null : new EmployeeDetails(employee);
//            }catch (Exception e){
//                return null;
//            }
//        });
//
//        if(Objects.nonNull(ret)){
//            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(ret, null, ret.getAuthorities());
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
//
//        return ret;
//    }



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

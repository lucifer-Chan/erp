package com.yintong.erp.utils.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午3:22
 * .
 **/
@Slf4j
public class SpringUtil implements ApplicationContextAware, HandlerInterceptor {
    private static ApplicationContext applicationContext;
    private static ThreadLocal<HttpServletRequest> requestLocal = new ThreadLocal<>();

    @Override
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;

    }

    /**
     * 获取applicationContext
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        if(null == applicationContext) log.error(">>>>>>>>>>>>>applicationContext is null in getApplicationContext");
        return applicationContext;
    }

    /**
     * 通过class获取Bean.
     *
     * @param clazz
     * @param <T>
     *
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        try {
            return getApplicationContext().getBean(clazz);
        } catch(Exception e) {
            log.error("SpringUtils.getBean({}) error: {}", clazz, e.getMessage());
            throw e;
        }
    }

    /**
     * 通过name获取 Bean.
     *
     * @param name
     *
     * @return
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param name
     * @param clazz
     * @param <T>
     *
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }


    /**
     * 获取request
     *
     * @return
     */
    public static HttpServletRequest getRequest() {
        HttpServletRequest request = requestLocal.get();
        if(null == request) {
            log.error(">>>>>>>>>>>>>>request is null in getRequest!!!");
        }
        return request;
    }

    public static void setRequest(HttpServletRequest request) {
        if(null == request) log.error(">>>>>>>>>>>>>request is null in setRequest");
        requestLocal.set(request);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o)  {
        setRequest(request);
        //请求由/m/打头的必须要带参数accessToken=xxx xxx为员工id经过AES加密
        String uri = request.getRequestURI();
        if(uri.startsWith("/m/") && !uri.startsWith("/m/token")){
            SessionUtil.getCurrentUser();
        }
        return true;
    }
}
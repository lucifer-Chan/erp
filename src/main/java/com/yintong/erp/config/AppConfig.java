package com.yintong.erp.config;

import com.yintong.erp.utils.common.SimpleCache;
import com.yintong.erp.utils.common.SpringUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午3:53
 **/
@Configuration
public class AppConfig implements WebMvcConfigurer {
    @Bean
    public SpringUtil springUtil(){
        return new SpringUtil();
    }

    @Bean
    public SimpleCache simpleCache(){
        return new SimpleCache();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(springUtil()).addPathPatterns("/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry){
        registry.addViewController("/login").setViewName("login.html");
    }
}

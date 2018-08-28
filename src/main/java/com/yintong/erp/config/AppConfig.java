package com.yintong.erp.config;

import com.yintong.erp.utils.bar.BarCodeProvider;
import com.yintong.erp.utils.common.SimpleCache;
import com.yintong.erp.utils.common.SimpleRemote;
import com.yintong.erp.utils.common.SpringUtil;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.persistence.EntityManagerFactory;

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

    @Bean
    public SimpleRemote simpleRemote(){
        return SimpleRemote.instance();
    }

    @Bean
    public BarCodeProvider barCodeProvider(){
        return new BarCodeProvider();
    }

    @Bean
    public EventListenerRegistry eventListenerRegistry(EntityManagerFactory emf) {
        BarCodeProvider barCodeProvider = barCodeProvider();
        barCodeProvider.collect(emf.getMetamodel().getManagedTypes());
        EventListenerRegistry registry = emf.unwrap(SessionFactoryImpl.class).getServiceRegistry().getService(EventListenerRegistry.class);
        registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(barCodeProvider);
        registry.getEventListenerGroup(EventType.PRE_UPDATE).appendListener(barCodeProvider);
        return registry;
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

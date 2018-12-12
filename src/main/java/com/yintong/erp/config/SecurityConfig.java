package com.yintong.erp.config;

import com.yintong.erp.security.EmployeeDetailService;
import com.yintong.erp.utils.common.Constants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午6:09
 * security的配置
 **/
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter implements Constants.Roles{

    private final AccessDeniedHandler accessDeniedHandler;

    private final AuthenticationSuccessHandler successHandler;

    private final AuthenticationFailureHandler failureHandler;

    private final LogoutSuccessHandler logoutSuccessHandler;

    public SecurityConfig(AccessDeniedHandler accessDeniedHandler
            , AuthenticationSuccessHandler successHandler
            , AuthenticationFailureHandler failureHandler
            , LogoutSuccessHandler logoutSuccessHandler) {
        this.accessDeniedHandler = accessDeniedHandler;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.logoutSuccessHandler = logoutSuccessHandler;
    }

    @Override
    @Order(Ordered.HIGHEST_PRECEDENCE)
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(employeeDetailService())
                .passwordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception{
//        http.csrf().disable();
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry =
                http.csrf().disable()
                        .authorizeRequests()
                            .antMatchers(OPEN_MATCHES.split(",")).permitAll()
                        .and()
                            .formLogin()
                            .loginPage("/login")
                                .failureHandler(failureHandler)
                                .successHandler(successHandler)
                            .permitAll()
                        .and()
                            .logout()
                                .logoutSuccessHandler(logoutSuccessHandler)
                            .permitAll()
                        .and()
                            .authorizeRequests()
                        .antMatchers("/m/**").permitAll()
                        .antMatchers("/", "/login.html").authenticated();
        //根据权限表中的信息制定权限规则
        employeeDetailService().matches().forEach(entry->
                registry.antMatchers(entry.getValue()).hasAnyRole(entry.getKey(), ADMIN_ROLE_CODE)
        );

        registry.anyRequest().authenticated();

        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler);
    }

    /**
     * 静态资源放行
     * @param web
     */
    @Override
    public void configure(WebSecurity web) {
//        web.ignoring().antMatchers("/assets/**", "/favicon.ico");
        web.ignoring().antMatchers(
                "/js/**"
                , "/tpl/**"

                , "/css/**"
                , "/font-awesome/**"
                , "/font/**"
                , "/img/**"
                , "/vendor/**"

                , "/favicon.ico");
    }

    @Bean
    @ConditionalOnMissingBean
    EmployeeDetailService employeeDetailService(){
        return new EmployeeDetailService();
    }
}




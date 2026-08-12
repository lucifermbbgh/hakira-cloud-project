package com.hakira.ledger.auth.config;

import com.hakira.ledger.auth.manager.DBUserManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * @BelongsProject: hakira-ledger-auth
 * @BelongsPackage: com.hakira.ledger.auth.config
 * @Author: hakiraKafka
 * @CreateTime: 2024-01-31  12:31:59
 * @Description: @ConditionalOnClass({EnableWebSecurity.class})当EnableWebSecurity这个类被加载到上下文当中
 * @Description: @EnableWebSecurity这个注解会在springboot中自动加载
 * @Description: EnableWebSecurity类自动加载是由spring-boot-starter-security依赖完成的
 * @Description: @EnableMethodSecurity开启方法安全基于方法授权
 * @Version: 1.0
 */
@Configuration // 配置类注解
//@EnableWebSecurity // 开启SpringSecurity的自定义配置（在Springboot项目中可以省略此注解）
@EnableMethodSecurity // 开启方法安全基于方法授权
public class WebSecurityConfig {
    @Value("${spring.security.user.name}")
    private String username;
    @Value("${spring.security.user.password}")
    private String password;

    /**
     * @description:基于内存的用户信息管理器
     * @author: hakiraKafka
     * @date: 2024/1/31 12:43
     * @return: org.springframework.security.core.userdetails.UserDetailsService
     **/
//    @Bean
    public UserDetailsService userDetailsService() {
        // 创建基于内存的用户信息管理器
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        // 使用manager对象管理UserDetails对象
        manager.createUser(
                // 创建UserDetails对象，用于管理用户名、用户密码、用于角色、用户权限等内容
                User.withDefaultPasswordEncoder()
                        .username(username)
                        .password(password)
                        .roles("USER")
                        .build());
        return manager;
    }

    /**
     * @description: 基于数据库的用户信息管理器
     * @author: hakiraKafka
     * @date: 2024/2/1 12:52
     * @return: org.springframework.security.core.userdetails.UserDetailsService
     **/
//    @Bean
    public UserDetailsService dbUserDetailsService() {
        return new DBUserManager();
    }

    /**
     * @description: SpringSecurity的自定义配置授权过滤器
     * @author: hakiraKafka
     * @date: 2024/2/1 17:25
     * @param: httpSecurity
     * @return: org.springframework.security.web.SecurityFilterChain
     **/
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // 用户授权配置
        httpSecurity.authorizeRequests(
                authorize -> authorize
//                        .requestMatchers("/user/getList").hasAuthority("USER_QUERY")// 添加权限-资源访问授权
//                        .requestMatchers("/user/register").hasAuthority("USER_ADD")// 添加权限-资源访问授权
//                        .requestMatchers("/user/**").hasRole("ADMIN")// 添加权限-资源访问授权
                        .anyRequest()// 对所有请求开启授权保护
                        .authenticated()// 需要身份认证才能访问，已认证的请求会被自动授权
        );

        // 用户登录配置
        httpSecurity.formLogin(form -> {// 前面的anyRequest()表示对所有请求开启授权保护，会让自定义登录页重定向到默认登录页，然后默认登录页重定向到自定义登录页，形成重定向循环
            form.loginPage("/login").permitAll().loginPage("/oauthGithub").permitAll()
                    .usernameParameter("username")// 配置自定义的表单提交的用户名参数名，默认为username，即请求体中传到后端的参数名
                    .passwordParameter("password")// 配置自定义的表单提交的密码参数名，默认为password，即请求体中传到后端的参数名
                    .failureUrl("/login?error")// 登录失败时重定向的地址，默认为error
                    .successHandler(new MyAuthenticationSuccessHandler())// 登录成功认证后的处理逻辑，默认是AuthenticationSuccessHandler接口中的success方法处理，这里通过自定义的MyAuthenticationSuccessHandler类实现接口重写success方法处理
                    .failureHandler(new MyAuthenticationFailureHandler())// 登录失败认证后的处理逻辑，默认是AuthenticationFailureHandler接口中的failure方法处理，这里通过自定义的MyAuthenticationFailureHandler类实现接口重写failure方法处理
            ;// permitAll()表示允许所有人访问登录页，防止重定向循环
        });// 使用表单授权方式，不使用默认登录页配置，使用自定义的登录页
//                .formLogin(withDefaults());// 使用表单授权方式
//                .httpBasic(withDefaults());// 使用基本授权方式

        // 用户登出配置
        httpSecurity.logout(logout -> {
            logout.logoutSuccessHandler(new MyLogoutSuccessHandler());// 用户登出账号成功后的处理逻辑，默认是LogoutSuccessHandler接口中的onLogoutSuccess方法处理，这里通过自定义的MyLogoutSuccessHandler类实现接口重写onLogoutSuccess方法处理
        });

        // 用户未认证时的异常处理
        httpSecurity.exceptionHandling(exception -> {
            exception.authenticationEntryPoint(new MyAuthenticationEntryPoint());// 请求未认证的情况下，自定义的异常处理逻辑，默认是AuthenticationEntryPoint接口中的commence方法处理，这里通过自定义的MyAuthenticationEntryPoint类实现接口重写commence方法处理
            exception.accessDeniedHandler(new MyAccessDeniedHandler());// 请求未授权的情况下，自定义的异常处理逻辑，默认是AccessDeniedHandler接口中的handle方法处理，这里通过自定义的MyAccessDeniedHandler类实现接口重写handle方法处理
        });

        // 跨域配置
        httpSecurity.cors(withDefaults());

        // session管理，对于同一用户在多个地方同时登录时，会将其他地方的登录状态关闭
        httpSecurity.sessionManagement(session -> {
            session.maximumSessions(1).expiredSessionStrategy(new MySessionInformationExpiredStrategy());
        });

        // 关闭csrf攻击防御
//        httpSecurity.csrf().disable();
        httpSecurity.csrf(csrf -> csrf.disable());
        return httpSecurity.build();
    }
}

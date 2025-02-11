package com.shark.aio.base.interceptor;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author lbx
 * @date 2023/5/30 - 14:53
 **/
@Configuration
@ComponentScan(basePackages = "com.shark.aio")
public class WebConfig  implements WebMvcConfigurer {

    /**
     * 配置拦截规则与注入拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println(00000);
        // addPathPattern 添加拦截规则 /** 拦截所有包括静态资源
        // excludePathPattern 排除拦截规则 所以我们需要放开静态资源的拦截
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
//                .excludePathPatterns("/")
//                .excludePathPatterns("/login")
//                .excludePathPatterns("/css/**","/fonts/**","/images/**","/js/**")
        ;
    }






}


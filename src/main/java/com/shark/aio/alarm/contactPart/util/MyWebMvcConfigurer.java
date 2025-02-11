package com.shark.aio.alarm.contactPart.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class MyWebMvcConfigurer extends WebMvcConfigurationSupport{
	/**
	* 配置静态访问资源
	* @param registry
	*/
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/background/**").addResourceLocations("classpath:/static/background/");
		registry.addResourceHandler("/build/**").addResourceLocations("classpath:/static/build/");
		registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
		registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
		registry.addResourceHandler("/json/**").addResourceLocations("classpath:/static/json/");
		registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/static/assets/");
		registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/static/fonts/");
		registry.addResourceHandler("/img/**").addResourceLocations("classpath:/static/img/");
		registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
		registry.addResourceHandler("/template/**").addResourceLocations("classpath:/template/");
		registry.addResourceHandler("/vendors/**").addResourceLocations("classpath:/static/vendors/");
		registry.addResourceHandler("/video/**").addResourceLocations("classpath:/static/video/");
        registry.addResourceHandler("/iconPath/**").addResourceLocations("file:"+ Constants.FILEPATH + Constants.USERS);
		registry.addResourceHandler("/coverPath/**").addResourceLocations("file:"+Constants.IMGROOTPATH+"cover/");
		registry.addResourceHandler("/faceResultPath/**").addResourceLocations("file:"+Constants.FACEIMGOUTPUTPATH);
		registry.addResourceHandler("/carResultPath/**").addResourceLocations("file:"+Constants.CARIMGOUTPUTPATH);
		registry.addResourceHandler("/imgOutput/**").addResourceLocations("file:"+Constants.IMGOUTPUTPATH);
		registry.addResourceHandler("/localVideoPath/**").addResourceLocations("file:"+Constants.LOCALVIDEOCOVERPATH);
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println(Constants.LOCALVIDEOCOVERPATH);
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}

		  /*@Override protected void addInterceptors(InterceptorRegistry registry) {
		  //TODO 自动生成的方法存根
	   InterceptorRegistration registration =
	   registry.addInterceptor(new Interceptor());
	   registration.addPathPatterns("/**");
	   registration.excludePathPatterns("/getPhoneCode","/getEmailCode","/msgConfig","/sendMsgConfig","/css/**","/js/**","/assets/**","/fonts/**","/img/**","/template/**","/iconPath/**"); }*/

}

package com.shark.aio.base.controller;

import com.shark.aio.base.authority.AuthorityEntity;
import com.shark.aio.base.authority.AuthorityMapping;
import com.shark.aio.base.information.InformationController;
import com.shark.aio.data.pollutionData.controller.PollutionController;
import com.shark.aio.user.entity.UserEntity;
import com.shark.aio.user.mapper.UserMapping;
import com.shark.aio.alarm.contactPart.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

@Controller
@Slf4j
public class BaseController {

	@Autowired
	private UserMapping userMapping;

	@Autowired
	InformationController informationController;
	@Autowired
	PollutionController pollutionController;
	@Autowired
	AuthorityMapping authorityMapping;

	@Autowired
	FFmpegConfiguration fFmpegConfiguration;

	@RequestMapping("/")
	public String AIO(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		/*
		 * 有无cookie缓存 有：进首页 无：跳转登录页面   key: value;浏览器带过来的
		 */
		Cookie[] cookies = req.getCookies();
		boolean flag = false;
		String userName = null;
		if (cookies != null) {
			for (Cookie ck : cookies) {
				if (ck.getName().equals(Constants.COOKIEHEAD)) {
					flag = true;
					userName =  URLDecoder.decode(ck.getValue(), "UTF-8");
					break;
				}
			}
		}
//
		if (!flag) {
			return Constants.LOGIN;
		}

////		/**
////		 * 根据用户名，查询权限，返回对应首页
////		 */

		try{
			UserEntity userEntity = userMapping.queryUserByUserName(userName);
//			UserEntity userEntity = templogin();
			if(userEntity == null){
				return Constants.LOGIN;
			}
			System.out.println(userEntity.toString());
			AuthorityEntity authorityEntity = authorityMapping.getAuthority(userEntity.getPostId());

			req.getSession().setAttribute("userEntity",userEntity);
			req.getSession().setAttribute("authorityEntity",authorityEntity);

			req.getSession().setAttribute("ffmpegEnabled", fFmpegConfiguration.getFfmpegEnabled());
			//TODO 路径
			System.out.println(Constants.USERS);
			req.getSession().setAttribute("userPath",Constants.FILEPATH+Constants.USERS);

			req.getSession().setMaxInactiveInterval(0);
			log.info("用户：" + userEntity.getUserName() + "登陆系统");
		}catch (Exception e){
			return Constants.LOGIN;
		}

		return informationController.indexWeb(req);

	}

	UserEntity templogin(){
		UserEntity entity = new UserEntity();
		entity.setUserName("admin");
		entity.setPassword("DCD70FE926C73EDDCBBD7792485023D");
		entity.setPhone("12312312312");
		entity.setEmail("lush@163.com");
		entity.setGender(2);
		entity.setIcon("admin/d6b8da4fbdcdd7aa726d72e537c75a7.jpg");
		entity.setNumber("dfy1234");
		entity.setPostId(1);
		entity.setPostName("超级管理员");
		return entity;
	}

}

package com.shark.aio.user.controller;

import com.aliyuncs.exceptions.ClientException;
import com.shark.aio.alarm.contactPart.util.*;
import com.shark.aio.base.authority.AuthorityEntity;
import com.shark.aio.base.authority.AuthorityMapping;
import com.shark.aio.base.information.InformationController;
import com.shark.aio.data.pollutionData.controller.PollutionController;
import com.shark.aio.user.entity.UserEntity;
import com.shark.aio.user.mapper.UserMapping;
import com.shark.aio.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
public class UserController {

	@Autowired
	protected UserService userService;
	@Autowired
	protected UserMapping userMapping;
	@Autowired
	protected InformationController informationController;
	@Autowired
	AuthorityMapping authorityMapping;
	@Autowired
	PollutionController pollutionController;


	/*
	 * 20220908-thg，获得手机验证码
	 */
	@RequestMapping(value = "/getPhoneCode" , method = RequestMethod.POST , produces = "text/html; charset=UTF-8")
	@ResponseBody
	public String getPhoneCode(String phone,HttpServletRequest req, HttpServletResponse response, HttpSession session) {
		/*
		 * response.setHeader("Content-type", "text/html;charset=UTF-8");
		 * response.setCharacterEncoding("UTF-8");
		 */

		try {
			if(ConfPhone.confPhone(phone)) {
				String code = VerCodeGenerateUtil.generateNumberCode();
				session.setMaxInactiveInterval(300);
				session.setAttribute("phone", phone);
				session.setAttribute("phonecode", code);
				
				SendSms.SendMessageCode(code, phone);
				log.info("已向手机号："+phone+"发送短信");
				return "发送验证码成功";
			}
			return "手机号不正确，请检查您输入的手机号。";
		} catch (ClientException e) {
			log.error("发送验证码失败");
			log.error("ErrCode:" + e.getErrCode());
			log.error("ErrMsg:" + e.getErrMsg());
			log.error("RequestId:" + e.getRequestId());
		}
		return "发送验证码失败";
	}
	
	/*
	 * 用户-密码注册
	 */
	@RequestMapping("/signup")
	public String signup(UserEntity userEntity, String code, HttpSession session, 
			@RequestParam(value = "file", required = false) List<MultipartFile> file, HttpServletRequest req) {

		//20220908-thg,加入手机号正确验证
		System.out.println(userEntity.toString());
		if(!ConfPhone.confPhone(userEntity.getPhone())) {
			log.info("手机号错误");
			req.setAttribute(Constants.ERROR, "手机号错误,请输入正确的手机号");
			return regist(req);
		}
		//20220908-thg,验证码
		String phone = (String) session.getAttribute("phone");
		if(phone==null){
			log.info("请先获取验证码");
			req.setAttribute(Constants.ERROR, "请先获取验证码");
			return regist(req);
		}
		if(!userEntity.getPhone().equals(phone)) {
			log.info("用户中途更换手机号码");
			req.setAttribute(Constants.ERROR, "请勿中途更换手机号码");
			return regist(req);
		}
		if(!code.equals(session.getAttribute("phonecode"))) {
			log.info("验证码错误或过期");
			req.setAttribute(Constants.ERROR, "验证码错误或过期");
			return regist(req);
		}
		userEntity.setPostId(5);
		userEntity.setPostName("待管理员审核");
		//20220908-thg,注册自动设置部门和邮箱
//		userEntity.setDepartmentId(5);
//		userEntity.setDepartmentName("未分配部门");
		userEntity.setEmail("未绑定邮箱");
		// 判断新用户(用户名-密码表)
		String isNewUser = userService.isNewUser(userEntity, req);
		System.out.println(isNewUser);
		if (isNewUser.equals(Constants.SUCCESSCODE)) {
			System.out.println("进入循环");
			String addResult = userService.addUser(userEntity, file, req);
			System.out.println(userEntity);
			System.out.println(file);
			System.out.println(req);
			System.out.println(addResult);
			if (addResult.equals(Constants.FAILCODE)) {
				System.out.println("注册失败");
				log.error(userEntity.getUserName() +"注册失败!");
//				return Constants.SIGNUP;
				return "redirect:/regist"; //qh 20220412
			}
			System.out.println("注册陈工");
			log.info("注册成功");
			return Constants.LOGIN;// 注册成功返回登录页面
		} else if (isNewUser.equals(Constants.ERROR)) {
			// 查询失败
			System.out.println("查询失败");
			log.error("UserController/signup, 用户信息查询失败!");
			return Constants.ERROR;
		} else {
			// 查询存在的,返回已经注册过
			System.out.println("已经注册过");
			log.info(userEntity.getUserName() +"已经注册过，请返回登录!");
			return Constants.LOGIN;
		}
	}

	/*
	 * 用户-密码登录
	 */
	@RequestMapping("/login")
	public String Login(UserEntity userEntity, HttpServletRequest req, HttpServletResponse response) {


		String login = userService.login(userEntity, req, response);
		if (login.equals(Constants.SUCCESSCODE)) {
			// 登录成功
			log.info("用户" + userEntity.getUserName() + "登录成功");
			System.out.println(userEntity.toString());
			//判断权限
			String userName = userEntity.getUserName();
			UserEntity user = userMapping.queryUserByUserName(userName);
			AuthorityEntity authorityEntity = authorityMapping.getAuthority(user.getPostId());

			req.getSession().setAttribute("userEntity",user);
			req.getSession().setAttribute("authorityEntity",authorityEntity);
			req.getSession().setAttribute("userPath",Constants.FILEPATH + Constants.USERS);
			req.getSession().setMaxInactiveInterval(0);
			return informationController.indexWeb(req);
		} else if (login.equals(Constants.ERROR)) {
			// 登录失败
			req.setAttribute(Constants.INFORMATION, Constants.LOGINERROE);
			log.error("用户" + userEntity.getUserName() + "登录失败");
			// 返回登录页面，缓存刚才登录的账户
			req.setAttribute(Constants.USERNAME, userEntity.getUserName());
			return Constants.LOGIN;// 用户名或密码错误
		} else {
			// 请先注册
			log.error("账号不存在，请先注册");
			req.setAttribute(Constants.INFORMATION, Constants.SIGNFIRST);
			return Constants.LOGIN;// 需要注册
		}
	}

	/*
	 * 登录页跳转到注册页
	 */
	@RequestMapping("/regist")
	public String regist(HttpServletRequest req) {
		// 将注册页面的部门、岗位两个选项的下拉框动态给前端
	//		List<DepartmentEntity> allDepart = userService.getAllDepart();
	//		List<PostEntity> allPost = userService.getAllPost();
//		req.setAttribute(Constants.DEPARTMENT, allDepart);
//		req.setAttribute(Constants.POST, allPost);
		log.info("进入注册页面");
		ArrayList<String> arrayList = new ArrayList<>();
		arrayList.add("a");
		arrayList.add("b");

		req.setAttribute("msg",arrayList);
		return Constants.SIGNUP;
	}
	
	/**
	 * 个人中心

	 * @param req
	 * @return
	 */
	@RequestMapping("/personalCenter")
	public String personalCenterWeb( HttpServletRequest req, HttpServletResponse response) throws UnsupportedEncodingException {

		Cookie[] cookies = req.getCookies();
		boolean flag = false;
		String userName = null;
		if (cookies != null) {
			for (Cookie ck : cookies) {
				if (ck.getName().equals(Constants.COOKIEHEAD)) {
					flag = true;
					userName = URLDecoder.decode(ck.getValue(), "UTF-8");
					log.info("BaseController, 本次登录用户:{}", ck.getValue());
					break;
				}
			}
		}
		if (!flag) {
			return Constants.LOGIN;
		}
		UserEntity userEntity = userMapping.queryUserByUserName(userName);
		req.getSession().setAttribute("userEntity",userEntity);
		// 在userEntity里面填充postid-->postName, departid --> departname;
//		userEntity.setDepartmentName(departmentMapping.getNameById(userEntity.getDepartmentId()));
//		userEntity.setPostName(postMapping.getNameById(userEntity.getPostId()));


//		头像
//		 ServletOutputStream outputStream = null;
//		    try {
//		    	byte[] bytes = null;
//		    	Path path =Path.of(userEntity.getIcon());
//		    	bytes = Files.readAllBytes(path);
//
//		        outputStream = response.getOutputStream();
//		        outputStream.write(bytes);
//		        outputStream.flush();
//		    } catch (IOException e) {
//		        e.printStackTrace();
//		        if (outputStream != null) {
//		            outputStream.close();
//		        }
//		    }
		req.setAttribute("INFORMATION", userEntity);
		return Constants.PERSONALCENTER;
	}
	
/**
 * 	修改头像
 */
	@RequestMapping("/iconUpdate")
	public String iconUpdate(String username, @RequestParam(value = "file") MultipartFile file,
			HttpServletRequest req, HttpServletResponse resp) throws IOException {

			userService.iconUpdate(username, file, resp, req);

			return personalCenterWeb( req, resp);

	}
	
	
	/**
	 * 退出登录
	 * @param resp
	 * @return
	 */
	@RequestMapping("/loginout")
	public String loginout(HttpServletRequest req, HttpServletResponse resp) {
		// 清除Cookie信息
		Cookie cookie = new Cookie(Constants.COOKIEHEAD, "");
		cookie.setMaxAge(0);
		resp.addCookie(cookie);
		req.getSession().invalidate();
		return "redirect:/";
	}
	
	/**
	 * 修改密码
	 * @param id
	 * @param newpwd
	 * @return
	 */
	public String fixpwd(int id, String newpwd) {
		// id -> userentity
		// userentity 更新pwd字段
		// update数据库
		return Constants.LOGIN;
	}
	
	
//	@RequestMapping("/delete")
//	public String delete(HttpServletRequest req) {
//		// 将注册页面的部门、岗位两个选项的下拉框动态给前端
//		userMapping.deleteUserByUserName("qh-02");
//		return Constants.SUCCESS;
//	}

	//20220915-thg,修改手机号
		/**
		 * 
		 * @param req
		 * @param response
		 * @param username
		 * @param
		 * @param code
		 * @param
		 * @param postId
		 * @param session
		 * @return
		 */
		@RequestMapping("/updatePhone")
//		public String updatePhone(HttpServletRequest req, HttpServletResponse response, String username, String phone,String code, CompanyNameEntity companyEntity, String postId, HttpSession session) {
		public String updatePhone(HttpServletRequest req, HttpServletResponse response, String username, String phone,String code, String postId, HttpSession session) throws UnsupportedEncodingException {

//			req.setAttribute(Constants.COMPANYNAMEENTITY, companyEntity);
			req.setAttribute(Constants.POSTID, postId);
			String updateEmail = userService.updatePhone(username, phone, code, session,req);
			if(updateEmail.equals(Constants.FAILCODE)) {
				log.info("UserController/updateEmail:修改手机失败");
//				return updatePhoneWeb(req, username, phone,companyEntity, postId);
				return updatePhoneWeb(req, username, phone, postId);
			}
			else {
				log.info("UserController/updateEmail:修改手机成功");
				req.setAttribute("msg", "修改手机成功");
				return personalCenterWeb( req, response);
			}
			
		}
		
	

	//20220915-thg,修改手机页面
	/**
	 * 
	 * @param req
	 * @param username
	 * @param
	 * @param postId
	 * @return
	 */
	@RequestMapping("/updatePhoneWeb")
//	public String updatePhoneWeb(HttpServletRequest req, String username,String phone, CompanyNameEntity companyEntity, String postId) {
	public String updatePhoneWeb(HttpServletRequest req, String username,String phone, String postId) {

//		req.setAttribute(Constants.COMPANYNAMEENTITY, companyEntity);
		req.setAttribute(Constants.POSTID, postId);
		req.setAttribute("userName", username);
		req.setAttribute("newPhone", phone);
		return Constants.UPDATEPHONE;
	}
	
	
	//20220915-thg,修改密码验证页面
	@RequestMapping("updatePwdWeb1")
	public String updatePwdWeb1(HttpServletRequest req) {

		return Constants.UPDATEPWD1;
	}
	
	@RequestMapping("updatePwdWeb2")
	public String updatePwdWeb2(HttpServletRequest req, String userName) {
		try {
			if(userMapping.queryUserByUserName(userName)==null) {
				req.setAttribute("msg", "查询不到该用户名，请确认用户名是否有误。");
				return Constants.UPDATEPWD1;
			}
			String phone = userMapping.getPhoneByUserName(userName);
			String email = userMapping.getEmailByUserName(userName);
//			req.setAttribute("phone", phone);
//			req.setAttribute("email", email);
			req.setAttribute("userName", userName);
			log.info("查询手机和邮箱成功");
			return Constants.UPDATEPWD2;
		}catch (Exception e) {
			// TODO: handle exception
			log.info("UserController.updatePwdWeb2:查询手机和邮箱失败",e);
		}
		return Constants.UPDATEPWD1;
	}
	
	//20220915-thg,修改密码验证身份
	@RequestMapping("updatePwdVerify")
	public String updatePwdVerify(String userName, String phone,String code, HttpSession session, HttpServletRequest req) {
		UserEntity userEntity = userMapping.queryUserByUserName(userName);
		if(userEntity == null) {
			req.setAttribute("msg", "查询不到该用户名，请确认用户名是否有误。");
			return Constants.UPDATEPWD1;
		}
		if(!phone .equals(userEntity.getPhone()) ){
			log.info(userName+"未使用注册手机进行密码更改");
			req.setAttribute("msg", "未使用注册手机进行密码更改！");
			return Constants.UPDATEPWD1;
		}
		if(code!=null) {
			log.info(userName+"正在使用手机验证修改密码");
			if(code.equals(session.getAttribute("phonecode"))) {

				req.setAttribute("userName", userName);
				log.info(userName+":手机验证码正确");
				session.setAttribute("userName",userName);
				return Constants.UPDATEPWD2;
			}
			else {
				log.info(userName+":手机验证码错误");
				req.setAttribute("msg", "验证码错误或过期！");
				return updatePwdWeb2(req, userName);
			}
		}
		else {
			log.info(userName+":验证码为空，无法验证");
			req.setAttribute("msg", "您的验证码为空！");
			return updatePwdWeb2(req, userName);
		}
	}
	
	//20220916-thg,修改密码
	@RequestMapping("/updatePwd")
	public String updatePwd(String password, HttpServletRequest req,HttpSession session) {
		String userName = (String)session.getAttribute("userName");
		String flag = userService.updatePwd(userName, password);
		if(flag.equals(Constants.FAILCODE)) {
			log.info("userController/updatePwd:修改密码失败");
			req.setAttribute("msg", "修改密码失败，请重试");
			return updatePwdWeb1(req);
		}
		else {
			log.info("userController/updatePwd:修改密码成功");
			req.setAttribute("msg", "密码修改成功，请返回登录");
			return Constants.LOGIN;
		}
	}

}

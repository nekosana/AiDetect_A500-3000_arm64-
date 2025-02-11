package com.shark.aio.base.interceptor;

import com.shark.aio.alarm.contactPart.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author lbx
 * @date 2023/5/30 - 14:53
 **/
@Slf4j
@Component
/**
 * 用于拦截有些页面只有在登录的情况下才可以访问
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println(1111111);


        // 登录拦截 需要在执行之前
        HttpSession session = request.getSession();
        Object loginUser = session.getAttribute("userEntity");
        System.out.println(loginUser);
        Cookie[] cookies = request.getCookies();
        System.out.println(cookies);
        boolean flag = false;
        if (cookies != null) {
            for (Cookie ck : cookies) {
                if (ck.getName().equals(Constants.COOKIEHEAD)) {
                    System.out.println("11111" + ck.getName());
                    flag = true;
                    break;
                }
            }
        }
        System.out.println(flag);
        if (loginUser != null && flag) {
            return true; // 放行
        }
        // 阻止，并重定向到登录页
        request.setAttribute("msg", "请先登录");
        request.getRequestDispatcher("/login").forward(request,response);

        // logback日志输出获取拦截的URI
        log.info("拦截的请求路径是{}"+request.getRequestURI());
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {}
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {}
}

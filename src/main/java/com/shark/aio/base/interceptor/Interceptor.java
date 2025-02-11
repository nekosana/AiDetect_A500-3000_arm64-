//package com.shark.aio.base.interceptor;
//
//import com.shark.aio.util.Constants;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
///**
// * @author lbx
// * @date 2023/5/30 - 15:30
// **/
//public class Interceptor implements HandlerInterceptor {
//
//    private static List<String> macAddressStrings = new ArrayList<>();
//    private static Set<String> URISet = new HashSet<>();
//    static {
////        macAddressStrings.add("00-BE-43-BB-F8-52");
////        macAddressStrings.add("00-62-0B-4A-D6-15");//中拓服务器的MAC地址
//        URISet.add("/");
//        URISet.add("/regist");
//        URISet.add("/signup");
//        URISet.add("/404");
//        URISet.add("");
//        URISet.add("/updatePwd");
//        URISet.add("/updatePwdWeb1");
//        URISet.add("/updatePwdWeb2");
//        URISet.add("/updatePwdVerify");
//
//
//    }
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
//            throws Exception {
//
//
////        try {
////            String localMacAddress = LocalMacUtil.getLocalMac();
////            if(!macAddressStrings.contains(localMacAddress)) {
////                return false;
////            }
////        } catch (UnknownHostException e) {
////            e.printStackTrace();
////            throw new RuntimeException("获取本机Mac地址失败");
////        }
//
//        //验证阿里云短信是否配置
//        String URI = request.getRequestURI();
//
////        if(SendSms.sendMsgMap.isEmpty()) {
////            System.out.println(request.getRequestURI()+":sendMsgMap is empty");
////            request.getRequestDispatcher("/msgConfig").forward(request, response);
////            return false;
////
////        }
//
//        if(URISet.contains(URI)) return true;
//        //验证是否通过url暴力访问
//        if(request.getCookies()!=null) {
//            for(Cookie cookie : request.getCookies()) {
//                if(cookie.getName().equals(Constants.COOKIEHEAD)) {
//                    return true;
//                }
//            }
//        }
//        request.getRequestDispatcher("/404").forward(request, response);
//        return false;
//
//    }
//}

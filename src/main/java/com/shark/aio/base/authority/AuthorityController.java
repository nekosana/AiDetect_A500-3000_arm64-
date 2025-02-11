package com.shark.aio.base.authority;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lbx
 * @date 2023/4/2 - 15:12
 **/
@Slf4j
@Controller
@MapperScan(value = "com.shark.aio.base.authority")
public class AuthorityController {

    @Autowired
    AuthorityMapping authorityMapping;

    @RequestMapping("/authorityManagement")
    public String authority(HttpServletRequest req){
        try{
            AuthorityEntity park = authorityMapping.getPark();
            AuthorityEntity free = authorityMapping.getFree();
            AuthorityEntity company = authorityMapping.getCompany();
            req.setAttribute("park", park);
            req.setAttribute("free", free);
            req.setAttribute("company", company);
            log.info("进入权限管理页面成功！");
            return "authorityManagement";
        }catch (Exception e){
            req.setAttribute("msg", "进入权限管理页面失败！");
            log.error("进入权限管理页面失败！",e);
            return "index";
        }
    }
    @RequestMapping("/authorityUpdate")
    public String authorityUpdate(HttpServletRequest req, AuthorityEntity object){
        try {
            authorityMapping.updateAuthority(object );
            log.info("修改权限管理成功！");
            return authority(req);
        }catch (Exception e){
            req.setAttribute("msg", "修改权限管理失败！");
            log.error("修改权限管理失败！",e);
            return "index";
        }
    }
}

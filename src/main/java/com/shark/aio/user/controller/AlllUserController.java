package com.shark.aio.user.controller;

import com.shark.aio.user.entity.PostEntity;
import com.shark.aio.user.entity.UserEntity;
import com.shark.aio.user.service.AllUserService;
import com.shark.aio.user.service.UserService;
import com.shark.aio.alarm.contactPart.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lbx
 * @date 2023/3/28 - 14:16
 **/
@Slf4j
@Controller
public class AlllUserController {

    @Autowired
    AllUserService allUserService;
    @Autowired
    UserService userService;
    /*
     * 全部用户信息
     */
    /**
     * 进入全部用户修改表
     *
     * @param
     * @param req
     * @return
     */
    @RequestMapping("/allUserEntity")
    public String allUserEntity(HttpServletRequest req) {
        List<UserEntity> allUser = allUserService.getAllUser();
        List<PostEntity> allPost = userService.getAllPost();
        if (allUser == null | null == allPost ) {
            log.error("BackSysController/allUserEntity, 进入全部用户修改表失败");
            req.setAttribute(Constants.ERROR, "进入全部用户修改表失败");
            return "allUser";
        }
        // 将注册页面的部门、岗位两个选项的下拉框动态给前端

        req.setAttribute(Constants.ALLPOST, allPost);
        req.setAttribute(Constants.ALLUSER, allUser);
        log.info("进入全部用户页面成功");
        return "allUser";
    }

    /**
     * 提交单条用户修改表
     *
     * @param postId
     * @param req
     * @return
     */
    @RequestMapping("/updataOneUserEntity")
    public String updataOneUserEntity(String postId, HttpServletRequest req, UserEntity userEntity, String originUserName) {

        String updataOneUserEntity = allUserService.updataOneUserEntity(userEntity, originUserName);
        if (updataOneUserEntity.equals(Constants.FAILCODE)) {
            log.error("updataOneUserEntity, 提交用户修改表失败！");
            req.setAttribute(Constants.ERROR, "提交用户修改表失败！");
            return allUserEntity( req);
        }
        req.setAttribute(Constants.POSTID, postId);
        req.setAttribute("msg", "修改成功");
        log.info("提交用户修改表成功！");
        return allUserEntity( req);
    }

    @RequestMapping("/queryUserByUserName")
    @ResponseBody
    public UserEntity queryUserByUserName(String originUserName){
        UserEntity userEntity = allUserService.queryUserByUserName(originUserName);
        return userEntity;
    }



    /**
     * 删除一条用户记录
     *
     * @param userName——需要删除的用户的用户名
     * @param
     * @param req
     * @param postId
     * @return
     */
    @RequestMapping("/deleteUser")
    public String deleteUser(String userName, HttpServletRequest req, String postId) {

        String deleteUser = allUserService.deleteUser(userName);
        if (deleteUser.equals(Constants.FAILCODE)) {
            log.error(" deleteUser, 用户" + userName + "删除失败");
            req.setAttribute(Constants.ERROR, "用户删除失败");
            return allUserEntity(req);
        }
        log.info("用户" + userName + "删除成功");
        req.setAttribute(Constants.POSTID, postId);
        req.setAttribute("msg", "用户" + userName + "删除成功");
        return allUserEntity(req);
    }
}

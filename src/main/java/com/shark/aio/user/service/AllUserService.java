package com.shark.aio.user.service;

import com.shark.aio.user.entity.UserEntity;
import com.shark.aio.user.mapper.AllUserMapping;
import com.shark.aio.alarm.contactPart.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lbx
 * @date 2023/3/28 - 14:27
 **/
@Slf4j
@Service
//扫不到这个mapping，换个文件目录就可以，也可以加这一条指向哪个目录
@MapperScan(value = "com.shark.aio.user.mapper")
public class AllUserService {

    @Autowired
    AllUserMapping allUserMapping;
    /**
     * 全部用户信息
     * @return
     */

    /*
     * 全部用户信息
     */
    public List<UserEntity> getAllUser() {
        try {
            List<UserEntity> allUser = allUserMapping.getAll();
            return allUser;
        } catch (Exception e) {
            // TODO: handle exception
            log.error("BackSysService/getAllUser, 获取全部用户信息失败, ", e);
            return null;
        }
    }

    /*
     * 20220826-thg，新注册用户信息
     */
    public String getNewUser(HttpServletRequest req){
        try {
            List<UserEntity> newUser = allUserMapping.getNew();
            req.setAttribute("newUser", newUser);
            return Constants.SUCCESSCODE;
        }catch (Exception e) {
            // TODO: handle exception
            log.error("BackSysService/getNewUser, 获取新用户信息失败",e);
            return Constants.FAILCODE;
        }
    }

    /*
     * 更新一条用户记录
     * 修改密码不包括在这里面
     */
    public String updataOneUserEntity(UserEntity userEntity, String originUserName) {
        if (userEntity == null) {
            return Constants.FAILCODE;
        }
        try {
            userEntity.setPostId(allUserMapping.queryNumberBypostName(userEntity.getPostName()));
            // 修改一条用户记录
            allUserMapping.updateUserById( userEntity, originUserName);;
            return Constants.SUCCESSCODE;
        } catch (Exception e) {
            log.error("BackSysService/updataOneUserEntity, 修改一条用户记录失败, ", e);
            return Constants.FAILCODE;
        }
    }

    /*
     * 删除一条用户记录
     */
    public String deleteUser(String userName) {
        if (userName == null) {
            return Constants.FAILCODE;
        }
        try {
            // 删除一条用户记录
            allUserMapping.deleteUserByUserName(userName);
            return Constants.SUCCESSCODE;
        } catch (Exception e) {
            log.info("BackSysService/deleteUser, 删除一条用户记录失败, ", e);
            return Constants.FAILCODE;
        }
    }

    /**
     * 根据用户名查询用户
     * @param userName
     * @return
     */
    public UserEntity queryUserByUserName(String userName) {
        if (userName == null) {
            return null;
        }
        try {

            return allUserMapping.queryUserByUserName(userName);
        } catch (Exception e) {
            log.error("AllUserService/deleteUser, 查询用户记录失败, 用户名：" +userName, e);
            throw new RuntimeException("查询用户信息失败！");
        }
    }



}

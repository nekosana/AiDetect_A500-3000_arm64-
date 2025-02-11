package com.shark.aio.user.mapper;

import com.shark.aio.user.entity.PostEntity;
import com.shark.aio.user.entity.UserEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author lbx
 * @date 2023/3/28 - 14:28
 **/
@Mapper
public interface AllUserMapping {
    /*
     * 删除用户
     */
    @Delete("delete from `user` where user_name = #{userName}")
    void deleteUserByUserName(String userName);

    /*
     * 全部用户信息
     */
    @Select("select * from `user`;")
    List<UserEntity> getAll();

    /*
     * 20220823-thg,新用户信息
     */
    @Select("SELECT * FROM `user` WHERE `post_id`=16;")
    List<UserEntity> getNew();

    /*
     * 更新用户信息
     */
    @Update("UPDATE `user` SET `phone`=#{user.phone}, `email`=#{user.email} ,`number`=#{user.number}, " +
            "`post_id`=#{user.postId} ,`post_name`=#{user.postName}"+ "WHERE `user_name`=#{originUserName};")
    void updateUserById(@Param("user") UserEntity userEntity,@Param("originUserName") String originUserName);


    @Select("select * from `post`")
    List<PostEntity> getAllPost();

    @Select("select * from `user` where `user_name`=#{userName}")
    public UserEntity queryUserByUserName(String userName);

    @Select("select number from `post` where `name`=#{name}")
    public int queryNumberBypostName(String name);
}

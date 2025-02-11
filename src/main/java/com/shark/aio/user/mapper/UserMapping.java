package com.shark.aio.user.mapper;

import cn.hutool.json.JSONUtil;
import com.shark.aio.user.entity.UserEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface UserMapping {
	/*
	 * 插入一个客户用户记录
	 */

	@Insert("INSERT INTO `user`(`user_name`, `password`, `phone`, `email`, `gender`,`icon`, `number`, `post_id`, `post_name`, `receive_message`) values (#{userName},#{password},#{phone},#{email},#{gender},#{icon},#{number},#{postId},#{postName},#{postName})")
	void insert(UserEntity customerUserEntity);
	
	/*
	 * 通过客户用户名查询用户
	 */
	@Select("SELECT * from `user` WHERE user_name = #{userName};")
	UserEntity queryUserByUserName(@Param("userName") String userName);
	
	/*
	 * 通过客户工号查询用户
	 */
	@Select("SELECT * from `user` WHERE number = #{number};")
	UserEntity queryUserByNumber(@Param("number") String number);
	

	/*
	 * 通过客户手机号查询用户
	 */
	@Select("SELECT * from `user` WHERE phone = #{phone};")
	UserEntity queryUserByPhone(@Param("phone") String phone);
	
	/*
	 * 通过客户邮箱查询用户
	 */
	@Select("SELECT * from `user` WHERE email = #{email};")
	UserEntity queryCuByEmail(@Param("email") String email);
	
	/*
	 * 更新username\passwd\openId
	 */
	@Update("UPDATE `user` SET `user_name`=#{userName}, `password`=#{password}"
			+ "WHERE `phone`=#{phone};")
	void updateUserByPhone(@Param("userName") String userName, @Param("password") String password, 
			 @Param("phone") String phone);

	/*
	 * 通过name查询user表id
	 */
	@Select("SELECT id FROM `user` where `user_name` = #{userName};")
	int getIdByUserName(String userName);
	
	/*
	 * 测试用
	 */
	@Select("select * from `user`;")
	List<UserEntity> getAll();
	
	
	/*
	 * 根据用户名更新头像
	 */
	@Update("UPDATE `user` SET `icon`=#{icon} WHERE `user_name`=#{userName};")
	void updateIconByUserName(@Param("userName") String userName, @Param("icon") String icon);
	
	/*
	 * 删除用户
	 */
	@Delete("delete from `user` where user_name = #{userName}")
	void deleteUserByUserName(String userName);
	
	/*
	 * 20220830-thg,根据用户名修改邮箱
	 */
	@Update("UPDATE `user` SET `email`=#{email} WHERE `user_name`=#{userName};")
	void updateEmailByUserName(@Param("email")String email, @Param("userName")String username);
	
	/*
	 * 20220915-thg,根据用户名修改手机
	 * 
	 */
	@Update("UPDATE `user` SET `phone`=#{phone} WHERE `user_name`=#{userName};")
	void updatePhoneByUserName(@Param("phone")String phone, @Param("userName")String username);
	
	/*
	 * 20220916-thg,根据用户名查询电话号
	 */
	@Select("SELECT `phone` FROM `user` WHERE `user_name`=#{username}")
	String getPhoneByUserName(String username);
	
	/*
	 * 20220916-thg,根据用户名查询邮箱
	 */
	@Select("SELECT `email` FROM `user` WHERE `user_name`=#{username}")
	String getEmailByUserName(String username);
	
	/*
	 * 20220916-thg,根据用户名修改密码
	 */
	@Update("UPDATE `user` SET `password`=#{password} WHERE `user_name`=#{userName};")
	void updatePwdByUserName(@Param("password")String password, @Param("userName")String userName);
}

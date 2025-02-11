package com.shark.aio.user.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
@Setter
@Getter
@ToString
public class UserEntity {
	private int id;
	private String userName;
	private String password;
//	private String openId;
	private String phone;
	private String email;
	private int gender;
	private String icon;
	private String number;
	private int postId;
	private String postName;
//	private int companyId;//公司id
//	private String companyName;//公司名称
//	private int wxId;
	private Timestamp created;
	private Timestamp updated;
}

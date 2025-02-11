package com.shark.aio.alarm.contactPart.util;

import java.util.Optional;

public interface Constants {

	/*
	 * 返回值
	 */
	String SUCCESSCODE = "200";
	String FAILCODE = "500";
	String ERROR = "error";
	
	/*
	 * 用户注册
	 */
	String ALREADY = "already";
	String BINDSUCCESS = "bindsuccess";
	//String FILEPATH = "/Users/lb/Downloads/shark/环保仪/data/";

	String PROJECT = "project/";
	
	/*
	 * 项目
	 */
	String REQUEST = "/request/";
	String SOW = "sow/";
	String QUOTATION = "quotation/";

	
	String[] statuss = new String[] {"项目新建", "项目评审中", "项目签订中", "项目设计中", "项目生产中", "项目装配中", "项目审核不通过", "项目已完成"};
	
	/*
	 * Redis
	 */
	long LoginTime = 3 * 24 * 60 * 60L; // Redis缓存3天
	String OPENID = "OPENID";
	
	/*
	 * cookie
	 */
	String COOKIEHEAD = "cookiehead";
	int COOKIE_TTL = 60 * 60 * 24; //24h
	
	/*
	 * 返回前端
	 */
	String MSG = "msg";
	String ALLMONITORCLASS = "allMonitorClass";
	String ALLMONITOR = "allMonitor";
	String ALLPOLLUTIONNAME = "allPollutionName";
	String INFORMATION ="information";
	String LOGINERROE ="loginerror";
	String SIGNFIRST ="signfirst";
	String USERNAME = "userName";
	String POSTID = "postId";
	String ALLPOST ="allPost";
	String ALLUSER = "allUser";

	/*
	 * 页面
	 */
	String LOGIN = "login";
	String INDEX = "index";
	String SIGNUP = "signup";
	String MAP = "map";
	String TRACE = "trace";
	String ADDPOLLUTION = "addPollution";
	String ALARMSETTINGS = "alarmSettings";
	String ADDALARMSETTING = "addAlarmSetting";
	String EDITALARMSETTING = "editAlarmSetting";
	String ALARMREOCRDS = "alarmRecords";
	String ALARMREOCRDSMEDIUM= "alarmRecordsMedium";
	String ALARMREOCRDSHIGH = "alarmRecordsHigh";
	String SUCCESS = "success";
	String ALLDETAIL ="allDetail";
	String PERSONALCENTER="personalCenter";
	String REASSEMBLE ="reAssemble";
	String WAIT = "wait";

	String UPDATEEMAIL = "updateEmail";
	String UPDATEPHONE = "updatePhone";
	String UPDATEPWD1 = "updatePwd1";//修改密码第一步：输入用户名
	String UPDATEPWD2 = "updatePwd2";//修改密码第二部：输入手机邮箱验证码
	String UPDATEPWD = "updatePwd";//修改密码最后一步：输入新密码




	/**
	 * 摄像头地址
	 */
	String LBXINPUT = "rtsp://admin:lbx123456@192.168.0.8:554";
	String THGINPUT = "rtsp://admin:lbx123456@192.168.0.9:554";

	/**
	 * 数据包名
	 */
	String POLLUTIONDATA = ProcessUtil.IS_WINDOWS?"\\污染源监测.txt":"/污染源监测.txt";
	String CONDITIONDATA = ProcessUtil.IS_WINDOWS?"\\末端监测.txt":"/末端监测.txt";
	String ELECTRICDATA = ProcessUtil.IS_WINDOWS?"\\用电监测.txt":"/用电监测.txt";

	/**
	 * 文件存储根路径
	 */
	//TODO 部署前检查路径是否正确
	String ROOTPATH =  ProcessUtil.IS_WINDOWS?"D:\\项目\\AIO":"/AIO";
//	String ROOTPATH = "/home/thg/Shark-NJU/AIO";//笔记本
	String DATAROOTPATH = ProcessUtil.IS_WINDOWS?ROOTPATH+"\\data\\":ROOTPATH+"/data/";
	String LOGDIR = DATAROOTPATH + (ProcessUtil.IS_WINDOWS?"日志\\":"日志/");
	String IMGROOTPATH = DATAROOTPATH + (ProcessUtil.IS_WINDOWS?"images\\":"images/");
	String VIDEOPATH = DATAROOTPATH + (ProcessUtil.IS_WINDOWS?"video\\":"video/");

	//视频的照片流
	String IMGOUTPUTPATH = ProcessUtil.IS_WINDOWS?IMGROOTPATH+"input\\":IMGROOTPATH+"input/";
	//人员识别结果
	String FACEIMGOUTPUTPATH = ProcessUtil.IS_WINDOWS?IMGROOTPATH+"faceResult\\":IMGROOTPATH+"faceResult/";
	//车牌识别结果
	String CARIMGOUTPUTPATH = ProcessUtil.IS_WINDOWS?IMGROOTPATH+"carResult\\":IMGROOTPATH+"carResult/";
	//工况数据
	String CONDITIONPATH = ProcessUtil.IS_WINDOWS?DATAROOTPATH+"末端监测\\":DATAROOTPATH+"末端监测/";
	//污染源数据
	String POLLUTIONPATH = ProcessUtil.IS_WINDOWS?DATAROOTPATH+"污染源监测\\":DATAROOTPATH+"污染源监测/";
	//用电数据
	String ELECTRICPATH = ProcessUtil.IS_WINDOWS?DATAROOTPATH+"用电监测\\":DATAROOTPATH+"用电监测/";

	String DISKCAPACITYPATH = DATAROOTPATH;

	String FILEPATH = Optional.ofNullable(ROOTPATH).orElse(System.getProperty("user.dir"));
	String USERS = ProcessUtil.IS_WINDOWS?"\\users\\":"/users/";

	//本地视频保存路径
	String LOCALVIDEOPATH = ProcessUtil.IS_WINDOWS? ROOTPATH +"\\localvideo\\": DATAROOTPATH +"localvideo/";

	String LOCALPICPATH = ProcessUtil.IS_WINDOWS? ROOTPATH +"\\localpic\\": DATAROOTPATH +"localpic/";
	//本地图片结果显示log
	String IMAGERSTLOGPATH = ProcessUtil.IS_WINDOWS? ROOTPATH +"\\imagerstlog\\": DATAROOTPATH +"imagerstlog/";
	Integer HASPERSON = 1;


	String WARINGVIDEOPATH = ProcessUtil.IS_WINDOWS? ROOTPATH +"\\waringvideo\\": DATAROOTPATH +"waringvideo/";

	//本地视频封面保存路径
//	String LOCALVIDEOCOVERPATH = ProcessUtil.IS_WINDOWS? ROOTPATH + "\\localpic\\":ROOTPATH + "localpic/";
	String LOCALVIDEOCOVERPATH = ProcessUtil.IS_WINDOWS? ROOTPATH + "\\localpic\\":"/AIO/data/localpic/";    //暂时先把这里linux下的路径写死

	String SERIALNPATH = ProcessUtil.IS_WINDOWS?DATAROOTPATH+"串口监测\\":DATAROOTPATH+"串口监测/";
	String SERIALDATA = ProcessUtil.IS_WINDOWS?"\\串口监测.txt":"/串口监测.txt";

	String CONFIGDEVICEPATH =  ProcessUtil.IS_WINDOWS?"D:\\项目\\AIO\\configdevice\\configdevice.docx":"/AIO/configdevice/configdevice.docx";
	String CONFIGDEVICEPARAMPATH = ProcessUtil.IS_WINDOWS?"D:\\项目\\AIO\\configdevice\\deviceparam":"/AIO/configdevice/deviceparam";
	String CONFIGSERIALDEVICEMPATH = ProcessUtil.IS_WINDOWS?"D:\\项目\\AIO\\configdevice\\serialdevice":"/AIO/configdevice/serialdevice";

	String JedisParameterId = "Parameter";
}

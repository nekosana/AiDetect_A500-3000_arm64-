package com.shark.aio.data.video.controller;

import com.aliyun.apache.hc.core5.http.HttpHeaders;
import com.github.pagehelper.PageInfo;
import com.shark.aio.alarm.contactPart.util.ClientDemo;
import com.shark.aio.base.annotation.Description;
import com.shark.aio.data.video.entity.*;
import com.shark.aio.data.video.service.VideoService;
import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.alarm.contactPart.util.IpUtil;
import com.shark.aio.alarm.contactPart.util.ObjectUtil;
import com.shark.aio.videodetect.mapper.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.Inet4Address;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Controller
@Slf4j
@CrossOrigin
public class VideoController {


	@Autowired
	private VideoService videoService;
	/**
	 * 跳转到摄像头列表页面
	 * @param request request
	 * @param feature 模糊查询特征字符串
	 * @return 页面
	 */
//	@RequestMapping("/videoMonitor")
//	@Description("跳转到摄像头列表页面")
//	public String toVideoPage(HttpServletRequest request,
//							 String feature){
////		如果前端未找到照片，捕获
//		try{
//			List<VideoEntity> videos = videoService.selectAllVideos(feature);
//
//			SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//			boolean pictureExist = true;
//
//			String a = timeFormat.format(new Date());
//			for (VideoEntity v : videos){
//				if (!new File(Constants.IMGOUTPUTPATH + v.getMonitorName() + "/" + a + ".jpg").exists()){
//					pictureExist = false;
//				}
//			}
//			if (!pictureExist){
//				pictureExist = true;
//				//当前时间减11秒,因为10秒一检查视频流是否断了
//				a = timeFormat.format(Date.from(new Date().toInstant().minus(Duration.ofSeconds(12))));
//				for (VideoEntity v : videos){
//					if (!new File(Constants.IMGOUTPUTPATH + v.getMonitorName() + a).exists()){
//						pictureExist = false;
//					}
//				}
//			}
//			if (!pictureExist){
//				a = timeFormat.format(Date.from(new Date().toInstant().minus(Duration.ofSeconds(60))));
//			}
//
//
//			request.setAttribute("time",a);
//			request.setAttribute("class","video");
//			request.setAttribute("allVideos",videos);
//		}catch (Exception e){
//			SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//			String a = timeFormat.format(new Date());
//		}
//
//		return "video";
//	}

	@RequestMapping("/videoMonitor")
	@Description("跳转到摄像头列表页面")
	public String toVideoPage(HttpServletRequest request,
							  String feature){
		// 如果前端未找到照片，捕获
		try {
			List<VideoEntity> videos = videoService.selectAllVideos(feature);
			String latestImageName = null;
			List<VideosssEntity> videosssss = new ArrayList<>();

			videoService.VideoRecord();
			// 遍历视频列表，查找最新的图片文件名
			for (VideoEntity v : videos) {
				VideosssEntity videosssEntity = new VideosssEntity();

				videosssEntity.setStream(v.getStream());
				videosssEntity.setId(v.getId());
				videosssEntity.setRtsp(v.getRtsp());
				videosssEntity.setDescription(v.getDescription());
				videosssEntity.setCarAi(v.isCarAi());
				videosssEntity.setMonitorName(v.getMonitorName());

				videosssEntity.setPersonAi(v.isPersonAi());


				String monitorImagePath = Constants.IMGOUTPUTPATH + v.getMonitorName();
				File monitorImageDir = new File(monitorImagePath);
				if (monitorImageDir.exists() && monitorImageDir.isDirectory()) {
					File[] imageFiles = monitorImageDir.listFiles();
					if (imageFiles != null && imageFiles.length > 0) {
						Arrays.sort(imageFiles, Comparator.comparingLong(File::lastModified).reversed());
						latestImageName = imageFiles[0].getName();
						videosssEntity.setStream2(latestImageName);

					}
				}



				videosssss.add(videosssEntity);
			}

			request.setAttribute("class", "video");
			request.setAttribute("allVideos", videosssss);
		} catch (Exception e) {
			// 异常处理逻辑
			e.printStackTrace();
		}
		return "video";
	}
	@RequestMapping("/videoMonitor2")
	@Description("跳转到目标识别之后的摄像头列表页面")
	public String toVideoPage2(HttpServletRequest request,
							  String feature){
		// 如果前端未找到照片，捕获
		try {
			List<VideoEntity> videos = videoService.selectAllVideos(feature);
			String latestImageName = null;
			List<VideosssEntity> videosssss = new ArrayList<>();


			// 遍历视频列表，查找最新的图片文件名
			for (VideoEntity v : videos) {
				VideosssEntity videosssEntity = new VideosssEntity();

				videosssEntity.setStream(v.getStream());
				videosssEntity.setId(v.getId());
				videosssEntity.setRtsp(v.getRtsp());
				videosssEntity.setDescription(v.getDescription());
				videosssEntity.setCarAi(v.isCarAi());
				videosssEntity.setMonitorName(v.getMonitorName());

				videosssEntity.setPersonAi(v.isPersonAi());


				String monitorImagePath = Constants.IMGOUTPUTPATH + v.getMonitorName();
				File monitorImageDir = new File(monitorImagePath);
				if (monitorImageDir.exists() && monitorImageDir.isDirectory()) {
					File[] imageFiles = monitorImageDir.listFiles();
					if (imageFiles != null && imageFiles.length > 0) {
						Arrays.sort(imageFiles, Comparator.comparingLong(File::lastModified).reversed());
						latestImageName = imageFiles[0].getName();
						videosssEntity.setStream2(latestImageName);

					}
				}



				videosssss.add(videosssEntity);
			}
			request.setAttribute("class", "video");
			request.setAttribute("allVideos", videosssss);
		} catch (Exception e) {
			// 异常处理逻辑
			e.printStackTrace();
		}
		return "video2";
	}



	@RequestMapping({"/videoMonitor/face","/videoMonitor/face/{pageSize}/{pageNum}"})
	public String toFaceRecordsOfVideoPage(HttpServletRequest request,
										   @PathVariable(required = false) Integer pageSize,
										   @PathVariable(required = false) Integer pageNum ){
		PageInfo<FaceRecordsEntity> faceRecords = videoService.selectFaceRecordsByPage(pageSize,pageNum);
		request.setAttribute("class","face");
		request.setAttribute("faceRecords",faceRecords);
		log.info("进入人员识别结果页面成功！");
		return "video";
	}

	@RequestMapping({"/videoMonitor/car","/videoMonitor/car/{pageSize}/{pageNum}"})
	public String toCarRecordsOfVideoPage(HttpServletRequest request,
										   @PathVariable(required = false) Integer pageSize,
										   @PathVariable(required = false) Integer pageNum){

		PageInfo<CarRecordsEntity> carRecords = videoService.selectCarRecordsByPage(pageSize,pageNum);
		request.setAttribute("class","car");
		request.setAttribute("carRecords",carRecords);
		log.info("进入车辆识别结果页面成功！");
		return "video";
	}
	@RequestMapping({"/localVideo","/localVideo/{pageSize}/{pageNum}"})
	public String toLocalVideoRecordsOfVideoPage(HttpServletRequest request,
										  @PathVariable(required = false) Integer pageSize,
										  @PathVariable(required = false) Integer pageNum){

		PageInfo<LocalVideoEntity> localVideoRecord = videoService.selectLocalVideoRecordsByPage(pageSize,pageNum);
		request.setAttribute("class","localvideo");
		request.setAttribute("localVideoRecord",localVideoRecord);
		log.info("进入本地视频结果页面成功！");
		return "video";
	}
	@RequestMapping({"/waringVideo","/waringVideo/{pageSize}/{pageNum}"})
	public String toWaringVideoRecordsOfVideoPage(HttpServletRequest request,
												 @PathVariable(required = false) Integer pageSize,
												 @PathVariable(required = false) Integer pageNum){

		PageInfo<WaringVideosEntity> waringVideos = videoService.selectWaringVideoRecordsByPage(pageSize,pageNum);
		request.setAttribute("class","waringvideo");
		request.setAttribute("waringVideoRecord",waringVideos);
		log.info("进入本地视频结果页面成功！");
		return "video";
	}

	@GetMapping("/addVideo")
	public String toAddVideoPage(){
		log.info("进入添加摄像头页面成功！");
		return "addVideo";
	}

//	@PostMapping("/addVideo")
//@PostMapping("/addVideo")
//	public String addVideo(@RequestParam String monitorName,
//			               @RequestParam String username,
//						   @RequestParam String password,
//						   @RequestParam String ip,
//						   @RequestParam(required = false,defaultValue = "true") boolean personAi,
//						   @RequestParam(required = false,defaultValue = "false") boolean carAi,
//						   String port,
//						   String description,
//						   HttpServletRequest request) throws InterruptedException {
//		if (ObjectUtil.isEmptyString(port))port="554";
//		ClientDemo client = new ClientDemo();
//
//
////		String message = client.HCIsAvailable(ip,Short.parseShort(port),username,password);
//		String message = "成功";
//
//
//		if (message.contains("成功")){
//			try {
//				int result = videoService.insertVideo(monitorName,username,password,ip,port,description,personAi,carAi);
//				if (result>0){
//					log.info("添加摄像头成功！");
//					Thread.sleep(2000);
//					return toVideoPage(request,null);
//				}
//			}catch (Exception e){
//				log.error("添加摄像头失败！",e);
//				request.setAttribute("error","数据库错误！请检查网络或摄像头信息是否和已有摄像头重复！");
//			}
//		}else{
//			log.error("添加摄像头失败！");
//			request.setAttribute("error","连接摄像头失败！请检查ip、端口、用户名、密码是否正确！");
//		}
//		return "addVideo";
//
//	}
	//下文是修改后的，上文是备份
@PostMapping("/addVideo")
public String addVideo(@RequestParam String monitorName,
					   @RequestParam String username,
					   @RequestParam String password,
					   @RequestParam String ip,
					   @RequestParam(required = false, defaultValue = "true") boolean personAi,
					   @RequestParam(required = false, defaultValue = "false") boolean carAi,
					   String port,
					   String description,
					   HttpServletRequest request) throws InterruptedException {
	if (ObjectUtil.isEmptyString(port)) port = "554";
	ClientDemo client = new ClientDemo();

	// String message = client.HCIsAvailable(ip, Short.parseShort(port), username, password);
	String message = "成功";

	if (message.contains("成功")) {
		try {
			int result = videoService.insertVideo(monitorName, username, password, ip, port, description, personAi, carAi);
			if (result > 0) {
				log.info("添加摄像头成功！");
				// 调用脚本
				callShellScript("/tmp/jar_restart.sh");
				Thread.sleep(2000);
				return toVideoPage(request, null);
			}
		} catch (Exception e) {
			log.error("添加摄像头失败！", e);
			request.setAttribute("error", "数据库错误！请检查网络或摄像头信息是否和已有摄像头重复！");
		}
	} else {
		log.error("添加摄像头失败！");
		request.setAttribute("error", "连接摄像头失败！请检查ip、端口、用户名、密码是否正确！");
	}
	return "addVideo";
}

	// 调用外部Shell脚本的方法
	private void callShellScript(String scriptPath) {
		try {
			// 使用 ProcessBuilder 来执行脚本
			ProcessBuilder processBuilder = new ProcessBuilder(scriptPath);
			processBuilder.inheritIO(); // 继承父进程的输入输出
			Process process = processBuilder.start();

			// 等待脚本执行完成
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				log.info("脚本执行成功");
			} else {
				log.error("脚本执行失败，退出代码：" + exitCode);
			}
		} catch (IOException | InterruptedException e) {
			log.error("执行脚本时发生错误", e);
		}
	}










	/**
	 * 跳转到视频播放页面
	 * @param stream rtmp流中的stream参数
	 * @return 视频播放页面
	 */
	@GetMapping("/videoPlay")
	public String toVideoPlayPage(String stream, HttpServletRequest request) throws SocketException {

		request.setAttribute("stream",new String[]{stream});
		Inet4Address inet4Address = IpUtil.getLocalIp4Address().orElse(null);
		String ip = inet4Address.toString().substring(1);
//		String ip = "192.168.0.3";
		System.out.println('\n'+ip+'\n');
		if (ip.length()>0){
			request.setAttribute("ip",ip);
		}else {
			request.setAttribute("msg","获取服务器ip地址失败！");
		}
		//		videoService.addSession(stream);
		log.info("进入摄像头实时监测页面成功！");
		return "videoPlay";
//		return stream;
//		return stream;
	}
//	@GetMapping("/videoPlay2")
	@Controller
	@RequestMapping("/videoPlay2")
	public class VideoPlayController {

		@Autowired
		private VideoRepository videoRepository;

		@GetMapping
		public String toVideoPlayPage(@RequestParam String stream, HttpServletRequest request) throws SocketException {
			// 打印stream参数
			if (stream != null) {
				System.out.println("Stream parameter: " + stream);
			} else {
				System.out.println("Stream parameter is missing.");
			}
			// 向Flask服务器发送stream参数
			sendStreamToFlask(stream);

			return "videoPlay2";
		}

		private void sendStreamToFlask(String stream) {
			RestTemplate restTemplate = new RestTemplate();
			String flaskUrl = "http://localhost:5555/receive_stream";

			// 使用RestTemplate发送POST请求，传递stream参数
			restTemplate.postForObject(flaskUrl, stream, String.class);
		}
	}


	@RequestMapping("/videoDelete")
	public String videoDelete(String rtsp, HttpServletRequest request) {
//		System.out.println(rtsp);
		try {
			videoService.deleteVideo(rtsp);
			//删除注册的bean
			//删除ffmpeh进程
			request.setAttribute("msg","摄像头删除成功！");

		}catch (Exception e){
			request.setAttribute("msg","摄像头删除失败！");
		}
		return  toVideoPage(request, null);
	}


	/**
	 * 退出视频播放页面，用于推流进程的开启关闭管理
	 * @param stream rtmp流中的stream参数
	 * @return 啥也不返回
	 */
	/*@PostMapping("/quitVideoPlay")
	@ResponseBody
	public String quitVideoPlayPage(@RequestParam String stream){
		videoService.dropSession(stream);
		return null;
	}*/



	@RequestMapping("/localVideoPlay")
	public void service(String path ,HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.reset();
        File file = new File(path);
        long fileLength = file.length();
        // 随机读文件
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        //获取从那个字节开始读取文件
        String rangeString = request.getHeader("Range");
        long range = 0;
        if (!StringUtils.isEmpty(rangeString)) {
            range = Long.valueOf(rangeString.substring(rangeString.indexOf("=") + 1, rangeString.indexOf("-")));
        }
        //获取响应的输出流
        OutputStream outputStream = response.getOutputStream();
        //设置内容类型
        response.setHeader("Content-Type", "video/mp4");
        //返回码需要为206，代表只处理了部分请求，响应了部分数据
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        // 移动访问指针到指定位置
        randomAccessFile.seek(range);
        // 每次请求只返回1MB的视频流
        byte[] bytes = new byte[1024 * 1024];
        int len = randomAccessFile.read(bytes);
        //设置此次相应返回的数据长度
        response.setContentLength(len);
        //设置此次相应返回的数据范围
        response.setHeader("Content-Range", "bytes " + range + "-" + (fileLength - 1) + "/" + fileLength);
        // 将这1MB的视频流响应给客户端
        outputStream.write(bytes, 0, len);
        outputStream.close();
        randomAccessFile.close();
        System.out.println("返回数据区间:【" + range + "-" + (range + len) + "】");

	}


}

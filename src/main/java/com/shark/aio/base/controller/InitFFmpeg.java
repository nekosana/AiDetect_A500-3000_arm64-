package com.shark.aio.base.controller;

import com.alibaba.fastjson.JSONObject;
import com.shark.aio.alarm.contactPart.util.ClientDemo;
import com.shark.aio.base.information.InformationEntity;
import com.shark.aio.base.information.InformationMapping;
import com.shark.aio.data.video.configuration.VideoConfiguration;
import com.shark.aio.data.video.entity.CarRecordsEntity;
import com.shark.aio.data.video.entity.FaceRecordsEntity;
import com.shark.aio.data.video.entity.VideoEntity;
import com.shark.aio.data.video.mapper.VideoMapping;
import com.shark.aio.data.video.service.ImageRecorderService;
import com.shark.aio.data.video.service.VideoRecorderService;
import com.shark.aio.data.video.service.VideoService;

import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.alarm.contactPart.util.ProcessUtil;
import com.shark.aio.alarm.contactPart.util.SendSms;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

@Slf4j
//@Component
public class InitFFmpeg implements ApplicationListener<ApplicationStartedEvent> {



    @Autowired
    private VideoMapping videoMapping;
    @Autowired
    private InformationMapping informationMapping;
    @Autowired
    private VideoService videoService;


    @Resource(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public static HashMap<VideoEntity, FFmpegProcess> ffmpegProcessMap = new HashMap<>();
    public static HashMap<VideoEntity, Future<?>> imageRecorderMap = new HashMap<>();

    @Override

    public void onApplicationEvent(ApplicationStartedEvent event) {
        try {

            log.info("加载海康SDK...");
//            ClientDemo.loadSDK();
            log.info("加载完成");
        } catch (Exception e) {
            log.error("第一次加载出错", e);
        }
        List<VideoEntity> videos = videoMapping.selectAllVideos();


        for (VideoEntity video : videos) {

            //在ioc容器中注册bean
            VideoConfiguration.registerBean(VideoRecorderService.class, video.getMonitorName());


            //这里是jar包启动就会自动推流
            try {
                FFmpegProcess p = ProcessUtil.videoPreview(video.getRtsp(), video.getStream());
                ffmpegProcessMap.put(video, p);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            //这里是调用算法的部分
            try {
                //配置参数，开启截图
                ImageRecorderService imageRecorderService = new ImageRecorderService(video.getRtsp(), 1280, 720, "0", video.getMonitorName());
                //文件监听
                videoService.runExample(imageRecorderService.getPARENT_DIR(), video);
//                threadPoolTaskExecutor.execute(imageRecorderService);
//                new Thread(imageRecorderService).start();

                Future<?> call = threadPoolTaskExecutor.submit(imageRecorderService);
                imageRecorderMap.put(video, call);
            } catch (Exception e) {
                log.error("打开监控" + video.getMonitorName() + "异常！" + e);
            }
        }
    }

    //    @Scheduled(cron ="0 */3 * * * ?")
    @Scheduled(cron = "*/10 * * * * ?")
    public void monitorFfmpegProcess() throws Exception {

        synchronized (imageRecorderMap) {
            if (!imageRecorderMap.isEmpty()) {
                for (VideoEntity video : imageRecorderMap.keySet()) {
                    Future<?> call = imageRecorderMap.get(video);
                    if (call.get() == null) {
                        ImageRecorderService imageRecorderService = new ImageRecorderService(video.getRtsp(), 1280, 720, "0", video.getMonitorName());
                        videoService.runExample(imageRecorderService.getPARENT_DIR(), video);
                        Future<?> newCall = threadPoolTaskExecutor.submit(imageRecorderService);
                        imageRecorderMap.put(video, newCall);
                        log.error("快照进程开启成功！");
                    }
                }
            } else {
//                log.info("暂无推流进程运行。");
            }
        }

        //这里是调用算法的部分
//        try {
//            ImageRecorderService imageRecorderService =new ImageRecorderService(video.getRtsp(),1280,720,"0",video.getMonitorName());
//            videoService.runExample(imageRecorderService.getPARENT_DIR(), video);
//            threadPoolTaskExecutor.execute(imageRecorderService);
//
//            Future<?> call = threadPoolTaskExecutor.submit(imageRecorderService);
//            call.get();
//
//
//        } catch (Exception e) {
//            log.error("打开监控"+video.getMonitorName()+"异常！"+e);
//        }


        synchronized (ffmpegProcessMap) {
            if (!ffmpegProcessMap.isEmpty()) {
                for (VideoEntity video : ffmpegProcessMap.keySet()) {
                    FFmpegProcess p = ffmpegProcessMap.get(video);
                    if (!p.isAlive()) {
                        int pid = ProcessUtil.IS_WINDOWS?ProcessUtil.getProcessIdInWindows(p):ProcessUtil.getProcessIdInLinux(p);
                        log.error("进程" + pid + "异常结束，准备重新开启推流进程。");
                        p.destroy();
                        FFmpegProcess newProcess = ProcessUtil.videoPreview(video.getRtsp(), video.getStream());
                        ffmpegProcessMap.put(video, newProcess);
                        log.error("推流进程开启成功！");
                    }
                }
            } else {
//                log.info("暂无推流进程运行。");
            }
        }
    }

    @Scheduled(cron = "0 0 22 * * ?")
    public void delete() throws NoSuchFieldException, IllegalAccessException {
        synchronized (ffmpegProcessMap) {
            if (!ffmpegProcessMap.isEmpty()) {
                for (VideoEntity video : ffmpegProcessMap.keySet()) {

                    String inputpath = Constants.IMGOUTPUTPATH + video.getMonitorName() + "/";
                    File imgdir = new File(inputpath);
                    File[] files = imgdir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            file.delete();
                        }
                    }

                }
            }
        }
    }

    @Scheduled(cron = "0 0 18 * * ?")
    public void sendAIResultMessage() {
        List<FaceRecordsEntity> faceRecords = videoMapping.selectTodayFaceRecordsTime();
        List<CarRecordsEntity> carRecords = videoMapping.selectTodayCarRecordsTime();
        InformationEntity informationEntity = informationMapping.getInformation();
        StringBuilder messageBuilder = new StringBuilder(0);
        if (faceRecords.isEmpty() && carRecords.isEmpty()) return;
        messageBuilder.append("过去24小时内");
        if (!faceRecords.isEmpty()) {
            HashMap<String, StringBuilder> faceMap = new HashMap<>();
            for (FaceRecordsEntity faceRecordsEntity : faceRecords) {
                String picutureUrl = faceRecordsEntity.getPictureUrl();
                int index = picutureUrl.lastIndexOf('/');
                String monitorName = picutureUrl.substring(0, index);

                if (!faceMap.containsKey(monitorName)) faceMap.put(monitorName, new StringBuilder());

                String time = faceRecordsEntity.getTime().toString().substring(5, 16);
                StringBuilder stringBuilder = faceMap.get(monitorName);
                stringBuilder.append(",").append(time);
            }
            messageBuilder.append(faceMap.keySet().size()).append("个监测点有人出现");
            if (carRecords.isEmpty()) messageBuilder.append("。");
        }
        if (!carRecords.isEmpty()) {
            HashMap<String, StringBuilder> carMap = new HashMap<>();
            for (FaceRecordsEntity carRecordsEntity : faceRecords) {
                String picutureUrl = carRecordsEntity.getPictureUrl();
                int index = picutureUrl.lastIndexOf('/');
                String monitorName = picutureUrl.substring(0, index);

                if (!carMap.containsKey(monitorName)) carMap.put(monitorName, new StringBuilder());

                String time = carRecordsEntity.getTime().toString().substring(5, 16);
                StringBuilder stringBuilder = carMap.get(monitorName);
                stringBuilder.append(",").append(time);
            }
            if (faceRecords.isEmpty()) messageBuilder.append(carMap.keySet().size()).append("个监测点有车出现。");
            else {
                messageBuilder.append(",").append(carMap.keySet().size()).append("个监测点有车出现。");
            }
        }
        System.out.println("++++++++++++++++++++++++++");
        System.out.println(messageBuilder);
        if (messageBuilder.length() != 0) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", messageBuilder.toString());
            SendSms.SendAlarmMessage(jsonObject, informationEntity.getTelephone());
        }
    }


}

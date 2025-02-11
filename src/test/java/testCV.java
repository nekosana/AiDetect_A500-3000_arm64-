import com.shark.aio.AIOCompanyApplication;
import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.data.video.entity.VideoEntity;
import com.shark.aio.data.video.service.VideoRecorderService;
import com.shark.aio.data.video.service.VideoService;
import com.shark.aio.util.RtspUtils;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;


@SpringBootTest(classes = AIOCompanyApplication.class)
public class testCV {

    @Autowired
    private VideoService videoService;
    @Test
    public void testzc() throws FrameGrabber.Exception {
        //ffmpeg -rtsp_transport tcp -i rtsp://admin:Shark666@nju@192.168.0.2:554 -vcodec libx264 -r 25 -video_size 1280*720 -preset ultrafast -tune zerolatency -f flv -an rtmp://localhost:1935/myapp/room
        //ffmpeg -rtsp_transport tcp -i rtsp://admin:lbx123456@192.168.0.3:554 -vcodec libx264 -r 25 -video_size 1280*720 -preset ultrafast -tune zerolatency -f flv -an rtmp://localhost:1935/myapp/room
        //ffmpeg -rtsp_transport tcp -i rtsp://admin:Shark666@nju@192.168.0.2:554/Streaming/tracks/101?starttime=20230221t110000z -allowed_media_types video -vcodec libx264 -r 25 -ar 22050 -preset ultrafast -tune zerolatency -f flv rtmp://localhost:1935/myapp/mystream
        //ffmpeg -re -i D:\aierhaisen1.mp4 -i D:\aierhaisen2.mp4 -vcodec libx264 -acodec aac -f flv rtmp://localhost:1935/myapp/mystream
        //String file = "rtsp://192.168.2.38:5554/2";
        String file =  "rtsp://admin:lbx123456@192.168.0.8:554";
        FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(file);
        grabber.setOption("rtsp_transport", "tcp"); // 使用tcp的方式，不然会丢包很严重
        // 一直报错的原因！！！就是因为是 2560 * 1440的太大了。。
        grabber.setImageWidth(2560);
        grabber.setImageHeight(1440);
        System.out.println("grabber start");
        grabber.start();
        CanvasFrame canvasFrame = new CanvasFrame("sdsaf");
        canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvasFrame.setAlwaysOnTop(true);
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        // OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        Frame frame;
        while (true){
            frame = grabber.grabImage();
            System.out.println(frame.image);
//            opencv_core.Mat mat = converter.convertToMat(frame);
            canvasFrame.showImage(frame);
        }
    }

    @Test
    public void testVideoRecord() {

        final int CORE_POOL_SIZE = 5;
        final int MAX_POOL_SIZE = 10;
        final int QUEUE_CAPACITY = 100;
        final Long KEEP_ALIVE_TIME = 1L;
        List<VideoEntity> videoList = videoService.selectAllVideos();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());
//        String inputFile = "rtsp://admin:lbx123456@192.168.0.8:554";
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
//        String dateStr = dateFormat.format(new Date(System.currentTimeMillis()));
//        String outputFile = "D:\\Soft\\test\\video\\" + dateStr + ".mp4";
//        executor.execute(() -> {
//            RtspUtils.videoPuller(inputFile, 60, outputFile);
//            System.out.println("Finished  thread 1");
//        });
//
//        String inputFile2 = "rtsp://admin:lbx123456@192.168.0.9:554";
//        String dateStr2 = dateFormat.format(new Date(System.currentTimeMillis()));
//        String outputFile2 = "D:\\Soft\\test\\video\\" + dateStr2 + "m.mp4";
//        executor.execute(() -> {
//            RtspUtils.videoPuller(inputFile2, 60, outputFile2);
//            System.out.println("Finished  thread 2");
//        });

        if(videoList.isEmpty()){
            System.out.println("视频列表为空");
            return ;
        }
        for (int i = 0; i < videoList.size(); i++) {
            String inputFile = videoList.get(i).getRtsp();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String dateStr = dateFormat.format(new Date(System.currentTimeMillis()));
            String outputFile = Constants.LOCALVIDEOPATH + videoList.get(i).getMonitorName() + "-" +dateStr + ".mp4";
            executor.execute(()->{
                RtspUtils.videoPuller(inputFile, 60, outputFile);
                System.out.println("Finished  thread 1");
            });
        }


        while (!executor.isTerminated()) {

        }
        executor.shutdown();
        System.out.println("Finished all threads");


    }
}

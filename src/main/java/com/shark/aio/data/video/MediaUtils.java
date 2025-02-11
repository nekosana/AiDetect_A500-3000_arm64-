package com.shark.aio.data.video;


import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

@Component
public class MediaUtils {

    @Autowired
    ThreadPoolTaskExecutor taskExecutor;
    /**
     * 视频帧率
     */
//    public static final int FRAME_RATE = 25;
    public static final int FRAME_RATE = 25;
    /**
     * 视频宽度s
     */
    public static final int FRAME_WIDTH = 1280;
    /**
     * 视频高度
     */
    public static final int FRAME_HEIGHT = 720;
    /**
     * 流编码格式
     */
    public static final int VIDEO_CODEC = avcodec.AV_CODEC_ID_H264;
    /**
     * 编码延时 zerolatency(零延迟)
     */
    public static final String TUNE = "zerolatency";
    /**
     * 编码速度 ultrafast(极快)
     */
    public static final String PRESET = "ultrafast";
    /**
     * 录制的视频格式 flv(rtmp格式) h264(udp格式) mpegts(未压缩的udp) rawvideo
     */
    public static final String FORMAT = "h264";
    /**
     * 比特率
     */
    public static final int VIDEO_BITRATE = 200000;

    private static FFmpegFrameGrabber grabber = null;
    private static FFmpegFrameRecorder recorder = null;
//    FFmpegFrameGrabber ff = new FFmpegFrameGrabber(new FileInputStream(originFile.getAbsoluteFile()),0);
//            ff.start();

    /**
     * 构造视频抓取器
     *
     * @param rtsp 拉流地址
     * @return
     */
    public static FFmpegFrameGrabber createGrabber(String rtsp) {
        // 获取视频源
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtsp);
        grabber.setOption("rtsp_transport", "tcp");
        //设置帧率
        grabber.setFrameRate(FRAME_RATE);
        //设置获取的视频宽度
        grabber.setImageWidth(FRAME_WIDTH);
        //设置获取的视频高度
        grabber.setImageHeight(FRAME_HEIGHT);
        //设置视频bit率
        grabber.setVideoBitrate(2000000);
        return grabber;
    }

    /**
     * 选择视频源
     *
     * @param src
     * @throws FrameGrabber.Exception
     * @author eguid
     */
    public MediaUtils from(String src) throws FrameGrabber.Exception {
        //FSF:为什么这个from方法没有被调用呢
        long start = System.currentTimeMillis();
        // 采集/抓取器
//        src = "rtsp://admin:lbx123456@192.168.0.3:554";
        System.out.println('\n'+"src=="+src+'\n');
        grabber = createGrabber(src);
        // 开始之后ffmpeg会采集视频信息
        grabber.start();
        grabber.flush();
        String form = src.substring(src.indexOf("@") + 1);
        return this;
    }

    /**
     * 推送图片流
     * @throws Exception
     */
    public MediaUtils startPush(String ip, Integer port) throws Exception {
        Long end = System.currentTimeMillis();
   //     System.out.println(form + " 开始推送 耗时：" + (end - start) + "ms");
        Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();
        try {
            Frame frame;
            long start = System.currentTimeMillis();
            int count = 0;
            while ((frame = grabber.grabImage()) != null) {
                System.out.println("catch......");
                if (System.currentTimeMillis()-start>=1000){
                    System.out.println("count=" + count);
                    start = System.currentTimeMillis();
                    count = 0;
                }else{
                    count ++;
                }
                //线程控制中断
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println(" 停止推送...");
                    return this;
                }
                System.out.println("convert......");
                BufferedImage bufferedImage = java2DFrameConverter.getBufferedImage(frame);
                byte[] bytes = imageToBytes(bufferedImage, "jpg");
                //使用udp发送图片数据
                //udpService.sendMessageBytes(bytes, ip, port);
                //使用websocket发送数据
                System.out.println("send......");
//                WebSocket.sendAll(bytes);
                System.out.println("after send......");
            }
        } catch (Exception e){
            Thread.currentThread().interrupt();
        }finally {
            if (grabber != null) {
                grabber.stop();
            }
        }
        return this;
    }

    /**
       * bufferedImage转base64
       * @param format -格式（jpg,png,bmp,gif,jpeg等等）
       * @return
       * @throws IOException
       */
     public static byte[] imageToBytes(BufferedImage image, String format) throws  IOException {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();// 字节流
          ImageIO.write(image, format, baos);// 写出到字节流
          byte[] bytes=baos.toByteArray();
          return bytes;
     }

}


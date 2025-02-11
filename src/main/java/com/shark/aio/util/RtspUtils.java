package com.shark.aio.util;/**
 * @description:
 * @author: Administrator
 * @time: 2024/12/15 0015 15:34
 */

import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.alarm.contactPart.util.FileUtil;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static cn.hutool.core.img.ImgUtil.rotate;

/**
 * @description:
 * @author: Administrator
 * @time: 2024/12/15 0015 15:34
 */
public class RtspUtils {
    /**
     * 判断流地址是否在线
     * @param rtspUrl 流地址
     * @return
     */
    public static Boolean isOnline(String rtspUrl) {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl);
        try {
            grabber.setTimeout(3000);
            grabber.start();
            return true;
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        } finally {
            if (grabber != null) {
                close(grabber);
            }
        }
        return false;
    }

    /**
     * 关闭grabber
     *
     * @param grabber
     * @return
     */
    private static boolean close(@NotNull FFmpegFrameGrabber grabber) {
        try {
            grabber.close();
            return true;
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 拉取指定时长的视频-保存为mp4
     * @param rtspUrl    流地址
     * @param duration   时长（秒）（TimeUnit Seconds）
     * @param outputFile 输出位置
     */
    public static void videoPuller(String rtspUrl, int duration, String outputFile) {
        // 创建抓取器
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl);
        // 如果不设置成tcp连接时，默认使用UDP，丢包现象比较严重
        grabber.setOption("rtsp_transport", "tcp"); // 设置成tcp以后比较稳定
        //socket网络超时时间
        grabber.setOption("stimeout","3000000");

        grabber.setImageWidth(1280);
        grabber.setImageHeight(720);
        grabber.setImageScalingFlags(0);
        grabber.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        grabber.setFrameRate(25);
        try {
            grabber.start();
            // 创建录制器
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, grabber.getImageWidth(), grabber.getImageHeight());
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // 设置视频编解码器
            recorder.setFormat("mp4"); // 设置视频输出格式
            // 设置音频相关参数
            recorder.setAudioChannels(2);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setSampleRate(44100);
            recorder.setAudioBitrate(192000);
            recorder.start();
            Frame frame;
            long startTime = System.currentTimeMillis();
            long endTime = startTime + (duration * 1000);
            while ((frame = grabber.grabFrame()) != null && System.currentTimeMillis() <= endTime) {
                recorder.record(frame);
            }
            recorder.stop();
            grabber.stop();
            recorder.close();
            grabber.close();
        } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String,Object> getScreenshot(String videoPath) throws Exception{
        Map<String,Object> rst = new HashMap<>();



        FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(videoPath);
        grabber.start();
        Frame frame = grabber.grabImage();
        String rotate = grabber.getVideoMetadata("rotate");

        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage bi = converter.getBufferedImage(frame);


        String imageMat = "jpg";

        //获取年月
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        String targetFileName = format.format(new Date())+File.separator;

        //获取日
        SimpleDateFormat formatDay = new SimpleDateFormat("dd");
        targetFileName += formatDay.format(new Date()) + File.separator;

        FileUtil.isDirExist(Constants.LOCALPICPATH + File.separator + targetFileName);

        //获取时分秒
        SimpleDateFormat formatHms = new SimpleDateFormat("HHmmss");
        targetFileName += formatHms.format(new Date());

        //生成四位随机数字
        Integer rndNum = new Random().nextInt(1000) + 9000;



        targetFileName += rndNum + "." + imageMat;

        //图片完整路径
        String imagePath = Constants.LOCALPICPATH  + targetFileName;

        File output = new File(imagePath);


        ImageIO.write(bi,imageMat,output);

        rst.put("videoWide",bi.getWidth());
        rst.put("videoHigh",bi.getHeight());
        rst.put("rotate", StringUtils.isBlank(rotate)?"0":rotate);
        rst.put("format",grabber.getFormat());
        targetFileName = targetFileName.replace('\\','/');
        rst.put("imgPath",targetFileName);
        grabber.stop();
        return rst;

    }

    @Test
    public void testPic(){
        try{
            getScreenshot("D:\\项目\\AIO\\localvideo\\测试摄像头-2024_12_23_17_08_36.mp4");
        }catch (Exception e){
            e.printStackTrace();
        }

    }




}

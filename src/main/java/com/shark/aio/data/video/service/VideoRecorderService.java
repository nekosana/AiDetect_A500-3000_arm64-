package com.shark.aio.data.video.service;

import com.shark.aio.data.video.entity.VideoEntity;
import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.alarm.contactPart.util.ProcessUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import static com.shark.aio.alarm.contactPart.util.Constants.IMGOUTPUTPATH;

/**
 * @author lbx
 * @date 2023/2/23 - 16:11
 **/

@Slf4j
@Getter
public class VideoRecorderService {


    static  SimpleDateFormat TimeFormat = new java.text.SimpleDateFormat("HH-mm-ss");
    private String inputFile;
    private String outputFile;
    private volatile boolean status = false;

    private boolean recording = false;

    private long noPersonTime;
    private long lastSaveTime;
    private int failCount = 0;

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public void setStatus(boolean status){
        if (this.status && !status){
            noPersonTime = System.currentTimeMillis();
        }
        this.status = status;
    }

    @Async("threadPoolTaskExecutor")
    public void testAsync() throws InterruptedException {
        while (true){
            System.out.println("异步方法执行");
            Thread.sleep(1000);
            System.out.println("异步方法结束");
        }
    }
    @Async("threadPoolTaskExecutor")
    public void startRecordVideo(VideoEntity video)
            throws Exception {
        //海歌摄像头
        //String inputFile = "rtsp://admin:Shark666@nju@192.168.0.2:554";
        //百翔摄像头
//        String inputFile = "rtsp://admin:lbx123456@192.168.0.3:554";

        SimpleDateFormat DataFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");

        this.inputFile = video.getRtsp();
        System.out.print("VideoRecorderService的getRtsp====");
        System.out.println("this.inputFile="+this.inputFile);
        System.out.println("video.getRtsp()="+video.getRtsp());
        this.outputFile =  Constants.VIDEOPATH + video.getMonitorName() + (ProcessUtil.IS_LINUX?"/":"\\") +"%Y-%m-%d_%H-%M-%S.mp4";
//        String url = "D:\\项目\\AIO\\recorder\\" + DataFormat.format(new Date()) ;
        String url =Constants.VIDEOPATH + video.getMonitorName();
        File localPath = new File(url);

        if (!localPath.exists()) {  // 获得文件目录，判断目录是否存在，不存在就新建一个
            localPath.mkdirs();
        }

        // %03d表示长度为3位，缺位的补零

//        String outputFile = "D:\\项目\\AIO\\recorder\\recorder%03d.mp4";

//        String time = (new java.text.SimpleDateFormat("yyyy")).format(new Date());
//        //       String outputFile = "D:\\项目\\AIO\\recorder\\" + time + ".mp4";
//        System.out.println("outputFile222    " + "D:\\项目\\AIO\\recorder\\recorder%03d.mp4");
//        System.out.println("outputFile    " + outputFile);
        frameRecord(inputFile, outputFile,1);
    }

    /**
     * 按帧录制视频
     * @param inputFile-该地址可以是网络直播/录播地址，也可以是远程/本地文件路径
     * @param outputFile-该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式
     * @param audioChannel
     * @throws Exception
     * @throws org.bytedeco.javacv.FrameRecorder.Exception
     */
    public void frameRecord(String inputFile, String outputFile, int audioChannel)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {

//        boolean status 该变量建议设置为全局控制变量，用于控制录制结束
        // 获取视频源
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        // 如果不设置成tcp连接时，默认使用UDP，丢包现象比较严重
        grabber.setOption("rtsp_transport", "tcp"); // 设置成tcp以后比较稳定
        //socket网络超时时间
        grabber.setOption("stimeout","3000000");

        grabber.setImageWidth(1280);
        grabber.setImageHeight(720);
        grabber.setImageScalingFlags(0);
        grabber.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        grabber.setFrameRate(25);
//        grabber.setFrameRate(5);
        // 一般来说摄像头的帧率是25
//        if (grabber.getFrameRate() > 0 && grabber.getFrameRate() < 100) {
//            framerate = grabber.getFrameRate();
//        } else {
//            framerate = 25.0;
//        }

        // 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 1280,720, audioChannel);
        // 设置视频编码H264
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        //设置音频编码
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        // 2000 kb/s, reasonable "sane" area for 720
//        recorder.setVideoBitrate(4000000);
//        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
//        recorder.setPixelFormat(0);
        //过滤掉日志
        avutil.av_log_set_level(avutil.AV_LOG_ERROR);

        recorder.setOption("strftime", "1"); //根据日期生成文件名

        recorder.setFormat("segment");
        //十秒钟一切
        recorder.setOption("segment_time", "3600");
        recorder.setOption("segment_filename", "10");
//      recorder.setOption("segment_filename", "D:\\项目\\AIO\\recorder\\" +(new java.text.SimpleDateFormat("yyyy-MM-dd HH-mm-ss")).format(new Date()) + "\\recorder%03d.mp4");
        //生成模式：live（实时生成）、cache（边缓存边生成，只支持m3u8清单文件缓存）
        recorder.setOption("segment_list_flags", "live");
        //强制锁定切片时长
        recorder.setOption("segment_atclocktime", "1");
        //使得每段视频都是从零开始计时
        recorder.setOption("reset_timestamps", "1");
        //设置帧率
        recorder.setFrameRate(25);

        //因为是直播，如果需要保证最小延迟，gop最好设置成帧率相同或者帧率*2
        //一个gop表示关键帧间隔，假设25帧/秒视频，gop是50，
        //则每隔两秒有一个关键帧，播放器必须加载到关键帧才能开始解码播放，就是说直播流最多两秒延迟
        recorder.setGopSize(50);//设置gop
//        recorder.setVideoQuality(0.8);//视频质量
        recorder.setVideoOption("preset", "slower");//视频质量
        recorder.setVideoBitrate(200000);//码率，10kb/s

        // 在视频质量和编码速度之间选择适合自己的方案，包括这些选项：
        // ultrafast,superfast, veryfast, faster, fast, medium, slow, slower, veryslow
        // ultrafast offers us the least amount of compression (lower encoder CPU) at the cost of a larger stream size
        // at the other end, veryslow provides the best compression (high encoder CPU) while lowering the stream size
        // (see: https://trac.ffmpeg.org/wiki/Encode/H.264)
        // ultrafast对CPU消耗最低
        // recorder.setVideoOption("preset", "veryslow");

        // 开始取视频源
        recordByFrame(grabber, recorder);
    }






    // 递归删除目录内的所有文件和子目录
    private static void deleteDirectoryContents(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                deleteDirectoryContents(file);
            }
            file.delete(); // 删除文件或空目录
        }
    }







    public void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder)
            throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
        try {//建议在线程中使用该方法
            grabber.start();
            recorder.start();
            failCount = 0;
            recording=true;
            Frame frame = null;
            while ( (frame = grabber.grabFrame()) != null) {
                if (!status){
                    //录制视频结束的地方，每N秒没有识别到画面中有有效对象，则停止录像功能
//                    if ((System.currentTimeMillis()-noPersonTime)>=(long)2*1000){
                    System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
                    int N2 = 2;
                    if ((System.currentTimeMillis()-noPersonTime)>=(long)N2*1000){
                        recording=false;
                        log.info("许久无人，停止录制");
                        System.out.println("许久无人，停止录制");

                        String parentDirPath = IMGOUTPUTPATH;  // 替换为实际文件夹路径
                        File parentDir = new File(parentDirPath);
                        System.out.println("parentDirPath==="+parentDirPath);
                        deleteDirectoryContents(parentDir);


                        break;
                    }
                }
                recorder.record(frame);
                //1.用线程sleep
                //2.获取当前时间，做时间差
                //3.@Scheduled

            }
            recorder.stop();
            grabber.stop();
        } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
            try {
                e.printStackTrace();
                System.out.println("录制器启动失败，正在重新启动...");
                if(++failCount>3)throw new RuntimeException();
                if (recorder != null) {
                    System.out.println("尝试关闭录制器");
                    recorder.stop();
                    grabber.stop();
                    System.out.println("尝试重新开启录制器");
//                    recorder.start();
                    frameRecord(inputFile,outputFile,1);
                }
            } catch (Exception e1) {
                log.error("视频录制失败！",e);
                throw e;
            }
        }finally {
            if (grabber != null) {
                grabber.stop();
            }
        }
    }







}

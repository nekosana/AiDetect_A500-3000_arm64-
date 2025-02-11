package com.shark.aio.data.video.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.shark.aio.alarm.GradedAlarm.JedisConnectionFactory;
import com.shark.aio.alarm.contactPart.util.*;
import com.shark.aio.base.controller.FFmpegProcess;
import com.shark.aio.data.video.configuration.VideoConfiguration;
import com.shark.aio.data.video.entity.*;
import com.shark.aio.data.video.mapper.VideoMapping;
import com.shark.aio.util.LogProcessor;
import com.shark.aio.util.RtspUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.scheduling.annotation.Scheduled;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.jetbrains.annotations.Async;
import org.json.JSONArray;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.shark.aio.alarm.contactPart.util.Constants.IMGOUTPUTPATH;
import static com.shark.aio.alarm.contactPart.util.Mp4Checker.check;
import static com.shark.aio.base.controller.InitFFmpeg.ffmpegProcessMap;
import static com.shark.aio.base.controller.InitFFmpeg.imageRecorderMap;

@Service
@Slf4j
@MapperScan(value = "com.shark.aio.data.video.mapper")
public class VideoService {

    @Autowired
    private VideoMapping videoMapping;

    @Resource(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    JedisConnectionFactory jedisConnectionFactory;

    @Value("${ffmpeg.predict.host}")
    @Setter
    private String host;

    @Value("${ffmpeg.predict.port}")
    @Setter
    private Integer port;
    /**
     * 存放rtsp地址和推拉流进程的映射关系
     */
    private static HashMap<String, FfmpegProcess> map = new HashMap<>();

    /**
     * 查询全部摄像头信息
     *
     * @return 摄像头list
     */
    public List<VideoEntity> selectAllVideos() {
        return videoMapping.selectAllVideos();
    }

    /**
     * 查询摄像头信息
     *
     * @param feature 模糊查询特征字符串
     * @return 分页查询结果
     */
    public List<VideoEntity> selectAllVideos(String feature) {
        List<VideoEntity> videos;
        if (feature != null && !feature.equals("")) {
            feature = "%" + feature + "%";
            videos = videoMapping.selectVideosByFeature(feature);
        } else {
            videos = videoMapping.selectAllVideos();
        }
        return videos;
    }

    public PageInfo<FaceRecordsEntity> selectFaceRecordsByPage(Integer pageSize, Integer pageNum) {
        List<FaceRecordsEntity> faceRecords = videoMapping.selectAllFaceRecords();

        return new PageInfo<>(faceRecords, 5);
    }
    public PageInfo<LocalVideoEntity> selectLocalVideoRecordsByPage(Integer pageSize,Integer page){
        List<LocalVideoEntity> localVideoRecords = videoMapping.getAllLocalVideo();

        return new PageInfo<>(localVideoRecords,5);
    }
    public PageInfo<WaringVideosEntity> selectWaringVideoRecordsByPage(Integer pageSize,Integer page){
        List<WaringVideosEntity> waringVideos = videoMapping.getWaringVideoInfo();
        return new PageInfo<>(waringVideos,5);
    }
    public PageInfo<CarRecordsEntity> selectCarRecordsByPage(Integer pageSize, Integer pageNum) {
        List<CarRecordsEntity> carRecords = videoMapping.selectAllCarRecords();

        return new PageInfo<>(carRecords, 5);
    }

    public void deleteVideo(String rtsp) throws InterruptedException {
        videoMapping.deleteVideo(rtsp);

        synchronized (ffmpegProcessMap) {
            if (!ffmpegProcessMap.isEmpty()) {
                for (VideoEntity video : ffmpegProcessMap.keySet()) {
                    if (video.getRtsp().equals(rtsp)) {
                        //注销进程
                        FFmpegProcess p = ffmpegProcessMap.get(video);
                        p.destroy();
                        //删除map表
                        ffmpegProcessMap.remove(video);
                        imageRecorderMap.remove(video);
                        //查看该bean
                        //VideoRecorderService videoRecorderService = VideoConfiguration.getBean(VideoRecorderService.class,video.getMonitorName());
                        //System.out.println(videoRecorderService);

                        //注销bean
                        VideoConfiguration.unRegistryBean(video.getMonitorName());

                        log.info("摄像头进程关闭成功！");
                    }

                }
            } else {
//                log.info("暂无推流进程运行。");
            }
        }
    }


    /**
     * 使map中，stream参数对应的rtsp地址下的页面数量加一
     * 如果在数量减少后rtsp地址下没有页面，则销毁推流进程
     *
     * @param stream
     */
    public void dropSession(String stream) {
        String rtsp = videoMapping.selectRtspByStream(stream);
        synchronized (map) {
            FfmpegProcess ffmpegProcess = map.get(rtsp);
            ffmpegProcess.decrease();
            log.info("目前流" + rtsp + "下的页面还有：" + ffmpegProcess.getCount() + "个");
            //如果流已无人使用，关闭推流进程
            if (ffmpegProcess.getCount() <= 0) {
                System.out.println(ProcessUtil.close(map.get(rtsp).getPid()));
                map.remove(rtsp);
            }
        }

    }

    public int insertVideo(String monitorName,
                           String username,
                           String password,
                           String ip,
                           String port,
                           String description,
                           boolean personAi,
                           boolean carAi) throws Exception {
        String rtsp = "rtsp://" + username + ":" + password + "@" + ip + ":" + port;
        String stream = "stream" + UUID.randomUUID();
        VideoEntity video = new VideoEntity(null, monitorName, rtsp, description, stream, personAi, carAi);
        int count = videoMapping.insertIntoVideo(video);
        if (count == 1) {
            VideoConfiguration.registerBean(VideoRecorderService.class, monitorName);
            try {
                FFmpegProcess process = ProcessUtil.videoPreview(video.getRtsp(), video.getStream());
                ffmpegProcessMap.put(video, process);

                ImageRecorderService imageRecorderService = new ImageRecorderService(video.getRtsp(), 1280, 720, "0", video.getMonitorName());
                imageRecorderService.saveCover();
                runExample(imageRecorderService.getPARENT_DIR(), video);
//            threadPoolTaskExecutor.execute(imageRecorderService);
                Future<?> call = threadPoolTaskExecutor.submit(imageRecorderService);
                imageRecorderMap.put(video, call);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }
        return count;

    }

    public void insertFaceRecord(FaceRecordsEntity faceRecord) {
        videoMapping.insertFaceRecord(faceRecord);
    }

    public void insertCarRecord(CarRecordsEntity carRecord) {
        videoMapping.insertCarRecord(carRecord);
    }


    public FaceRecordsEntity callFaceAI(File file, VideoEntity video) {
        boolean personAI = video.isPersonAi();
        boolean carAI = video.isCarAi();
        if (!personAI && !carAI) return null;
        /*double random = Math.random();
        if (random<0.33)file=new File("/home/thg/0.jpg");
        else if (random<0.67)file = new File("/home/thg/car.png");
        else file = new File("/home/thg/carId.jpeg");*/
        FaceRecordsEntity faceRecord = null;
        CarRecordsEntity carRecord = null;
        DataOutputStream out = null;
        VideoRecorderService videoRecorderService = VideoConfiguration.getBean(VideoRecorderService.class, video.getMonitorName());
        final String newLine = "\r\n";
        final String prefix = "--";
        try {
//            String ssd = "http://"+host+":"+port+"/ssd_predict";
            String ssd = "http://127.0.0.1:5002/json_feed";
            //源头在这
            URL url = new URL(ssd);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            System.out.println("起始点");
//            System.out.println(conn.getInputStream());
//            System.out.println("AAAAAABBBBBBBBBBBBBBBBBB");
//            System.out.println("连接http://localhost:5000/ssd_predict");
//            System.out.println("连接"+ssd);
//            System.out.println("conn====="+conn);

            String BOUNDARY = "-------7da2e536604c8";
//            System.out.println("H111111111111111111111111");
//            conn.setRequestMethod("POST");
//            //这里就出错不能往下了
//            System.out.println("H111111111111111111111111");
//            conn.setDoOutput(true);
//            System.out.println("H2222222222222222222222222");
//            conn.setDoInput(true);
//            conn.setUseCaches(false);
//            System.out.println("H4444444444444444444");
//            conn.setRequestProperty("connection", "Keep-Alive");
//            conn.setRequestProperty("Charsert", "UTF-8");
//            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
//
//            System.out.println("WWWWWWWWWWWWWWWWWWWWW");
//            out = new DataOutputStream(conn.getOutputStream());
//            System.out.println("NNNNNNNNNNNNNNNN");
//            //创建一个 DataOutputStream 对象，用于向 HttpURLConnection 的输出流中写入数据
//            // conn 是一个 HttpURLConnection 对象，表示一个与服务器的 HTTP 连接。
//            //getOutputStream() 方法从 conn 对象获取一个 OutputStream 对象，这个流用于向服务器发送数据。它是一个字节流，适用于发送二进制数据或原始字节数据。
//            //就是定义了一个out，然后可以使用“out.方法"从这里输出数据，向服务器那边输入数据
//            System.out.println("CCCCCCCCCCCCCCCCCC");
//
//            StringBuilder sb1 = new StringBuilder();
//            sb1.append(prefix);
//            sb1.append(BOUNDARY);
//            sb1.append(newLine);
//            sb1.append("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"" + newLine);
//            sb1.append("Content-Type:application/octet-stream");
//            sb1.append(newLine);
//            sb1.append(newLine);
//            out.write(sb1.toString().getBytes());
//            System.out.println("DDDDDDDDDDDDDDDDD");
//            DataInputStream in = new DataInputStream(new FileInputStream(file));
//            byte[] bufferOut = new byte[1024];
//            int bytes = 0;
//            while ((bytes = in.read(bufferOut)) != -1) {
//                out.write(bufferOut, 0, bytes);
//            }
//            out.write(newLine.getBytes());
//            in.close();
//
//            byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
//            out.write(end_data);
//            out.flush();
//            out.close();
//            System.out.println("BBBBBBBBBBBBBBBBBBBBBBB");
//            System.out.println(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            //创建一个 BufferedReader 对象，用于从网络连接的输入流中高效地读取文本数据
            //conn 是一个 HttpURLConnection 对象，代表一个与服务器的 HTTP 连接。
            //getInputStream() 方法从 conn 对象获取一个输入流 (InputStream)，用于读取从服务器返回的数据。这个流以字节的形式提供数据。
            String line = null;
            line = reader.readLine();
//            System.out.println("CCCCCCCCCCCCCCCCCC");
            System.out.println(line);
            //从 reader 对象中读取一行文本，并将其赋值给变量 line
            //reader 是一个读取流对象，通常是 BufferedReader 或类似的类，用于从输入流（例如文件、网络连接、标准输入等）中读取文本数据
            //readLine() 方法从输入流中读取一行文本，并将其返回为一个 String 对象，会读取直到行结束符（通常是换行符 \n 或回车符 \r）
//            System.out.println("22222222222222");

            reader.close();
//            System.out.println("222222222222");
            JSONObject result = JSON.parseObject(line);
//            System.out.println("33333333333333");
//            System.out.println(result);
            //parseObject的功能是 将 JSON 字符串解析为 JSONObject 对象
            String msg = "";
            boolean isPerson = result.getString("class_name").contains("person");
            boolean isCar = result.getString("class_name").contains("car") || result.getString("class_name").contains("bus");
            System.out.print("isPerson==");
            System.out.print(isPerson);
            System.out.print('\n'+"isCar==");
            System.out.print(isCar);

            if (isPerson||isCar) {
//                System.out.println("YESESESESE");
                if (true) {
                    log.info("算法：" + line.substring(0, Math.min(line.length(), 100)));
//                System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOKKKKKKKKKKKKKKKKKKKK");
//                System.out.println(result);
//                System.out.println(result.getString("objects"));


                    //判断有几个人和车以及最高概率
//                String[] classes = result.getString("class_name").split(";");
//                System.out.println("kkkkkkkkkkkkkkkkkkkkkkkkkkk");
                    System.out.println(result.getString("class_name"));
//                System.out.println(result.getString("class_name").split(","));
                    String[] classes = result.getString("class_name").split(",");


                    //改变格式，从字符串变成数组，方便操作
                    int personCount = 0;
                    int carCount = 0;
                    double personScore = 0;
                    double carScore = 0;
//                    System.out.println("wewewewewewewewewewewewewewewewewe");
                    for (String s : classes) {
//                    System.out.println("s=============" + s);
//                    System.out.println("LLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
                        //新定义一个s来遍历classes数组中的所有元素
                        //person
//                    System.out.println("personAI===" + personAI);
//                        System.out.println("carAI===" + carAI);
//                        System.out.println("s.contains(persn)=="+s.contains("person"));
//                        System.out.println("s.contains(person)==" + s.contains("person"));
                        if (personAI && s.contains("person")) {
//                        System.out.println("personCount==" + personCount);
                            personCount++;
//                        System.out.println("oooooooooooooooooooooooo");
//                        System.out.println("s.indexOf(\"person: \") + \"person: \".length())=="+s.indexOf("person: ") + "person: ".length());
//                        System.out.println("person:.length()=========="+"person: ".length());
                            String A1 = s.substring(s.indexOf("person: ") + "person: ".length());
//                            System.out.println("A1=="+A1);
                            System.out.println(A1.substring(0, 4));
//                        double thisPersonScore = Double.parseDouble(s.substring(s.indexOf("car: ") + "car: ".length()));
                            double thisPersonScore = Double.parseDouble(A1.substring(0, 4));
                            //Double.parseDouble把字符串类型转为double类型数据方便比较
//                        System.out.println("23333333333333333333333333");
//                        if (thisPersonScore > personScore) {
//                            personScore = thisPersonScore;
//                        }
                            System.out.println("thisPersonScore===="+thisPersonScore);
                            if (thisPersonScore != personScore) {
                                personScore = thisPersonScore;
                            }
                    }

                            //car
                    if (carAI && s.contains("car")) {
                        carCount++;
                        String A2 = s.substring(s.indexOf("car: ") + "car: ".length());
                        double thisCarScore = Double.parseDouble(A2.substring(0, 4));
                        System.out.println("thisCarScore==="+thisCarScore);
                        if (thisCarScore > carScore) {
                            carScore = thisCarScore;
                        }
                    }


                            //bus
                    if (carAI && s.contains("bus")) {
                        carCount++;
//                        double thisCarScore = Double.parseDouble(s.substring(s.indexOf("bus: ") + "bus: ".length()));
                        String A3 = s.substring(s.indexOf("bus: ") + "bus: ".length());
                        double thisCarScore = Double.parseDouble(A3.substring(0, 4));
                        System.out.println("thisBusCarScore==="+thisCarScore);
                        if (thisCarScore > carScore) {
                            carScore = thisCarScore;
                        }
                    }


//                    System.out.println("HEREHEREHERESSSSSSSSSSSSSSSSSSSSSSSSSS");
                        try (Jedis jedis = jedisConnectionFactory.getJedis()) {
                            System.out.println("INNNNNNNNNNNNNNNNNNNNNNN");
                            jedis.auth("dianzi327");
//                            System.out.println('\n' + "personScore============" + personScore + '\n');
                            if (personScore > 0.6) {
                                videoRecorderService.setStatus(true);
//                            System.out.println("SSSSSSSSSSSSSSSSSSSSSSSS");
//                        video.setRtsp("rtsp://admin:lbx123456@192.168.0.3:554");//如果没问题，后续改一下这里
                                System.out.println(video.getRtsp());
//                            System.out.print("THISHTISHTIS===");
//                            System.out.print(jedis.exists(video.getRtsp() + "1second"));
//                            System.out.print(jedis.exists(video.getRtsp() + "2second"));
//                            System.out.println(jedis.exists(video.getRtsp()));

                                //该视频没有正在录制并且识别出人的概率在0.3以上，开始录制
//                        if ( jedis.exists(video.getRtsp() + "2second")) {
                                if (jedis.exists(video.getRtsp() + "1second")) {
//                        if ( jedis.exists(video.getRtsp())) {
                                    //睡眠两秒，防止第一张图片准备录制，但第二张图片判断时，第一张图片还没开始录制，只要延时的时间大于两张照片到达的间隔即可
                                    System.out.println("没进入videoRecorderService判断");
                                    Thread.sleep(3000);
                                    if (!videoRecorderService.isRecording()) {
                                        System.out.println("进入了videoRecorderService判断");
                                        //录制视频
                                        log.info("监控点" + video.getMonitorName() + "开始录制...");
                                        videoRecorderService.startRecordVideo(video);
//                                    videoRecorderService.setRecording(true);
                                        System.out.println("视频正在录制");
                                    }

                                }

                                //1秒内均有识别到人再录制
                                jedis.setex(video.getRtsp() + "1second", 1, "true");
//                            System.out.println("视频正在录制");

                            } else {
                                videoRecorderService.setStatus(false);
////                            videoRecorderService.setRecording(false);
//                            System.out.println("视频录制完成");
                            }

				/*msg = line.substring(line.indexOf("\"img_str\":\"")+"\"img_str\":\"".length(), line.indexOf("\"}"));
				log.info(msg);*/

//                            //如果五分钟内没有进行该操作，则保存照片 + 插入数据库
//                            String fileName = DateUtil.fileFormat.format(new Date()) + ".png";

//                        if (personAI && (personScore > 0.6) && jedis.exists(video.getRtsp() + "2second")

                            System.out.println("==========================================================================");
                            System.out.println("personAI="+personAI);
                            System.out.println("(!jedis.exists(video.getMonitorName() + _person))=="+(!jedis.exists(video.getMonitorName() + "_person")));
                            System.out.println("(personScore > 0.5)=="+(personScore > 0.5));
                            System.out.println("==========================================================================");


//                            if (personAI && (personScore > 0.5) && jedis.exists(video.getRtsp() + "1second")
//                                    && (!jedis.exists(video.getMonitorName() + "_person"))) {
                            if (personAI && (personScore > 0.5) && (!jedis.exists(video.getMonitorName() + "_person"))) {
//                                System.out.println("==========================================================================");
//                                System.out.println("personAI="+carAI);
//                                System.out.println("jedis.exists(video.getRtsp() + 1second=="+jedis.exists(video.getRtsp() + "1second"));
//                                System.out.println("(personScore > 0.5)=="+(personScore > 0.5));
//                                System.out.println("==========================================================================");
//                                System.out.println("personaiINININININ");
                                //满足摄像头打开了人员ai+置信度>0.6+不知道什么条件+redis缓存中说明不存在人的条件才能进入
//                            System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMM");


                                BASE64Decoder decoder = new BASE64Decoder();
                                System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMM");
                                // 解密
                                System.out.println(result);
//                                System.out.println("result.getString(img_str)=="+result.getString("img_str"));
//                                String[] classes2 = result.getString("img_str").split(",");
                                System.out.println("OOOOOOOOOOOOOOOO");
                                String[] classes2 = result.getString("img_str_person").split(",");
                                System.out.println("ppppppppppppppppppppp");
//                                System.out.println(classes2);
                                System.out.println(classes2.length);
                                int c1 = 0;
                                if(classes2.length != 0){
                                    c1 = 0;
                                }
                                for (String s2 : classes2) {
//                                    int c1 = 0;
                                    String s3 = "";

//                                    String s2 = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAIBAQEBAQIBAQECAgICAgQDAgICAgUEBAMEBgUGBgYFBgYGBwkIBgcJBwYGCAsICQoKCgoKBggLDAsKDAkKCgr/2wBDAQICAgICAgUDAwUKBwYHCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgr/wAARCACmAEwDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD7cltFdNwHOPSmR2c6n5YWP0XNWkJIyavafMiDL4NfHcyPe5WZ4spX+UjBHYis7V9ZtNDt3mkdSwUkBiBXR3MkLzNKAMnivD/2wde/4RLwTeX8KMyraPIQpOcgZ7VtRj7SaiQ7IwPiX+274Y+Ht41jeXsEbq2MKBIR9cZxXN6b/wAFJ/CMsyxSazEgcgAta4/pX552HxKOt3E17qEzs9xPIxZpCTneccnmo77VY3uY3Byvmrkbsd6+g/suKp8xjGvCUrH7O/Cn4q6P8UNHGo6Vdo58sPtU9u5/M1163Jxg9QetfPH7Baf8SG3jjHy/2eVI9Oh/pX0bHapuIK9TXhVGoTaN7OWwsVz6GpVHmjfxST2XljcgqW3jCxAGo50KzTscytpPCdzR9e9TIryJhRitDZvTacU1hDDlnwAOTQqaG2VY7Mty1edftKeAofFXgqfzG2MI2TeewIwf0rsvFnxV8K+FItt9exBh/CT/AFrwz46ftk+BLfwdd2cd5HLNghIYRkufSvSweEnOomkcdfERhFo/KHxX4J1D4e/EDWPDDZ8q31Kb7PuOf3ZckfXrX0H+zr8AfC/xQ0iA+IkUSuuYpFfn6Yrzb4rrc+M/HF/4qNqyLOVCLnoBmuu/Z4+LOseAPENnpt2N9sbhFVSOV5xivq6kbUbHn4dN1Ln6TfsZ+AZPBOlzaTJO0otoyElZeoyAP0Ne6XSbJQFI/AV57+zPbxXvgk+IYpctcAEr6D0r0O4dXUFeSOtfDYtN1nY92HwjyjTIBSBFQbSalsWXbhqe6x7uGX8651HTUmTtIxIlZ2xuwKo+LL+LSdCuL2V8bEzjFaMSktlRxn0rjvj5fppHgK7uJZAqm3ds5xjArtoRU6iRlNtRbPhT9oj4yXnibx1f2VlqsvlWrbAoYgDNeK6sz3MhkaZsgkjkmqeteLTrfjXVr9Z93najJxnsGIA/Koru8cqT/KvtMNRVOmrHzNerKVRlS40hJpy8k5we2afYaTBaXK3MP31cEE9jWbe6pLGSwaq1v4lIfb5vOeea1nHm3Kp1ZaWPt/8AY/8A2wdS0S+h8I61GfIKqoLjKBBjOMdyTX21oWtWHiG0TULHOyXkqwIK/XIr8YLDx1caJJDqFpd7JIpA6nrX6af8ErNc+KX7RWihNF8M3M+nW8TR3WozuBEr54OcfpXgY/LHL34HuYPFpxtI+jrPTUkhLZA44xVOWxeNyvmV6J4m+APxa0Cwkv7Hw/BfJENzx2t2DIVHXCkDJwc49jXEWeowXcPmmE5BKsCvII6g+hrwJUakH7ysdjq05vQzEsxuOxcZr5x/4KH/ABQs/A/wmvBNqwti9rMgZoy25iAFAx7mvp69sn0+0a4lP3RX5cf8FXfiFNr3xMtfB8F4z28ILzIAcK2MYNenleH9tUT7HHjKvsqZ8saLqbfanunbl5Sxz3ya1JtbDZAbP411Xwu+A8vjXw6ddnkkjiV9o2jqc4HbvXIePvDt34I8XXfhW8kDy2hAd1UgEn619lFckUj5ebcpXKWr6qPKPI6djXNT60kczHJBBq9qcwKk57c1yGpXDrdbQTyama1NqUrKx0q6/NeQ+WJMD2r9pf8Ag3C+K11L8C9R+FuovGUi1GW4tmJUMOenAyfxNfiJoI3IRJ/e4r6y/wCCdn7ZfiX9k34i2ep2kHn6UkzPcW44bJ7ip5VKDR6WGjee5/SIGDLgdexr5K+O/hKEfFbWLjRJjbwz3HmMiHALkDcR9SKufAj/AIKd/DL42eFIL/T9BvbW6lxEyyplVkIOM4HTjrT/ABMX8Ta1PrNyxZpmyO2B6V8/jYK/L1PQhScNWcT8ZvF2n+FfB899fyKiIhbcGHNfjX8ZtSu/jt+0hqOpaOZL20kdfIuIyWRlBw233r70/wCClPxq1Dwl4IurSxn5fCIm4jg8Yx+teDf8E1/gXJ4+8SzeJLmONI4ZFQvLFuIyQeB06DH411ZZReCwrrS6nDjJ+3qqCPavgd8ENF+FfwluZbvTEuUW2LrLJCcElcgD1wSOnpX59ftJXWn6j8atevrCTfGbkKT/ALQUA1+zvxG8D2l54fk0VLeMWywFfKQbVwPTb0r8gv2v/hLcfCr4s32nO/mLfyPdRnJO1CeF5r0MBXddylLW+x5uIpeykkeMXSCfOD+Vc/q2lEXKlR3rqns2zlR9afJpKSxZdRkDggV2NNo5ouzuc7ptn5JBJ7+ldl8P7XWdd8RW/h7w/amW5uWCrgcL7mqvw+8FP4z8ZQeGUfasrcsVzX6zfsbf8Ez/AATZWegX8NhHHLOyySyABpGDFCc8cAAH8+2KyqT9nE9TBxc53O7/AOCT/wCypL4j8NDVvFqXCaUbdZ7eWJwGdxhQvIOOrH8K+9YvgN8MYIlhXwvFJtUDfNPKzH6nfWz4D8C+GPh34YtPCfhHSo7Oxs49kEMeeBnJJJJJOc9a2a8iaU5czO2rVcnZH4Pf8FA/gx8fPHPx90y703RHuvDAuDuYXGCX6AbB147+9fYX7Fnwh0HwB8JLUtoQs9TZFe6Lk7t2TwQfwr1s6FomuRot5p0cjIwKMy5wR0Iqc6dFaTgxRgYPYYriljnPCqjtY1jhOWs5lXW7D7TZTRSAtujKn1r89v8AgoJ+y3eePPiXF4jsNTMVzcQqkMTH5Vxxj2GOa/RW6HmFhjqK8F/ad8FSPeW3iY2zSR2amSQIDwvAruyWolW5Xszzc6ptUeZdD8u/GH7Jvxt8MB5z4OuLmBQSJYFLAgHrXGP4P8SWQaG/0mWJkbDh0Iwa/ZH9nbWPAfxI0SfSP7MiaZRsmhmjDccjIPfNcx+0r+xT8L7vRZfEum+H/JmYnzDAdoB7HHQ19dKlFPY+Ro4qrdJn5ifs4+GZ5PjXpcEdqXJlIZQK/e39irRo5bGzngj/AHen2e3B7E4X/GvzH+FXwP8ABvhf4iwSWelkXmSqyMwPUjnpX66fsm+EB4Y+FNnfyRlZtRUSuD2A4FeHjk76H1+Xz5qR6mhyKWkUAClNeU2bvc+P9Pl+yvt3ZGeKuSXEMhznmsyUyE5H6VCZJFYHcfzrxLXZ61+5eeZVYlj2rlviTo0nibw9d6ZEV3TQsmX6AGtZ5Xfl3/M1FMvmRsNw6V34OM6VVSRwYuVOrRcWfNv7ONtqPwi+K99Y3d35kLhl2q/cNzx7civoH4peLLPWPC0tlDKpEsKEYPQ5/wAK8x8ZaA+neKDfxy4wCdpUc598Zrl/H/xAvdH0yXyZQCI+CT0r9CoRlVw6kz4GsoxxHKjkPBULN8XRCVy6FtgHJJDDoK/Vn4Ia5b618NNKdI9ktvbLBNH3Vl4/XrX5q/sjaR/wlPxGPi24XJg3sCB1wyg/zz+FfY3hfxdrvhG8Fxo160Yz80ZOVb6ivBzCylZn1uXK1E+l0JIp6sAMYrzf4WfFzVvF/iVtE1aG3jU2rPF5aEFnDLx1PYsfwr0RdrjI/HivFkm9UdbWp8dzuFGFqlLIWbbmp7tyrYqO0VZDuI5+leWlY9Fu6IRGTkk024Rooi2fxrSihi37WTk9OKi1W2t0tJZXBwsZOBXfhIuc0cOJcYwaPnP41eK9Ti1hrPT2YPuJ3D0ryPxi3iXVLZheXheMrkjuPavUviiqS6rLeOoGxTuYntxXhnxd+IkWgafM9m24qCowcDNfqGCoKOHjc/P8Q08Vp3PUP2NPjZpXg3x0fCWsXMcaTgqM8n5jk8DnqBX2nBf21zCl1BKro4yCpr8TtL+KviPwv8TrPxasZmd7yOIkORtMjhAeuPvMvav3d/Z1/Z/0u6+E2ja14wvpZr27s45LyCKVgkcm3kAgjPPccGvls6o8ld2Pr8FONOlZlT4Ya19g+ImkmFwDNdCI/RgQa+jQp7CvnT4qeGLL4PeNvDeu6ZdMbKbUo4lV+WEvzPjJz1VT1+lfRkRLRhmXBIyRnpXgv3Ud0ZqR8iWljbXQYzShTjgmq5tUt5diODz1FNe0mKBnBwBzTbUx8hmwR615Frux6NvcuXZZESASSEDFct8QPFKafossqyKBtxWrrmpQ2sJw+4BfWvEPi/40FyHgjlIC9VDcdK+jyfASrVVc8XMK6pwbR5n8YvF8swfynUGQ7WHsa5bQvhZp/jPRJZ9WsxIm1uDz2NQ+NL9tXu4THHhSyZGevNem+ELOLRfDRguCp3A7vxGK+3r144eKj2PjLOrV5kfB/wAWfB2n6F4kuNM0ueWPypleNwfmjZWDAj6EA/hX7Ef8Erv27/C37R/wjsPA/irV/wCzvFmjWgjurK+IUXSIQpmRi2OTjjg89OK/Lf4ufC+/1fxzc6jbSqIpXARQCx98+ld98EPh/qXg7WLTUtNuZopQMzTwTMhHIwAQefp7V89mE44qKaPoMHzR3P0+/a18d+GfFtxpvhfRNatrttNvWuJzbyZ8qUKUXJHB4Y8c16n8KP2hPB2s+BrObxP4ht7fUIk8q6RlIyygfNwMcjBr4b+FCXUNq11cyNI0wG5nOSTmvSbSS2WABrh0PcKa8CaXNY9mmm4nVfbDKp+UAHggVk30qxnaoxye9JPqZgDfJu9eKz5tYhaRiUzgc5rzoxtJI75yaiYfj/Wm0/S5J4XDYTBHpXzd8QfE7zJJID1fB56V6z8W/EqQ+bDE2A0ZyN3Q5FfMnjXX7v8AtF1ZlEfmEncwAHNfoOTYf2VHnPj8zrSnPkR0nhvTf7Uv45pOURkyD6Fciun8W+I5bJW06xxjy/mPcGuM8A+O/CyQtYz+JdOWdpQvl/a0L9OOM56V2dj4Zg1WRr6W+EgZ+Ao4x/8AqrizLE3rWuaYPAtU+ZnFWvhyfVLzzSOpr0jwN4Jt7dI5bmNlj7A8bqn07w1BC2YY+/XFdLpOmyIBGd3HbFeDXxLSse3h6EUbugXMdjAsEA+UcCt+PUpSg57etZej2AVAChHIwcVrppq4ry3WbZ3OMYKx0jXry5DDrVPUiDGVVcZXrRRTj8aHV+AZb/sYah8edFTV9I+KR0G6kISPfo4u48lHcEgyJ/cI6/xdKzPBP/BHubTfiLpfif4v/GKz1/S7bUN40qz0E2/mP5cg3OxlbK8r8mMcE57UUV9zQxuJpYOVOL0t2X52ufD45J4uPqfY2mfs8/BKw0FNBX4T+G2hW0FtKW0WHdIgAHJ255wDXxf+118CvDPwU+P8GieBUW20fXtEGoQaaMlbGWOYxSKhOSUfcrAE/LtIHBoor5qEpSep9Bhf4JzljokRiGHGceladlpCDDbhx7UUVz10jug3c2LWzEaAls45HFWQxAwDRRXnvc3buf/Z";
                                    c1++;
                                    //如果五分钟内没有进行该操作，则保存照片 + 插入数据库
                                    String fileName = DateUtil.fileFormat.format(new Date()) + "_"+c1 +".png";
                                    System.out.println("s2========"+s2);
                                    if(c1 == 1){
                                        s3 = s2.substring(2,s2.length()-1);
                                    }else{
                                        s3 = s2.substring(1,s2.length()-1);
                                    }

                                    System.out.println("S3========="+s3);
//                                    byte[] b = decoder.decodeBuffer(result.getString("img_str"));
                                    byte[] b = decoder.decodeBuffer(s3);

//                            System.out.println(b.toString());
//                                    for (int i = 0; i < b.length; i++) {
//                                        System.out.println(b[i]);
//                                    }

                                    System.out.println("M111111111111111");
                                    // 处理数据
                                    for (int i = 0; i < b.length; ++i) {
                                        if (b[i] < 0) {
                                            b[i] += 256;
                                        }
                                    }
                                    System.out.println("M22222222222222");
                                    String outpath = Constants.FACEIMGOUTPUTPATH + video.getMonitorName() + "/";
//                                    System.out.println("地下的outpath==" + out2);
                                    File dir = new File(outpath);
                                    if (!dir.exists()) dir.mkdirs();
                                    OutputStream out2 = new FileOutputStream(outpath + fileName);
                                    System.out.println("地下的outpath==" + out2);
                                    out2.write(b);
                                    out2.flush();
                                    out2.close();


                                    log.info("监控点" + video.getMonitorName() + "有人，图片已存");
                                    System.out.println("监控点" + video.getMonitorName() + "有人，图片已存");
                                    faceRecord = new FaceRecordsEntity();
//						faceRecord.setResult(personCount + "个人");
                                    faceRecord.setResult("监控点：" + video.getMonitorName() + "，有人出现");
                                    faceRecord.setScore(personScore);
                                    faceRecord.setPictureUrl(video.getMonitorName() + "/" + fileName);
                                    insertFaceRecord(faceRecord);
                                    //3分钟内该监测点还有人就不开始新的录制
                                    //上面有录制视频的功能了，这里进入的前提是有人，可以截图，并且设置3min内视频不会重复录制
                                    jedis.setex(video.getMonitorName() + "_person", 3 * 60, "true");
                                }
                                personScore = 0;
                            }
//                            jedis.setex(video.getMonitorName() + "_person", 3 * 3, "true");
                            System.out.println("==========================================================================");
                            System.out.println("carAI="+carAI);
                            System.out.println("(!jedis.exists(video.getMonitorName() + _car))=="+(!jedis.exists(video.getMonitorName() + "_car")));
                            System.out.println("(carScore > 0.5)=="+(carScore > 0.5));
                            System.out.println("==========================================================================");
                        if (carAI && (!jedis.exists(video.getMonitorName() + "_car")) && (carScore > 0.5)) {
//                            System.out.println("==========================================================================");
//                            System.out.println("carAI="+carAI);
//                            System.out.println("(!jedis.exists(video.getMonitorName() + _car))=="+(!jedis.exists(video.getMonitorName() + "_car")));
//                            System.out.println("(carScore > 0.5)=="+(carScore > 0.5));
//                            System.out.println("==========================================================================");
                            System.out.println("caraiINININININ");
                            BASE64Decoder decoder = new BASE64Decoder();
                            // 解密
//                            byte[] b2 = decoder.decodeBuffer(result.getString("img_str"));

//                            String[] classes2 = result.getString("img_str").split(",");
                            String[] classes2 = result.getString("img_str_car").split(",");
                            System.out.println(classes2.length);
                            int c1 = 0;
                            if (classes2.length != 0) {
                                c1 = 0;
                            }
                            for (String s2 : classes2) {
                                String s3 = "";
                                c1++;
                                String fileName = DateUtil.fileFormat.format(new Date()) + "_"+c1 +".png";
                                System.out.println("s22========"+s2);
                                if(c1 == 1){
                                    s3 = s2.substring(2,s2.length()-1);
                                }else{
                                    s3 = s2.substring(1,s2.length()-1);
                                }
                                byte[] b2 = decoder.decodeBuffer(s3);
//                                byte[] b2 = decoder.decodeBuffer(s3);

                                // 处理数据
                                for (int i = 0; i < b2.length; ++i) {
                                    if (b2[i] < 0) {
                                        b2[i] += 256;
                                    }
                                }
//                                carRecord = callLicenseAI(file, video);
//                                if (carRecord == null) {
                                    String outpath = Constants.CARIMGOUTPUTPATH + video.getMonitorName() + "/";
                                    File dir = new File(outpath);
                                    if (!dir.exists()) dir.mkdirs();
                                    OutputStream out2 = new FileOutputStream(outpath + fileName);
                                    out2.write(b2);
                                    out2.flush();
                                    out2.close();
                                    carRecord = new CarRecordsEntity();
                                    carRecord.setResult("监控点：" + video.getMonitorName() + carCount + "辆车");
                                    carRecord.setScore(carScore);
                                    carRecord.setPictureUrl(video.getMonitorName() + "/" + fileName);
//                                }
                                log.info("监控点" + video.getMonitorName() + "有车，图片已存");
                                System.out.println("有车，图片已存");
                                insertCarRecord(carRecord);
                                jedis.setex(video.getMonitorName() + "_car", 1 * 60, "true");
                            }
                            carScore = 0;
                        }
//                        }
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }


        } catch (Exception e) {
            log.error("发送POST请求出现异常111！", e);
        }
        //删除照片文件

        return faceRecord;
    }


    public CarRecordsEntity callLicenseAI(File file, VideoEntity video) {

        CarRecordsEntity carRecord = null;
        DataOutputStream out = null;
        final String newLine = "\r\n";
        final String prefix = "--";
        try {
            String yolo = "http://"+host+":"+port+"/carID_image_predict";
            URL url = new URL(yolo);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String BOUNDARY = "-------7da2e536604c8";
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            out = new DataOutputStream(conn.getOutputStream());
            StringBuilder sb1 = new StringBuilder();
            sb1.append(prefix);
            sb1.append(BOUNDARY);
            sb1.append(newLine);
            sb1.append("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"" + newLine);
            sb1.append("Content-Type:application/octet-stream");
            sb1.append(newLine);
            sb1.append(newLine);
            out.write(sb1.toString().getBytes());
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            byte[] bufferOut = new byte[1024];
            int bytes = 0;
            while ((bytes = in.read(bufferOut)) != -1) {
                out.write(bufferOut, 0, bytes);
            }
            out.write(newLine.getBytes());
            in.close();

            byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(end_data);
            out.flush();
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            line = reader.readLine();

            JSONObject result = JSON.parseObject(line);
            String msg = "";
            if (!ObjectUtil.isEmptyString(result.getString("class_name"))) {
				/*msg = line.substring(line.indexOf("\"img_str\":\"")+"\"img_str\":\"".length(), line.indexOf("\"}"));
				log.info(msg);*/
                log.info("车牌识别算法：" + line.substring(0, 100));
                carRecord = new CarRecordsEntity();
                String[] results = result.getString("class_name").split("/n");
                StringBuffer IDs = new StringBuffer();
                double score = 0;
                for (String s : results) {
                    String[] oneResult = s.split(" ");
                    IDs.append(oneResult[0] + " ");
                    double thisScore = Double.parseDouble(oneResult[1]);
                    if (thisScore > score) {
                        score = thisScore;
                    }
                }
                carRecord.setResult(IDs.toString());
                carRecord.setScore(score);

                BASE64Decoder decoder = new BASE64Decoder();
                try {
                    // 解密
                    byte[] b = decoder.decodeBuffer(result.getString("img_str_car"));
                    // 处理数据
                    for (int i = 0; i < b.length; ++i) {
                        if (b[i] < 0) {
                            b[i] += 256;
                        }
                    }
                    String fileName = DateUtil.fileFormat.format(new Date()) + ".png";

                    System.out.println('\n'+fileName+'\n');

                    String outpath = Constants.CARIMGOUTPUTPATH + video.getMonitorName() + "/";
                    File dir = new File(outpath);
                    if (!dir.exists()) dir.mkdirs();
                    OutputStream out2 = new FileOutputStream(outpath + fileName);
                    out2.write(b);
                    out2.flush();
                    out2.close();
                    carRecord.setPictureUrl(video.getMonitorName() + "/" + fileName);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }


        } catch (Exception e) {
            log.error("发送POST请求出现异常2222！", e);
            e.printStackTrace();
        }
        return carRecord;
    }

    public void runExample(String PARENT_DIR, VideoEntity video) throws java.lang.Exception {

        File parentDir = FileUtils.getFile(PARENT_DIR);

        FileAlterationObserver observer = new FileAlterationObserver(parentDir);

        observer.addListener(new FileAlterationListenerAdaptor() {

            @Override
            public void onFileCreate(File file) {

                if(ProcessUtil.IS_WINDOWS){
                    callFaceAI(file, video);
                }else {
                    ImageProcessor imageProcessor=new ImageProcessor();
                    double personFLag=imageProcessor.processImages(parentDir);
                    VideoRecorderService videoRecorderService = VideoConfiguration.getBean(VideoRecorderService.class, video.getMonitorName());
                    // FLag为1代表有人出现或在有面积较大的活动的物体
                    if (personFLag==1) {

                        callFaceAI(file, video);
                    }else {
                        videoRecorderService.setStatus(false);
                    }
                }

            }

            @Override
            public void onFileDelete(File file) {
//                log.info("File deleted: " + file.getName());
            }

            @Override
            public void onDirectoryCreate(File dir) {
                System.out.println("Directory created: " + dir.getName());
            }

            @Override
            public void onDirectoryDelete(File dir) {
                System.out.println("Directory deleted: " + dir.getName());
            }
        });

        // 轮询间隔
        long interval = TimeUnit.SECONDS.toMillis(1);//将一秒转换为毫秒数
        //创建文件变化监听器
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);

        monitor.start();
    }
    // 方法：获取目录中最新创建的N个文件
    private File[] getLatestFiles(File directory, int n) {
        File[] files = directory.listFiles();
        if (files != null && files.length > 0) {
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            return Arrays.copyOfRange(files, 0, Math.min(files.length, n));
        }
        return new File[]{}; // 如果没有文件，返回空数组
    }
    public static double calculateAverageDifference(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();

        long sumDiff = 0;
        int totalPixels = width * height;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = (rgb1) & 0xff;

                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = (rgb2) & 0xff;

                int diffR = r1 - r2;
                int diffG = g1 - g2;
                int diffB = b1 - b2;

                sumDiff += (Math.abs(diffR) + Math.abs(diffG) + Math.abs(diffB)); // 计算RGB差值的平均
            }
        }

        return (double) sumDiff / totalPixels; // 返回所有像素差的平均值
    }
    private static ThreadLocal<String> localRtsp = new ThreadLocal<String>();


    public void VideoRecord() {

//        final int CORE_POOL_SIZE = 5;
//        final int MAX_POOL_SIZE = 10;
//        final int QUEUE_CAPACITY = 100;
//        final Long KEEP_ALIVE_TIME = 1L;
        Map<VideoEntity,Boolean> onLineMap = new HashMap<>();
//
//        ThreadPoolExecutor executor = new ThreadPoolExecutor(
//                CORE_POOL_SIZE,
//                MAX_POOL_SIZE,
//                KEEP_ALIVE_TIME,
//                TimeUnit.SECONDS,
//                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
//                new ThreadPoolExecutor.CallerRunsPolicy());



        //清除无效MP4文件



        threadPoolTaskExecutor.submit(()->{
            deleteInvalidVideo(Constants.LOCALVIDEOPATH);
            try{
            while(true){
                List<VideoEntity> tempvideoList = selectAllVideos();
                for (int i = 0; i < tempvideoList.size(); i++) {
                    if(!onLineMap.containsKey(tempvideoList.get(i)) || (!onLineMap.get(tempvideoList.get(i)) && RtspUtils.isOnline(tempvideoList.get(i).getRtsp()))){
                        onLineMap.put(tempvideoList.get(i),true);
                        String inputFile = tempvideoList.get(i).getRtsp();
                        FileUtil.isDirExist(Constants.LOCALVIDEOPATH);
                        threadPoolTaskExecutor.submit(()->{
                            VideoService.localRtsp.set(inputFile);
                            while(RtspUtils.isOnline(localRtsp.get())){
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                                long time = System.currentTimeMillis();
                                String dateStr = dateFormat.format(new Date(time));
                                String outputFile = Constants.LOCALVIDEOPATH+ videoMapping.selectVideosByRtsp(inputFile).get(0).getMonitorName() + "-" +dateStr + ".mp4";
                                RtspUtils.videoPuller(inputFile, 60, outputFile);
                                try{

                                    //判断视频是否是有人出现的视频
                                    copyWaringVideos(time,outputFile,inputFile,dateStr);

                                    //保存截屏图片
                                    Map<String,Object> rst = RtspUtils.getScreenshot(outputFile);

                                    LocalVideoEntity localVideoEntity = new LocalVideoEntity();
                                    localVideoEntity.setVideoName(videoMapping.selectVideosByRtsp(inputFile).get(0).getMonitorName() + "-" +dateStr);

                                    localVideoEntity.setVideoPath(outputFile);
                                    localVideoEntity.setVideoPng(rst.get("imgPath").toString());
                                    localVideoEntity.setCreateTime(dateStr);
                                    localVideoEntity.setUpdateTime(dateStr);

                                    videoMapping.insertLocalVideoInfo(localVideoEntity);

                                }catch (Exception e){
                                    e.printStackTrace();
                                    log.error(e.toString());
                                }
                            }
                            List<VideoEntity> temp = videoMapping.selectVideosByRtsp(localRtsp.get());
                            if(!temp.isEmpty()){
                                onLineMap.remove(temp.get(0));
                            }
                        });
                    }
                }
                for (Map.Entry<VideoEntity,Boolean> e : onLineMap.entrySet()) {
                    if(videoMapping.selectVideosByRtsp(e.getKey().getRtsp()).isEmpty()){
                        onLineMap.remove(e.getKey());
                    }
                }
                Thread.currentThread().wait(10000);

            }
            }catch(Exception e){
                log.error("创建录制视频线程失效");
            }

        });
    }

    public static void BufferedOneArrayOne(String inputFile,String outputFile) throws
            IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
        byte[] bytes = new byte[2048];
        int len;
        while ((len = bis.read(bytes)) != -1) {
            bos.write(bytes,0,len);
        }
        bis.close();
        bos.close();
    }

    //删除失效的mp4视频
    public void deleteInvalidVideo(String FileName) {
        File file = new File(FileName);
        if (file.exists()) {
            File[] videos = file.listFiles();
            if(videos.length > 0){
                for (File f : videos) {
                    if (!check(f.getPath())) {
                        f.delete();
                    }
                }
            }
        }
    }

    //定时删除五天的数据
    @Transactional
    @Scheduled(cron = "0 0 12 * * ?")
    public void removeExpiredVideo(){
        List<LocalVideoEntity> list = videoMapping.getAllLocalVideo();
        for(LocalVideoEntity e : list){
            String recordDateString = e.getCreateTime();
            Date recordDate = new Date(recordDateString);
            Date tempDate = new Date(System.currentTimeMillis());
            if(getDateDiff(recordDate,tempDate)>5){

                File picfile = new File(Constants.LOCALPICPATH + e.getVideoPath().replace("/","\\"));
                if(picfile.exists()){
                    if(picfile.delete()){
                        log.info("文件删除成功");
                    }else{
                        log.error("文件"+picfile+"删除失败");
                    }
                }
                File videofile = new File(e.getVideoPath());
                if(videofile.exists()){
                    if(videofile.delete()){
                        log.info("文件删除成功");
                    }else{
                        log.error("文件"+videofile+"删除失败");
                    }
                }
                videoMapping.deleteLocalVideoInfo(e.getId());
            }
        }
    }

    public long getDateDiff(Date endDate,Date nowDate){
        long nd = 1000*24*60*60;
        long nh = 1000*60*60;
        long nm = 1000*60;

        long diff = endDate.getTime() - nowDate.getTime();

        return diff/nd;
    }


    @Transactional
    public void copyWaringVideos(long time,String origionFile,String monitorName,String dateStr){
        LogProcessor logProcessor = new LogProcessor();

        String timeOfLog = new SimpleDateFormat("yyyy_MM_dd_HH").format(new Date(time))+".log";
        try{
            // 提取 rtsp_url，去掉 "rtsp://"
            String rtspUrl = monitorName.substring("rtsp://".length());

            // 分割 user:password 和 ip:port
            String[] userIpSplit = rtspUrl.split("@", 2);
            String userPassword = userIpSplit[0];
            String ipPort = userIpSplit[1];

            // 分割 username 和 password
            String[] userSplit = userPassword.split(":", 2);
            String username = userSplit[0];
            String password = userSplit[1];

            // 分割 ip 和 port
            String ip = ipPort.substring(0, ipPort.lastIndexOf(":"));
            System.out.println("Constants.IMAGERSTLOGPATH+ip+ File.separator + timeOfLog:");
            System.out.println(Constants.IMAGERSTLOGPATH+ip+ File.separator + timeOfLog);


//            if(logProcessor.processLogFile(Constants.IMAGERSTLOGPATH+monitorName+ File.separator + timeOfLog) == Constants.HASPERSON){
            if(logProcessor.processLogFile(Constants.IMAGERSTLOGPATH+ip+ File.separator + timeOfLog) == Constants.HASPERSON){
                String dateStrFile = dateStr.replace(" ","_").replace(":","_");
//                String copyFile = Constants.WARINGVIDEOPATH+ monitorName + "-" +dateStrFile + ".mp4";
                String copyFile = Constants.WARINGVIDEOPATH+ ip + "-" +dateStrFile + ".mp4";

                System.out.println("copyFile:");
                System.out.println(copyFile);



                FileUtil.isDirExist(Constants.WARINGVIDEOPATH);
                BufferedOneArrayOne(origionFile,copyFile);

                WaringVideosEntity waringVideos = new WaringVideosEntity();
//                waringVideos.setVideoName(monitorName + "-" +dateStr + ".mp4");
                waringVideos.setVideoName(ip + "-" +dateStr + ".mp4");
                waringVideos.setVideoPath(copyFile);
                waringVideos.setCreateTime(dateStr);
                waringVideos.setUpdateTime(dateStr);

                videoMapping.insertWaringVideoInfo(waringVideos);
                System.out.println("success");

            }
        }catch (Exception e){
            log.error(e.toString());
            e.printStackTrace();
        }
    }


}

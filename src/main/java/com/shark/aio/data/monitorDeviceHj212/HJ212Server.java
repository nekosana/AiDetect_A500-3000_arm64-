package com.shark.aio.data.monitorDeviceHj212;

import com.fazecast.jSerialComm.SerialPort;
import com.shark.aio.alarm.GradedAlarm.JedisConnectionFactory;
import com.shark.aio.alarm.GradedAlarm.SerializeUtil;
import com.shark.aio.alarm.contactPart.util.HttpClientUtil;
import com.shark.aio.data.monitorDeviceHj212.entity.MonitorDeviceEntity;
import com.shark.aio.data.monitorDeviceHj212.entity.SerialDeviceEntity;
import com.shark.aio.data.monitorDeviceHj212.entity.SerialEntity;
import com.shark.aio.data.monitorDeviceHj212.entity.Serials_type;
import com.shark.aio.data.monitorDeviceHj212.mapper.SerialsMapping;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author DaHuaJia
 * @Describe 环保设备对接程序启动类，Netty服务器端启动类，接收设备上传的数据。
 * @Date 2022-10-09 11:08:32
 */
@Slf4j
@Component
@Order(1)
public class HJ212Server implements ApplicationRunner {

    /**
     * Netty服务端监听的端口号
     */
    public static final int PORT = 9999;

    /**
     * 分发线程组,用于处理客户端的连接请求
     */
    //表示一个 NIO 的EventLoopGroup
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(10);

    /**
     * 工作线程组, 用于处理与各个客户端连接的 IO 操作
     */
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(20);
    @Autowired
    JedisConnectionFactory jedisConnectionFactory;
    private ScheduledExecutorService scheduler;
    @Autowired
    private DataProcessingService dataProcessingService;
    @Autowired
    private SerialsMapping serialsMapping;
    private static final Logger log = LoggerFactory.getLogger(HJ212Server.class);
    private final List<SerialPort> openPorts = new ArrayList<>();
    public HJ212Server(DataProcessingService dataProcessingService) {
        this.dataProcessingService = dataProcessingService;
    }

    /**
     * 启动服务
     */
    public static void runEPServer(){
        try{
            //ServerBootstrap是一个用来创建服务端Channel的工具类，创建出来的Channel用来接收进来的请求；只用来做面向连接的传输，像TCP/IP。
            ServerBootstrap b = new ServerBootstrap();
            //创建事件循环组
            b.group(bossGroup, workerGroup);
            //指定 Channel 的类型. 因为是服务器端, 因此使用了 NioServerSocketChannel.
            b.channel(NioServerSocketChannel.class);
            //Handler: 设置数据的处理器,指定client处理Handler
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    //接收客户端请求的处理流程
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("serverDecoder", new StringDecoder(CharsetUtil.UTF_8));
                    pipeline.addLast("serverEncoder", new StringEncoder(CharsetUtil.UTF_8));
                    // netty提供了空闲状态监测处理器 0表示禁用事件
                    pipeline.addLast(new IdleStateHandler(65,0,0, TimeUnit.MINUTES));
                    pipeline.addLast(new HJ212ServerHandler());
                }
            });
            log.info("HJ212 端口号  =  " + PORT);
            b.bind(PORT).sync();

        }catch (Exception e){
            e.printStackTrace();
            shutdown();
        }
    }

    /**
     * 关闭服务
     */
    public static void shutdown(){
        // 优雅关闭
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    @Override
    public void run(ApplicationArguments args) {
        // 启动环保监测Netty服务端
        runEPServer();
        startURLPortListener();
//        startSerialPortListener();
        //startSerialPortListenerEnd();
    }



    private void startURLPortListener() {
        // 初始化 ScheduledExecutorService，使用单线程调度器
        scheduler = Executors.newSingleThreadScheduledExecutor();
        String ip = "4nbsf9900182.vicp.fun"; // 替换为目标IP
        String port = "27020";         // 替换为目标端口
        String endpoint = "/getPara"; // 替换为目标接口
        String url = String.format("http://%s:%s%s", ip, port,endpoint);
        // 定义定时任务
        Runnable task = () -> {
            try (Jedis jedis = jedisConnectionFactory.getJedis()) {
                List<SerialDeviceEntity> deviceList = new ArrayList<>();

                Set<String> allKeys = jedis.keys("SerialDevice_*");

                for (String key : allKeys) {
                    // 查找包含 "serials" 的设备 ID
                    // 反序列化设备数据
                    byte[] data = jedis.get(key.getBytes());
                    SerialDeviceEntity serialDevice = (SerialDeviceEntity) SerializeUtil.deserialize(data);

                    // 将设备数据添加到列表
                    deviceList.add(serialDevice);

                }
                for(SerialDeviceEntity serialDevice:deviceList)
                {
                    String name = serialsMapping.getTypeWithBrandAndModel(serialDevice.getBrandModel().split("_")[0],serialDevice.getBrandModel().split("_")[1]);
                    Map<String, String> params = new HashMap<>();
                    params.put("name", name);

                    try {
                        // 发送GET请求，假设不需要额外的查询参数
                        String responseData = HttpClientUtil.doGet(url, params);
//                        responseData = "{\n" +
//                                "    \"cleaningStartTime\": 0,\n" +
//                                "    \"max\": 1,\n" +
//                                "    \"spanEndTime\": 0,\n" +
//                                "    \"range\": 1\n" +
//                                "}";

                        if (responseData != null && !responseData.isEmpty()) {
                            System.out.println("成功获取数据");
                            // 处理数据
                            dataProcessingService.processDataSerial(responseData, null,serialDevice,name);
                        } else {
                            System.out.println("请求返回空数据或响应为空");
                        }
                    } catch (Exception e) {
                        log.error("发送HTTP请求或处理数据时发生错误", e);
                    }
                }
            }catch (Exception e1) {
                log.error("redis加入失败！", e1);
            }
        };
        // 安排任务每小时执行一次，初始延迟为0
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.HOURS);
        log.info("已启动定时任务，每小时发送一次请求到 http://{}:{}/{}", "192.168.1.100", "8080", "/api/data");
    }

    private void startSerialPortListenerEnd() {
        Runnable task = () -> {
            try (Jedis jedis = jedisConnectionFactory.getJedis()) {
                List<SerialDeviceEntity> deviceList = new ArrayList<>();

                // 获取 Redis 中所有的键，假设键以 monitorDevice_ 开头
                Set<String> allKeys = jedis.keys("SerialDevice_*");
                for (String key : allKeys) {
                    // 反序列化设备数据
                    byte[] data = jedis.get(key.getBytes());
                    SerialDeviceEntity serialDevice = (SerialDeviceEntity) SerializeUtil.deserialize(data);
                    // 将设备数据添加到列表
                    deviceList.add(serialDevice);
                }
                for (SerialDeviceEntity serialDevice : deviceList) {
                    SerialEntity serialEntity = new SerialEntity();
                    serialEntity.setPort(serialDevice.getPort());
                    serialEntity.setBaud_rate(serialDevice.getBaudRate());
                    serialEntity.setStart_bit(serialDevice.getStartBit());
                    serialEntity.setEnd_bit(serialDevice.getEndBit());
                    String brand = serialDevice.getBrandModel().split("_")[0];
                    String model = serialDevice.getBrandModel().split("_")[1];
                    String info = serialDevice.getInfo();
                    configureAndStartListenerEnd(serialEntity, brand, model, info);
                }

            } catch (Exception e1) {
                log.error("redis加入失败！", e1);
            }
        };
        // 安排任务每小时执行一次，初始延迟为0
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.HOURS);
    }

    private void configureAndStartListenerEnd(SerialEntity serialEntity,String brand,String model,String info) throws IOException, InterruptedException {
        SerialPort serialPort = SerialPort.getCommPort(serialEntity.getPort());
        serialPort.setComPortParameters(serialEntity.getBaud_rate(), 8, serialEntity.getEnd_bit(), serialEntity.getStart_bit());
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
        if (serialPort.openPort()) {
            log.info("成功打开串口: {}", serialPort.getSystemPortName());
        } else {
            log.error("无法打开串口 {}", serialPort.getSystemPortName());
            return;
        }
        InputStream in = serialPort.getInputStream();
        int datalen = in.available();
        byte[] buffer;
        int len;
        while (datalen > -1) {
            datalen = in.available();
            buffer = new byte[datalen];
            len = in.read(buffer);
            String received = new String(buffer, 0, len, StandardCharsets.UTF_8);
            System.out.println(received);
            dataProcessingService.processData(received, brand,model,info);
            break;
        }
    }

    private void startSerialPortListener() {
            // 列出所有可用的串口
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            System.out.println("未检测到可用的串口设备");
            return;
        }
        System.out.println("检测到"+ports.length+"个可用的串口设备");
        for (SerialPort serialPort : ports) {
            configureAndStartListener(serialPort);
        }
    }
    private void configureAndStartListener(SerialPort serialPort) {

        System.out.println("准备配置串口: "+serialPort.getSystemPortName());
        // 配置串口参数
        serialPort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
        if (serialPort.openPort()) {
            log.info("成功打开串口: {}", serialPort.getSystemPortName());
            openPorts.add(serialPort);
        } else {
            log.error("无法打开串口 {}", serialPort.getSystemPortName());
            return;
        }
        // 创建新的线程来读取串口数据
        new Thread(() -> {
            try {
                InputStream in = serialPort.getInputStream();

                int datalen = in.available();
                byte[] buffer;
                int len;
                while ( datalen>-1 ) {
                    Thread.sleep(10000);
                    datalen = in.available();
                    buffer = new byte[datalen];
                    len = in.read(buffer);
//                    String received = new String(buffer, 0, len, StandardCharsets.UTF_8);
//                    System.out.println(received);
//                    log.info("接收到串口数据: {}", received);
                    // 调用 DataProcessingService 处理数据，ctx 传递 null
//                    dataProcessingService.processData(received, null);
                }
            }  catch (IOException | InterruptedException e) {
                log.error("串口 {} 读取错误", serialPort.getSystemPortName(), e);
            } finally {
                if (serialPort.isOpen()) {
                    serialPort.closePort();
                    log.info("串口 {} 已关闭", serialPort.getSystemPortName());
                }
            }
        }, "SerialPort-Listener-Thread-" + serialPort.getSystemPortName()).start();
    }

}

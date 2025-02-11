package com.shark.aio.data.monitorDeviceHj212;

import com.alibaba.fastjson.JSONObject;
import com.shark.aio.alarm.GradedAlarm.JedisConnectionFactory;
import com.shark.aio.alarm.GradedAlarm.SerializeUtil;
import com.shark.aio.alarm.contactPart.ContractPartController;
import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.alarm.contactPart.util.ProcessUtil;
import com.shark.aio.alarm.entity.AlarmRecordEntity;
import com.shark.aio.alarm.entity.AlarmSettingsEntity;
import com.shark.aio.base.information.InformationMapping;
import com.shark.aio.data.monitorDeviceHj212.entity.MonitorDeviceEntity;
import com.shark.aio.data.monitorDeviceHj212.mapper.SerialsMapping;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @author DaHuaJia
 * @Description 环保设备数据处理类，处理设备的上传数据
 * @Date 2022-10-09 11:08:57
 **/
@Slf4j
@Component
public class HJ212ServerHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    SerialsMapping serialsMapping;
    @Autowired
    protected MonitorDeviceService monitorDeviceService;
    @Autowired
    protected ContractPartController contractPartController;
    @Autowired
    MonitorDeviceMapping monitorDeviceMapping;
    @Autowired
    InformationMapping informationMapping;

    @Autowired
    JedisConnectionFactory jedisConnectionFactory;
    // 当前类
    private static HJ212ServerHandler hJ212ServerHandler;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        hJ212ServerHandler = this;
        hJ212ServerHandler.monitorDeviceService = this.monitorDeviceService;
        hJ212ServerHandler.contractPartController = this.contractPartController;
        hJ212ServerHandler.informationMapping = this.informationMapping;
        hJ212ServerHandler.monitorDeviceMapping = this.monitorDeviceMapping;
        hJ212ServerHandler.serialsMapping = this.serialsMapping;
    }


    /**
     * 定义一个HashMap，用于保存所有的channel和设备ID的对应关系。
     */
    private static Map deviceInfo = new HashMap(64);


    /**
     * 消息读取
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                Jedis jedis = new Jedis("127.0.0.1", 6379);
        System.out.println("收到HJ212协议数据为 ===> " + msg);


        //CRC校验
        if (!HJ212MsgUtils.checkData((String) msg).equals("error")) {
            // 解析物联网设备发来的数据
            JSONObject data = null;
            try(Jedis jedis = hJ212ServerHandler.jedisConnectionFactory.getJedis()){
                data = HJ212MsgUtils.dealMsg1(jedis, (String) msg);
            }

            //存储数据
            if (data != null) {
//                System.out.println("设备消息解析JSON结果：" + data.toJSONString());
                try {
                    //由mn对应数据库找到绑定的监测点名称
                    String deviceId = net.sf.json.JSONObject.fromObject(data).getString("MN");

                    if (deviceId != null) {
//                        MonitorDeviceEntity monitorDevice = hJ212ServerHandler.monitorDeviceService.getMonitorDevice(deviceId);
                        try (Jedis jedis = hJ212ServerHandler.jedisConnectionFactory.getJedis()) {
                            if (!jedis.exists(deviceId)) System.out.println("未绑定deviceId");
                            if (jedis.exists(deviceId)) {
                                if ("false".equals(jedis.get(deviceId))) {
                                    hJ212ServerHandler.monitorDeviceMapping.updateMonitorDevice(deviceId, "true");
                                }
                                MonitorDeviceEntity monitorDevice = (MonitorDeviceEntity) SerializeUtil.deserialize(jedis.get(("monitorDevice_" + deviceId).getBytes()));

                                //回应数采仪
                                push(ctx);
                                //redis三小时key,是否失去连接超过三小时
                                jedis.setex(deviceId, 3 * 60 * 60, "true");
                                String monitorName = monitorDevice.getMonitorName();
                                String monitorClass = monitorDevice.getMonitorClass();
                                SimpleDateFormat DataFormat = new SimpleDateFormat("yyyy-MM-dd");
                                String date = DataFormat.format(new Date());
                                //根目录 + 监测点 + 日期
                                String documentPath = ProcessUtil.IS_WINDOWS ?
                                        Constants.DATAROOTPATH + monitorClass + "\\" + monitorName + "\\" + date :
                                        Constants.DATAROOTPATH + monitorClass + "/" + monitorName + "/" + date;

                                String filePath = documentPath + (ProcessUtil.IS_WINDOWS ? "\\" : "/") + monitorClass + ".txt";
                                File document = new File(documentPath);
                                if (!document.exists()) {
                                    document.mkdirs();
                                }
                                File file = new File(filePath);

                                OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file, true)), StandardCharsets.UTF_8);
                                out.write((data.toJSONString() + "\n"));
                                out.flush();
                                out.close();


                                //报警服务
                                JSONObject CPObject = data.getJSONObject("CP");

                                for (String key : CPObject.keySet()) {
                                    if (jedis.exists("alarm_" + key)) {
                                        AlarmSettingsEntity alarmSettingsEntity = (AlarmSettingsEntity) SerializeUtil.deserialize(jedis.get(("alarm_" + key).getBytes()));

                                        Double value = Double.parseDouble(CPObject.getString(key));
                                        if (!jedis.exists(key)) {
                                            jedis.setex(key, 24 * 60 * 60, "1");
                                        }
                                        //判断8小时恒定值
                                        if (!jedis.exists("invariable_" + key)) {
                                            jedis.setex("invariable_" + key, 8 * 60 * 60, String.valueOf(value));
                                        } else {
                                            if (value != Double.parseDouble(jedis.get("invariable_" + key))) {
                                                jedis.setex("invariable_" + key, 8 * 60 * 60, String.valueOf(value));
                                            }
                                        }

                                        if (value > alarmSettingsEntity.getLowerLimit() &&
                                                value < alarmSettingsEntity.getUpperLimit()) {

                                        } else {
                                            Timestamp alarmTime = Timestamp.valueOf(data.getString("DataTime"));
                                            AlarmRecordEntity alarmRecordEntity = new AlarmRecordEntity();
                                            alarmRecordEntity.setAlarmTime(alarmTime);
                                            alarmRecordEntity.setMonitor(monitorName);
                                            alarmRecordEntity.setMonitorClass(monitorClass);
                                            alarmRecordEntity.setMonitorValue(key);
                                            alarmRecordEntity.setMonitorData(value.toString());
                                            alarmRecordEntity.setMessage(alarmSettingsEntity.getMessage());
                                            alarmRecordEntity.setUnit(alarmSettingsEntity.getUnit());

                                            hJ212ServerHandler.monitorDeviceService.insertAlarmRecord(alarmRecordEntity);
                                            //超限增加1次
                                            jedis.incr(key);
                                            if ("3".equals(jedis.get(key))) {
                                                //超过3次超标，传到园区端
//                                                InformationEntity informationEntity = hJ212ServerHandler.informationMapping.getInformation();
                                                hJ212ServerHandler.contractPartController.alarmToPart2("more3", key, null, alarmRecordEntity, alarmTime);
                                                jedis.setex(key, 24 * 60 * 60, "1");
                                            }

                                        }
                                    }
                                }

                            }
                        } catch (Exception e1) {
                            log.error("redis加入失败！", e1);
                        }
                    }

                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                    log.error("HJ212数据接收失败！", e);
                }

            }
        }


    }

    /***
     * 超时关闭socket 连接
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;

            String eventType = null;
            switch (event.state()) {
                case READER_IDLE:
                    eventType = "读超时";
                    break;
                case WRITER_IDLE:
                    eventType = "写超时";
                    break;
                case ALL_IDLE:
                    eventType = "读写超时";
                    break;
                default:
                    eventType = "设备超时";
            }
            log.warn(ctx.channel().id() + " : " + eventType + "---> 关闭该设备");
            ctx.channel().close();
        }
    }

    /**
     * 异常处理, 出现异常关闭channel
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        log.error("========= 链接出现异常！exceptionCaught.");
//        ctx.fireExceptionCaught(cause);
//        onUnhandledInboundException(cause);
        // 将通道从deviceInfo中删除
        deviceInfo.remove(ctx.channel().id());
        // 关闭通道链接，节省资源
        ctx.channel().close();
    }
    protected void onUnhandledInboundException(Throwable cause) {
        try {
            log.error("========= 重写！================");
        } finally {
            io.netty.util.ReferenceCountUtil.release(cause);
        }
    }

    /**
     * 每加入一个新的链接，保存该通道并写入上线日志。该方法在channelRead方法之前执行
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        // 获取设备ID
        String deviceId = "abc";
        // 将该链接保存到deviceInfo
        deviceInfo.put(ctx.channel().id(), deviceId);
//        log.warn("========= " + ctx.channel().id() + "设备加入链接。");
    }

    /**
     * 每去除一个新的链接，去除该通道并写入下线日志
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        // 将通道从deviceInfo中删除
        deviceInfo.remove(ctx.channel().id());
//        log.warn("========= " + ctx.channel().id() + "设备断开链接。");
    }


    /**
     * 向一个客户端发送消息
     *
     * @param
     */

    public void push(ChannelHandlerContext ctx) {

        Channel channel = ctx.channel();

//        System.out.println(ctx.channel());
        SimpleDateFormat DataFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String date = DataFormat.format(new Date());
        channel.writeAndFlush("##0087QN=" + date + ";ST=91;CN=9014;PW=123456;MN=010000A8900016F000169DC0;Flag=4;CP=&&&&2F80\r\n");
    }


}


//class MessageEncodeFixedLengthHandler extends MessageToByteEncoder<Message> {
//    @Override
//    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
//        String jsonStr = msg.toJsonString();
//        // 如果长度不足，则进行补0
//        if (jsonStr.length() < length) {
//            jsonStr = addSpace(jsonStr);
//        }
//        // 使用Unpooled.wrappedBuffer实现零拷贝，将字符串转为ByteBuf
//        ctx.writeAndFlush(Unpooled.wrappedBuffer(jsonStr.getBytes()));
//    }
//}



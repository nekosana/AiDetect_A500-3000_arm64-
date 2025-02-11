package com.shark.aio.alarm.GradedAlarm;

import com.shark.aio.alarm.entity.AlarmSettingsEntity;
import com.shark.aio.alarm.mapper.AlarmMapping;
import com.shark.aio.data.monitorDeviceHj212.MonitorDeviceMapping;
import com.shark.aio.data.monitorDeviceHj212.MonitorDeviceService;
import com.shark.aio.data.monitorDeviceHj212.SerialDeviceService;
import com.shark.aio.data.monitorDeviceHj212.entity.FieldChineseMapperEntity;
import com.shark.aio.data.monitorDeviceHj212.entity.MonitorDeviceEntity;
import com.shark.aio.data.monitorDeviceHj212.entity.SerialDeviceEntity;
import com.shark.aio.data.monitorDeviceHj212.mapper.SerialsMapping;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Controller;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.List;

/**
 * @author lbx
 * @date 2023/5/8 - 14:05
 **/

/**
 * 如果接收到信息，如果id存在redis，就重新设置过期时间
 * 如果id不存在redis，报警，新建字段和过期时间
 * 3h还没接到，改id的字段失效
 */
@Controller
@Slf4j
@Order(3)
public class RedisSetex implements ApplicationRunner {

    @Autowired
    protected AlarmMapping alarmMapping;
    @Autowired
    MonitorDeviceMapping monitorDeviceMapping;
    @Autowired
    protected MonitorDeviceService monitorDeviceService;

    @Autowired
    protected SerialDeviceService serialDeviceService;

    @Autowired
    JedisConnectionFactory jedisConnectionFactory;

    @Autowired
    SerialsMapping serialsMapping;

    Jedis jedis = new Jedis("192.168.0.114", 6379);



    @Override
    public void run(ApplicationArguments args) throws Exception {
        //redis存对象，需要序列化
//        AlarmRecordEntity alarmRecordEntity = new AlarmRecordEntity();
//        jedis.set("user".getBytes(), SerializeUtil.serialize(alarmRecordEntity));
//        System.out.println("--------------------------------------------------------------------");
//        AlarmRecordEntity userResult = (AlarmRecordEntity) SerializeUtil.deserialize(jedis.get("user".getBytes()));
//        System.out.println(userResult.toString());


        try (Jedis jedis = jedisConnectionFactory.getJedis()) {
            //jedis.auth("dianzi327");
            //与其用sleep， 不如用管道  20240730thg
            Pipeline pipeline = jedis.pipelined();
            //接入系统的所有设备id
            List<String> allDevice = getDeviceId();
            for (String id : allDevice) {
                //
                pipeline.setex(id, 3 * 60 * 60, "false");
//            jedis.setex(id, 10, "true");
                System.out.println(id + "已加入了redis");
            }
            List<String> allSerialDevice = getSerialDeviceId();
            for (String id : allSerialDevice) {
                //
                pipeline.setex(id, 3*60*60, "false");
//            jedis.setex(id, 10, "true");
                System.out.println(id + "已加入了redis");
            }

            //报警阈值加入redis
            List<AlarmSettingsEntity> allAlarmSettings = monitorDeviceService.getAllAlarmSettings();
            for (AlarmSettingsEntity alarmSettings : allAlarmSettings) {
                pipeline.set(("alarm_" + alarmSettings.getMonitorValue()).getBytes(), SerializeUtil.serialize(alarmSettings));
//                System.out.println(alarmSettings.getMonitorValue() + "已加入了redis");
                //Thread.currentThread().sleep(200);
            }

            //数采仪参数代码映射的中文名称
            List<FieldChineseMapperEntity> allFieldChinese = getFieldChineseMapper();
            for (FieldChineseMapperEntity field : allFieldChinese) {
                pipeline.set(field.getCode(), field.getChinese());
                //Thread.currentThread().sleep(200);
//            System.out.println(field.getCode() + "已加入了redis");
            }

            //设备信息加入redis，方便数据存储调用信息
            List<MonitorDeviceEntity> allMonitorDeviceEntity = monitorDeviceService.getMonitorDevice();
            for (MonitorDeviceEntity monitorDeviceEntity : allMonitorDeviceEntity) {
                pipeline.set(("monitorDevice_" + monitorDeviceEntity.getDeviceId()).getBytes(), SerializeUtil.serialize(monitorDeviceEntity));
            }
            List<SerialDeviceEntity> allSerialDeviceEntity = serialDeviceService.getSerialDevice();
            for (SerialDeviceEntity serialDeviceEntity : allSerialDeviceEntity) {
                pipeline.set(("SerialDevice_" + serialDeviceEntity.getBrandModel()).getBytes(), SerializeUtil.serialize(serialDeviceEntity));
            }

            pipeline.sync();//管道提交
            for (String id : allDevice) {
                System.out.println(id + "已加入了redis");
            }
            for (String id : allSerialDevice) {
                System.out.println(id + "已加入了redis");
            }

        } catch (Exception e) {
            log.error("redis加入失败！", e);
        }

    }

    public List<String> getSerialDeviceId() {
        try {
            List<String> allDeviceId = serialsMapping.getAllDeviceId();
            return allDeviceId;
        } catch (Exception e) {
            // TODO: handle exception
            log.error("GradedAlarmController/getDeviceId, 获取全部设备id失败, ", e);
            return null;
        }
    }


    public List<String> getDeviceId() {
        try {
            List<String> allDeviceId = alarmMapping.getAllDeviceId();
            return allDeviceId;
        } catch (Exception e) {
            // TODO: handle exception
            log.error("GradedAlarmController/getDeviceId, 获取全部设备id失败, ", e);
            return null;
        }
    }


    public List<FieldChineseMapperEntity> getFieldChineseMapper() {
        try {
            List<FieldChineseMapperEntity> allFieldChineseMapper = monitorDeviceMapping.getAllFieldMapping();
            return allFieldChineseMapper;
        } catch (Exception e) {
            // TODO: handle exception
            log.error("GradedAlarmController/getFieldChineseMapper, 获取全部字段映射失败, ", e);
            return null;
        }
    }
}
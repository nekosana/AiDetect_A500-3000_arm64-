package com.shark.aio.data.monitorDeviceHj212;

import com.shark.aio.alarm.GradedAlarm.JedisConnectionFactory;
import com.shark.aio.alarm.GradedAlarm.SerializeUtil;
import com.shark.aio.alarm.entity.AlarmRecordEntity;
import com.shark.aio.alarm.entity.AlarmSettingsEntity;
import com.shark.aio.data.monitorDeviceHj212.entity.MonitorDeviceEntity;
import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.alarm.contactPart.util.ObjectUtil;
import com.shark.aio.serial.mapper.SerialMapper;
import com.shark.aio.serial.utils.SerialUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author lbx
 * @date 2023/3/19 - 14:53
 **/
@Slf4j
@Service
public class MonitorDeviceService {
    @Autowired
    MonitorDeviceMapping monitorDeviceMapping;
    @Autowired
    SerialMapper serialMapper;

    @Autowired
    JedisConnectionFactory jedisConnectionFactory;


    /**
     * 由数据包里的mn找到对应的监测点
     *
     * @return
     */
    public List<MonitorDeviceEntity> getMonitorDevice(){
        try{
            List<MonitorDeviceEntity> monitorDeviceEntity = monitorDeviceMapping.getAllMonitorDeviceEntity();

            return monitorDeviceEntity;
        }catch (Exception e){
            log.error("ConditionService/getMonitorDevice:获取监测点名字失败！",e);
            return null;
        }
    }

    public List<AlarmSettingsEntity> getAllAlarmSettings(){
        try{
            List<AlarmSettingsEntity> alarmSettingsEntity = monitorDeviceMapping.getAllAlarmSettingsEntity();

            return alarmSettingsEntity;
        }catch (Exception e){
            log.error("ConditionService/getAllAlarmSettings:获取报警设置失败！",e);
            return null;
        }
    }

    public void insertAlarmRecord(AlarmRecordEntity alarmRecordEntity){
        try{
            monitorDeviceMapping.insertAlarmRecordEntity(alarmRecordEntity);
//            return "success";
        }catch (Exception e){
            log.error("ConditionService/insertAlarmRecord:报警插入失败！",e);
//            return null;
        }
    }
    /**
     * 获取全部设备名称
     * @return List
     */
    public List<String> getSerialDeviceNames(){
        try {
            List<String> SerialDeviceNames = serialMapper.findName();
            return SerialDeviceNames;
        }catch (Exception e){
            log.error("PollutionService/getAllDeviceId:获取全部监测点失败！",e);
            return null;
        }
    }

    /**
     * 工具方法，供控制层调用，向request中存放所有监测类型和污染物
     * 查询监测类型或污染物名称失败后，返回false
     * @param request 要设置的request
     * @return 是否成功
     */
    public boolean searchMonitor(HttpServletRequest request){
        List<String> allMonitor = getAllMonitor();
        List<String> allMonitorClass = getAllMonitorClass();
        List<String> allDeviceId = getAllDeviceId();
        List<String> allSerial = SerialUtils.getAllSerial();
        //20241223更改加入串口设备名称
        List<String> allSerialDeviceNames = getSerialDeviceNames();
        List<String> allSerialDeviceBrandModel = serialMapper.selectSerialDevicesBrandModel();
        List<String> allConfigModel = serialMapper.getAllConfigModel();
        List<String> allConfigDevice = serialMapper.getBrandAndModelFromWaterMonitorDevice();
        if (allMonitor == null || allMonitorClass == null){
            return false;
        }
        request.setAttribute(Constants.ALLMONITOR, allMonitor);
        request.setAttribute(Constants.ALLMONITORCLASS, allMonitorClass);
        request.setAttribute("allDeviceId", allDeviceId);
        request.setAttribute("allSerialDeviceName", allSerialDeviceNames);
        request.setAttribute("allSerialDeviceBrandModel", allSerialDeviceBrandModel);
        request.setAttribute("allSerial", allSerial);
        request.setAttribute("allConfigModel", allConfigModel);
        request.setAttribute("allConfigDevice", allConfigDevice);
        return true;
    }

    /**
     * 获取全部设备id
     * @return 全部监测类型List
     */
    public List<String> getAllDeviceId(){
        try {
            List<String> allDeviceId = monitorDeviceMapping.getAllDeviceId();
            return allDeviceId;
        }catch (Exception e){
            log.error("PollutionService/getAllDeviceId:获取全部监测点失败！",e);
            return null;
        }
    }
    /**
     * 获取全部监测点
     * @return 全部监测类型List
     */
    public List<String> getAllMonitor(){
        try {
            List<String> allMonitor = monitorDeviceMapping.getAllMonitor();
            return allMonitor;
        }catch (Exception e){
            log.error("PollutionService/getAllMonitor:获取全部监测点失败！",e);
            return null;
        }
    }
    /**
     * 获取全部监测类型
     * @return 全部监测类型List
     */
    public List<String> getAllMonitorClass(){
        try {
            List<String> allMonitorClass = monitorDeviceMapping.getAllMonitorClass();
            return allMonitorClass;
        }catch (Exception e){
            log.error("AlarmService/getAllMonitorClass:获取全部监测类型失败！",e);
            return null;
        }
    }
    /**
     * 获取全部污染物名称
     * @return 全部污染物List
     */
    public List<String> getAllPollutionName(){
        try {
            List<String> allPollutionName = monitorDeviceMapping.getAllPollutionName();
            return allPollutionName;
        }catch (Exception e){
            log.error("AlarmService/getAllMonitorClass:获取全部污染物名称失败！",e);
            return null;
        }
    }
    /**
     * 新增预警设置或监测类型或污染物类型
     * 预警设置中，监测值不能重复
     * 监测类型和污染物类型不能和已有的重复
     * @param monitorDeviceEntity 新关联
     * @param newMonitorName 新监测名称
     * @param existMonitorName 已有的监测名称
     * @return 返回给页面的消息
     */
    public String managMonitorDevice(MonitorDeviceEntity monitorDeviceEntity,String newMonitorName, String existMonitorName,
                                     String deleteDeviceId, String deleteMonitorName){
        if (!ObjectUtil.isEmpty(monitorDeviceEntity)){
            if(monitorDeviceEntity.getDeviceId() != null){
                try (Jedis jedis = jedisConnectionFactory.getJedis()) {
                    monitorDeviceMapping.insert(monitorDeviceEntity);
                    jedis.setex(monitorDeviceEntity.getDeviceId(), 3*60*60, "false");
                    jedis.set(("monitorDevice_" + monitorDeviceEntity.getDeviceId()).getBytes(), SerializeUtil.serialize(monitorDeviceEntity));
                    log.info("新增监测点与设备关联成功！");
                }catch (DataIntegrityViolationException e){
                    log.error("pollutionMonitor:新增监测点与设备关联失败！",e);
                    return "设备不能和已有的重复！";
                }
                return "新增设备成功！";
            }

        }

        //新增监测值，判断是否和已有的监测值重复
        if (newMonitorName!=null){
            //插入新的监测类型
            if (!ObjectUtil.isEmptyString(existMonitorName)){
                List<String> allMonitor = new ArrayList<>();
                Collections.addAll(allMonitor, existMonitorName.split(","));
                if (allMonitor.contains(newMonitorName)){
                    return "和已有监测类型重复！";
                }
            }
            try {
                monitorDeviceMapping.insertMonitor(newMonitorName);
                log.info("新增监测类型成功！");
                return "新增监测类型成功！";
            }catch (Exception e){
                log.error("新增监测类型失败！",e);
                return "新增监测类型失败！";
            }
        }
        if (deleteDeviceId !=null){
            try {
                monitorDeviceMapping.deleteDeviceId(deleteDeviceId);
                log.info("删除设备ID成功！");
                return "删除设备ID成功！";
            }catch (Exception e){
                log.error("删除设备ID失败！",e);
                return "删除设备ID失败！";
            }
        }
        if (deleteMonitorName !=null){
            try {
                monitorDeviceMapping.deleteMonitorName(deleteMonitorName);
                log.info("删除监测点成功！");
                return "删除监测点成功！";
            }catch (Exception e){
                log.error("删除监测点失败！",e);
                return "删除监测点失败！";
            }
        }
        return "未接收到数据！";
    }

    /*
     * 删除一条设备记录
     */
    public String deleteDevice(String id) {
        if (id == null) {
            return Constants.FAILCODE;
        }
        try {
            monitorDeviceMapping.deleteDeviceById(id);
            try (Jedis jedis = jedisConnectionFactory.getJedis()) {
                if(jedis.exists(id)){
                    jedis.del(id);
                }
                if(jedis.exists(("monitorDevice_" + id).getBytes())){
                    jedis.del(id);
                }
            }
            return Constants.SUCCESSCODE;
        } catch (Exception e) {
            log.info("BackSysService/deleteUser, 删除一条设备记录失败, ", e);
            return Constants.FAILCODE;
        }
    }


}

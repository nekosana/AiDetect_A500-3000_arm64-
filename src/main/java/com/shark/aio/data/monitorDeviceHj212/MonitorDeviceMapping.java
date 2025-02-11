package com.shark.aio.data.monitorDeviceHj212;

import com.shark.aio.alarm.entity.AlarmRecordCompanyMediumEntity;
import com.shark.aio.alarm.entity.AlarmRecordEntity;
import com.shark.aio.alarm.entity.AlarmSettingsEntity;
import com.shark.aio.data.monitorDeviceHj212.entity.FieldChineseMapperEntity;
import com.shark.aio.data.monitorDeviceHj212.entity.MonitorDeviceEntity;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author lbx
 * @date 2023/3/19 - 14:31
 **/
@Mapper
public interface MonitorDeviceMapping {
    @Select("select name from monitor")
    public List<String> getAllMonitor();
    @Select("select * from `monitor_device` ")
    List<MonitorDeviceEntity> getAllMonitorDeviceEntity();

    @Select("SELECT * from `monitor_device` WHERE device_id = #{deviceId};")
    MonitorDeviceEntity getMonitorDeviceEntityByDeviceId(@Param("deviceId")String deviceId);

    @Select("SELECT * from `alarm_settings`")
    List<AlarmSettingsEntity> getAllAlarmSettingsEntity();
    @Select("select name from pollution")
    public List<String> getAllPollutionName();

    @Select("select device_id from monitor_device")
    public List<String> getAllDeviceId();

    /**
     * 从监测类型表monitor_class表中查询所有监测类型
     * @return 所有监测类型List
     */
    @Select("select name from monitor_class")
    public List<String> getAllMonitorClass();

    @Insert("INSERT INTO `alarm_records` (`alarm_time`, `monitor`, `monitor_class`, `monitor_value`, `monitor_data` , `unit`, `message`) " +
            "VALUES (#{alarmTime}, #{monitor}, #{monitorClass}, #{monitorValue}, #{monitorData}, #{unit}, #{message}  );")
    void insertAlarmRecordEntity(AlarmRecordEntity alarmRecordEntity);

    @Insert("INSERT INTO `alarm_records_company_medium`(`alarm_time`, `company`, `monitor_name`, `monitor_class`, `device_id`, `message`) " +
            " VALUES( #{alarmTime}, #{company}, #{monitorName}, #{monitorClass}, #{deviceId}, #{message});")
    void insertAlarmRecordCompanyMedium(AlarmRecordCompanyMediumEntity alarmRecordCompanyMediumEntity);

    @Insert("INSERT INTO `alarm_records_company_high`(`alarm_time`, `company`, `key`,  `message`) " +
            " VALUES( #{alarmTime}, #{company}, #{key}, #{message});")
    void insertAlarmRecordCompanyHigh(Timestamp alarmTime, String company, String key, String message);

    @Update("UPDATE `monitor_device` SET `status`=#{status} WHERE `device_id`=#{deviceId};")
    void updateMonitorDevice(@Param("deviceId") String deviceId, @Param("status") String status);


    @Insert("INSERT INTO `monitor_device` (`monitor_name`, `monitor_class`, `device_id`, `status`) VALUES (#{monitorName}, #{monitorClass}, #{deviceId}, #{status});")
    void insert(MonitorDeviceEntity monitorDeviceEntity);

    /**
     * 向污染物类型pollution表中插入新的污染物
     * @param name 新污染物名称
     * @return 成功插入的记录数，即插入成功为1，插入失败为0
     */
    @Insert("INSERT INTO `pollution` SET `name`=#{name}")
    public int insertPollution(String name);

    /**
     * 向监测点monitor表中插入一条新的记录
     * @param name 监测类型名
     * @return 成功插入的记录数，即插入成功为1，插入失败为0
     */
    @Insert("INSERT INTO `monitor` SET `name`=#{name}")
    public int insertMonitor(String name);


    @Delete("delete from `monitor_device` where device_id = #{deleteDeviceId}")
    void deleteDeviceId(String deleteDeviceId);

    @Delete("delete from `monitor` where name = #{deleteMonitorName}")
    void deleteMonitorName(String deleteMonitorName);
    @Delete("delete from `monitor_device` where device_id = #{deleteMonitorId}")
    void deleteDeviceById(String deleteMonitorId);



    @Select("select * from `field_mapping` ")
    List<FieldChineseMapperEntity> getAllFieldMapping();

}

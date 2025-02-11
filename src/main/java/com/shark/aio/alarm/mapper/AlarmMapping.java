package com.shark.aio.alarm.mapper;

import com.shark.aio.alarm.entity.AlarmRecordCompanyHighEntity;
import com.shark.aio.alarm.entity.AlarmRecordCompanyMediumEntity;
import com.shark.aio.alarm.entity.AlarmRecordEntity;
import com.shark.aio.alarm.entity.AlarmSettingsEntity;
import com.shark.aio.alarm.service.AlarmService;
import org.apache.ibatis.annotations.*;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface AlarmMapping {


    @Select("select device_id from monitor_device")
    List<String> getAllDeviceId();

    /**
     * 查询所有预警设置
     * @return 所有预警设置List
     */
    @Select("select * from alarm_settings")
    List<AlarmSettingsEntity> getAllAlarmSettings();

    /**
     * 查询某类监测项目的数据
     * @param monitorClass
     * @return
     */
    @Select("select * from `alarm_settings` where `monitor_class` = #{monitorClass}")
    AlarmSettingsEntity getAlarmSettingsEntity(String monitorClass);

    /**
     * 根据查询关键字feature，查询符合要求的预警设置
     * 有一个字段符合查询关键字即可
     * @param feature 查询关键字
     * @return 查询结果
     */
    @Select("select * from alarm_settings " +
            "where monitor_class like #{feature} " +
            "or monitor_value like #{feature} " +
            "or lower_limit like #{feature} " +
            "or upper_limit like #{feature} " +
            "or unit like #{feature} " +
            "or message like #{feature}")
    public List<AlarmSettingsEntity> getAlarmSettingsByFeatures(String feature);

    @Select("select * from `alarm_settings` where `monitor_value`  = #{monitorValue}")
    AlarmSettingsEntity getAlarmSettings(String monitorValue);
    /**
     * 查询预警设置表alarm_settings
     * 根据监测值查询监测值
     * 该方法用于判断，给定的监测值在数据库中是否存在
     * @param monitorValue 监测值
     * @return 监测值
     */
    @Select("select monitor_value from alarm_settings where monitor_value=#{monitorValue}")
    public String getMonitorValue(String monitorValue);

    /**
     * 向预警设置表alarm_settings插入新的记录
     * @param alarmSettingsEntity 要插入的记录
     * @return 成功插入的记录数，即插入成功为1，插入失败为0
     */
    @Insert("insert into alarm_settings values (null,#{monitorClass},#{monitorValue},#{lowerLimit},#{upperLimit},#{unit},#{message})")
    public int insertAlarmEntity(AlarmSettingsEntity alarmSettingsEntity);

    /**
     * 根据id删除alarm_settings表中的一条记录
     * @param id 要删除的记录的id
     * @return 成功删除的记录数，即删除成功为1，删除失败为0
     */
    @Delete("delete from alarm_settings where id=#{id}")
    public int deleteAlarmSettingById(int id);

    /**
     * 根据id编辑alarm_settings表中的记录
     * @param alarmSettingsEntity 编辑后的字段
     * @return 成功编辑的记录数，即编辑成功为1，编辑失败为0
     */
    @Update("UPDATE `alarm_settings` " +
            "SET " +
            "`monitor_class` = #{monitorClass}, " +
            "`monitor_value` = #{monitorValue}, " +
            "`lower_limit` = #{lowerLimit}, " +
            "`upper_limit` = #{upperLimit}, " +
            "`unit` = #{unit}, " +
            "`message` = #{message} " +
            "WHERE `id`=#{id}")
    public int updateAlarmSetting(AlarmSettingsEntity alarmSettingsEntity);

    /**
     * 从监测类型表monitor_class表中查询所有监测类型
     * @return 所有监测类型List
     */
    @Select("select name from monitor_class")
    List<String> getAllMonitorClass();

    @Select("select name from monitor")
    List<String> getAllMonitor();
    /**
     * 向监测类型monitor_class表中插入一条新的记录
     * @param name 监测类型名
     * @return 成功插入的记录数，即插入成功为1，插入失败为0
     */
    @Insert("INSERT INTO `monitor_class` SET `name`=#{name}")
    int insertMonitorClass(String name);

    /**
     * 向监测点monitor表中插入一条新的记录
     * @param name 监测类型名
     * @return 成功插入的记录数，即插入成功为1，插入失败为0
     */
    @Insert("INSERT INTO `monitor` SET `name`=#{name}")
    int insertMonitor(String name);

    /**
     * 从污染物类型pollution表中查询所有污染物
     * @return 所有污染物List
     */
    @Select("select name from pollution")
    List<String> getAllPollutionName();

    /**
     * 向污染物类型pollution表中插入新的污染物
     * @param name 新污染物名称
     * @return 成功插入的记录数，即插入成功为1，插入失败为0
     */
    @Insert("INSERT INTO `pollution` SET `name`=#{name}")
    int insertPollution(String name);




    /**
     * 查询所有报警记录
     * @return 报警记录List
     */
    @Select("SELECT * FROM `alarm_records`")
    List<AlarmRecordEntity> getAllAlarmRecords();

    @Select("SELECT * FROM `alarm_records_company_high`")
    List<AlarmRecordCompanyHighEntity> getAllAlarmRecordsHigh();
    @Select("SELECT * FROM `alarm_records_company_medium`")
    List<AlarmRecordCompanyMediumEntity> getAllAlarmRecordsMedium();

    /**
     * 根据条件查询报警记录
     * @param features 查询条件
     * @return 报警记录List
     */
    @SelectProvider(type = AlarmService.class, method = "selectRecordsByDynamicSql")
    List<AlarmRecordEntity> getAlarmRecordsByFeature(HashMap<String,String> features);

    @SelectProvider(type = AlarmService.class, method = "selectRecordsMediumByDynamicSql")
    List<AlarmRecordCompanyMediumEntity> getAlarmRecordsMediumByFeature(HashMap<String,String> features);

    @SelectProvider(type = AlarmService.class, method = "selectRecordsHighByDynamicSql")
    List<AlarmRecordCompanyHighEntity> getAlarmRecordsByHighFeature(HashMap<String,String> features);


}

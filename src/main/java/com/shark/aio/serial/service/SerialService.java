package com.shark.aio.serial.service;

import com.github.pagehelper.PageInfo;
import com.shark.aio.alarm.entity.AlarmRecordEntity;
import com.shark.aio.serial.entity.*;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SerialService {

    List getMonitorBandByName(String name);
    List getMonitorModelByBand(String band);
    List getMonitorNames();
    List getMonitorModelByBrandAndName(String brand,String name);
    List getMonitorSoftwareversionByBandAndModel(String band,String model);
    int insertSerialDevice(SerialDeviceEntity serialDevice);
    Boolean delSerialDevice(String bandmodel);
    Boolean updateSerialDevce(SerialDeviceEntity serialDevice);
    List<SerialDeviceEntity> selectSerialDevices();
    List<SerialDeviceEntity> selectSerialDevicesByBrandmodel(String brandModel);
    List<String> selectSerialDevicesBrandModel();
    List<SerialMonitorAlarmEntity> selectAllAlarmRecords();
    PageInfo<SerialMonitorAlarmEntity> getAlarmRecordsByPage(Integer pageNum, Integer pageSize, HashMap<String,String> features);
    boolean setAttributeBYSeriamMonitorsAndDataNames(HttpServletRequest request);
    int insertSerialMonitorAlarmRecords(SerialMonitorAlarmEntity serialMonitorAlarm);

    //查询所有的配置类型
    List<String> getAllConfigModel();
    //创建配置表，以品牌_型号_版本命名
    void createConfigWaterQualityMonitorDevice(String tableName);
    //根据配置模型如ZP查询参数名称
    List<String> getFieldNamesWithChineseName(String type);
    //插入配置表
    void insertIntoDeviceTable(String tableName, ConfigDeviceEntity configDeviceEntity);
    List<ConfigDeviceEntity> getAllConfigDeviceInfo( String tableName);

    Boolean updateConfigDevice( String tableName, ConfigDeviceEntity configDeviceEntity);
    List<String> getBrandAndModelFromWaterMonitorDevice();
    void deleteDeviceByBrandAndModel(String brand,  String model);
    int checkTableExists( String tableName);
    void deleteTable( String tableName);
    int checkDeviceExistence(String brand, String model, String softwareVersion);
    void insertWaterQualityMonitorDevice(WaterQualityMonitorDeviceEntity waterQualityMonitorDevice);

    List<InfoAndNameVo> getInfoByDeviceId(String deviceId);

    List<ThresholdAndNameVo> getThresholdByType(String type);

}

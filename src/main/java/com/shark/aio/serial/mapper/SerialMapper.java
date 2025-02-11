package com.shark.aio.serial.mapper;

import com.shark.aio.alarm.entity.AlarmRecordEntity;
import com.shark.aio.alarm.service.AlarmService;
import com.shark.aio.serial.entity.*;
import com.shark.aio.serial.service.SerialServiceImpl;
import org.apache.ibatis.annotations.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface SerialMapper {
    // 根据 name 查询所有的 brand（去重）
    @Select("SELECT DISTINCT brand FROM water_quality_monitor_device WHERE name = #{name}")
    List<String> findBrandsByName(@Param("name") String name) ;

    // 根据 brand 查询所有的 model（去重）
    @Select("SELECT DISTINCT model FROM water_quality_monitor_device WHERE brand = #{brand}")
    List<String> findModelsByBrand(@Param("brand") String brand);

    @Select("SELECT DISTINCT name FROM water_quality_monitor_device ")
    List<String> findName();
    @Select("SELECT DISTINCT software_version FROM water_quality_monitor_device WHERE brand = #{brand} AND model = #{model}")
    List<String> findSoftwareVersionsByBrandAndModel(@Param("brand") String brand, @Param("model") String model);
    @Select("SELECT DISTINCT model FROM water_quality_monitor_device WHERE brand = #{brand} AND name = #{name}")
    List<String> getMonitorModelByBandAndName(@Param("brand") String brand, @Param("name") String name);
    @Insert("INSERT INTO serial_device (brand_model, software_version, communication_id, port, baud_rate, start_bit, end_bit, info, `status`) " +
            "VALUES (#{brandModel}, #{softwareVersion}, #{communicationId}, #{port}, #{baudRate}, #{startBit}, #{endBit}, #{info}, #{status})")
    int insertSerialDevice(SerialDeviceEntity serialDevice);

    // 根据 brand_model 删除一条 SerialDevice 记录
    @Delete("DELETE FROM serial_device WHERE brand_model = #{brandModel}")
    Boolean delSerialDevice(@Param("brandModel") String brandModel);

    // 更新一条 SerialDevice 记录
    @Update("UPDATE serial_device SET software_version = #{softwareVersion}, communication_id = #{communicationId}, port = #{port}, " +
            "baud_rate = #{baudRate}, start_bit = #{startBit}, end_bit = #{endBit}, info = #{info}, status = #{status} " +
            "WHERE brand_model = #{brandModel}")
    Boolean updateSerialDevice(SerialDeviceEntity serialDevice);

    // 查询所有 SerialDevice 记录
    @Select("SELECT * FROM serial_device WHERE brand_model = #{brandModel}")
    List<SerialDeviceEntity> selectSerialDevicesByBrandmodel(@Param("brandModel") String brandModel);

    @Select("SELECT * FROM serial_device")
    List<SerialDeviceEntity> selectSerialDevices();
    @Select("SELECT brand_model FROM serial_device")
    List<String> selectSerialDevicesBrandModel();

    //报警表查询操作
    @Select("SELECT monitor_type FROM serial_alarm_records")
    List<String> selectAllAlarmRecordsDataType();
    @Select("SELECT * FROM serial_alarm_records")
    List<SerialMonitorAlarmEntity> selectAllAlarmRecords();
    @SelectProvider(type = SerialServiceImpl.class, method = "selectRecordsByDynamicSql")
    List<SerialMonitorAlarmEntity> getAlarmRecordsByFeature(HashMap<String,String> features);
    //插入报警信息
    @Insert("INSERT INTO serial_alarm_records (record_time, brand_model, monitor_type, monitor_data, info) " +
            "VALUES (#{recordTime}, #{brandModel}, #{monitorType}, #{monitorData}, #{info})")
    int insertSerialAlarmRecord(SerialMonitorAlarmEntity serialAlarmRecordEntity);

    //获得所有的配置类型
    @Select("SELECT model_info FROM serial_device_info")
    List<String> getAllConfigModel();
    //创建配置表
    // 创建表的方法 - 表名根据品牌、型号和版本动态拼接
    @Insert("CREATE TABLE `${tableName}` ("
            + "id INT NOT NULL AUTO_INCREMENT, "
            + "name VARCHAR(200), "
            + "registeraddr VARCHAR(20), "
            + "registeraddrlen VARCHAR(20), "
            + "verifyaddr VARCHAR(20), "
            + "verifyaddrlen VARCHAR(20), "
            + "info VARCHAR(100), "
            + "PRIMARY KEY (id));")
    void createConfigWaterQualityMonitorDevice(@Param("tableName") String tableName);
    //查询配置参数名
    @Select("SELECT CONCAT(field_name, '_', chinese_name) AS field_name_chinese_name "
            + "FROM field_translation "
            + "WHERE type = #{type}")
    List<String> getFieldNamesWithChineseName(@Param("type") String type);
    //插入配置表信息
    // 插入动态表中的数据
    // 使用实体类插入数据到动态表中
    @Insert("INSERT INTO `${tableName}` (name, registeraddr, registeraddrlen, verifyaddr, verifyaddrlen, info) "
            + "VALUES (#{configDeviceEntity.name}, #{configDeviceEntity.registeraddr}, #{configDeviceEntity.registeraddrlen}, #{configDeviceEntity.verifyaddr}, "
            + "#{configDeviceEntity.verifyaddrlen}, #{configDeviceEntity.info})")
    void insertIntoDeviceTable(
            @Param("tableName") String tableName,
            @Param("configDeviceEntity") ConfigDeviceEntity configDeviceEntity);
    // 查询表中所有设备参数信息
    @Select("SELECT * FROM `${tableName}` ")
    List<ConfigDeviceEntity> getAllConfigDeviceInfo(@Param("tableName") String tableName);
    //跟新配置表数据
    @Update("UPDATE ${tableName} SET " +
            "registeraddr = #{configDeviceEntity.registeraddr}, " +
            "registeraddrlen = #{configDeviceEntity.registeraddrlen}, " +
            "verifyaddr = #{configDeviceEntity.verifyaddr}, " +
            "verifyaddrlen = #{configDeviceEntity.verifyaddrlen}, " +
            "info = #{configDeviceEntity.info} " +
            "WHERE name = #{configDeviceEntity.name}")
    Boolean updateConfigDevice(@Param("tableName") String tableName,
                               @Param("configDeviceEntity")ConfigDeviceEntity configDeviceEntity);

    @Select("SELECT COUNT(*) FROM water_quality_monitor_device WHERE brand = #{brand} AND model = #{model} AND software_version = #{softwareVersion}")
    int checkDeviceExistence(@Param("brand") String brand, @Param("model") String model, @Param("softwareVersion") String softwareVersion);
    //查询数据库中是否有指定表
    @Select("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = #{tableName}")
    int checkTableExists(@Param("tableName") String tableName);

    //插入water_quality_monitor_device
    @Insert("INSERT INTO water_quality_monitor_device (name, brand, model, software_version, info) " +
            "VALUES (#{name}, #{brand}, #{model}, #{softwareVersion}, #{info})")
    void insertWaterQualityMonitorDevice(WaterQualityMonitorDeviceEntity waterQualityMonitorDevice);
    //删除指定表
    @Update("DROP TABLE IF EXISTS ${tableName}")
    void deleteTable(@Param("tableName") String tableName);

    //查询配置设备名
    @Select("SELECT CONCAT(brand, '_', model) AS brand_model "
            + "FROM water_quality_monitor_device ")
    List<String> getBrandAndModelFromWaterMonitorDevice();
    //删除配置设备通过brand，model
    @Delete("DELETE FROM water_quality_monitor_device WHERE brand = #{brand} AND model = #{model}")
    void deleteDeviceByBrandAndModel(@Param("brand") String brand, @Param("model") String model);

    @Select("SELECT `name`, `info` FROM `${table_name}` ")
    List<InfoAndNameVo> getInfoByDeviceId(@Param("table_name") String table_name);

    @Select("SELECT `field_name`, `threshold` FROM `field_translation` WHERE type = #{type} AND `threshold` IS NOT NULL")
    List<ThresholdAndNameVo> getThresholdByType(@Param("type") String type);



}

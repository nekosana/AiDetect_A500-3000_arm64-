package com.shark.aio.serial.service;

import com.github.pagehelper.PageInfo;
import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.alarm.contactPart.util.ObjectUtil;
import com.shark.aio.alarm.entity.AlarmRecordEntity;
import com.shark.aio.serial.entity.*;
import com.shark.aio.serial.mapper.SerialMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.swing.plaf.synth.SynthEditorPaneUI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SerialServiceImpl implements SerialService{
    @Autowired
    private SerialMapper serialMapper;
    @Override
    public List getMonitorBandByName(String name) {
        return serialMapper.findBrandsByName(name);
    }

    @Override
    public List getMonitorModelByBand(String band) {
        return serialMapper.findModelsByBrand(band);
    }

    @Override
    public List getMonitorNames() {
        return serialMapper.findName();
    }

    @Override
    public List getMonitorModelByBrandAndName(String brand, String name) {
        return serialMapper.getMonitorModelByBandAndName(brand,name);
    }

    @Override
    public List getMonitorSoftwareversionByBandAndModel(String band, String model) {
        return serialMapper.findSoftwareVersionsByBrandAndModel(band,model);
    }

    @Override
    public void insertWaterQualityMonitorDevice(WaterQualityMonitorDeviceEntity waterQualityMonitorDevice) {
        serialMapper.insertWaterQualityMonitorDevice(waterQualityMonitorDevice);
    }

    @Override
    public List<InfoAndNameVo> getInfoByDeviceId(String deviceId) {
        return serialMapper.getInfoByDeviceId(deviceId);
    }

    public  List<ThresholdAndNameVo> getThresholdByType(String type){
        return serialMapper.getThresholdByType(type);
    }


    @Override
    public int insertSerialDevice(SerialDeviceEntity serialDevice) {
        return serialMapper.insertSerialDevice(serialDevice);
    }

    @Override
    public Boolean delSerialDevice(String bandmodel) {
        return serialMapper.delSerialDevice(bandmodel);
    }

    @Override
    public Boolean updateSerialDevce(SerialDeviceEntity serialDevice) {
        return serialMapper.updateSerialDevice(serialDevice);
    }

    @Override
    public List<SerialDeviceEntity> selectSerialDevices() {
        return serialMapper.selectSerialDevices();
    }

    @Override
    public List<SerialDeviceEntity> selectSerialDevicesByBrandmodel(String brandModel) {
        return serialMapper.selectSerialDevicesByBrandmodel(brandModel);
    }

    @Override
    public List<String> getAllConfigModel() {
        return serialMapper.getAllConfigModel();
    }

    @Override
    public void createConfigWaterQualityMonitorDevice(String tableName) {
        serialMapper.createConfigWaterQualityMonitorDevice(tableName);
    }

    @Override
    public List<String> getFieldNamesWithChineseName(String type) {
        return serialMapper.getFieldNamesWithChineseName(type);
    }

    @Override
    public void insertIntoDeviceTable(String tableName, ConfigDeviceEntity configDeviceEntity) {
        serialMapper.insertIntoDeviceTable(tableName,configDeviceEntity);
    }

    @Override
    public List<ConfigDeviceEntity> getAllConfigDeviceInfo(String tableName) {
        return serialMapper.getAllConfigDeviceInfo(tableName);
    }

    @Override
    public Boolean updateConfigDevice(String tableName, ConfigDeviceEntity configDeviceEntity) {
        return serialMapper.updateConfigDevice(tableName,configDeviceEntity);
    }

    @Override
    public List<String> getBrandAndModelFromWaterMonitorDevice() {
        return serialMapper.getBrandAndModelFromWaterMonitorDevice();
    }

    @Override
    public void deleteDeviceByBrandAndModel(String brand, String model) {
        serialMapper.deleteDeviceByBrandAndModel(brand,model);
    }

    @Override
    public int checkDeviceExistence(String brand, String model, String softwareVersion) {
        return serialMapper.checkDeviceExistence(brand,model,softwareVersion);
    }
    @Override
    public int checkTableExists(String tableName) {
        return serialMapper.checkTableExists(tableName);
    }

    @Override
    public void deleteTable(String tableName) {
        serialMapper.deleteTable(tableName);
    }

    @Override
    public List<String> selectSerialDevicesBrandModel() {
        return serialMapper.selectSerialDevicesBrandModel();
    }

    @Override
    public List<SerialMonitorAlarmEntity> selectAllAlarmRecords() {
        return serialMapper.selectAllAlarmRecords();
    }

    @Override
    public PageInfo<SerialMonitorAlarmEntity> getAlarmRecordsByPage(Integer pageNum, Integer pageSize, HashMap<String, String> features) {
        List<SerialMonitorAlarmEntity> allAlarmRecords = null;
        try{
            if (features==null || features.isEmpty()){
                allAlarmRecords = serialMapper.selectAllAlarmRecords();
            }
            else{
                System.out.println("map:"+features);
                allAlarmRecords = serialMapper.getAlarmRecordsByFeature(features);
            }
//
        }catch (Exception e){
            System.out.println("获取报警记录失败！"+e);
        }
        return new PageInfo<>(allAlarmRecords, 5);
    }

    @Override
    public boolean setAttributeBYSeriamMonitorsAndDataNames(HttpServletRequest request) {
        List<String> allSerialMonitors = serialMapper.selectSerialDevicesBrandModel();
        //目前没有总监测数据表，暂时先查询已经记录报警的数据得到所有监测数据类型
        List<String> allmonitorDataNames = serialMapper.selectAllAlarmRecordsDataType();
        if (allSerialMonitors==null || allmonitorDataNames==null){
            System.out.println("获取全部监测类型和污染物名称失败！");
            return false;
        }
        //System.out.println("获取全部监测类型和污染物名称成功！");
        request.setAttribute("allSerialMonitors", allSerialMonitors);
        request.setAttribute("allmonitorDataNames", allmonitorDataNames);
        return true;
    }

    @Override
    public int insertSerialMonitorAlarmRecords(SerialMonitorAlarmEntity serialMonitorAlarm) {
        return serialMapper.insertSerialAlarmRecord(serialMonitorAlarm);
    }
    public String selectRecordsByDynamicSql(HashMap<String,String> features){
        String sql = new SQL(){
            {
                SELECT("*");
                FROM("serial_alarm_records");
                if (!ObjectUtil.isEmptyString(features.get("brandModel"))){
                    WHERE("brand_model = '"+features.get("brandModel")+"'");
                }
                if (!ObjectUtil.isEmptyString(features.get("monitorType"))){
                    WHERE("monitor_type = '"+features.get("monitorType")+"'");
                }
                if (!ObjectUtil.isEmptyString(features.get("monitorData"))){
                    WHERE("monitor_data = '"+features.get("monitorData")+"'");
                }
                if (!ObjectUtil.isEmptyString(features.get("startTime"))){
                    WHERE("record_time >= '"+features.get("startTime")+"'");
                }
                if (!ObjectUtil.isEmptyString(features.get("endTime"))){
                    WHERE("record_time <= '"+features.get("endTime")+"'");
                }
            }
        }.toString();
        return sql;
    }
}

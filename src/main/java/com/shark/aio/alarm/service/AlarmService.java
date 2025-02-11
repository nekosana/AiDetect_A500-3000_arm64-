package com.shark.aio.alarm.service;

import com.github.pagehelper.PageInfo;
import com.shark.aio.alarm.entity.AlarmRecordCompanyHighEntity;
import com.shark.aio.alarm.entity.AlarmRecordCompanyMediumEntity;
import com.shark.aio.alarm.entity.AlarmRecordEntity;
import com.shark.aio.alarm.entity.AlarmSettingsEntity;
import com.shark.aio.alarm.mapper.AlarmMapping;
import com.shark.aio.data.monitorDeviceHj212.MonitorDeviceMapping;
import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.alarm.contactPart.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.SQL;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j

@MapperScan(value = "com.shark.aio.alarm.mapper")
public class AlarmService {

    @Autowired
    private AlarmMapping alarmMapping;
    @Autowired
    private MonitorDeviceMapping monitorDeviceMapping;

    /**
     * 查询所有预警设置，分页且模糊查询
     * 分页通过AOP实现，详见com.shark.aio.util.pageAspect类
     * 方法参数pageNum和pageSize在方法体内从未使用，它们通过AOP在分页时使用
     * 模糊查询参数feature可以为空，代表不进行模糊查询
     * 若feature不为空，先进行模糊查询，再进行分页
     * @param pageNum 分页插件：当前页码
     * @param pageSize 分页插件：每页大小
     * @param feature 模糊查询：查询关键字
     * @return 查询后并分页的结果
     */
    public PageInfo<AlarmSettingsEntity> getAlarmSettingsByPage(Integer pageNum, Integer pageSize, String feature){
        List<AlarmSettingsEntity> alarmSettings;
        if(feature!=null&&!feature.equals("")){
            feature = "%"+feature+"%";
            alarmSettings = alarmMapping.getAlarmSettingsByFeatures(feature);
        }else{
            alarmSettings = alarmMapping.getAllAlarmSettings();
        }
        PageInfo<AlarmSettingsEntity> pageInfo = new PageInfo<>(alarmSettings, 5);
        return pageInfo;
    }

    /**
     * 新增预警设置或监测类型或污染物类型
     * 预警设置中，监测值不能重复
     * 监测类型和污染物类型不能和已有的重复
     * @param alarmSettingsEntity 新预警设置
     * @param newMonitorClass 新监测类型
     * @param existMonitorClass 已有的监测类型
     * @param newPollution 新污染物
     * @param existPollutionName 已有的污染物
     * @return 返回给页面的消息
     */
    public String addAlarmSetting(AlarmSettingsEntity alarmSettingsEntity,
                                  String newMonitorClass, String existMonitorClass,
                                  String newPollution, String existPollutionName){

        //新增预警设置，判断上下限阈值的合理性和监测值是否重复
        if (!ObjectUtil.isEmpty(alarmSettingsEntity) && !ObjectUtil.isEmpty(alarmSettingsEntity.getUpperLimit())){
            if (alarmSettingsEntity.getUpperLimit()<= alarmSettingsEntity.getLowerLimit()) return "上限阈值须大于下限阈值！";
           try {
               alarmMapping.insertAlarmEntity(alarmSettingsEntity);
               log.info("新增预警设置提交成功！");
           }catch (DataIntegrityViolationException e){
                log.info("新增预警设置提交失败！");
                log.error("新增预警设置提交失败！",e);
                return "监测值不能和已有的重复！";
           }

           return "新增预警设置成功！";
        }

        //新增监测值，判断是否和已有的监测值重复
        if (newMonitorClass!=null){
            //插入新的监测类型
            if (!ObjectUtil.isEmptyString(existMonitorClass)){
                List<String> allMonitorClass = new ArrayList<>();
                Collections.addAll(allMonitorClass, existMonitorClass.split(","));
                if (allMonitorClass.contains(newMonitorClass)){
                    return "和已有监测类型重复！";
                }
            }
            try {
                log.info("新增监测类型提交成功！");
                alarmMapping.insertMonitorClass(newMonitorClass);
                return "新增监测类型成功！";
            }catch (Exception e){
                log.info("新增监测类型提交失败！");
                log.error("新增监测类型提交失败！",e);
                return "新增监测类型提交失败！";
            }
        }

        //新增污染物，判断是否和已有的污染物重复
        if (newPollution!=null){
            //插入新的污染物
            if(!ObjectUtil.isEmptyString(existPollutionName)){
                List<String> pollutionNameList = Arrays.stream(existPollutionName.split(",")).collect(Collectors.toList());
                if (pollutionNameList.contains(newPollution)){
                    return "和已有污染物重复！";
                }
            }
            try {
                alarmMapping.insertPollution(newPollution);
                log.info("新增污染物提交成功！");
                return "新增污染物提交成功！";
            }catch (Exception e){
                log.info("新增污染物提交成功！");
                log.error("新增污染物提交失败！",e);
                return "新增污染物失败！";
            }
        }
        return "未接收到数据！";
    }

    /**
     * 根据id删除预警设置
     * @param id id
     * @return 返回页面的消息
     */
    public String deleteAlarmSettingById(int id){
        try {
            alarmMapping.deleteAlarmSettingById(id);
            log.info("删除预警设置成功!");
            return "删除预警设置成功！";
        }catch (Exception e){
            log.info("删除预警设置失败!" );
            log.error("删除预警设置失败!",e);
            return "删除预警设置失败";
        }
    }

    /**
     * 编辑预警设置
     * @param alarmSettingsEntity 修改后的预警设置
     * @return 返回页面的消息
     */
    public String editAlarmSetting(AlarmSettingsEntity alarmSettingsEntity){
        if (alarmSettingsEntity.getUpperLimit()<= alarmSettingsEntity.getLowerLimit()) return "上限阈值须大于下限阈值！";
//        if (alarmMapping.getMonitorValue(alarmSettingsEntity.getMonitorValue())!=null) return "监测值和已有监测值重复！";
        try {
            alarmMapping.updateAlarmSetting(alarmSettingsEntity);
            log.info("预警设置成功!" );
            return "修改成功！";

        }catch (Exception e){
            log.info("预警设置失败！");
            log.error("预警设置失败！",e);
            return "预警设置失败！";
        }
    }

    /**
     * 获取全部监测类型
     * @return 全部监测类型List
     */
    public List<String> getAllMonitorClass(){
        try {
            List<String> allMonitorClass = alarmMapping.getAllMonitorClass();
            return allMonitorClass;
        }catch (Exception e){
            log.error("获取全部监测类型失败！",e);
            return null;
        }
    }

    /**
     * 获取全部污染物名称
     * @return 全部污染物List
     */
    public List<String> getAllPollutionName(){
        try {
            List<String> allPollutionName = alarmMapping.getAllPollutionName();
            return allPollutionName;
        }catch (Exception e){
            log.error("获取全部污染物名称失败！",e);
            return null;
        }
    }
    public List<String> getAllMonitor(){
        try {
            List<String> allMonitor = alarmMapping.getAllMonitor();
            return allMonitor;
        }catch (Exception e){
            log.error("获取监测点名称失败！",e);
            return null;
        }
    }


    public List<String> getAllDeviceId(){
        try {
            List<String> allDeviceId = monitorDeviceMapping.getAllDeviceId();
            return allDeviceId;
        }catch (Exception e){
            log.error("获取设备Id名称失败！",e);
            return null;
        }
    }
    /**
     * 工具方法，供控制层调用，向request中存放所有监测类型和污染物
     * 查询监测类型或污染物名称失败后，返回false
     * @param request 要设置的request
     * @return 是否成功
     */
    public boolean setAttributeBYMonitorAndPollution(HttpServletRequest request){
        List<String> allMonitorClass = getAllMonitorClass();
        List<String> allPollutionName = getAllPollutionName();
        List<String> allMonitor = getAllMonitor();
        if (allPollutionName==null || allMonitorClass==null){
            log.error("获取全部监测类型和污染物名称失败！");
            return false;
        }
        log.info("获取全部监测类型和污染物名称成功！");
        request.setAttribute(Constants.ALLMONITORCLASS, allMonitorClass);
        request.setAttribute(Constants.ALLPOLLUTIONNAME, allPollutionName);
        request.setAttribute("allMonitor", allMonitor);
        return true;
    }

    public boolean setAttributeMedium(HttpServletRequest request){
        List<String> allMonitorClass = getAllMonitorClass();
        List<String> allDeviceId = getAllDeviceId();
        List<String> allMonitor = getAllMonitor();
        if (allDeviceId==null || allMonitorClass==null){
            log.error("获取全部监测类型和污染物名称失败！");
            return false;
        }
        log.error("获取全部监测类型和污染物名称成功！");
        request.setAttribute(Constants.ALLMONITORCLASS, allMonitorClass);
        request.setAttribute("allDeviceId", allDeviceId);
        request.setAttribute("allMonitor", allMonitor);
        return true;
    }

    public boolean setAttributeHigh(HttpServletRequest request){
        List<String> allPollutionName = getAllPollutionName();
        if (allPollutionName==null){
            log.error("获取全部监测类型和污染物名称失败！");
            return false;
        }
        log.info("获取全部监测类型和污染物名称成功！");
        request.setAttribute(Constants.ALLPOLLUTIONNAME, allPollutionName);
        return true;
    }
    /**
     * 从数据库查询报警记录
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param features 查询条件，为null则不进行条件查询，直接查询所有记录
     * @return 分页后的查询记录
     */
    public PageInfo<AlarmRecordEntity> getAlarmRecordsByPage(Integer pageNum, Integer pageSize, HashMap<String,String> features){
        List<AlarmRecordEntity> allAlarmRecords = null;
        try{
            if (features==null || features.isEmpty()){
                allAlarmRecords = alarmMapping.getAllAlarmRecords();
            }
            else{
                System.out.println("map:"+features);
                allAlarmRecords = alarmMapping.getAlarmRecordsByFeature(features);
            }
//            for(AlarmRecordEntity alarmRecordEntity : allAlarmRecords){
//                String timestamp = String.valueOf(alarmRecordEntity.getAlarmTime());
//
//            }
        }catch (Exception e){
            log.error("获取报警记录失败！",e);
        }
        return new PageInfo<>(allAlarmRecords, 5);
    }

    public PageInfo<AlarmRecordCompanyHighEntity> getAlarmRecordsHighByPage(Integer pageNum, Integer pageSize, HashMap<String,String> features){
        List<AlarmRecordCompanyHighEntity> allAlarmRecordsHigh = null;
        try{
            if (features==null || features.isEmpty()){
                allAlarmRecordsHigh = alarmMapping.getAllAlarmRecordsHigh();
            }
            else{
                System.out.println("map:"+features);
                allAlarmRecordsHigh = alarmMapping.getAlarmRecordsByHighFeature(features);
            }
//            for(AlarmRecordEntity alarmRecordEntity : allAlarmRecords){
//                String timestamp = String.valueOf(alarmRecordEntity.getAlarmTime());
//
//            }
        }catch (Exception e){
            log.error("获取报警记录失败！",e);
        }
        return new PageInfo<>(allAlarmRecordsHigh, 5);
    }

    public PageInfo<AlarmRecordCompanyMediumEntity> getAlarmRecordsMediumByPage(Integer pageNum, Integer pageSize, HashMap<String,String> features){
        List<AlarmRecordCompanyMediumEntity> allAlarmRecordsMedium = null;
        try{
            if (features==null || features.isEmpty()){
                allAlarmRecordsMedium = alarmMapping.getAllAlarmRecordsMedium();
            }
            else{
                System.out.println("map:"+features);
                allAlarmRecordsMedium = alarmMapping.getAlarmRecordsMediumByFeature(features);
            }
//            for(AlarmRecordEntity alarmRecordEntity : allAlarmRecords){
//                String timestamp = String.valueOf(alarmRecordEntity.getAlarmTime());
//
//            }
        }catch (Exception e){
            log.error("获取报警记录失败！",e);
        }
        return new PageInfo<>(allAlarmRecordsMedium, 5);
    }

    /**
     * 动态SQL，用于报警记录的条件查询
     * @param features 查询条件
     * @return SQL语句
     */
    public String selectRecordsByDynamicSql(HashMap<String,String> features){
        String sql = new SQL(){
            {
                SELECT("*");
                FROM("alarm_records");
                if (!ObjectUtil.isEmptyString(features.get("monitor"))){
                    WHERE("monitor = '"+features.get("monitor")+"'");
                }
                if (!ObjectUtil.isEmptyString(features.get("monitorClass"))){
                    WHERE("monitor_class = '"+features.get("monitorClass")+"'");
                }
                if (!ObjectUtil.isEmptyString(features.get("monitorValue"))){
                    WHERE("monitor_value = '"+features.get("monitorValue")+"'");
                }
                if (!ObjectUtil.isEmptyString(features.get("startTime"))){
                    WHERE("alarm_time >= '"+features.get("startTime")+"'");
                }
                if (!ObjectUtil.isEmptyString(features.get("endTime"))){
                    WHERE("alarm_time <= '"+features.get("endTime")+"'");
                }
            }
        }.toString();
        return sql;
    }

    public String selectRecordsMediumByDynamicSql(HashMap<String,String> features){
        String sql = new SQL(){
            {
                SELECT("*");
                FROM("alarm_records_company_medium");
                if (!ObjectUtil.isEmptyString(features.get("monitorName"))){
                    WHERE("monitor_name = '"+features.get("monitorName")+"'");
                }
                if (!ObjectUtil.isEmptyString(features.get("monitorClass"))){
                    WHERE("monitor_class = '"+features.get("monitorClass")+"'");
                }
                if (!ObjectUtil.isEmptyString(features.get("deviceId"))){
                    WHERE("device_id = '"+features.get("deviceId")+"'");
                }
                if (!ObjectUtil.isEmptyString(features.get("startTime"))){
                    WHERE("alarm_time >= '"+features.get("startTime")+"'");
                }
                if (!ObjectUtil.isEmptyString(features.get("endTime"))){
                    WHERE("alarm_time <= '"+features.get("endTime")+"'");
                }
            }
        }.toString();
        return sql;
    }

    public String selectRecordsHighByDynamicSql(HashMap<String,String> features){
        String sql = new SQL(){
            {
                SELECT("*");
                FROM("alarm_records_company_high");
                if (!ObjectUtil.isEmptyString(features.get("monitorValue"))){
                    WHERE("monitor_value = '"+features.get("monitorValue")+"'");
                }
                if (!ObjectUtil.isEmptyString(features.get("startTime"))){
                    WHERE("alarm_time >= '"+features.get("startTime")+"'");
                }
                if (!ObjectUtil.isEmptyString(features.get("endTime"))){
                    WHERE("alarm_time <= '"+features.get("endTime")+"'");
                }
            }
        }.toString();
        return sql;
    }
}

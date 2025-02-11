package com.shark.aio.data.conditionData.service;

import com.shark.aio.data.monitorDeviceHj212.MonitorDeviceMapping;
import com.shark.aio.alarm.contactPart.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lbx
 * @date 2023/2/10 - 18:35
 **/
@Service
@Slf4j

@MapperScan(value = "com.shark.aio.data.monitorDeviceHj212")
public class ConditionService {
    @Autowired
    protected MonitorDeviceMapping monitorDeviceMapping;

    /**
     * 获取全部监测点
     * @return 全部监测类型List
     */
    public List<String> getAllMonitor(){
        try {
            List<String> allMonitor = monitorDeviceMapping.getAllMonitor();
            return allMonitor;
        }catch (Exception e){
            log.error("ConditionService/getAllMonitor:获取全部监测点失败！",e);
            return null;
        }
    }



//    /**
//     * 获取全部污染物名称
//     * @return 全部污染物List
//     */
//    public List<String> getAllPollutionName(){
//        try {
//            List<String> allPollutionName = alarmMapping.getAllPollutionName();
//            return allPollutionName;
//        }catch (Exception e){
//            log.error("AlarmService/getAllMonitorClass:获取全部污染物名称失败！",e);
//            return null;
//        }
//    }

    /**
     * 工具方法，供控制层调用，向request中存放所有监测类型和污染物
     * 查询监测类型或污染物名称失败后，返回false
     * @param request 要设置的request
     * @return 是否成功
     */
    public boolean searchMonitor(HttpServletRequest request){
        List<String> allMonitor = getAllMonitor();
//        List<String> allPollutionName = getAllPollutionName();
        if (allMonitor == null){
            return false;
        }
        request.setAttribute(Constants.ALLMONITOR, allMonitor);
//        request.setAttribute(Constants.ALLPOLLUTIONNAME, allPollutionName);
        return true;
    }

}

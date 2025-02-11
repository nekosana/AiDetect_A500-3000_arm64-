package com.shark.aio.data.monitorDeviceHj212;

import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.data.monitorDeviceHj212.entity.MonitorDeviceEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lbx
 * @date 2023/3/19 - 15:08
 **/
@Slf4j
@Controller
public class MonitorDeviceController {
    @Autowired
    MonitorDeviceService monitorDeviceService;
    /**
     * 跳转到新增污染源页面
     * @param request request
     * @return 新增污染源页面
     */
    @GetMapping("/pollution/add")
    public String toAddPollutionPage(HttpServletRequest request){
        if (!monitorDeviceService.searchMonitor(request)){
            log.error("进入新增污染源页面失败！");
            return "500";
        }else  log.info("进入新增污染源页面成功！");
        return Constants.ADDPOLLUTION;
    }

    /**
     * 新增监测点或设备关联
     * 监测点不能重复
     * @param request
     * @param monitorDeviceEntity
     * @param newMonitorName 新监测点名称
     * @param existMonitorName 已有的监测点名称
     * @return 操作成功返回预警设置页，否则返回新增页
     */
    @PostMapping("/pollution/submit/add")
    public String managMonitorDevice(HttpServletRequest request, MonitorDeviceEntity monitorDeviceEntity, String newMonitorName,
                                     String existMonitorName, String deleteDeviceId, String deleteMonitorName){
        String msg = monitorDeviceService.managMonitorDevice(monitorDeviceEntity,newMonitorName,existMonitorName, deleteDeviceId, deleteMonitorName);
        request.setAttribute(Constants.MSG, msg);
//        if (msg.contains("成功")) return "forward:/pollutionMonitor";
//        else return toAddPollutionPage(request);
        return toAddPollutionPage(request);
    }

    @RequestMapping("/device")
    public String deviceWeb(HttpServletRequest request) {

        List<MonitorDeviceEntity> allMonitorDeviceEntity = monitorDeviceService.getMonitorDevice();

        request.setAttribute("allMonitorDeviceEntity", allMonitorDeviceEntity);

        return "device";
    }

    @RequestMapping("/deleteDevice")
    public String deleteDevice(String deviceId, HttpServletRequest req) {

        String code = monitorDeviceService.deleteDevice(deviceId);
        if (code.equals(Constants.FAILCODE)) {
            log.error(" deleteUser, 设备" + deviceId + "删除失败");
            req.setAttribute(Constants.ERROR, "设备删除失败");
            return deviceWeb(req);
        }
        log.info("设备" + deviceId + "删除成功");
        req.setAttribute("msg", "设备" + deviceId + "删除成功");
        return deviceWeb(req);
    }
}

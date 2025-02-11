package com.shark.aio.serial.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.alarm.contactPart.util.DateUtil;
import com.shark.aio.alarm.contactPart.util.ObjectUtil;
import com.shark.aio.alarm.contactPart.util.ProcessUtil;
import com.shark.aio.data.conditionData.controller.ConditionController;
import com.shark.aio.data.monitorDeviceHj212.mapper.SerialsMapping;
import com.shark.aio.serial.entity.ConfigDeviceEntity;
import com.shark.aio.serial.entity.SerialDeviceEntity;
import com.shark.aio.serial.entity.SerialMonitorAlarmEntity;
import com.shark.aio.serial.entity.WaterQualityMonitorDeviceEntity;
import com.shark.aio.serial.service.SerialService;
import com.shark.aio.serial.utils.SerialUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

import static com.shark.aio.alarm.contactPart.util.Constants.*;

@Slf4j
@Controller
@RequestMapping({"/api/serial-device",""})
public class SerialController {
    @Autowired
    private SerialService serialService;
    @Autowired
    SerialsMapping serialsMapping;
    /**
     * 获取所有设备名（第一级）
     */
    @GetMapping("/device-names")
    @ResponseBody
    public List<String> getDeviceNames() {

        return serialService.getMonitorNames();
    }

    /**
     * 根据设备名获取品牌列表（第二级）
     */
    @GetMapping("/brands")
    @ResponseBody
    public List<String> getBrandsByDeviceName(@RequestParam("deviceName") String deviceName) {
        return serialService.getMonitorBandByName(deviceName);
    }

    /**
     * 根据设备名和品牌获取型号列表（第三级）
     */
    @GetMapping("/models")
    @ResponseBody
    public List<String> getModelsByDeviceNameAndBrand(@RequestParam("brand") String brand,@RequestParam("deviceName") String deviceName) {
        return serialService.getMonitorModelByBrandAndName(brand,deviceName);
    }

    @GetMapping("/softwareversions")
    @ResponseBody
    public List<String> getSoftwareversionsByBrandAndModel(@RequestParam("brand") String brand,@RequestParam("model") String model) {
        return serialService.getMonitorSoftwareversionByBandAndModel(brand,model);
    }

    @PostMapping("/addSerialDevice")
    public String addSerialDevice(
            HttpServletRequest request,
            @RequestParam(value = "deviceName", required = false) String deviceName,
            @RequestParam(value ="brand", required = false) String brand,
            @RequestParam(value ="model", required = false) String model,
            @RequestParam(value = "softwareVersion", required = false) String softwareVersion,
            @RequestParam(value = "port", required = false) String port,
            @RequestParam(value = "baudRate", required = false) Long baudRate,
            @RequestParam(value = "communicationId", required = false) String communicationId,
            @RequestParam(value = "startBit", required = false) Integer startBit,
            @RequestParam(value = "endBit", required = false) Integer endBit,
            @RequestParam(value = "info", required = false) String info // 备注字段可以为空
    ) {
        //参数校验
        String ans = SerialUtils.validateParams(brand,model,softwareVersion,communicationId,port,baudRate,startBit,endBit);
        if(ans.equals("success")){
            String brandmodel = brand + "_" + model;
            SerialDeviceEntity serialDevice = SerialUtils.buildSerialDeviceEntity(brandmodel,softwareVersion,
                    communicationId,port,baudRate,startBit,endBit,info);
            if(serialService.selectSerialDevicesByBrandmodel(brandmodel).size()!=0){
                serialService.updateSerialDevce(serialDevice);
                request.setAttribute("msg", "设备已存在，更新配置成功");
            }else{
                //若不存在则创建设备
                if(serialService.insertSerialDevice(serialDevice)==1){
                    request.setAttribute("msg", "创建设备成功");
                }else {
                    request.setAttribute("msg", "创建设备失败");
                }
            }
            // 生成配置文件
            String xiexian = ProcessUtil.IS_WINDOWS?"\\":"/";
            String filePath = CONFIGSERIALDEVICEMPATH + xiexian+ brand + "_" + model + ".ini";  // 文件路径
            generateConfigFile(serialDevice, filePath);
        }else {
            request.setAttribute("msg", ans);
        }


        List<String> allSerialDeviceNames = serialService.getMonitorNames();
        List<String> allSerialDeviceBrandModel = serialService.selectSerialDevicesBrandModel();
        List<String> allSerial = SerialUtils.getAllSerial();
        request.setAttribute("allSerialDeviceName", allSerialDeviceNames);
        request.setAttribute("allSerialDeviceBrandModel", allSerialDeviceBrandModel);
        request.setAttribute("allSerial", allSerial);
        request.setAttribute("activeTab", "serial-device-tab");
        return "addPollution";

    }

    @PostMapping("/deleteSerialDevice")
    public String deleteSerialDevice(
            HttpServletRequest request,
            @RequestParam("brandModel") String brandModel
    ) {
        String ans;
        // 校验品牌型号
        if (brandModel == null || brandModel.trim().isEmpty()) {
            ans = "品牌型号不能为空！";
        }
        // 调用删除服务
        boolean result = serialService.delSerialDevice(brandModel);
        if(result||serialService.selectSerialDevicesByBrandmodel(brandModel).size()==0){
            ans = "删除成功！";
        }else {
            ans = "删除失败！";
        }
        List<String> allSerialDeviceNames = serialService.getMonitorNames();
        List<String> allSerialDeviceBrandModel = serialService.selectSerialDevicesBrandModel();
        request.setAttribute("allSerialDeviceName", allSerialDeviceNames);
        request.setAttribute("allSerialDeviceBrandModel", allSerialDeviceBrandModel);
        request.setAttribute("activeTab", "serial-device-tab");
        request.setAttribute("msg", ans);
        return "addPollution";
    }

    /**
     * 跳转到报警记录页面
     * GET对应侧边栏
     * POST对应页面中的条件查询
     *
     * @param request  request
     * @param pageNum  分页插件：当前页码
     * @param pageSize 分页插件：每页大小
     * @return 报警记录
     */
    @RequestMapping(value = {"/alarmrecords", "/alarmrecords/{pageNum}/{pageSize}"})
    public String toAlarmRecordsLowPage(HttpServletRequest request,
                                        @PathVariable(required = false) Integer pageNum,
                                        @PathVariable(required = false) Integer pageSize
    ) {
        //条件查询的HashMap，若没有条件查询默认为null
        HashMap<String, String> features = null;
        //条件查询是POST方法，此处判断POST
        if (request.getMethod().equals("POST")) {
            //获取查询条件，并放置到HashMap中
            features = new HashMap<>();
            String startTime = request.getParameter("startTime");
            String endTime = request.getParameter("endTime");
            try {
                //开始结束时间转化为时间戳
                if (!ObjectUtil.isEmptyString(startTime)) features.put("startTime", DateUtil.toTimestamp(startTime));
                if (!ObjectUtil.isEmptyString(endTime)) features.put("endTime", DateUtil.toTimestamp(endTime));
            } catch (ParseException e) {
                System.out.println("转化为时间戳失败！"+e);
            }
            //监测设备、监测类型、监测数据
            String brandModel = request.getParameter("brandModel");
            if (!ObjectUtil.isEmptyString(brandModel)) features.put("brandModel", brandModel);
            String monitorType = request.getParameter("monitorType");
            if (!ObjectUtil.isEmptyString(monitorType)) features.put("monitorType", monitorType);
            String monitorData = request.getParameter("monitorData");
            if (!ObjectUtil.isEmptyString(monitorData)) features.put("monitorData", monitorData);

            //返回前端，方便下一次筛选
            request.setAttribute("brandModel", features.get("brandModel"));
            request.setAttribute("monitorType", features.get("monitorType"));
            request.setAttribute("monitorData", features.get("monitorData"));
            request.setAttribute("startTime", startTime);
            request.setAttribute("endTime", endTime);
        }
        if(null!=features){
            System.out.println("features is "+features.toString());
        }
        //查询数据库
        PageInfo<SerialMonitorAlarmEntity> allAlarmRecords = serialService.getAlarmRecordsByPage(pageNum, pageSize, features);
        if (allAlarmRecords == null) {
            //log.error("报警记录页面失败！");
            return "500";
        }
        if (!serialService.setAttributeBYSeriamMonitorsAndDataNames(request)) {
            System.out.println("报警记录页面失败！");
            return "500";
        }
        request.setAttribute("records", allAlarmRecords);
        return "/serialAlarmRecords";
    }


    @RequestMapping("/returnSerialData")
    @CrossOrigin
    public void returnSerialData(String monitorName, String modelName, String dir, String keys, HttpServletResponse rep, HttpServletRequest request) throws IOException {
        if (!"null".equals(monitorName)) {
            String separator = ProcessUtil.IS_WINDOWS ? "\\" : "/";
            String name = Constants.SERIALNPATH + monitorName + separator + modelName + separator + dir + separator + Constants.SERIALDATA;
            JSONObject result = new JSONObject();
            String info = serialsMapping.getTypeWithBrandAndModel(monitorName,modelName);
            List<String> allParameterType = serialsMapping.getAllParameterTypeWithInfo(info);

            // 初始化每个 parameterType 的 JSON 对象
            Map<String, JSONObject> parameterTypeMap = new HashMap<>();
            for (String parameterType : allParameterType) {
                JSONObject temp = new JSONObject();
                List<String> keySet = new ArrayList<>();
                List<String> keySet_notime = new ArrayList<>();
                keySet.add("日期-时间");
                keySet_notime.add("--- 请选择数据 ---");

                List<String> allKeySet = serialsMapping.getAllChineseNameWithInfoAndParameterType(info, parameterType);
                for (String key : allKeySet) {
                    if (!keySet.contains(key)) {
                        keySet.add(key);
                        if (!key.contains("Flag")) {
                            keySet_notime.add(key);
                        }
                    }
                }

                temp.put("keySet", keySet);
                temp.put("keySet_notime", keySet_notime);
                temp.put("data", new ArrayList<JSONObject>()); // 初始化空数据列表

                parameterTypeMap.put(parameterType, temp);
            }

            // 读取文件并分类数据
            try (FileInputStream fin = new FileInputStream(name);
                 InputStreamReader reader = new InputStreamReader(fin, "utf-8");
                 BufferedReader buffReader = new BufferedReader(reader)) {

                String strTmp;
                while ((strTmp = buffReader.readLine()) != null) {
                    ConditionController.sample(buffReader, getLineNumber(new File(name)));
                    JSONObject jsonObject = JSON.parseObject(strTmp);
                    JSONObject cpObject = jsonObject.getJSONObject("CP");
                    String dataTime = jsonObject.getString("DataTime");

                    // 遍历所有 parameterType，筛选并添加数据
                    for (String parameterType : allParameterType) {
                        JSONObject temp = parameterTypeMap.get(parameterType);
                        List<String> keySet = (List<String>) temp.get("keySet");

                        // 创建一个新的数据对象，仅包含 keySet 中的键
                        JSONObject dataObj = new JSONObject();
                        dataObj.put("日期-时间", dataTime);
                        for (String key : keySet) {
                            if ("日期-时间".equals(key)) continue;
                            if (cpObject.containsKey(key)) {
                                dataObj.put(key, cpObject.get(key));
                            } else {
                                dataObj.put(key, null); // 如果缺少某个键，填充 null
                            }
                        }

                        // 添加到对应的 parameterType 数据列表
                        ((List<JSONObject>) temp.get("data")).add(dataObj);
                    }
                }

                // 将 parameterTypeMap 转换为 result JSON 对象
                for (Map.Entry<String, JSONObject> entry : parameterTypeMap.entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                }

                // 设置响应类型和编码
                rep.setContentType("application/json;charset=utf-8");
                rep.getWriter().write(result.toJSONString());

            } catch (Exception e) {
                log.error("数据文件查询失败！", e);
                // 构建错误响应，保持与成功响应的数据结构一致
                JSONObject errorResult = new JSONObject();
                for (String parameterType : allParameterType) {
                    JSONObject temp = new JSONObject();
                    temp.put("keySet", new JSONArray()); // 空数组
                    temp.put("keySet_notime", new JSONArray());
                    temp.put("data", new JSONArray());
                    errorResult.put(parameterType, temp);
                }
                // 设置响应类型和编码
                rep.setContentType("application/json;charset=utf-8");
                rep.getWriter().write(errorResult.toJSONString());
            }
        }
    }



//    @RequestMapping("/returnSerialData")
//    @CrossOrigin
//    public void returnSerialData(String monitorName, String modelName,String dir, String keys, HttpServletResponse rep, HttpServletRequest request) throws IOException {
//
//        if (!"null".equals(monitorName)) {
//            String name = Constants.SERIALNPATH + monitorName + (ProcessUtil.IS_WINDOWS ? "\\" : "/") + modelName+ (ProcessUtil.IS_WINDOWS ? "\\" : "/") + dir + (ProcessUtil.IS_WINDOWS ? "\\" : "/") + Constants.SERIALDATA;
//            try {
//                FileInputStream fin = new FileInputStream(name);
//                InputStreamReader reader = new InputStreamReader(fin, "utf-8");
//                BufferedReader buffReader = new BufferedReader(reader);
//                String strTmp = "";
//                JSONObject result = new JSONObject();
//                String info = "ZP";
//                List<String>allParameterType = serialsMapping.getAllParameterTypeWithInfo(info);
//
//                for(String parameterType :allParameterType)
//                {
//                    JSONObject temp = new JSONObject();
//                    ArrayList<JSONObject> data = new ArrayList<>();
//                    ArrayList<String> keyset = new ArrayList<>();
//                    List<String> keyset_notime = new ArrayList<>();
//                    keyset.add("日期-时间");
//                    keyset_notime.add("--- 请选择数据 ---");
//
//                    List<String>allKeySet = serialsMapping.getAllChineseNameWithInfoAndParameterType(info,parameterType);
//                    for (String key : allKeySet) {
//                        if (!keyset.contains(key)) {
//                            keyset.add(key);
//                            if (!key.contains("Flag")){
//                                keyset_notime.add(key);
//                            }
//
//                        }
//                    }
//                    long length = getLineNumber(new File(name));
//                    while ((strTmp = buffReader.readLine()) != null) {
//                        ConditionController.sample(buffReader, length);
//                        JSONObject jsonObject = JSON.parseObject(strTmp);
//                        JSONObject dataObj = jsonObject.getJSONObject("CP");
//                        dataObj.put("日期-时间", jsonObject.getString("DataTime"));
//                        data.add(dataObj);
//
//                    }
//                    temp.put("keySet", keyset);
//                    temp.put("keySet_notime", keyset_notime);
//                    temp.put("data", data.toArray());
//                    result.put(parameterType,temp);
//                }
//                buffReader.close();
//                String response = result.toJSONString();
//                rep.setContentType("application/json;charset=utf-8");
//                rep.getWriter().write(response);
//
//            } catch (Exception e) {
//                log.error("数据文件查询失败！");
//                request.setAttribute(Constants.MSG, "暂无数据");
//                JSONObject result = new JSONObject();
//                result.put("keySet", "暂无数据");
//                result.put("data", null);
//                String response = result.toJSONString();
//                rep.setContentType("text/html;charset=utf-8");
//                rep.getWriter().write(response);
//            }
//        }
//    }

//    @RequestMapping("/returnSerialData")
//    @CrossOrigin
//    public void returnSerialData1(String monitorName, String modelName,String dir, String keys, HttpServletResponse rep, HttpServletRequest request) throws IOException {
//
//        if (!"null".equals(monitorName)) {
//            String name = Constants.SERIALNPATH + monitorName + (ProcessUtil.IS_WINDOWS ? "\\" : "/") + modelName+ (ProcessUtil.IS_WINDOWS ? "\\" : "/") + dir + (ProcessUtil.IS_WINDOWS ? "\\" : "/") + Constants.SERIALDATA;
//            try {
//                FileInputStream fin = new FileInputStream(name);
//                InputStreamReader reader = new InputStreamReader(fin, "utf-8");
//                BufferedReader buffReader = new BufferedReader(reader);
//                String strTmp = "";
//                ArrayList<JSONObject> data = new ArrayList<>();
//                ArrayList<String> keyset = new ArrayList<>();
//                List<String> keyset_notime = new ArrayList<>();
//                keyset.add("日期-时间");
//                keyset_notime.add("--- 请选择数据 ---");
//                JSONObject result = new JSONObject();
//
//                //假设现在程序可以给我返回设备info 即 cod AM那些
//                String info = "ZP";
//                List<String>allKeySet = serialsMapping.getAllParameterTypeWithInfo(info);
//                for (String key : allKeySet) {
//                    if (!keyset.contains(key)) {
//                        keyset.add(key);
//                        if (!key.contains("Flag")){
//                            keyset_notime.add(key);
//                        }
//
//                    }
//                }
//
//                long length = getLineNumber(new File(name));
//                while ((strTmp = buffReader.readLine()) != null) {
//                    ConditionController.sample(buffReader, length);
//                    JSONObject jsonObject = JSON.parseObject(strTmp);
//                    JSONObject dataObj = jsonObject.getJSONObject("CP");
//                    dataObj.put("日期-时间", jsonObject.getString("DataTime"));
//                    data.add(dataObj);
//
//                    for (String key : dataObj.keySet()) {
//                        if (!keyset.contains(key)) {
//                            keyset.add(key);
//                            if (!key.contains("Flag")){
//                                keyset_notime.add(key);
//                            }
//
//                        }
//                    }
//                }
//                buffReader.close();
//                result.put("keySet", keyset);
//
//                result.put("keySet_notime", keyset_notime);
//                result.put("data", data.toArray());
//                String response = result.toJSONString();
//                rep.setContentType("text/html;charset=utf-8");
//                rep.getWriter().write(response);
//            } catch (Exception e) {
//                log.error("数据文件查询失败！");
//                request.setAttribute(Constants.MSG, "暂无数据");
//                JSONObject result = new JSONObject();
//                result.put("keySet", "暂无数据");
//                result.put("data", null);
//                String response = result.toJSONString();
//                rep.setContentType("text/html;charset=utf-8");
//                rep.getWriter().write(response);
//            }
//        }
//    }

    public long getLineNumber(File file) {
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
                lineNumberReader.skip(Long.MAX_VALUE);
                long lines = lineNumberReader.getLineNumber() + 1;
                fileReader.close();
                lineNumberReader.close();
                return lines;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    @RequestMapping("/serialMonitor")
    public String serialMonitorWeb(HttpServletRequest request, HttpServletResponse rep, String key) throws IOException {

        File file = new File(Constants.SERIALNPATH);
        if (!file.exists()) file.mkdirs();
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            request.setAttribute(Constants.MSG, "暂无数据");
            request.setAttribute("allMonitors", null);
        } else {
            String[] fileList = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                fileList[i] = files[i].getName();
            }
            request.setAttribute("allMonitors", fileList);
        }

        return "serialMonitor";

    }

    @RequestMapping("returnSerialFileList")
    @CrossOrigin
    @ResponseBody
    public String[] returnConditionFileList(String monitorName) {
        try {
            File conditionDir = new File(Constants.SERIALNPATH + monitorName);
            File[] files = conditionDir.listFiles();
            if (files == null) {
                return null;
            } else {
                String[] fileList = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    fileList[i] = files[i].getName();
                }
                Arrays.sort(fileList,(o1, o2)->{
                    return o2.compareTo(o1);
                });
                return fileList;
            }
        } catch (Exception e) {
            log.error("数据文件查询失败！");
            return null;
        }
    }

    @RequestMapping("returnModelFileList")
    @CrossOrigin
    @ResponseBody
    public String[] returnModelFileList(String monitorName,String modelName) {
        try {
            File conditionDir = new File(Constants.SERIALNPATH +monitorName+(ProcessUtil.IS_WINDOWS ? "\\" : "/")+ modelName);
            File[] files = conditionDir.listFiles();
            if (files == null) {
                return null;
            } else {
                String[] fileList = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    fileList[i] = files[i].getName();
                }
                Arrays.sort(fileList,(o1, o2)->{
                    return o2.compareTo(o1);
                });
                return fileList;
            }
        } catch (Exception e) {
            log.error("数据文件查询失败！");
            return null;
        }
    }

    @PostMapping("/configSerialDevice")
    public String configSerialDevice(
            HttpServletRequest request,
            @RequestParam("newDeviceName") String deviceName,
            @RequestParam("newDeviceBrand") String deviceBrand,
            @RequestParam("newDeviceModel") String deviceModel,
            @RequestParam("newDeviceVersion") String deviceVersion,
            @RequestParam("deviceSelection") String deviceSelect
    ) {
        //判断是否有该设备，有则返回提示，
        if(serialService.checkDeviceExistence(deviceBrand,deviceModel,deviceVersion)>0){
            String newConfigDeviceTableName = deviceBrand+"_"+deviceModel;
            request.setAttribute("msg", "已存在该设备，可修改配置信息");
            //加入配置信息返回前端
            List<ConfigDeviceEntity> configDeviceEntities = serialService.getAllConfigDeviceInfo(newConfigDeviceTableName);
            request.setAttribute("configDeviceEntities",configDeviceEntities);
            request.setAttribute("newConfigDeviceTableName", newConfigDeviceTableName);
        }else{
            //没有向数据库插入
            WaterQualityMonitorDeviceEntity waterQualityMonitorDevice = new WaterQualityMonitorDeviceEntity();
            waterQualityMonitorDevice.setName(deviceName);
            waterQualityMonitorDevice.setBrand(deviceBrand);
            waterQualityMonitorDevice.setModel(deviceModel);
            waterQualityMonitorDevice.setSoftwareVersion(deviceVersion);
            waterQualityMonitorDevice.setInfo(deviceSelect);
            serialService.insertWaterQualityMonitorDevice(waterQualityMonitorDevice);
            //创建新设备配置表
            String newConfigDeviceTableName = deviceBrand+"_"+deviceModel;
            serialService.createConfigWaterQualityMonitorDevice(newConfigDeviceTableName);
            //查询配置信息添加进表中
            List<String> nameList = serialService.getFieldNamesWithChineseName(deviceSelect);
            for (String name : nameList) {
                ConfigDeviceEntity device = new ConfigDeviceEntity();
                device.setName(name); // 设置 name 字段
                device.setRegisteraddr(null); // 其余字段为空
                device.setRegisteraddrlen(null);
                device.setVerifyaddr(null);
                device.setVerifyaddrlen(null);
                device.setInfo(null);
                // 调用 Mapper 方法插入到表中
                serialService.insertIntoDeviceTable(newConfigDeviceTableName, device);
            }
            List<ConfigDeviceEntity> configDeviceEntities = serialService.getAllConfigDeviceInfo(newConfigDeviceTableName);
            request.setAttribute("msg", "成功创建设备，开始配置");
            request.setAttribute("newConfigDeviceTableName", newConfigDeviceTableName);
            request.setAttribute("configDeviceEntities", configDeviceEntities);
        }

        return "configSerialDevice";
    }

    @PostMapping("/saveConfig")
    public String saveConfig(
            HttpServletRequest request,
            @RequestParam Map<String, String> config,
            @RequestParam String newConfigDeviceTableName
    ) {
        Iterator<Map.Entry<String, String>> iterator = config.entrySet().iterator();
        if (iterator.hasNext()) {
            iterator.next(); // 获取第一个元素
            iterator.remove(); // 删除第一个元素
        }
        // 创建一个新的设备配置列表
        List<ConfigDeviceEntity> configDeviceEntities = new ArrayList<>();

        // 遍历传递的配置数据
        for (Map.Entry<String, String> entry : config.entrySet()) {
            String key = entry.getKey();  // 例如 "config[Device1].registeraddr"
            String value = entry.getValue();  // 例如 "address1"

            // 提取设备名称，假设设备名称格式是 config[Device1].registeraddr
            String deviceName = key.split("\\[")[1].split("\\]")[0];

            // 提取字段名，假设字段格式是 config[Device1].registeraddr
            String field = key.split("\\.")[1];

            // 查找或创建一个新的 ConfigDeviceEntity 对象
            ConfigDeviceEntity deviceEntity = SerialUtils.getDeviceEntityByName(configDeviceEntities, deviceName);
            if (deviceEntity == null) {
                deviceEntity = new ConfigDeviceEntity();
                deviceEntity.setName(deviceName);  // 设置设备名称
                configDeviceEntities.add(deviceEntity);  // 将新设备配置添加到列表
            }

            // 根据字段名更新设备的相应属性
            switch (field) {
                case "registeraddr":
                    deviceEntity.setRegisteraddr(value);
                    break;
                case "registeraddrlen":
                    deviceEntity.setRegisteraddrlen(value);
                    break;
                case "verifyaddr":
                    deviceEntity.setVerifyaddr(value);
                    break;
                case "verifyaddrlen":
                    deviceEntity.setVerifyaddrlen(value);
                    break;
                case "info":
                    deviceEntity.setInfo(value);
                    break;
                default:
                    break;
            }
        }
        //更新数据库
        for (ConfigDeviceEntity deviceEntity : configDeviceEntities) {
            // 调用 Mapper update 更新
            serialService.updateConfigDevice(newConfigDeviceTableName,deviceEntity);
        }
        // 生成配置文件
        String xiexian = ProcessUtil.IS_WINDOWS?"\\":"/";
        String filePath = CONFIGDEVICEPARAMPATH + xiexian + newConfigDeviceTableName + ".ini"; // 拼接文件路径
        generateConfigFile(configDeviceEntities, filePath,newConfigDeviceTableName); // 生成配置文件

        // 设置成功提示
        request.setAttribute("msg", "设备配置已保存！");
        request.setAttribute("newConfigDeviceTableName", newConfigDeviceTableName);
        request.setAttribute("configDeviceEntities", configDeviceEntities);

        // 返回视图
        return "configSerialDevice";  // 返回到设备配置页面
    }
    @PostMapping("/deleteConfigDevice")
    public String deleteConfigDevice(
            HttpServletRequest request,
            String deleteConfigDevice
    ) {
        String[] parts = deleteConfigDevice.split("_");
        String brand = parts[0];
        String model = parts[1];
        if(serialService.checkTableExists(deleteConfigDevice)>0){
            serialService.deleteTable(deleteConfigDevice);
            serialService.deleteDeviceByBrandAndModel(brand,model);
            request.setAttribute("msg", "配置设备删除成功！");
        }else {
            request.setAttribute("msg", "配置设备不存在，删除失败！");
        }
        List<String> allSerialDeviceNames = serialService.getMonitorNames();
        List<String> allSerialDeviceBrandModel = serialService.selectSerialDevicesBrandModel();
        List<String> allConfigDevice = serialService.getBrandAndModelFromWaterMonitorDevice();
        request.setAttribute("allSerialDeviceName", allSerialDeviceNames);
        request.setAttribute("allSerialDeviceBrandModel", allSerialDeviceBrandModel);
        request.setAttribute("activeTab", "serial-device-tab");
        request.setAttribute("allConfigDevice", allConfigDevice);
        return "addPollution";
    }

    // 下载解析文件
    @RequestMapping("/downloadConfigDeviceFile")
    public void download(HttpServletResponse resp) throws Exception {
        try {
            // 根据操作系统判断文件路径
            String filePath = CONFIGDEVICEPATH;

            // 固定的文件名
            String fileName = "configdevice.docx";

            File file = new File(filePath);

            // 如果文件不存在
            if (!file.exists()) {
                System.out.println("下载文件不存在");
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);  // 设置404响应
                return;
            }

            // 解决下载文件时文件名乱码问题
            byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
            fileName = new String(fileNameBytes, 0, fileNameBytes.length, StandardCharsets.ISO_8859_1);

            // 设置响应头，告诉浏览器下载该文件
            resp.reset();
            resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("utf-8");
            resp.setContentLength((int) file.length());
            resp.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            // 读取文件并写入到响应输出流
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
                 OutputStream os = resp.getOutputStream()) {

                byte[] buff = new byte[1024];
                int i;
                while ((i = bis.read(buff)) != -1) {
                    os.write(buff, 0, i);
                    os.flush();
                }
                System.out.println("文件下载成功");
            } catch (IOException e) {
                System.out.println("文件下载失败");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);  // 设置500错误
            }

        } catch (Exception e) {
            System.out.println("文件下载发生错误"+e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);  // 设置500错误
        }
    }


    public void generateConfigFile(List<ConfigDeviceEntity> configDeviceEntities, String filePath, String newConfigDeviceTableName) {
        try {
            // 获取文件所在的目录
            File directory = new File(filePath).getParentFile();

            // 如果文件夹不存在，创建文件夹
            if (directory != null && !directory.exists()) {
                directory.mkdirs();  // 创建多级目录
            }

            // 创建输出文件，如果文件已存在则会覆盖
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false));  // false表示覆盖

            // 使用设备表名作为配置文件的标题
            writer.write("[" + newConfigDeviceTableName + "]\n");

            for (ConfigDeviceEntity deviceEntity : configDeviceEntities) {
                // 获取实体的配置参数（英文_中文）
                String name = deviceEntity.getName();
                String[] nameParts = name.split("_");

                if (nameParts.length == 2) {
                    String englishName = nameParts[0];  // 英文部分
                    String chineseName = nameParts[1];  // 中文部分

                    // 获取设备的寄存器地址、位数和是否解析
                    String registerAddr = deviceEntity.getRegisteraddr();  // 寄存器地址
                    String registerAddrLen = deviceEntity.getRegisteraddrlen();  // 寄存器位数
                    String info = deviceEntity.getInfo();  // 是否解析

                    // 如果 info 不为空，设置为 1
                    String isParse = (info != null && !info.isEmpty()) ? "1" : "";

                    // 写入中文注释
                    writer.write("#" + chineseName + "\n");

                    // 写入英文参数名和对应的值
                    writer.write(englishName + "=" + registerAddr + "," + registerAddrLen);

                    // 如果 info 不为空，添加是否解析
                    if (!isParse.isEmpty()) {
                        writer.write("," + isParse);
                    }
                    writer.write("\n");
                }
            }

            writer.close();  // 关闭文件写入
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 生成配置文件的方法
    private void generateConfigFile(SerialDeviceEntity serialDevice, String filePath) {
        try {
            // 获取文件所在的目录
            File directory = new File(filePath).getParentFile();

            // 如果文件夹不存在，创建文件夹
            if (directory != null && !directory.exists()) {
                directory.mkdirs();  // 创建多级目录
            }

            // 创建输出文件，如果文件已存在则会覆盖
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false));  // false表示覆盖

            // 写入配置文件内容
            writer.write("machine_name = " + serialDevice.getBrandModel() + "\n");
            writer.write("machine_id = " + serialDevice.getInfo() + "\n");
            writer.write("\n");

            writer.write("port = " + serialDevice.getPort() + "\n");
            writer.write("baudRate = " + serialDevice.getBaudRate() + "\n");
            writer.write("data_bits = " + serialDevice.getStartBit() + "\n");
            writer.write("stop_bits = " + serialDevice.getEndBit() + "\n");

            writer.close();  // 关闭文件写入
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

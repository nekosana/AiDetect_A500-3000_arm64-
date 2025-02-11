package com.shark.aio.data.monitorDeviceHj212;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.shark.aio.alarm.GradedAlarm.JedisConnectionFactory;
import com.shark.aio.alarm.contactPart.ContractPartController;
import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.alarm.contactPart.util.ProcessUtil;
import com.shark.aio.base.information.InformationMapping;
import com.shark.aio.data.monitorDeviceHj212.entity.SerialDeviceEntity;
import com.shark.aio.data.monitorDeviceHj212.mapper.SerialsMapping;
import com.shark.aio.serial.entity.InfoAndNameVo;
import com.shark.aio.serial.entity.SerialMonitorAlarmEntity;
import com.shark.aio.serial.entity.ThresholdAndNameVo;
import com.shark.aio.serial.service.SerialService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DataProcessingService {
//    private static HJ212ServerHandler hJ212ServerHandler;
    @Autowired
    JedisConnectionFactory jedisConnectionFactory;
    @Autowired
    SerialsMapping serialsMapping;
    @Autowired
    protected MonitorDeviceService monitorDeviceService;
    @Autowired
    protected ContractPartController contractPartController;
    @Autowired
    MonitorDeviceMapping monitorDeviceMapping;
    @Autowired
    InformationMapping informationMapping;
    @Autowired
    SerialService serialService;

    @Value("${fixed.ST}")
    private String ST;
    @Value("${fixed.SIZE}")
    private String SIZE;
    @Value("${fixed.PW}")
    private String PW;
    @Value("${fixed.End}")
    private String End;
    @Value("${fixed.CN}")
    private String CN;
    @Value("${fixed.QN}")
    private String QN;
    @Value("${fixed.Flag}")
    private String Flag;


    /**
     * 定义一个HashMap，用于保存所有的channel和设备ID的对应关系。
     * 这里保留在服务中可能不太合适，视具体需求决定是否需要。
     */
    private Map<String, String> deviceInfo = new HashMap<>(64);

    /**
     * 处理串口收到的数据
     *
     */
    public void processData(Object msg, String brand,String model,String info) {
        String data= (String)msg;
        System.out.println("串口收到数据为 ===> " + data);
        try {
            // 解析原始JSON字符串
            cn.hutool.json.JSONObject originalJson = JSONUtil.parseObj(data);

            String deviceId = brand+"_"+model;
            try (Jedis jedis = jedisConnectionFactory.getJedis()) {
                if (!jedis.exists(deviceId)) System.out.println("未绑定串口设备");
                if (jedis.exists(deviceId)) {
                    if ("false".equals(jedis.get(deviceId))) {
                        serialsMapping.updateSerialDevice(deviceId, "true");
                    }
                    //SerialDeviceEntity serialDevice = (SerialDeviceEntity) SerializeUtil.deserialize(jedis.get(("serialDevice_" + deviceId).getBytes()));
                    //redis三小时key,是否失去连接超过三小时
                    jedis.setex(deviceId, 3 * 60 * 60, "true");

                    // 创建新的JSON对象，用于存放固定字段
                    JSONObject finalJson = new JSONObject();
                    finalJson.put("ST", ST);
                    finalJson.put("MN","XXY123456");
                    finalJson.put("SIZE", SIZE);
                    finalJson.put("PW", PW);
                    // 设置当前时间，格式为 "yyyy-MM-dd HH:mm:ss"
                    String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    finalJson.put("DataTime", currentTime);
                    finalJson.put("End", End);
                    finalJson.put("CN", CN);
                    finalJson.put("QN", QN);
                    finalJson.put("Flag", Flag);

                    // 创建CP对象，用于存放映射后的数据
                    JSONObject cpJson = new JSONObject();

                    // 遍历原始JSON中的所有键值对
                    for (Map.Entry<String, Object> entry : originalJson.entrySet()) {

                        String originalKey = entry.getKey();
                        Object value = entry.getValue();

                        // 获取中文字段名
                        //String chineseKey = FieldMappingUtil.getChineseFieldName(originalKey);
                        String chinesename = serialsMapping.selectChineseNameByFieldName(info,originalKey);
                        if(chinesename==null)
                        {
                            chinesename = originalKey;
                        }
                        int OC = 0;
                        SerialMonitorAlarmEntity serialMonitorAlarmEntity = new SerialMonitorAlarmEntity();
                        serialMonitorAlarmEntity.setRecordTime(new Timestamp(System.currentTimeMillis()));
                        serialMonitorAlarmEntity.setBrandModel(deviceId);

                        switch (chinesename) {
                            case "工作状态":
                                OC = Integer.parseInt((String) value);
                                switch (OC) {
                                    case 0:
                                        value = "运行";
                                        break;
                                    case 1:
                                        value = "维护";
                                        break;
                                    case 2:
                                        value = "故障";
                                        serialMonitorAlarmEntity.setMonitorType(chinesename);
                                        serialMonitorAlarmEntity.setMonitorData((String) value);
                                        serialMonitorAlarmEntity.setInfo("工作状态出现故障");
                                        break;
                                    case 3:
                                        value = "校准";
                                        break;
                                    case 5:
                                        value = "反吹";
                                        break;
                                    case 6:
                                        value = "电源故障";
                                        serialMonitorAlarmEntity.setMonitorType(chinesename);
                                        serialMonitorAlarmEntity.setMonitorData((String) value);
                                        serialMonitorAlarmEntity.setInfo("电源出现故障");
                                        break;
                                    case 7:
                                        value = "测量";
                                        break;
                                    case 8:
                                        value = "标定";
                                        break;
                                    case 9:
                                        value = "待机";
                                        break;
                                    case 10:
                                        value = "运维";
                                        break;
                                }
                                break;

                            case "报警信息":
                                OC = Integer.parseInt((String) value);
                                switch (OC) {
                                    case 0:
                                        value = "无报警";
                                        break;
                                    case 1:
                                        value = "系统故障";
                                        break;
                                    case 2:
                                        value = "电源故障";
                                        break;
                                    case 3:
                                        value = "缺试剂";
                                        break;
                                    case 4:
                                        value = "缺蒸馏水";
                                        break;
                                    case 5:
                                        value = "加热故障";
                                        break;
                                    case 6:
                                        value = "排残液故障";
                                        break;
                                    case 7:
                                        value = "测量值超量程异常";
                                        break;
                                    case 8:
                                        value = "其他故障";
                                        break;
                                    case 9:
                                        value = "采集水样、试剂超时";
                                        break;
                                    case 10:
                                        value = "其他报警";
                                        break;
                                }
                                if(OC!=0)
                                {
                                    serialMonitorAlarmEntity.setMonitorType(chinesename);
                                    serialMonitorAlarmEntity.setMonitorData((String) value);
                                    serialMonitorAlarmEntity.setInfo("发生了一次报警");
                                }
                                break;

                            case "是否有零点校准结果" :
                            case "零点测量结果":
                            case "是否有满量程校准结果":
                                OC = Integer.parseInt((String) value);
                                switch (OC) {
                                    case 0:
                                        value = "无";
                                        break;
                                    case 1:
                                        value = "成功";
                                        break;
                                    case 2:
                                        value = "失败";
                                        serialMonitorAlarmEntity.setMonitorType(chinesename);
                                        serialMonitorAlarmEntity.setMonitorData((String) value);
                                        serialMonitorAlarmEntity.setInfo("该次测量失败了");
                                        break;
                                }
                                break;
                            case "校准方法":
                                OC = Integer.parseInt((String) value);
                                switch (OC) {
                                    case 0:
                                        value = "人工校准";
                                        break;
                                    case 1:
                                        value = "自动校准";
                                        break;
                                }
                                break;
                        }
                        // 将中文字段名和对应的值添加到CP对象中
                        if(serialMonitorAlarmEntity.getMonitorType()!=null)
                            serialService.insertSerialMonitorAlarmRecords(serialMonitorAlarmEntity);
                        cpJson.put(chinesename, value);
                    }

                    // 将CP对象添加到最终JSON中
                    finalJson.put("CP", cpJson);

                    SimpleDateFormat DataFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String date = DataFormat.format(new Date());
                    //根目录 + 监测点 + 日期
                    String documentPath = ProcessUtil.IS_WINDOWS ?
                            Constants.SERIALNPATH + brand + "\\" + model + "\\" + date :
                            Constants.SERIALNPATH + brand + "/" + model + "/" + date;

                    String filePath = documentPath + (ProcessUtil.IS_WINDOWS ? "\\" : "/") + brand + ".txt";
                    File document = new File(documentPath);
                    if (!document.exists()) {
                        document.mkdirs();
                    }
                    File file = new File(filePath);

                    OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file, true)), StandardCharsets.UTF_8);
                    out.write((finalJson.toJSONString() + "\n"));
                    out.flush();
                    out.close();
                }
                    } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            } catch (Exception e1) {
                log.error("redis加入失败！", e1);
            }
    }

    public void processDataSerial(String data, Object ctx, SerialDeviceEntity monitorDevice, String type) {
        try {
            // 解析原始JSON字符串
            cn.hutool.json.JSONObject originalJson = JSONUtil.parseObj(data);
            String deviceId = monitorDevice.getBrandModel();
            try (Jedis jedis = jedisConnectionFactory.getJedis()) {
                if (!jedis.exists(deviceId)) {
                    System.out.println("未绑定串口设备");
                }
                    if (jedis.exists(deviceId)) {
                    if ("false".equals(jedis.get(deviceId))) {
                        serialsMapping.updateSerialDevice(deviceId, "true");
                    }
                    //redis三小时key,是否失去连接超过三小时
                    System.out.println("串口收到数据为 ===> " + data);
                    jedis.setex(deviceId, 3*60*60, "true");
                    // 创建新的JSON对象，用于存放固定字段
                    JSONObject finalJson = new JSONObject();
                    finalJson.put("ST", ST);
                    finalJson.put("MN","XXY123456");
                    finalJson.put("SIZE", SIZE);
                    finalJson.put("PW", PW);
                    // 设置当前时间，格式为 "yyyy-MM-dd HH:mm:ss"
                    String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    finalJson.put("DataTime", currentTime);
                    finalJson.put("End", End);
                    finalJson.put("CN", CN);
                    finalJson.put("QN", QN);
                    finalJson.put("Flag", Flag);

                    // 创建CP对象，用于存放映射后的数据
                    JSONObject cpJson = new JSONObject();
                    String brand = deviceId.split("_")[0];
                    String model = deviceId.split("_")[1];

                    String info = type;


                    List<InfoAndNameVo> Allrule = serialService.getInfoByDeviceId(deviceId);
                    Map<String,String>rule = new HashMap<>();
                    for(InfoAndNameVo temp:Allrule)
                    {
                        if( !(temp.getInfo().equals("") || temp.getInfo()==null))
                        rule.put(temp.getName().split("_")[0],temp.getInfo());
                    }


                    List<ThresholdAndNameVo> Allthreshold = serialService.getThresholdByType(info);
                    Map<String,String>threshold = new HashMap<>();
                    for(ThresholdAndNameVo temp:Allthreshold)
                    {
                        if(!(temp.getThreshold().equals("") || temp.getThreshold()==null))
                        threshold.put(temp.getFieldName(),temp.getThreshold());
                    }

                    //这里存放额外的、比较特殊的预警规则
                    //预警规则:标液浓度/仪器量程上限小于0.4或者大于0.6
                    double StandardSolutionConcentration = -1;
                    double instrumentRange = -1;

                    //预警规则:相对误差（（浓度指示值-标液浓度）/标液浓度）超出对应限值±10%
                    double concentrationIndicationValue = -1;

                    //预警规则:仪器量程上限低于排放限值的1.5倍，或高于排放标准限值的3.2倍
                    double EmissionStandardLimits = -1;

                    // 遍历原始JSON中的所有键值对
                    for (Map.Entry<String, Object> entry : originalJson.entrySet()) {
                        SerialMonitorAlarmEntity serialMonitorAlarmEntity = new SerialMonitorAlarmEntity();
                        serialMonitorAlarmEntity.setRecordTime(new Timestamp(System.currentTimeMillis()));
                        serialMonitorAlarmEntity.setBrandModel(deviceId);

                        String originalKey = entry.getKey();
                        Object value = entry.getValue();

                        // 获取中文字段名
                        String chinesename = serialsMapping.selectChineseNameByFieldName(info,originalKey);

                        if(chinesename==null)
                        {
                            chinesename = originalKey;
                        }
                        String id =null;
                        if(threshold.containsKey(originalKey))
                        {
                            String eachThreshlod = threshold.get(originalKey);
                            if(eachThreshlod!=null)
                            {
                                String[] thresholdList = eachThreshlod.split(";");
                                for(String temp:thresholdList)
                                {
                                    String eachValue = temp.split(":")[0];
                                    String eachResult = temp.split(":")[1];
                                    if(eachValue.equals("min"))
                                    {
                                        if(Integer.parseInt((String)value)<Integer.parseInt(eachResult))
                                        {
                                            serialMonitorAlarmEntity.setMonitorType(chinesename);
                                            serialMonitorAlarmEntity.setMonitorData((String) value);
                                            serialMonitorAlarmEntity.setInfo("小于阈值");
                                        }
                                    }else if(eachValue.equals("max"))
                                    {
                                        if(Integer.parseInt((String)value)>Integer.parseInt(eachResult))
                                        {
                                            serialMonitorAlarmEntity.setMonitorType(chinesename);
                                            serialMonitorAlarmEntity.setMonitorData((String) value);
                                            serialMonitorAlarmEntity.setInfo("大于阈值");
                                        }
                                    }
                                }
                            }
                        }
                        if(rule.containsKey(originalKey))
                        {
                            String eachRule = rule.get(originalKey);
                            if(eachRule!=null)
                            {
                                String[] ruleList = eachRule.split(";");
                                for(String temp:ruleList)
                                {
                                    String eachValue = temp.split(":")[0];
                                    String eachResult = temp.split(":")[1];
                                    if(eachResult!=null)
                                    {
//                                        if( ((String)value).equals(eachValue)) {
                                        if( value.equals(eachValue)) {
                                            value = eachResult;
                                            if((value.equals("失败") || value.equals("故障"))||value.equals("异常"))
                                            {
                                                serialMonitorAlarmEntity.setMonitorType(chinesename);
                                                serialMonitorAlarmEntity.setMonitorData((String) value);
                                                serialMonitorAlarmEntity.setInfo("出现异常");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        switch (chinesename)
                        {
                            // 每24h水质自动分析仪未出现结果为1的标样校验结果
                            case "标样核查的结果标识":
                                id = Constants.JedisParameterId+"_"+deviceId+"_"+chinesename;
                                if(jedis.get(id)==null)
                                {
                                    jedis.setex(id,24*60*60,(String)value);
                                }else if( ((String)value).equals("1") )
                                {
                                    //如果结果是1，才存进去，如果不是1，就不更新过期时间
                                    jedis.setex(id,24*60*60,"1");
                                }
                                break;
                            case "工作状态":
                                id = Constants.JedisParameterId+"_"+deviceId+"_"+chinesename;
                                if(jedis.get(id)==null)
                                {
                                    jedis.setex(id,72*60*60, (String) value);
                                }
                                else if(!(value.equals("故障")))
                                {
                                    jedis.setex(id,72*60*60, (String) value);
                                }
                                break;
                            case "标液浓度":
                                StandardSolutionConcentration = Double.parseDouble((String)value);
                                break;
                            case "浓度指示值":
                                concentrationIndicationValue = Double.parseDouble((String)value);
                                break;
                            case "仪器量程":
                                instrumentRange= Double.parseDouble((String)value);
                                break;
                            case "排放限值":
                                EmissionStandardLimits = Double.parseDouble((String)value);
                                break;
                        }
                        //寻找解析规则和预警规则
                        if(serialMonitorAlarmEntity.getMonitorType()!=null)
                            serialService.insertSerialMonitorAlarmRecords(serialMonitorAlarmEntity);
                        cpJson.put(chinesename, value);
                    }
                    //预警规则:标液浓度/仪器量程上限小于0.4或者大于0.6
                    if(StandardSolutionConcentration!=-1 && instrumentRange!=-1)
                    {
                        if(StandardSolutionConcentration/instrumentRange<0.4 || StandardSolutionConcentration/instrumentRange>0.6)
                        {
                            SerialMonitorAlarmEntity serialMonitorAlarmEntity = new SerialMonitorAlarmEntity();
                            serialMonitorAlarmEntity.setRecordTime(new Timestamp(System.currentTimeMillis()));
                            serialMonitorAlarmEntity.setBrandModel(deviceId);
                            serialMonitorAlarmEntity.setMonitorType("标液浓度");
                            serialMonitorAlarmEntity.setMonitorData(String.valueOf(StandardSolutionConcentration));
                            serialMonitorAlarmEntity.setInfo("标液浓度/仪器量程上限小于0.4或者大于0.6");
                            serialService.insertSerialMonitorAlarmRecords(serialMonitorAlarmEntity);
                        }
                    }
                    //预警规则:相对误差（（浓度指示值-标液浓度）/标液浓度）超出对应限值±10%
                    if(concentrationIndicationValue!=-1 && instrumentRange!=-1 && StandardSolutionConcentration!=-1)
                    {
                        if((concentrationIndicationValue-StandardSolutionConcentration)/StandardSolutionConcentration
                                -0.5*instrumentRange >0.1*instrumentRange ||
                                0.5*instrumentRange-(concentrationIndicationValue-StandardSolutionConcentration)/StandardSolutionConcentration
                                        >0.1*instrumentRange )
                        {
                            SerialMonitorAlarmEntity serialMonitorAlarmEntity = new SerialMonitorAlarmEntity();
                            serialMonitorAlarmEntity.setRecordTime(new Timestamp(System.currentTimeMillis()));
                            serialMonitorAlarmEntity.setBrandModel(deviceId);
                            serialMonitorAlarmEntity.setMonitorType("浓度指示值");
                            serialMonitorAlarmEntity.setMonitorData(String.valueOf(concentrationIndicationValue));
                            serialMonitorAlarmEntity.setInfo("相对误差（（浓度指示值-标液浓度）/标液浓度）超出对应限值±10%");
                            serialService.insertSerialMonitorAlarmRecords(serialMonitorAlarmEntity);
                        }
                    }
                    //预警规则:仪器量程上限低于排放限值的1.5倍，或高于排放标准限值的3.2倍
                    if(EmissionStandardLimits!=-1 && instrumentRange!=-1)
                    {
                        if(instrumentRange<1.5*EmissionStandardLimits || instrumentRange>3.2*EmissionStandardLimits)
                        {
                            SerialMonitorAlarmEntity serialMonitorAlarmEntity = new SerialMonitorAlarmEntity();
                            serialMonitorAlarmEntity.setRecordTime(new Timestamp(System.currentTimeMillis()));
                            serialMonitorAlarmEntity.setBrandModel(deviceId);
                            serialMonitorAlarmEntity.setMonitorType("仪器量程");
                            serialMonitorAlarmEntity.setMonitorData(String.valueOf(instrumentRange));
                            serialMonitorAlarmEntity.setInfo("仪器量程上限低于排放限值的1.5倍，或高于排放标准限值的3.2倍");
                            serialService.insertSerialMonitorAlarmRecords(serialMonitorAlarmEntity);
                        }
                    }
                    // 将CP对象添加到最终JSON中
                    finalJson.put("CP", cpJson);
                    SimpleDateFormat DataFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String date = DataFormat.format(new Date());
                    //根目录 + 监测点 + 日期
                    String documentPath = ProcessUtil.IS_WINDOWS ?
                            Constants.SERIALNPATH + brand + "\\" + model + "\\" + date :
                            Constants.SERIALNPATH + brand + "/" + model + "/" + date;
                    String filePath = documentPath + (ProcessUtil.IS_WINDOWS ? "\\" : "/") + "串口监测" + ".txt";
                    File document = new File(documentPath);
                    if (!document.exists()) {
                        document.mkdirs();
                    }
                    File file = new File(filePath);
                    OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file, true)), StandardCharsets.UTF_8);
                    out.write((finalJson.toJSONString() + "\n"));
                    out.flush();
                    out.close();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e1) {
            log.error("redis加入失败！", e1);
        }
    }
    /**
     * 向一个客户端发送消息
     *
     * @param ctx ChannelHandlerContext
     */
    private void push(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();

        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String date = dataFormat.format(new Date());
        channel.writeAndFlush("##0087QN=" + date + ";ST=91;CN=9014;PW=123456;MN=010000A8900016F000169DC0;Flag=4;CP=&&&&2F80\r\n");
    }
}
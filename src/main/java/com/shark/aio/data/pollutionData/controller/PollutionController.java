package com.shark.aio.data.pollutionData.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shark.aio.alarm.entity.AlarmSettingsEntity;
import com.shark.aio.alarm.mapper.AlarmMapping;
import com.shark.aio.data.conditionData.controller.ConditionController;
import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.alarm.contactPart.util.ProcessUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 接受数采仪数据
 * 将数据存储数据库（何种方式分类存储）
 * 将数据传到前端（传多少条，太多会卡）
 */
@Controller
@Slf4j
public class PollutionController {

    @Autowired
    private AlarmMapping alarmMapping;

    /**
     * 验证码校验、接收数据
     * @param request
     * @param response
     * @return
     * @throws ServletException
     * @throws IOException
     */
//	@RequestMapping("/receivePollutionData")
//	@ResponseBody
//	protected String receivePollutionData(HttpServletRequest request, HttpServletResponse response)
//			throws ServletException, IOException {
//
//		log.info("进入接收方法");
//		String strjson = request.getParameter("verify");
//		if (strjson != null) {
//			log.info(JSON.toJSONString(strjson));
//			return strjson;
//		} else {
//			response.setCharacterEncoding("utf-8");
//			JSONObject jsonInfo = getJsonInfo(request);
//			response.getWriter().print("application/json 的 serlvet接收到的数据如下：");
//			response.getWriter().print(jsonInfo);
//		}
//		return strjson;
//	}

//	public static void main(String arg[]){
//		PollutionController c = new PollutionController();
//		HttpServletRequest  request = null;
//		c.getJsonInfo(request);
//	}

    /**
     * 数据转换json，更换时间格式，写入数据文件
     *
     * @param
     * @return
     */
    @Test
//	private JSONObject getJsonInfo(HttpServletRequest request) {
//		JSONObject json = new JSONObject();
//		try {
//
////			InputStreamReader in = new InputStreamReader(request.getInputStream(), "utf-8");
////			BufferedReader br = new BufferedReader(in);
////			StringBuilder sb = new StringBuilder();
////			String line = null;
////			while ((line = br.readLine()) != null) {
////				sb.append(line);
////				log.info(line);
////			}
////			br.close();
//			StringBuilder sb = new StringBuilder();
//			BufferedReader br = null;
//			try {
//				String line = null;
//				br = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
//				while ((line = br.readLine()) != null) {
//					sb.append(line);
//					log.info(line);
//				}
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} finally {
//				br.close();
//			}
//
//			log.info(String.valueOf(sb));
//			json = JSONObject.fromObject(sb.toString());
//			//换时间戳-->字符串
//			//先把要换的数据从json找到，再调用换格式的方法
//			//原方法为：找到json中时间戳的字段，修改后更新整个json
//			//JSONObject为逐级找{}，JSONArray为逐级找[]
//			JSONObject update1 = JSONObject.fromObject(json.getString("data"));
//			JSONArray update2=JSONArray.fromObject(update1.getString("dataPoints"));
//			JSONObject update3= update2.getJSONObject(0);
////			update3.put("time", TimeStamp2Date(JSONObject.fromObject(JSONArray.fromObject(JSONObject.fromObject(json.getString("data")).getString("dataPoints")).get(0)).getString("time")));//直接提交price的key，如果该key存在则替换value
////			update2.set(0, update3);//覆盖掉原来的值
////			update1.put("dataPoints", update2);  //再覆盖
////			json.put("data", update1);
//
//			//现方法：找到该字段，修改后，取值直接写入文件
//			String time0 = TimeStamp2Date(JSONObject.fromObject(JSONArray.fromObject(JSONObject.fromObject(json.getString("data")).getString("dataPoints")).get(0)).getString("time"));
//			String time1 = TimeStamp2Date(JSONObject.fromObject(JSONArray.fromObject(JSONObject.fromObject(json.getString("data")).getString("dataPoints")).get(1)).getString("time"));
//
//			String name0 = JSONObject.fromObject(JSONArray.fromObject(JSONObject.fromObject(json.getString("data")).getString("dataPoints")).get(0)).getString("variableName");
//			String name1 = JSONObject.fromObject(JSONArray.fromObject(JSONObject.fromObject(json.getString("data")).getString("dataPoints")).get(1)).getString("variableName");
//
//			String value0 = JSONObject.fromObject(JSONArray.fromObject(JSONObject.fromObject(json.getString("data")).getString("dataPoints")).get(0)).getString("value");
//			String value1 = JSONObject.fromObject(JSONArray.fromObject(JSONObject.fromObject(json.getString("data")).getString("dataPoints")).get(1)).getString("value");
//
//			String deviceID = JSONObject.fromObject((json.getString("data"))).getString("deviceId");
//
//			Double value0Double = Double.parseDouble(value0);
//			AlarmSettingsEntity alarmSettingsEntity = null;
//			AlarmRecordEntity alarmRecordEntity= null ;
//			alarmSettingsEntity =alarmMapping.getAlarmSettingsEntity(name0);
//			if(alarmSettingsEntity!= null){
//				if(alarmSettingsEntity.getLowerLimit() > value0Double){
//					alarmRecordEntity.setAlarmTime(time0);
//					alarmRecordEntity.setMonitor("监测点1");
//					alarmRecordEntity.setMonitorClass("污染源监控");
//					alarmRecordEntity.setMonitorValue(name0);
//					alarmRecordEntity.setMonitorData(value0);
//					alarmRecordEntity.setMessage("低于阈值");
//					alarmMapping.insertalarmRecords(alarmRecordEntity);
//				}
//				if(alarmSettingsEntity.getUpperLimit() < value0Double){
//					alarmRecordEntity.setAlarmTime(time0);
//					alarmRecordEntity.setMonitor("监测点1");
//					alarmRecordEntity.setMonitorClass("污染源监控");
//					alarmRecordEntity.setMonitorValue(name0);
//					alarmRecordEntity.setMonitorData(value0);
//					alarmRecordEntity.setMessage("高于阈值");
//					alarmMapping.insertalarmRecords(alarmRecordEntity);
//				}
//			}
//
//
//			//			String pretty = JSON.toJSONString(json, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
////					SerializerFeature.WriteDateUseDateFormat);
//			String documenPath = Constants.POLLUTIONPATH + DateUtil.Data;
//			String filePath = documenPath + Constants.POLLUTIONDATA;
//			File document = new File(documenPath);
//			if (!document.exists()) {
//				document.mkdirs();
//			}
//			File file = new File(filePath);
//			String space = " ";
//			try {
//				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file, true));
////				out.write(String.valueOf(json + "\r\n").getBytes());
//				out.write(("{\"data\":[" +
//						"{\"名称" + "\":" + "\"" + name0 + "\"," +
//						"\"时间\":" + "\"" + time0 + "\","+
//						"\"幅值\":" + "\"" + value0 + "\"}," +
//						"{\"名称" + "\":" + "\"" + name1 + "\"," +
//						"\"时间\":" + "\"" + time1 + "\"," +
//						"\"幅值\":" + "\"" + value1 + "\"}]," +
//						"\"设备ID\":" + "\"" + deviceID + "\"}" + "\r\n").getBytes());
//				out.flush();
//				try {
//					out.close();
//				} catch (IOException e) {
//					log.info("FileService/saveData, 关闭文件失败", e);
//				}
//				System.out.println("json数据保存到成功！！！");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			System.out.println("从前端接收到的json数据：" + json);
////			log.info("从前端接收到的json数据：" + json);
////			log.info(
////					"温度值" + JSONObject.fromObject(JSONArray.fromObject(JSONObject.fromObject(json.getString("data")).getString("dataPoints")).get(0)).getString("value") + "湿度值"
////							+ JSONObject.fromObject(JSONArray.fromObject(JSONObject.fromObject(json.getString("data")).getString("dataPoints")).get(1)).getString("value"));
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//		}
//		return json;
//	}


    /**
     * 前端访问数据文件
     * @param filePath
     * @param req
     * @throws IOException
     */
    @RequestMapping("/returnPollutionData")
    @CrossOrigin
    public void returnPollutionData(String monitorName, String dir, String keys, HttpServletResponse rep, HttpServletRequest request) throws IOException {

        if (!"null".equals(monitorName)) {
            String name = Constants.POLLUTIONPATH + monitorName + (ProcessUtil.IS_WINDOWS ? "\\" : "/") + dir + (ProcessUtil.IS_WINDOWS ? "\\" : "/") + Constants.POLLUTIONDATA;
            try {
                FileInputStream fin = new FileInputStream(name);
                InputStreamReader reader = new InputStreamReader(fin, "utf-8");
                BufferedReader buffReader = new BufferedReader(reader);
                String strTmp = "";
                ArrayList<JSONObject> data = new ArrayList<>();
                ArrayList<String> keyset = new ArrayList<>();
                List<String> keyset_notime = new ArrayList<>();
                keyset.add("日期-时间");
                keyset_notime.add("--- 请选择数据 ---");
                JSONObject result = new JSONObject();
                long length = getLineNumber(new File(name));
                while ((strTmp = buffReader.readLine()) != null) {
                    ConditionController.sample(buffReader, length);
                    JSONObject jsonObject = JSON.parseObject(strTmp);
                    JSONObject dataObj = jsonObject.getJSONObject("CP");
                    dataObj.put("日期-时间", jsonObject.getString("DataTime"));
                    data.add(dataObj);
                    for (String key : dataObj.keySet()) {
                        if (!keyset.contains(key)) {
                            keyset.add(key);
                            if (!key.contains("Flag")){
                                keyset_notime.add(key);
                            }

                        }
                    }
                }
                buffReader.close();
                result.put("keySet", keyset);

                result.put("keySet_notime", keyset_notime);
                result.put("data", data.toArray());
                String response = result.toJSONString();

                rep.setContentType("text/html;charset=utf-8");
                rep.getWriter().write(response);
            } catch (Exception e) {
                log.error("数据文件查询失败！");
                request.setAttribute(Constants.MSG, "暂无数据");
                JSONObject result = new JSONObject();
                result.put("keySet", "暂无数据");
                result.put("data", null);
                String response = result.toJSONString();
                rep.setContentType("text/html;charset=utf-8");
                rep.getWriter().write(response);
            }

//            returnTrendChart(monitorName, keys, rep, request);
        }

    }


    /**
     * 时间戳转字符串
     *
     * @param timestampString
     * @return
     */
    public static String TimeStamp2Date(String timestampString) {

        String formats = "yyyy-MM-dd HH:mm:ss";

        Long timestamp = Long.parseLong(timestampString) * 1000;

        String date = new SimpleDateFormat(formats, Locale.CHINA).format(new Date(timestamp));

        return date;

    }

    @RequestMapping("/pollutionMonitor")
    public String pollutionMonitorWeb(HttpServletRequest request, HttpServletResponse rep, String key) throws IOException {

        File file = new File(Constants.POLLUTIONPATH);
        if (!file.exists()) file.mkdirs();
        File[] files = file.listFiles();
        if (files==null || files.length == 0) {
            request.setAttribute(Constants.MSG, "暂无数据");
            request.setAttribute("allMonitors", null);
        } else {
            String[] fileList = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                fileList[i] = files[i].getName();
            }
            request.setAttribute("allMonitors", fileList);
        }

        AlarmSettingsEntity alarmSettingsEntity = new AlarmSettingsEntity();
//        alarmSettingsEntity.setMonitorClass("未知");
//        alarmSettingsEntity.setMonitorValue("请选择数据");
//        alarmSettingsEntity.setUpperLimit(0.0);
//        alarmSettingsEntity.setLowerLimit(0.0);
//        alarmSettingsEntity.setUnit("未知");
//        alarmSettingsEntity.setMessage("未知");
//        request.getSession().setAttribute("alarmSettingsEntity",alarmSettingsEntity);


        if (null != key && !"--- 请选择数据 ---".equals(key)){
            if (null != alarmMapping.getAlarmSettings(key)){
                alarmSettingsEntity = alarmMapping.getAlarmSettings(key);
//                result.put("qqq", null);x
                System.out.println( alarmSettingsEntity);
            }else {
//                request.setAttribute("msg", "未设置该数据参数的预警！");
//                request.setAttribute("alarmSettingsEntity", alarmSettingsEntity);
//                result.put("qqq", "未设置该数据参数的预警！");
                alarmSettingsEntity.setMonitorClass("未知");
                alarmSettingsEntity.setMonitorValue("请选择数据");
                alarmSettingsEntity.setUpperLimit(0.0);
                alarmSettingsEntity.setLowerLimit(0.0);
                alarmSettingsEntity.setUnit("未知");
                alarmSettingsEntity.setMessage("未知");
            }
        }
        request.setAttribute("alarmSettingsEntity", alarmSettingsEntity);
        return "pollutionMonitor";
    }
    @RequestMapping("/pollutionAlarm")
    @CrossOrigin
    @ResponseBody
    public void pollutionAlarm( HttpServletRequest request, HttpServletResponse rep, String key) throws IOException {

        JSONObject result = new JSONObject();
        if (null != key && !"--- 请选择数据 ---".equals(key)){
            if (null == alarmMapping.getAlarmSettings(key)){
                result.put("qqq", "未设置该数据参数的预警！");
            }
        }
        String response = result.toJSONString();
        rep.setContentType("text/html;charset=utf-8");
        rep.getWriter().write(response);

    }
    @RequestMapping("returnPollutionFileList")
    @CrossOrigin
    @ResponseBody
    public String[] returnConditionFileList(String monitorName) {
        try {
            File conditionDir = new File(Constants.POLLUTIONPATH + monitorName);
            File[] files = conditionDir.listFiles();
            if (files == null) {
                return null;
            } else {
                String[] fileList = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    fileList[i] = files[i].getName();
                }
                Arrays.sort(fileList,(o1,o2)->{
                    return o2.compareTo(o1);
                });
                return fileList;
            }
        } catch (Exception e) {
            log.error("数据文件查询失败！");
            return null;
        }
    }

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


    /**
     * 画图
     *
     * @param
     * @param
     * @throws IOException
     */
    @RequestMapping("/returnTrendChart")
    @CrossOrigin
    @ResponseBody
    public void returnTrendChart(String monitorName, String keys, HttpServletResponse req, HttpServletRequest request) throws IOException {
//        monitorName2 = "厂房";
//        key = "功率因数";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate currentDate = LocalDate.now().minusDays(3);

        LocalDate previousDay = currentDate.minusDays(1);
        LocalDate previousDay2 = currentDate.minusDays(2);
        LocalDate previousDay3 = currentDate.minusDays(3);
        LocalDate previousDay4 = currentDate.minusDays(4);
        LocalDate tomorrowDay = currentDate.plusDays(1);
        LocalDate tomorrowDay2 = currentDate.plusDays(2);
        String today = currentDate.format(fmt);
        String yesterday = previousDay.format(fmt);
        String yesterday2 = previousDay2.format(fmt);
        String yesterday3 = previousDay3.format(fmt);
        String yesterday4 = previousDay4.format(fmt);
        String tomorrow = tomorrowDay.format(fmt);
        String tomorrow2 = tomorrowDay2.format(fmt);

        String confirm = null;
        String[] numsData;
        String[] numsTime = new String[]{yesterday4, yesterday3, yesterday2, yesterday, today, tomorrow, tomorrow2};
        if (keys == null || "null".equals(keys) || monitorName == null || "null".equals(monitorName )){

            confirm = null;
            numsData = new String[]{"0","0","0","0","0","0","0"};
        }else if ( !"--- 请选择数据 ---".equals(keys)) {
            String name = Constants.POLLUTIONPATH + monitorName + (ProcessUtil.IS_WINDOWS ? "\\" : "/");
            File doc_today = new File(name + today);
            File doc_yesterday = new File(name + yesterday);
            File doc_yesterday2 = new File(name + yesterday2);
            File doc_yesterday3 = new File(name + yesterday3);
            File doc_yesterday4 = new File(name + yesterday4);
            if (!doc_today.exists() || !doc_yesterday.exists() || !doc_yesterday2.exists() || !doc_yesterday3.exists() || !doc_yesterday4.exists()) {
//                request.setAttribute(Constants.MSG, "数据样本不足，未能生成趋势图！");
                System.out.println("数据样本不足，未能生成趋势图！");
                numsData =  new String[]{"0","0","0","0","0","0","0"};
                confirm = "数据样本不足，未能生成趋势图！";
            } else {
                String houzhui = (ProcessUtil.IS_WINDOWS ? "\\" : "/") + Constants.POLLUTIONDATA;
                String todayFile = name + today + houzhui;
                String yesterdayFile = name + yesterday + houzhui;
                String yesterday2File = name + yesterday2 + houzhui;
                String yesterday3File = name + yesterday3 + houzhui;
                String yesterday4File = name + yesterday4 + houzhui;

                String a = average(todayFile, keys);
                String b = average(yesterdayFile, keys);
                String c = average(yesterday2File, keys);
                String d = average(yesterday3File, keys);
                String e = average(yesterday4File, keys);

                Double a1 = Double.parseDouble(a);
                Double b1 = Double.parseDouble(b);
                Double c1 = Double.parseDouble(c);
                Double d1 = Double.parseDouble(d);
                Double e1 = Double.parseDouble(e);

                double f1 = a1 + b1 + c1 + d1;
                double f2 = f1 / 4;
                double g1 = b1 + c1 + d1 + e1 + f2;
                double g2 = g1 / 5;

                int weishu = 0;
                if (a.contains(".")) {
                    weishu = a.length() - (a.indexOf(".") + 1);
                }

                String f = String.format("%." + weishu + "f", f2);
                String g = String.format("%." + weishu + "f", g2);

                numsData = new String[]{e, d, c, b, a, f, g};
            }
        } else {
            System.out.println("字段选择错误");
            confirm = "请重新选择数据参数";
            numsData = new String[]{"0","0","0","0","0","0","0"};
        }

        JSONObject result = new JSONObject();
        result.put("numsTime", numsTime);
        result.put("numsData", numsData);
        result.put("qqq", confirm);
        String response = result.toJSONString();

        req.setContentType("text/html;charset=utf-8");
        req.getWriter().write(response);

    }

    public String average(String fileURL, String key) throws IOException {

        ArrayList<Double> value = new ArrayList<>();
        FileInputStream fin = new FileInputStream(fileURL);
        InputStreamReader reader = new InputStreamReader(fin, "utf-8");
        BufferedReader buffReader = new BufferedReader(reader);
        String strTmp = "";
        int weishu = 0;
        while ((strTmp = buffReader.readLine()) != null) {
//                    ConditionController.sample(buffReader, length);
            JSONObject jsonObject = JSON.parseObject(strTmp);
            if (jsonObject != null) {
                JSONObject dataObj = jsonObject.getJSONObject("CP");
                if (dataObj.containsKey(key)){
                    if ( dataObj.getString(key).contains(".")) {
                        weishu = dataObj.getString(key).length() - (dataObj.getString(key).indexOf(".") + 1);
                    } else {
                        weishu = 0;
                    }

                    value.add(Double.parseDouble(dataObj.getString(key)));
                }

            }
        }
//        System.out.println(value.get(0));

        DoubleSummaryStatistics statistics = value.stream().mapToDouble(Number::doubleValue).summaryStatistics();
//        System.out.println(statistics);
        String out = String.format("%." + weishu + "f", statistics.getAverage());
//        System.out.println(out);
        buffReader.close();

        return out;
    }

}

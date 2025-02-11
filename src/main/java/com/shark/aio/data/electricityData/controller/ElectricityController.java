package com.shark.aio.data.electricityData.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shark.aio.data.conditionData.controller.ConditionController;
import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.alarm.contactPart.util.ProcessUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * 接受用电数据
 * 将数据存储数据库（何种方式分类存储）
 * 将数据传到前端（传多少条，太多会卡）
 */

@Controller
@Slf4j
public class ElectricityController {


    /**
     * 前端访问数据文件
     *
     * @param
     * @param req
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping("/returnElectricData")
    public void returnElectricityData(String monitorName, String dir, HttpServletResponse req,HttpServletRequest request) throws IOException {
        if (!"null".equals(monitorName) && !"null".equals(dir)) {
            String name = Constants.ELECTRICPATH + monitorName + (ProcessUtil.IS_WINDOWS ? "\\" : "/") + dir + Constants.ELECTRICDATA;
            try {
                FileInputStream fin = new FileInputStream(name);
                InputStreamReader reader = new InputStreamReader(fin, "utf-8");
                BufferedReader buffReader = new BufferedReader(reader);
                String strTmp = "";
                ArrayList<JSONObject> data = new ArrayList<>();
                ArrayList<String> keyset = new ArrayList<>();
                keyset.add("日期-时间");
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
                        }
                    }
                }
                result.put("keySet", keyset);
                result.put("data", data.toArray());
                String response = result.toJSONString();
                buffReader.close();
                req.setContentType("text/html;charset=utf-8");
                req.getWriter().write(response);
            } catch (Exception e) {
                log.error("数据文件查询失败！");
                request.setAttribute(Constants.MSG, "暂无数据");
                JSONObject result = new JSONObject();
                result.put("keySet", "暂无数据");
                result.put("data", null);
                String response = result.toJSONString();
                req.setContentType("text/html;charset=utf-8");
                req.getWriter().write(response);
            }

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


    @RequestMapping("/electricityMonitor")
    public String electricityMonitorWeb(HttpServletRequest request) {
        File file = new File(Constants.ELECTRICPATH);
        if (!file.exists()) file.mkdirs();
        File[] files = file.listFiles();
        if (files.length == 0) {
            request.setAttribute(Constants.MSG, "暂无数据");
            request.setAttribute("allMonitors", null);
        } else {
            String[] fileList = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                fileList[i] = files[i].getName();
            }
            Arrays.sort(fileList,(o1,o2)->{
                return  o2.compareTo(o1);
            });
            request.setAttribute("allMonitors", fileList);
        }

        log.info("进入用电监测页面成功！");
        return "electricityMonitor";
    }

    @RequestMapping("returnElectricFileList")
    @CrossOrigin
    @ResponseBody
    public String[] returnElectricFileList(String monitorName, HttpServletRequest request) {
        try {
            File electricDir = new File(Constants.ELECTRICPATH + monitorName);
            File[] files = electricDir.listFiles();
            if (files == null) {
                request.setAttribute(Constants.MSG, "暂无数据");
                return null;
            } else {
                String[] fileList = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    fileList[i] = files[i].getName();
                }
                Arrays.sort(fileList,(o1,o2)->{
                    return  o2.compareTo(o1);
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
}

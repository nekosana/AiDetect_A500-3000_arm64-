package com.shark.aio.data.conditionData.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shark.aio.data.conditionData.service.ConditionService;
import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.alarm.contactPart.util.ProcessUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 接受数工况数据
 * 将数据存储数据库（何种方式分类存储）
 * 将数据传到前端（传多少条，太多会卡）
 */

/**
 * 数采仪数据格式
 * ##0746ST=31;CN=2061;PW=123456;MN=7568770259402;Flag=0;CP=&&DataTime=20221008100000;B02-Min=1.6960,B02-Avg=3.0586,B02-Max=3.7704,B02-Cou=11010.8437;S01-Min=17.7469,S01-Avg=19.4636,S01-Max=19.6944;S02-Min=3.2459,S02-Avg=5.6705,S02-Max=6.9578;S03-Min=30.0434,S03-Avg=30.2675,S03-Max=30.4503;S08-Min=-0.4643,S08-Avg=-0.3541,S08-Max=0.0000;S05-Min=6.1814,S05-Avg=6.8655,S05-Max=7.0097;a24088-Min=2.4957,a24088-Avg=3.2176,a24088-Max=4.7744,a24088-Cou=0.0354;25-Min=6.4292,25-Avg=12.1457,25-Max=20.0606,25-Cou=0.1336;a05002-Min=2.7715,a05002-Avg=7.8561,a05002-Max=14.7934,a05002-Cou=0.0863;17-Min=0.0000,17-Avg=0.0000,17-Max=0.0000,17-Cou=0.0000;18-Min=0.0000,18-Avg=0.0000,18-Max=0.0000,18-Cou=0.0000;16-Min=0.0000,16-Avg=0.0000,16-Max=0.0000,16-Cou=0.0000&&7BC0;
 * <p>
 * {##0746ST=31;CN=2061;PW=123456;MN=7568770259402;Flag=0;
 * CP=&&DataTime=20221008100000;16-Avg=0.0000,16-Max=0.0000,16-Cou=0.0000
 * &&7BC0}
 */
@Controller
@Slf4j
public class ConditionController {

    @Autowired
    private ConditionService conditionService;


    /**
     * 前端访问数据文件
     *
     * @param monitorName
     * @param req
     * @throws IOException
     */
    @RequestMapping("/returnConditionData")
    @CrossOrigin
    public void returnConditionData(String monitorName, String dir, HttpServletResponse req, HttpServletRequest request) throws IOException {
        if (!"null".equals(monitorName)) {
            String name = Constants.CONDITIONPATH + monitorName + (ProcessUtil.IS_WINDOWS ? "\\" : "/") + dir + (ProcessUtil.IS_WINDOWS ? "\\" : "/") + Constants.CONDITIONDATA;
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

                    sample(buffReader, length);
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
//			System.out.println(result);
                result.put("data", data.toArray());
//			System.out.println(result);
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


    /**
     * 跳转到工况历史页面
     * GET对应侧边栏
     * POST对应页面中的条件查询
     *
     * @param request request
     * @return
     */
//	@RequestMapping(value = {"/conditionHistory","/conditionHistory/{pageNum}/{pageSize}"})
//	public String toConditionHistoryPage(HttpServletRequest request,
//									 @PathVariable(required = false) Integer pageNum,
//									 @PathVariable(required = false) Integer pageSize
//	){
//		//条件查询的HashMap，若没有条件查询默认为null
//		HashMap<String,String> features = null;
//		//条件查询是POST方法，此处判断POST
//		if (request.getMethod().equals("POST")){
//			//获取查询条件，并放置到HashMap中
//			features = new HashMap<>();
//			String date = request.getParameter("date");
//			try {
//				//开始结束时间转化为时间戳
//				if (!ObjectUtil.isEmptyString(date)) features.put("date", DateUtil.toTimestamp(date));
//			}catch (ParseException e){
//				log.error("转化为时间戳失败！",e);
//			}
//			PageInfo<> pageInfo = new PageInfo(List,5);
//			//.json文件  datatable库
//			request.setAttribute("date", date);
//			request.setAttribute("history","对象");
//		}
//		request.setAttribute("class","conditionHistory");
//
//		return "conditionMonitor";
//	}

//	/**
//	 * 跳转到工况历史页面
//	 * GET对应侧边栏
//	 * POST对应页面中的条件查询
//	 * @param request request
//	 * @param pageNum 分页插件：当前页码
//	 * @param pageSize 分页插件：每页大小
//	 * @return
//	 */
//	@RequestMapping(value = {"/conditionOnline","/conditionOnline/{pageNum}/{pageSize}"})
//	public String toConditionOnlinePage(HttpServletRequest request,
//									 @PathVariable(required = false) Integer pageNum,
//									 @PathVariable(required = false) Integer pageSize
//	){
//		//条件查询的HashMap，若没有条件查询默认为null
//		HashMap<String,String> features = null;
//		//条件查询是POST方法，此处判断POST
//		if (request.getMethod().equals("POST")){
//			//获取查询条件，并放置到HashMap中
//			features = new HashMap<>();
//			String date = request.getParameter("date");
//			try {
//				//开始结束时间转化为时间戳
//				if (!ObjectUtil.isEmptyString(date)) features.put("date", DateUtil.toTimestamp(date));
//			}catch (ParseException e){
//				log.error("转化为时间戳失败！",e);
//			}
//			request.setAttribute("date", date);
//			request.setAttribute("class","conditionOnline");
//			request.setAttribute("online","对象");
//		}
//		return "conditionMonitor";
//	}
    @RequestMapping("/conditionMonitor")
    public String conditionMonitorWeb(HttpServletRequest request) {
        File file = new File(Constants.CONDITIONPATH);
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
            Arrays.sort(fileList,(o1, o2)->{
                return  o2.compareTo(o1);
            });
            request.setAttribute("allMonitors", fileList);
        }
        log.info("进入末端监测页面成功！");
        return "conditionMonitor";
    }

    @RequestMapping("returnConditionFileList")
    @ResponseBody
    @CrossOrigin
    public String[] returnConditionFileList(String monitorName) {
        try {
            File conditionDir = new File(Constants.CONDITIONPATH + monitorName);
            File[] files = conditionDir.listFiles();
            if (files == null) {
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

    public static void sample(BufferedReader buffReader, long length) throws IOException {
        if (length > 10000) {
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
        } else if (length > 7000) {
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
        } else if (length > 5000) {
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
        } else if (length > 2000) {
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
            buffReader.readLine();
        } else if (length > 1000) {
            buffReader.readLine();
            buffReader.readLine();
        }
    }

}

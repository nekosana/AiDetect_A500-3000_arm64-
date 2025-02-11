package com.shark.aio.util;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

public class LogProcessor {
    //可在常量池中定义 0 无人 1 有人 3 未找到指定文件

    public int processLogFile(String logFilePath) throws JSONException{
        File logFile = new File(logFilePath);

        // 检查日志文件是否存在
        if (!logFile.exists()) {
            return 3;  // 之后替换成常量池中名称
        }

        // 读取文件并解析第一行
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line ;
            while((line = reader.readLine()).length()!=0){
                if(line == "") continue;
                JSONObject logEntry = new JSONObject(line);
                // 提取字段
                String imageInput = logEntry.getString("imageInput");
                String time = logEntry.getString("time");
                boolean isDetectPeople = false;
                boolean isDetectCar = false;

                JSONArray result = logEntry.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject detection = result.getJSONObject(i);
                    String label = detection.getString("label");

                    // 判断标签并设置标志
                    if (label.equals("person")) {
                        isDetectPeople = true;
                        return 1;
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 如果文件没有内容或发生错误，返回null
        return 0;
    }
    @Test
    public  void main() {
        LogProcessor logProcessor = new LogProcessor();

        // 假设日志文件路径
        String logFilePath = "D:\\项目\\AIO\\日志\\2024_12_18_17.log";
        // 输出检测信息
        try{
            int res = logProcessor.processLogFile(logFilePath);
            System.out.println(res);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}


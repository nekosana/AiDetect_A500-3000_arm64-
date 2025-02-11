package com.shark.aio.alarm.contactPart.util;

import com.shark.aio.base.controller.FFmpegProcess;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @Description: 调用命令 【windows&Linux】 @Author: cq @Date: 2022/12/20 @Version: V1.0
 * 用于启动ffmpeg 随之将rtsp 转化为rtmp 返回给Flv.js使用
 */

public class CommandUtil {

    /** 调用linux命令* */
    public FFmpegProcess linuxExec(String[] cmd) throws NoSuchFieldException, IllegalAccessException {
        StringBuffer command = new StringBuffer();
        for (String c : cmd){
            command.append(c);
            command.append(" ");
        }
        System.out.println("执行命令 " + command.toString() );
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            Process process = processBuilder.start();

            /*String line;
            BufferedReader stdoutReader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuffer out = new StringBuffer();
            while ((line = stdoutReader.readLine()) != null) {
                out.append(line);
            }
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            process.destroy();*/
            return new FFmpegProcess(process);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /** 调用windwos命令* */
    public FFmpegProcess winExec(String cmd) throws NoSuchFieldException, IllegalAccessException {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(cmd);
            new InputStreamReader(process.getInputStream());
            return new FFmpegProcess(process);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

package com.shark.aio.base.diskCapacity;

import com.shark.aio.alarm.contactPart.util.Constants;
import com.shark.aio.alarm.contactPart.util.ProcessUtil;
import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;

/**
 * @author lbx
 * @date 2023/3/30 - 13:53
 **/
@Slf4j
@Controller
public class DiskCapacityController {

    /**
     * 磁盘、内存的使用情况
     *
     * @param
     * @param req
     * @return
     */
    @RequestMapping("/diskCapacityManagement")
    public String diskCapacityManagement(HttpServletRequest req, HttpServletResponse reps) throws IOException {

        try{
            OperatingSystemMXBean mem = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            // 获取内存总容量
            long totalMemorySize = mem.getTotalPhysicalMemorySize();
            // 获取可用内存容量(剩余物理内存）
            long freeMemorySize = mem.getFreePhysicalMemorySize();

            float usedRAMRate = (float) (((totalMemorySize - freeMemorySize) * 1.0 / totalMemorySize) * 100);

            DecimalFormat df = new DecimalFormat("#0.00");
//        File[] disks = File.listRoots();
            File disks = new File(Constants.DISKCAPACITYPATH);
            //获取总容量
            long totalSpace = disks.getTotalSpace();
            // 获取剩余容量
            long usableSpace = disks.getUsableSpace();
            // 获取已经使用的容量
            long usedSpace = totalSpace - usableSpace;
            // 获取使用率
            float useRate = (float) ((usedSpace * 1.0 / totalSpace) * 100);

            boolean isConnect = isConnect("www.baidu.com");
            System.out.println("网络状态" + isConnect);
            DiskCapacityEntity diskCapacity = new DiskCapacityEntity();
            diskCapacity.setTotalMemorySize(transformation(totalMemorySize));
            diskCapacity.setTotalSpace(transformation(totalSpace));
            diskCapacity.setConnect(isConnect);
            diskCapacity.setUsedRAMRate(df.format(usedRAMRate) + "%");
            diskCapacity.setUseRate(df.format(useRate) + "%");
            if (useRate > 70){
                req.setAttribute(Constants.MSG, "磁盘空间不足，请尽快进行文件管理！");
            }


            req.setAttribute("diskCapacity", diskCapacity);
            log.info("进入磁盘管理页面成功！");
        }catch (Exception e){
            log.error("进入磁盘管理页面失败！",e);
        }


        return "diskCapacityManagement";
    }

    /**
     * 将字节容量转化为GB
     */
    public static String transformation(long size) {
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format((float) size / 1024 / 1024 / 1024) + "GB" + "   ";
    }


    public static boolean isConnect(String urlPath){
        //定义其返回的状态，默认为false，网络不正常
        boolean connect = false;
        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            //-c3代表Linux测试三次，Windows不用加-c参数
            String command = ProcessUtil.IS_WINDOWS?"ping " : "ping -c3 ";
            process =  runtime.exec( command + urlPath);
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is,"GBK");
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
//            System.out.println("返回值为: {}"+sb);
            is.close();
            isr.close();
            br.close();
            if (null != sb && !sb.toString().equals("")) {
                String ttl = ProcessUtil.IS_WINDOWS ? "TTL" : "ttl";
                if (sb.toString().indexOf(ttl) > 0) {
                    // 网络畅通
                    connect = true;
                } else {
                    // 网络不畅通
                    connect = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connect;
    }

}

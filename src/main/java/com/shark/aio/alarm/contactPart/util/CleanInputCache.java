package com.shark.aio.alarm.contactPart.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;

public class CleanInputCache extends Thread {
    private String type;
    private Process process;

    public CleanInputCache(Process process, String type) {
        this.process = process;
        this.type = type;
    }



    public void run() {

        try {
            InputStream is;
            if (this.type.equals("ERROR")) {
                is = process.getErrorStream();
            } else {
                is = process.getInputStream();
            }
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;

            while (process.isAlive()) {
//                System.out.println(br.ready());
                line = br.readLine();
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//
//
//
//
//    }

//    public void run() {
//
//        try {
//            InputStream is;
//            if (this.type.equals("ERROR")) {
//                is = process.getErrorStream();
//            } else {
//                is = process.getInputStream();
//            }
//            InputStreamReader isr = new InputStreamReader(is);
//            BufferedReader br = new BufferedReader(isr);
//            String line = null;

// 设置超时时间（毫秒）
//                int timeout = 5000; // 5秒超时
//
//                // 创建线程异步读取输出信息
//                Thread inputThread = new Thread(() -> {
//                    try {
//                        long startTime = System.currentTimeMillis();
//
//                        while (true) {
//                            // 检查是否超时
//                            if (System.currentTimeMillis() - startTime > timeout) {
//                                System.out.println("Read timeout reached.");
//                                break;
//                            }
//                            startTime = System.currentTimeMillis();
//                            // 尝试读取一行文本
//
//
//                            String line = null;
//
//                            if (br.ready()) {
//                                line = br.readLine();
//                                System.out.println("1   " + line);
//                            } else {
//                                Thread.sleep(1000);
//                                if (br.ready()) {
//                                    line = br.readLine();
//                                    System.out.println("2   " + line);
//                                } else {
//                                    Thread.sleep(5000);
//                                    if (br.ready()) {
//                                        line = br.readLine();
//                                        System.out.println("3   " + line);
//                                    } else {
////                            process.destroyForcibly();
////                            process.destroy();
//                                        System.out.println(ProcessUtil.getProcessIdInLinux(process));
//                                        ProcessUtil.close(ProcessUtil.getProcessIdInLinux(process));
//                                        System.out.println(1111111);
//                                        break;
//                                    }
//                                }
//                            }
//
//
//                            // 如果读取到文本，处理文本
////                        if (line != null) {
////                            System.out.println("Output: " + line);
////                        }
//
//                            // 如果输入流结束，退出循环
//                            if (line == null && !process.isAlive()) {
//                                System.out.println("Input stream has ended.");
//                                break;
//                            }
//                        }
//                    } catch (IOException e) {
//                        System.out.println(89778);
//                        e.printStackTrace();
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//                // 启动读取线程
//                inputThread.start();
//                // 等待命令行进程结束
//                int exitCode = process.waitFor();
//                // 关闭读取线程
//                inputThread.join();
//
//                System.out.println("Command exited with code " + exitCode);


            while (process.isAlive()) {
//                System.out.println( Math.random() + "     " + process);

//                if (is.read(buffer) != -1) {
//                    System.out.println("1   " );
//                } else {
//                    System.out.println(0000);
//                    Thread.sleep(1000);
//                    if (is.read(buffer) != -1){
//                        System.out.println("2   ");
//                    }else {
//                        Thread.sleep(5000);
//                        if (is.read(buffer) != -1){
//                            System.out.println("3   ");
//                        }else {
////                            process.destroyForcibly();
////                            process.destroy();
//                            System.out.println(1111111);
//                            break;
//                        }
//                    }
//                }


//                if ((line = br.readLine()) != null) {
//                    System.out.println("1   " + type + ">>>" + line);
//                } else {
//                    Thread.sleep(1000);
//                    if ((line = br.readLine()) != null) {
//                        System.out.println("2   " + type + ">>>" + line);
//                    } else {
//                        Thread.sleep(5000);
//                        if ((line = br.readLine()) != null) {
//                            System.out.println("3   " + type + ">>>" + line);
//                        } else {
////                            process.destroyForcibly();
//                            process.destroy();
//                            System.out.println(1111111);
//                            break;
//                        }
//                    }
//                }
//
//            }
//        } catch (Exception e) {
//
        }
    }
}

class AA {

    public long getUnixPid(Process process) {
        try {
            // 获取Java虚拟机的进程ID
            String name = ManagementFactory.getRuntimeMXBean().getName(
            );
            long currentPid = Long.parseLong(name.split("@")[0]);

            // 获取子进程的进程ID
            if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field pidField = process.getClass().getDeclaredField("pid");
                pidField.setAccessible(true);
                return (long) pidField.get(process);
            }

            return currentPid; // 如果获取失败，返回当前Java进程的PID

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


}
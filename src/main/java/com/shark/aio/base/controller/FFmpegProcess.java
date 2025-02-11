package com.shark.aio.base.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FFmpegProcess {
    private Process process;

    private long lastTime = System.currentTimeMillis();

    private Thread info;

    private Thread error;

    public boolean isAlive(){
        long time = System.currentTimeMillis()-lastTime;
        return process.isAlive()&&time<2000;
    }

    public FFmpegProcess(Process process){
        this.process = process;
        this.info = new Thread(() -> {

            try {
                InputStream is;
                is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;

                while (process.isAlive()) {
                    line = br.readLine();
                    System.out.println(line);
                    lastTime = System.currentTimeMillis();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        this.error = new Thread(() -> {

            try {
                InputStream is;
                is = process.getErrorStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;

                while (process.isAlive()) {
//                System.out.println(br.ready());
                    line = br.readLine();
//                    System.out.println(line);
                    lastTime = System.currentTimeMillis();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        this.info.start();
        this.error.start();
    }

    public void destroy(){
        this.info.interrupt();
        this.error.interrupt();
        this.process.destroy();

    }

}

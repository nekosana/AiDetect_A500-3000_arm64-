package com.shark.aio.serial.utils;

import com.fazecast.jSerialComm.SerialPort;
import com.shark.aio.serial.entity.ConfigDeviceEntity;
import com.shark.aio.serial.entity.SerialDeviceEntity;

import java.util.ArrayList;
import java.util.List;

public class SerialUtils {


    public static String validateParams(String brand, String Model, String softwareVersion, String communicationId,
                                        String port, Long baudRate, Integer startBit, Integer endBit) {
        StringBuilder error = new StringBuilder();

        // 校验品牌型号
        if (brand == null || brand.trim().isEmpty()) {
            error.append("品牌不能为空！\n");
        }
        //校验型号
        if (Model == null || Model.trim().isEmpty()) {
            error.append("型号不能为空！\n");
        }
        // 校验软件版本号
        if (softwareVersion == null || softwareVersion.trim().isEmpty()) {
            error.append("软件版本号不能为空！\n");
        }

        // 校验通讯ID
        if (communicationId == null || communicationId.trim().isEmpty()) {
            error.append("通讯ID不能为空！\n");
        }

        // 校验串口号
        if (port == null || port.trim().isEmpty()) {
            error.append("串口号不能为空！\n");
        }

        // 校验波特率
        if (baudRate == null) {
            error.append("波特率不能为空！\n");
        }

        // 校验开始位
        if (startBit == null) {
            error.append("开始位必须是整数！\n");
        }

        // 校验结束位
        if (endBit == null) {
            error.append("结束位必须是整数！\n");
        }
        // 如果没有任何错误信息，返回 success
        if (error.length() == 0) {
            return "success";
        }
        // 返回错误信息字符串
        return error.toString();
    }
    public static SerialDeviceEntity buildSerialDeviceEntity(String brandModel, String softwareVersion, String communicationId,
                                                       String port, Long baudRate, Integer startBit, Integer endBit, String info) {
        SerialDeviceEntity serialDevice = new SerialDeviceEntity();
        serialDevice.setBrandModel(brandModel);
        serialDevice.setSoftwareVersion(softwareVersion);
        serialDevice.setCommunicationId(communicationId);
        serialDevice.setPort(port);
        serialDevice.setBaudRate(baudRate);
        serialDevice.setStartBit(startBit);
        serialDevice.setEndBit(endBit);
        serialDevice.setInfo(info); // info 可以为空
        serialDevice.setStatus("false"); // 假设默认状态是 false
        return serialDevice;
    }
    public  static List<String> getAllSerial(){
        List<String> serialList = new ArrayList<>();
        // 列出所有可用的串口
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            serialList.add("未检测到可用的串口设备");
            return serialList;
        }
        for (SerialPort serialPort : ports) {
            serialList.add(serialPort.getSystemPortName());
        }

        return serialList;
    }
    public static ConfigDeviceEntity getDeviceEntityByName(List<ConfigDeviceEntity> configDeviceEntities, String deviceName) {
        for (ConfigDeviceEntity entity : configDeviceEntities) {
            if (entity.getName().equals(deviceName)) {
                return entity;
            }
        }
        return null;
    }
}

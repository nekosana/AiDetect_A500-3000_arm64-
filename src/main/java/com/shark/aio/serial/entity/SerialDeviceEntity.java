package com.shark.aio.serial.entity;

import lombok.Data;

@Data
public class SerialDeviceEntity {
    private Long id;

    private String brandModel;


    private String softwareVersion;


    private String communicationId;


    private String port;


    private Long baudRate ;


    private Integer startBit ;


    private Integer endBit ;


    private String info;


    private String status ;
}

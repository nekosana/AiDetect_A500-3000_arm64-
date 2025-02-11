package com.shark.aio.data.monitorDeviceHj212.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SerialDeviceEntity implements Serializable {
    private int id;
    private String brandModel;
    private String softwareVersion;
    private String communicationId;
    private String port;
    private int baudRate;
    private int startBit;
    private int endBit;
    private String info;
    private String status;
    private static final long serialVersionUID = 4755849618638567853L;
}

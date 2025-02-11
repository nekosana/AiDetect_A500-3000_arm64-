package com.shark.aio.data.monitorDeviceHj212.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lbx
 * @date 2023/3/15 - 23:12
 **/
@Data
public class MonitorDeviceEntity implements Serializable {
    private int id;
    private String monitorName;
    private String monitorClass;
    private String deviceId;
    private String status = "false";
    private static final long serialVersionUID = 4755849618638567840L;

}

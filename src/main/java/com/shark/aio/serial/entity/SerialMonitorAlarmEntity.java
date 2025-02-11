package com.shark.aio.serial.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;


@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class SerialMonitorAlarmEntity {
    private Integer id;
    private Timestamp recordTime;
    private String brandModel;
    private String monitorType;
    private String monitorData;
    private String info;
}

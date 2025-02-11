package com.shark.aio.alarm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AlarmRecordEntity implements Serializable{
    /*
    create table AIO.alarm_records
(
    id            int auto_increment comment '主键',
    alarm_time    timestamp    not null comment '报警时间',
    monitor       varchar(255) not null comment '监测点',
    monitor_class varchar(255) not null comment '监测类型',
    monitor_value varchar(255) not null comment '监测值',
    monitor_data  varchar(255) not null comment '监测数据',
    message       varchar(255) not null comment '告警信息',
    constraint id
        primary key (id)
)
    comment '报警记录表';

     */
    private Integer id;
    private Timestamp alarmTime;
    private String monitor;
    private String monitorClass;
    private String monitorValue;
    private String monitorData;
    private String unit;
    private String message;
    private static final long serialVersionUID = 45L;

}

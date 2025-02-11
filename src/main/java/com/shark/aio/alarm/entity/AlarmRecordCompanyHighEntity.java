package com.shark.aio.alarm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

/**
 * @author lbx
 * @date 2023/4/19 - 16:49
 **/
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AlarmRecordCompanyHighEntity {

    private Integer id;
    private Timestamp alarmTime;
    private String company;
    private String monitorValue;
    private String message;
}

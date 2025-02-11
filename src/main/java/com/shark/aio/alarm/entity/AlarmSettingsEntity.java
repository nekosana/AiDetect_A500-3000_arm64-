package com.shark.aio.alarm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AlarmSettingsEntity implements Serializable {

    private Integer id;
    private String monitorClass;
    private String monitorValue;
    private Double lowerLimit;
    private Double upperLimit;
    private String unit;
    private String message;
    private static final long serialVersionUID = 44L;
}

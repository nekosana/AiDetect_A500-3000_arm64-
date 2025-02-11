package com.shark.aio.data.pollutionData.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author lbx
 * @date 2023/2/13 - 17:30
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PollutionMonitorEntity {

    private Integer id;
    private String monitorName; //监测点
    private String deviceId; //设备ID
//    private String pollutionName; //污染物
}

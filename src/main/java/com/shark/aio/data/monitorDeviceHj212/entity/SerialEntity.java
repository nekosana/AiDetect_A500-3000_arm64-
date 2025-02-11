package com.shark.aio.data.monitorDeviceHj212.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SerialEntity {
    private String port ;
    private int baud_rate;
    private int start_bit;
    private int end_bit ;
}

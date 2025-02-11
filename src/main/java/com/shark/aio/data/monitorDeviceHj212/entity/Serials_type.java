package com.shark.aio.data.monitorDeviceHj212.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Serials_type {
    private int id;
    private String device_name;
    private String type;
}

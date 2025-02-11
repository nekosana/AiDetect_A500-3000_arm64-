package com.shark.aio.serial.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaterQualityMonitorDeviceEntity {
    private Integer id;

    private String name;


    private String brand;


    private String model;


    private String softwareVersion;


    private String info;

    @Override
    public String toString() {
        return "WaterQualityMonitorDevice{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", softwareVersion='" + softwareVersion + '\'' +
                ", info='" + info + '\'' +
                '}';
    }
}

package com.shark.aio.serial.entity;

import lombok.Data;

@Data
public class ConfigDeviceEntity {
    private int id; // 主键，自增

    private String name; // 参数名

    private String registeraddr; // 寄存器地址

    private String registeraddrlen; // 寄存器地址长度

    private String verifyaddr; // 校验地址

    private String verifyaddrlen; // 校验地址长度

    private String info; // 信息
}

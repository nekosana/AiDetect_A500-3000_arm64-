package com.shark.aio.data.video.entity;/**
 * @description:
 * @author: Administrator
 * @time: 2024/12/29 0029 14:28
 */

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description:
 * @author: Administrator
 * @time: 2024/12/29 0029 14:28
 */
@Data
public class WaringVideosEntity {
    int id;
    String videoName;
    String videoPath;
    private String createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

    private String updateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
}

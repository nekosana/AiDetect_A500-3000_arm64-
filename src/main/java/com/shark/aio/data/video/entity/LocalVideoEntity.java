package com.shark.aio.data.video.entity;/**
 * @description:
 * @author: Administrator
 * @time: 2024/12/19 0019 11:03
 */

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description:
 * @author: Administrator
 * @time: 2024/12/19 0019 11:03
 */
@Slf4j
@Data
public class LocalVideoEntity {
    private int id;
    private String videoName;
    private String videoPath;
    private String videoPng;


    private String createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

    private String updateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
}

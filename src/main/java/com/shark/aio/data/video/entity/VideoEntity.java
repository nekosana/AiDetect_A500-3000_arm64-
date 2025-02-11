package com.shark.aio.data.video.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VideoEntity {

    private Integer id = 1;
    private String monitorName = "监测点";
    private String rtsp = "rtmp://localhost:1935/myapp/room";
    private String description = "测试用监测点";
    private String stream = "room";
    private boolean personAi;
    private boolean carAi;

}

package com.shark.aio.data.video.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FaceRecordsEntity {

    private int id;
    private String pictureUrl;
    private String result;
    private double score;
    private String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

}

package com.shark.aio.data.video.controller;/**
 * @description:
 * @author: Administrator
 * @time: 2024/12/16 0016 20:36
 */

import com.shark.aio.data.video.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: Administrator
 * @time: 2024/12/16 0016 20:36
 */
@Component
@ConditionalOnProperty(value = "videorecord.enable",havingValue = "true")
public class VideoInit implements ApplicationListener<ApplicationStartedEvent> {
    @Autowired
    VideoService videoService;
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        videoService.VideoRecord();
    }
}

package com.shark.aio.base.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FFmpegConfiguration {

    @Value("${ffmpeg.enable}")
    private Boolean ffmpegEnabled;

    public boolean getFfmpegEnabled(){
        //System.out.println("ffmpegEnabled: "+ffmpegEnabled);
        return ffmpegEnabled!=null&&ffmpegEnabled;
    }

    @Bean
    @ConditionalOnProperty(value = "ffmpeg.enable", havingValue = "true")
    public InitFFmpeg initFFmpeg(){
        return new InitFFmpeg();
    }
}

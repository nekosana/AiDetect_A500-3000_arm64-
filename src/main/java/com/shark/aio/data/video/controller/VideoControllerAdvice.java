package com.shark.aio.data.video.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
@Slf4j
@ControllerAdvice
public class VideoControllerAdvice {

    /**
     * FaceController类的全局异常处理，捕获进程操作产生的NoSuchFieldException和IllegalAccessException
     * 返回错误信息，并跳转到视频播放页面
     * @return 视频播放页
     */
    @ExceptionHandler({NoSuchFieldException.class, IllegalAccessException.class})
    public ModelAndView addSessionExceptionHandler(){

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("msg","推流进程操作出现异常！");
        log.error("推流进程操作出现异常！");
        modelAndView.setViewName("videoPlay");
        return modelAndView;
    }
}

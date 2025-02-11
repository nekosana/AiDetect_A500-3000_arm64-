package com.shark.aio.alarm.GradedAlarm.neww;

import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

/**
 * @author lbx
 * @date 2023/5/11 - 14:54
 **/
@Component

public class RedisListenerErrorHandle implements ErrorHandler {


    @Override
    public void handleError(Throwable throwable) {
//        System.out.println( throwable.getMessage());
//        System.out.println(throwable);
        System.out.println("正常监听");

    }
//自己定义的RedisMessageListenerContainer需要手动设置一个ErrorHandle

//    public void setErrorHandler(ErrorHandler errorHandler) {
//
//        this.errorHandler = errorHandler;
//
//    }

}

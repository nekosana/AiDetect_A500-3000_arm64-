package com.shark.aio.base;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AIOLog {
    private static final Logger User_OperationLogger = LoggerFactory.getLogger("User_Operation");
    private static final Logger MonitorLogger = LoggerFactory.getLogger("Monitor");
    private static final Logger AlarmLogger = LoggerFactory.getLogger("Alarm");
    private static final Logger StorageLogger = LoggerFactory.getLogger("Storage");
    //用户
    @Before("execution(* com.shark.aio.base.controller.*.*(..))||execution(* com.shark.aio.operation.*.*(..))||execution(* com.shark.aio.user.*.*(..))||execution(* com.shark.aio.base.information.*.*(..))")
    public void User_Operationlog(JoinPoint joinPoint) {
        User_OperationLogger.info("After User_Operation method: " + joinPoint.getSignature().getName());
    }

    //监测点
    @Before("execution(* com.shark.aio.data.video.service.VideoService.*.*(..))")
    public void Monitorlog(JoinPoint joinPoint) {
        MonitorLogger.info("After Monitor method: " + joinPoint.getSignature().getName());
    }
    //预警
    @Before("execution(* com.shark.aio.alarm.*.*(..))")
    public void Alarmlog(JoinPoint joinPoint) {
        AlarmLogger.info("After Alarm method: " + joinPoint.getSignature().getName());
    }
    //存储
    @Before("execution(* com.shark.aio.data.*.*(..))")
    public void Storagelog(JoinPoint joinPoint) {
        StorageLogger.info("After MStorage method: " + joinPoint.getSignature().getName());
    }
}

package com.shark.aio.alarm.contactPart.util;

import com.github.pagehelper.PageHelper;
import com.shark.aio.base.annotation.Description;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@org.aspectj.lang.annotation.Aspect
@Component
@Slf4j
public class Aspect {
    /**
     * 重用切入点方法
     * 所有service层中，以ByPage结尾的方法在执行之前将开启分页插件
     * 方法返回值必须为PageInfo类型
     * 方法参数第一个和第二个必须为Integer类型（int都不行）
     * 第一个参数为页码pageNum
     * 第二个参数为每页记录数pageSize
     */
    @Pointcut(value = "execution(public com.github.pagehelper.PageInfo com.shark.aio.*..service.*.*ByPage(Integer,Integer,..))")
    public void pointcut(){}


    @Before("pointcut()")
    public void startPage(JoinPoint joinPoint){
        Object[] allArgs = joinPoint.getArgs();
        if(allArgs[0]==null)allArgs[0]=1;
        if(allArgs[1]==null)allArgs[1]=10;
        int pageNum = (int)allArgs[0];
        int pageSize = (int)allArgs[1];
        PageHelper.startPage(pageNum, pageSize);
    }



    @Around("@annotation(com.shark.aio.base.annotation.Description)")
    public Object controllerAOP(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        String methodName = methodSignature.getName();//获取方法名
        Description annotation = methodSignature.getMethod().getAnnotation(Description.class);//获取注解
        String desc = ObjectUtil.isEmptyString(annotation.value())?methodName: annotation.value();//设置日志内容
        log.info(desc+"方法开始执行");
        long start = System.currentTimeMillis();
        try {
            Object res;
            res = joinPoint.proceed();
            log.info(desc+"成功！耗时："+(System.currentTimeMillis()-start)+"ms");
            return res;
        }catch (Throwable e){
            log.error(desc+"失败！",e);
            return "500";

        }
    }
}

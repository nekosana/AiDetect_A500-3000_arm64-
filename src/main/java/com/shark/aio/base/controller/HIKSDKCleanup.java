package com.shark.aio.base.controller;


import com.shark.aio.alarm.contactPart.util.ClientDemo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

//@Component
@Slf4j
public class HIKSDKCleanup implements ApplicationListener<ContextClosedEvent> {
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("释放海康SDK...");
        ClientDemo.cleanup();
    }
}

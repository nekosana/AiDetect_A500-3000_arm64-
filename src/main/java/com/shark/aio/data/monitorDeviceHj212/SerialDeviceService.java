package com.shark.aio.data.monitorDeviceHj212;

import com.shark.aio.alarm.GradedAlarm.JedisConnectionFactory;
import com.shark.aio.data.monitorDeviceHj212.entity.SerialDeviceEntity;
import com.shark.aio.data.monitorDeviceHj212.mapper.SerialsMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SerialDeviceService  {
    @Autowired
    SerialsMapping serialsMapping;
    @Autowired
    JedisConnectionFactory jedisConnectionFactory;


    public List<SerialDeviceEntity> getSerialDevice(){
        try{
            List<SerialDeviceEntity> serialDeviceEntities = serialsMapping.getAllSerialDeviceEntity();

            return serialDeviceEntities;
        }catch (Exception e){
            log.error("ConditionService/getMonitorDevice:获取监测点名字失败！",e);
            return null;
        }
    }

}

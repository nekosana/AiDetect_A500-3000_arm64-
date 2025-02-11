package com.shark.aio.data.monitorDeviceHj212.mapper;


import com.shark.aio.data.monitorDeviceHj212.entity.SerialDeviceEntity;
import com.shark.aio.data.monitorDeviceHj212.entity.Serials_type;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SerialsMapping {
    @Select("SELECT type FROM `serials_type` where `device_name`=#{device_name}")
    public Serials_type selectTypeByDeviceName(String device_name);

    @Select("SELECT chinese_name FROM `field_translation` WHERE `type`=#{type} AND `field_name`=#{field_name}")
    String selectChineseNameByFieldName(@Param("type") String type, @Param("field_name") String fieldName);

    @Select("SELECT brand_model FROM  `serial_device`")
    List<String> getAllDeviceId();

    @Select("SELECT * FROM `serial_device`")
    List<SerialDeviceEntity> getAllSerialDeviceEntity();

    @Select("SELECT * FROM `serial_device` WHERE brand_model = #{brand_model};")
    SerialDeviceEntity getSerialDeviceEntityByBrandModel(@Param("brand_model")String brand_model);

    @Update("UPDATE `serial_device` SET `status`=#{status} WHERE `brand_model`=#{brand_model};")
    void updateSerialDevice(@Param("brand_model") String brand_model, @Param("status") String status);

    @Select("SELECT `chinese_name` FROM `field_translation` WHERE `type`=#{type} AND `parameter_type` = #{parameter_type}")
    List<String> getAllChineseNameWithInfoAndParameterType(@Param("type")String type,@Param("parameter_type")String parameter_type);

    @Select("SELECT DISTINCT `parameter_type` FROM `field_translation` WHERE `type`=#{type}")
    List<String> getAllParameterTypeWithInfo(@Param("type")String type);

    @Select("SELECT info FROM `water_quality_monitor_device` where `brand`=#{brand} AND `model` = #{model}")
    String getTypeWithBrandAndModel(@Param("brand")String brand,@Param("model")String model);

    @Select("COUNT * FROM `seiral_device` where `brand_model`=#{brand_model}")
    int findIfExists(@Param("brand_model")String brand_model);


}

package com.shark.aio.base.information;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author lbx
 * @date 2023/4/12 - 17:34
 **/
@Mapper
public interface InformationMapping {

    @Update("UPDATE `information` SET `company`=#{company}, `industry`=#{industry}, `description`=#{description}," +
            " `location`=#{location}, `telephone`=#{telephone}  WHERE `id`= 1;")
    void updateInformation(InformationEntity informationEntity);


    @Select("select * from `information` WHERE `id`= 1;")
    InformationEntity getInformation();
}

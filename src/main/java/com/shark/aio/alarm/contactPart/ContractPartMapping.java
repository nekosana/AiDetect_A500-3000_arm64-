package com.shark.aio.alarm.contactPart;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author lbx
 * @date 2023/4/17 - 18:15
 **/
@Mapper
public interface ContractPartMapping {

    @Select("select * from `part_host` where `id` = 1;")
    PartHostEntity getPartHost();

    @Update("UPDATE `part_host` SET `ip` = #{ip}, `port` = #{port} WHERE `id`=1")
    public void updatePartHost(PartHostEntity partHostEntity);
}

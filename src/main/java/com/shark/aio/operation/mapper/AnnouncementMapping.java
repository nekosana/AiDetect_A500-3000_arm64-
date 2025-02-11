package com.shark.aio.operation.mapper;

import com.shark.aio.operation.entity.AnnouncementEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementMapping {

    /**
     * 查询全部公告（不包括公告内容）
     * @return
     */
    @Select("SELECT `id`,`title`,`publish_time`,`publisher` FROM `announcement`")
    public List<AnnouncementEntity> selectAllAnnouncement();

    /**
     * 根据特征feature模糊查询公告信息
     * @param feature
     * @return
     */
    @Select("SELECT `id`,`title`,`publish_time`,`publisher` " +
            "FROM `announcement` " +
            "where " +
            "title like #{feature} " +
            "or publish_time like #{feature} " +
            "or publisher like #{feature} " +
            "or context like #{feature}")
    public List<AnnouncementEntity> selectAnnouncementByFeature(String feature);

    /**
     * 根据id查询公告
     * @param id
     * @return
     */
    @Select("SELECT * FROM `announcement` where `id`=#{id}")
    public AnnouncementEntity selectAnnouncementById(int id);
}

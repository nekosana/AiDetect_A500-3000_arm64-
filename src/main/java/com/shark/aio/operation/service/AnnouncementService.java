package com.shark.aio.operation.service;

import com.github.pagehelper.PageInfo;
import com.shark.aio.operation.entity.AnnouncementEntity;
import com.shark.aio.operation.mapper.AnnouncementMapping;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@MapperScan(value = "com.shark.aio.operation.mapper")
public class AnnouncementService {

    @Autowired
    private AnnouncementMapping announcementMapping;


    /**
     * 分页查询摄像头信息
     * @param pageSize 分页每页大小
     * @param pageNum   分页页码
     * @param feature   模糊查询特征字符串
     * @return 分页查询结果
     */
    public PageInfo<AnnouncementEntity> selectAnnouncementByPage(Integer pageSize, Integer pageNum, String feature){
        List<AnnouncementEntity> announcement;
        if(feature!=null&&!feature.equals("")){
            feature = "%"+feature+"%";
            announcement = announcementMapping.selectAnnouncementByFeature(feature);
        }else{
            announcement = announcementMapping.selectAllAnnouncement();
        }
        return new PageInfo<>(announcement,5);
    }

    public AnnouncementEntity selectAnnouncementById(int id){
        return announcementMapping.selectAnnouncementById(id);
    }
}

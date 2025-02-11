package com.shark.aio.data.video.mapper;

import com.shark.aio.data.video.entity.*;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VideoMapping {
    /**
     * 插入一个摄像头
     * @param video
     */
    @Insert("INSERT INTO `video` VALUES (null, #{monitorName}, #{rtsp}, #{description},#{stream},#{personAi},#{carAi})")
    int insertIntoVideo(VideoEntity video);
    @Delete("delete from `video` where rtsp = #{rtsp}")
    void deleteVideo(String rtsp);
    /**
     * 查询全部摄像头
     * @return
     */
    @Select("SELECT * FROM `video`")
    List<VideoEntity> selectAllVideos();

    /**
     * 根据特征feature模糊查询摄像头信息
     * @param feature
     * @return
     */
    @Select("SELECT * FROM `video` where monitor_name like #{feature} or description like #{feature}")
    List<VideoEntity> selectVideosByFeature(String feature);

    /**
     * 根据stream查询rtsp
     * @param stream
     * @return
     */
    @Select("SELECT `rtsp` FROM `video` WHERE `stream`=#{stream}")
    String selectRtspByStream(String stream);

    @Select("SELECT * FROM video WHERE rtsp=#{rtsp}")
    List<VideoEntity> selectVideosByRtsp(String rtsp);

    @Insert("INSERT INTO `face_records` (`picture_url`, `result`, `score`, `time`) VALUES(#{pictureUrl},#{result},#{score},#{time})")
    void insertFaceRecord(FaceRecordsEntity faceRecords);

    @Select("SELECT * FROM face_records")
    List<FaceRecordsEntity> selectAllFaceRecords();


    @Insert("INSERT INTO car_records (picture_url,result,score) VALUES(#{pictureUrl},#{result},#{score})")
    void insertCarRecord(CarRecordsEntity carRecords);
    @Select("SELECT * FROM car_records")
    List<CarRecordsEntity> selectAllCarRecords();


    @Select("SELECT * FROM face_records WHERE time>DATE_SUB(NOW(), INTERVAL 1 DAY ) AND time<=NOW();")
    List<FaceRecordsEntity> selectTodayFaceRecordsTime();

    @Select("SELECT * FROM car_records WHERE time>DATE_SUB(NOW(), INTERVAL 1 DAY ) AND time<=NOW();")
    List<CarRecordsEntity> selectTodayCarRecordsTime();

    @Select("SELECT * FROM local_videos")
    List<LocalVideoEntity> selectLocalVideo();
    @Insert("INSERT INTO video_info (video_name,video_path,video_png,create_time,update_time) values (#{videoName},#{videoPath},#{videoPng},#{createTime},#{updateTime})")
    void insertLocalVideoInfo(LocalVideoEntity localVideoEntity);
    @Delete("Delete from video_info where id = #{id}")
    void deleteLocalVideoInfo(int id);
    @Select("SELECT * from video_info")
    List<LocalVideoEntity> getAllLocalVideo();


    @Insert("Insert into waring_videos (video_name,video_path,create_time,update_time) values (#{videoName},#{videoPath},#{createTime},#{updateTime})")
    int insertWaringVideoInfo(WaringVideosEntity waringVideosEntity);
    @Select("Select * from waring_videos")
    List<WaringVideosEntity> getWaringVideoInfo();

}

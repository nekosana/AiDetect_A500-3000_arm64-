package com.shark.aio.videodetect.mapper;

import com.shark.aio.videodetect.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    // You can add custom query methods here if needed
}

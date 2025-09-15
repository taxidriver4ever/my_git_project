package org.example.tiktokproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.tiktokproject.pojo.MySQLVideo;
import org.example.tiktokproject.pojo.NormalUser;
import org.example.tiktokproject.pojo.Video;

import java.util.List;

@Mapper
public interface VideoMapper {
    public void insertVideo(MySQLVideo video);
}

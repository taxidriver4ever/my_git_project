package org.example.tiktokproject.controller;

import jakarta.annotation.Resource;
import org.example.tiktokproject.pojo.NormalResult;
import org.example.tiktokproject.pojo.NormalUser;
import org.example.tiktokproject.pojo.PhotoAndVideo;
import org.example.tiktokproject.pojo.Video;
import org.example.tiktokproject.service.SendPhotoService;
import org.example.tiktokproject.service.VideoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CrossOrigin
@RestController
@RequestMapping("/video")
public class VideoGetController {

    @Resource
    private SendPhotoService sendPhotoService;

    @Resource
    private VideoService videoService;

    @PostMapping("/SendUrl")
    public NormalResult SendUrl(@RequestBody PhotoAndVideo urls) {
        sendPhotoService.SendToPython(urls);
        return NormalResult.success();
    }

    @PostMapping("/GetVideo")
    public NormalResult GetVideo(@RequestBody NormalUser normalUser) {
        List<Video> videos = videoService.getVideos();
        return NormalResult.success(videos);
    }
}

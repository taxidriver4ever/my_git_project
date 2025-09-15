package org.example.tiktokproject.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

// 单帧检测结果类
@Setter
@Getter
@ToString
public class FrameResult {
    // getter 和 setter 方法
    private boolean success;
    private List<Prediction> predictions;

    @JsonProperty("image_size")
    private int[] imageSize;

    private String url;

    @JsonProperty("thread_id")
    private int threadId;

    private Timing timing;

    // 构造函数、getter 和 setter 方法
    public FrameResult() {}

}
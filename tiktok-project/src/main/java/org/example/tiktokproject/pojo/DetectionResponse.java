package org.example.tiktokproject.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

// 主响应类
@Setter
@Getter
@ToString
public class DetectionResponse {
    // getter 和 setter 方法
    private boolean success;
    private List<FrameResult> results;

    @JsonProperty("total_count")
    private int totalCount;

    @JsonProperty("success_count")
    private int successCount;

    @JsonProperty("processing_mode")
    private String processingMode;

    // 构造函数、getter 和 setter 方法
    public DetectionResponse() {}

}
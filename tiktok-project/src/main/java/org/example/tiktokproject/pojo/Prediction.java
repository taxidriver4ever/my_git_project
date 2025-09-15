package org.example.tiktokproject.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// 预测结果类
@Setter
@Getter
@ToString
public class Prediction {
    // getter 和 setter 方法
    private String object;
    private String object_zh;
    private String confidence;
    private double score;

    // 构造函数、getter 和 setter 方法
    public Prediction() {}

}
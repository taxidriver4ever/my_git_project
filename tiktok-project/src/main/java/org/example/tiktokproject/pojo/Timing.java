package org.example.tiktokproject.pojo;

import lombok.Getter;
import lombok.Setter;

// 时间统计类
@Setter
@Getter
public class Timing {
    // getter 和 setter 方法
    private double total;
    private double download;
    private double init;
    private double process;
    private double inference;

    // 构造函数、getter 和 setter 方法
    public Timing() {}

}

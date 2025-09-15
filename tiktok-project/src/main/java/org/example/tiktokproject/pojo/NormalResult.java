package org.example.tiktokproject.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NormalResult implements Serializable {
    private int code;
    private String message;
    private Object data;

    public static NormalResult success(Object data) {
        return new NormalResult(200, "success", data);
    }
    public static NormalResult success() {
        return new NormalResult(200, "success", null);
    }
    public static NormalResult error(String message) {
        return new NormalResult(500, message, null);
    }
}

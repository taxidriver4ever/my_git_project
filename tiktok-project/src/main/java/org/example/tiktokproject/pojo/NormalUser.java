package org.example.tiktokproject.pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NormalUser implements Serializable {
    private int id;
    private String userName;
    private String userPassword;
    private String userEmail;
    private String avatar;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}

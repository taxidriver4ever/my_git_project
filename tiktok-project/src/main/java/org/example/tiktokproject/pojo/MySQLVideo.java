package org.example.tiktokproject.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MySQLVideo implements Serializable {
    private Integer id;

    private String title;

    private String description;

    private String url;

    private String author;

    private String tags;

    private LocalDateTime uploadDate;

    private Boolean published;

    private String coverUrl;
}

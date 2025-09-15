package org.example.tiktokproject.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhotoAndVideo implements Serializable {
    private String photoA;
    private String photoB;
    private String photoC;
    private String photoD;
    private String photoE;
    private String video;
    private String title;
    private String author;
}

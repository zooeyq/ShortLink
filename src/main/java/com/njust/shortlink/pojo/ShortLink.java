package com.njust.shortlink.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShortLink {

    private Long id; //主键

    private String shortCode;  //短链接唯一标识

    private String originalUrl; //原网址

    private Boolean enabled; //是否启用

    private LocalDateTime expiresAt; //过期时间

    private LocalDateTime createdAt; //创建时间

    private LocalDateTime updatedAt; //最后修改时间
}
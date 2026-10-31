package com.njust.shortlink.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建短链接的响应 DTO
 */
@Data
public class CreateLinkResponse {
    private String shortUrl;    // 短链接地址
    private String shortCode;   // 短码
    private String originalUrl; // 原始网址

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt; // 过期时间
}

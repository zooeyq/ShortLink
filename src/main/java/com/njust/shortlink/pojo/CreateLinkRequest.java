package com.njust.shortlink.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Data
public class CreateLinkRequest {

    @NotBlank(message = "原网址不能为空") // 这个注解表示originalUrl字段不能为空，如果为空则会抛出异常
    @URL(message = "网址格式不正确") // 这个注解表示originalUrl字段必须是网址格式，如果格式不正确则会抛出异常
    @Pattern(regexp = "(?i)^https?://.*", message = "只支持 http 或 https 网址")
    private String originalUrl;

    @Future(message = "过期时间必须晚于当前时间") // 这个注解表示expiresAt字段必须是未来的日期，如果不是则会抛出异常
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 这个注解表示expiresAt字段的日期时间格式为"yyyy-MM-dd HH:mm:ss"
    private LocalDateTime expiresAt;
}

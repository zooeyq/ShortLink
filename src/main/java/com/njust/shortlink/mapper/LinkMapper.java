package com.njust.shortlink.mapper;

import com.njust.shortlink.pojo.ShortLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LinkMapper {
    int insert(ShortLink shortLink);
    ShortLink selectByShortCode(@Param("shortCode") String shortCode);
}

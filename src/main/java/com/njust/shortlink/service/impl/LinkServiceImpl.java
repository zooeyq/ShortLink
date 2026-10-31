package com.njust.shortlink.service.impl;

import com.njust.shortlink.mapper.LinkMapper;
import com.njust.shortlink.pojo.CreateLinkRequest;
import com.njust.shortlink.pojo.CreateLinkResponse;
import com.njust.shortlink.pojo.ShortLink;
import com.njust.shortlink.service.LinkService;
import com.njust.shortlink.utils.ShortCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;

@Slf4j
@Service
public class LinkServiceImpl implements LinkService {

    @Value("${short-link.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired
    private LinkMapper linkMapper;

    @Override
    public CreateLinkResponse createLink(CreateLinkRequest request) {

        // 生成短码
        String shortCode = ShortCodeUtil.generate();

        // 构建持久化对象
        ShortLink shortLink = new ShortLink();
        shortLink.setShortCode(shortCode);
        shortLink.setOriginalUrl(request.getOriginalUrl());
        shortLink.setEnabled(true);
        shortLink.setExpiresAt(request.getExpiresAt());

        // 持久化到数据库
        insertWithRetry(shortLink);
        shortCode = shortLink.getShortCode();
        String shortUrl = baseUrl.replaceAll("/+$", "") + "/s/" + shortCode;
        log.info("Saved short link to database with ID: {}", shortLink.getId());

        // 构建响应 DTO
        CreateLinkResponse response = new CreateLinkResponse();
        response.setShortUrl(shortUrl);
        response.setShortCode(shortCode);
        response.setOriginalUrl(request.getOriginalUrl());
        response.setExpiresAt(request.getExpiresAt());

        log.info("Generated short URL: {}", shortUrl);
        return response;
    }

    @Override
    public ShortLink getShortLinkByShortCode(String shortCode) {
        ShortLink shortLink = linkMapper.selectByShortCode(shortCode);
        if (shortLink == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "短链接不存在");
        }
        if (!Boolean.TRUE.equals(shortLink.getEnabled())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "短链接已停用");
        }
        LocalDateTime expiresAt = shortLink.getExpiresAt();
        if (expiresAt != null && !expiresAt.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "短链接已过期");
        }
        return shortLink;
    }

    private void insertWithRetry(ShortLink shortLink) {
        for (int attempt = 0; attempt < 5; attempt++) {
            try {
                linkMapper.insert(shortLink);
                return;
            } catch (DuplicateKeyException exception) {
                if (attempt == 4) {
                    throw exception;
                }
                shortLink.setShortCode(ShortCodeUtil.generate());
            }
        }
    }
}

package com.njust.shortlink.controller;

import com.njust.shortlink.pojo.CreateLinkRequest;
import com.njust.shortlink.pojo.CreateLinkResponse;
import com.njust.shortlink.pojo.Result;
import com.njust.shortlink.service.LinkService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
@Slf4j
public class LinkController {

    @Autowired
    private LinkService linkService;

    @PostMapping("/links")
    public Result<CreateLinkResponse> createLink(@Valid @RequestBody CreateLinkRequest request) {
        CreateLinkResponse response = linkService.createLink(request);
        log.info("Generated short URL: {}", response.getShortUrl());
        return Result.success(response);
    }

}

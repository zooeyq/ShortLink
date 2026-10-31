package com.njust.shortlink.controller;

import com.njust.shortlink.pojo.ShortLink;
import com.njust.shortlink.service.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.net.URI;

@RestController
@Slf4j
public class RedirectController {

    @Autowired
    private LinkService linkService;

    @GetMapping("/s/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable("shortCode") String shortCode) {
        ShortLink shortLink = linkService.getShortLinkByShortCode(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(shortLink.getOriginalUrl()))
                .cacheControl(CacheControl.noStore())
                .build();
    }



}

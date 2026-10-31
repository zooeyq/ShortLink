package com.njust.shortlink.service;

import com.njust.shortlink.pojo.CreateLinkRequest;
import com.njust.shortlink.pojo.CreateLinkResponse;
import com.njust.shortlink.pojo.ShortLink;

public interface LinkService {
    CreateLinkResponse createLink(CreateLinkRequest request);

    ShortLink getShortLinkByShortCode(String shortCode);
}

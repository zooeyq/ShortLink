package com.njust.shortlink;

import com.njust.shortlink.controller.LinkController;
import com.njust.shortlink.exception.GlobalExceptionHandler;
import com.njust.shortlink.mapper.LinkMapper;
import com.njust.shortlink.pojo.CreateLinkRequest;
import com.njust.shortlink.pojo.CreateLinkResponse;
import com.njust.shortlink.service.LinkService;
import com.njust.shortlink.service.impl.LinkServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CreateLinkTests {
    private LinkMapper insertMapper(java.util.function.ToIntFunction<com.njust.shortlink.pojo.ShortLink> insert) {
        return new LinkMapper() {
            @Override
            public int insert(com.njust.shortlink.pojo.ShortLink link) {
                return insert.applyAsInt(link);
            }

            @Override
            public com.njust.shortlink.pojo.ShortLink selectByShortCode(String code) {
                throw new UnsupportedOperationException("Query is not expected in create tests");
            }
        };
    }

    private MockMvc mvc() {
        LinkController controller = new LinkController();
        LinkService service = new LinkServiceImpl() {
            @Override
            public CreateLinkResponse createLink(CreateLinkRequest request) {
            CreateLinkResponse response = new CreateLinkResponse();
            response.setShortUrl("http://localhost:8080/s/abc1234");
            return response;
            }
        };
        ReflectionTestUtils.setField(controller, "linkService", service);
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void acceptsJsonAndReturnsResult() throws Exception {
        mvc().perform(post("/api/links").contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\":\"https://example.com\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.shortUrl").value("http://localhost:8080/s/abc1234"));
    }

    @Test
    void rejectsInvalidInputWithUnifiedBadRequest() throws Exception {
        for (String body : new String[]{
                "{}", "{\"originalUrl\":\"   \"}",
                "{\"originalUrl\":\"ftp://example.com\"}",
                "{\"originalUrl\":\"https://example.com\",\"expiresAt\":\"2000-01-01 00:00:00\"}",
                "{\"originalUrl\":\"https://example.com\",\"expiresAt\":\"invalid\"}", "{"}) {
            mvc().perform(post("/api/links").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(0));
        }
    }

    @Test
    void returnsCodeThatWasActuallySavedAfterCollision() {
        LinkServiceImpl service = new LinkServiceImpl();
        AtomicInteger attempts = new AtomicInteger();
        AtomicReference<String> savedCode = new AtomicReference<>();
        LinkMapper mapper = insertMapper(link -> {
            if (attempts.incrementAndGet() == 1) {
                throw new DuplicateKeyException("collision");
            }
            savedCode.set(link.getShortCode());
            return 1;
        });
        ReflectionTestUtils.setField(service, "linkMapper", mapper);
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080/");
        CreateLinkRequest request = new CreateLinkRequest();
        request.setOriginalUrl("https://example.com");
        CreateLinkResponse response = service.createLink(request);
        assertEquals(2, attempts.get());
        assertEquals(savedCode.get(), response.getShortCode());
        assertEquals("http://localhost:8080/s/" + savedCode.get(), response.getShortUrl());
    }

    @Test
    void stopsAfterFiveCollisions() {
        LinkServiceImpl service = new LinkServiceImpl();
        AtomicInteger attempts = new AtomicInteger();
        LinkMapper mapper = insertMapper(link -> {
            attempts.incrementAndGet();
            throw new DuplicateKeyException("collision");
        });
        ReflectionTestUtils.setField(service, "linkMapper", mapper);
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");
        CreateLinkRequest request = new CreateLinkRequest();
        request.setOriginalUrl("https://example.com");
        assertThrows(DuplicateKeyException.class, () -> service.createLink(request));
        assertEquals(5, attempts.get());
    }
}
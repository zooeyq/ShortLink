package com.njust.shortlink;

import com.njust.shortlink.controller.RedirectController;
import com.njust.shortlink.exception.GlobalExceptionHandler;
import com.njust.shortlink.mapper.LinkMapper;
import com.njust.shortlink.pojo.ShortLink;
import com.njust.shortlink.service.impl.LinkServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RedirectTests {
    private MockMvc mvc(ShortLink link) {
        LinkMapper mapper = new LinkMapper() {
            @Override
            public int insert(ShortLink value) {
                throw new UnsupportedOperationException("Redirect must not insert records");
            }

            @Override
            public ShortLink selectByShortCode(String code) {
                assertEquals("aB3x9Qk", code);
                return link;
            }
        };
        LinkServiceImpl service = new LinkServiceImpl();
        ReflectionTestUtils.setField(service, "linkMapper", mapper);
        RedirectController controller = new RedirectController();
        ReflectionTestUtils.setField(controller, "linkService", service);
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private ShortLink link(Boolean enabled, LocalDateTime expiresAt) {
        ShortLink link = new ShortLink();
        link.setOriginalUrl("https://example.com/article?q=java&page=2#section");
        link.setEnabled(enabled);
        link.setExpiresAt(expiresAt);
        return link;
    }

    @Test
    void redirectsPermanentAndFutureLinksWithoutJsonBody() throws Exception {
        for (ShortLink link : new ShortLink[]{link(true, null), link(true, LocalDateTime.now().plusDays(1))}) {
            mvc(link).perform(get("/s/aB3x9Qk"))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", link.getOriginalUrl()))
                    .andExpect(header().string("Cache-Control", "no-store"))
                    .andExpect(content().string(""));
        }
    }

    @Test
    void missingLinkReturns404() throws Exception {
        mvc(null).perform(get("/s/aB3x9Qk"))
                .andExpect(status().isNotFound()).andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("短链接不存在"));
    }

    @Test
    void disabledOrUnknownStateReturns403() throws Exception {
        for (Boolean enabled : new Boolean[]{false, null}) {
            mvc(link(enabled, null)).perform(get("/s/aB3x9Qk"))
                    .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Location"))
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").value("短链接已停用"));
        }
    }

    @Test
    void expiredOrJustExpiredLinkReturns410() throws Exception {
        for (LocalDateTime expires : new LocalDateTime[]{LocalDateTime.now().minusDays(1), LocalDateTime.now()}) {
            mvc(link(true, expires)).perform(get("/s/aB3x9Qk"))
                    .andExpect(status().isGone()).andExpect(header().doesNotExist("Location"))
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").value("短链接已过期"));
        }
    }
}

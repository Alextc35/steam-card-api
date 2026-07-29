package com.alextc.steamcardapi.controller;

import static com.alextc.steamcardapi.TestFixtures.STEAM_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alextc.steamcardapi.TestFixtures;
import com.alextc.steamcardapi.exception.InvalidCardParameterException;
import com.alextc.steamcardapi.exception.SteamApiException;
import com.alextc.steamcardapi.model.RenderedHttpResource;
import com.alextc.steamcardapi.model.SteamCardData;
import com.alextc.steamcardapi.model.SteamCardRequest;
import com.alextc.steamcardapi.service.SteamCardService;
import com.alextc.steamcardapi.service.SteamProfileService;
import com.alextc.steamcardapi.svg.SteamProfileCardRenderer;
import com.alextc.steamcardapi.validation.SteamRequestValidator;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SteamProfileController.class)
class SteamProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SteamRequestValidator requestValidator;

    @MockBean
    private SteamProfileService profileService;

    @MockBean
    private SteamProfileCardRenderer profileCardRenderer;

    @MockBean
    private SteamCardService cardService;

    private SteamCardRequest request;
    private SteamCardData data;

    @BeforeEach
    void setUp() {
        request = TestFixtures.request();
        data = TestFixtures.cardData(request.layout(), request.theme());
        when(requestValidator.cardRequest(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(request);
    }

    @Test
    void returnsProfileJson() throws Exception {
        when(profileService.getProfileData(request)).thenReturn(data);

        mockMvc.perform(get("/api/steam/profile").param("steamId", STEAM_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.profile.nickname").value("Alex <Dev> & Co"))
                .andExpect(jsonPath("$.selectedGame.appId").value(730));
    }

    @Test
    void returnsSvgWithCacheHeadersAndEtag() throws Exception {
        when(profileService.getCardData(request)).thenReturn(data);
        when(profileCardRenderer.render(data)).thenReturn("<svg role=\"img\"></svg>");
        when(cardService.svg("<svg role=\"img\"></svg>")).thenReturn(resource("<svg role=\"img\"></svg>", "\"abc\""));

        mockMvc.perform(get("/api/steam/card.svg").param("vanity", "alextc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/svg+xml;charset=UTF-8"))
                .andExpect(header().string("ETag", "\"abc\""))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Cache-Control", "max-age=300, public"));
    }

    @Test
    void returnsNotModifiedWhenEtagMatches() throws Exception {
        when(profileService.getCardData(request)).thenReturn(data);
        when(profileCardRenderer.render(data)).thenReturn("<svg role=\"img\"></svg>");
        when(cardService.svg("<svg role=\"img\"></svg>")).thenReturn(resource("<svg role=\"img\"></svg>", "\"abc\""));

        mockMvc.perform(get("/api/steam/card.svg").header("If-None-Match", "\"abc\""))
                .andExpect(status().isNotModified())
                .andExpect(header().string("ETag", "\"abc\""));
    }

    @Test
    void rejectsInvalidParametersWithJsonError() throws Exception {
        when(requestValidator.cardRequest(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new InvalidCardParameterException("theme must be valid"));

        mockMvc.perform(get("/api/steam/card.svg").param("theme", "<script>"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.error").value("Invalid request"))
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void returnsSvgFallbackWhenSteamFails() throws Exception {
        when(profileService.getCardData(request)).thenThrow(new SteamApiException("upstream unavailable"));
        when(profileCardRenderer.renderFallback(any(), any())).thenReturn("<svg>fallback</svg>");
        when(cardService.svg("<svg>fallback</svg>")).thenReturn(resource("<svg>fallback</svg>", "\"fallback\""));

        mockMvc.perform(get("/api/steam/card.svg"))
                .andExpect(status().isOk())
                .andExpect(content().string("<svg>fallback</svg>"))
                .andExpect(header().string("ETag", "\"fallback\""));
    }

    private RenderedHttpResource resource(String svg, String eTag) {
        return new RenderedHttpResource(svg.getBytes(StandardCharsets.UTF_8), "image/svg+xml;charset=UTF-8", eTag);
    }
}

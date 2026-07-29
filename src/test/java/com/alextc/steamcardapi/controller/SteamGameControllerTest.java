package com.alextc.steamcardapi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alextc.steamcardapi.TestFixtures;
import com.alextc.steamcardapi.client.SteamStoreApiClient;
import com.alextc.steamcardapi.exception.InvalidCardParameterException;
import com.alextc.steamcardapi.model.CardImageMode;
import com.alextc.steamcardapi.model.GameImageType;
import com.alextc.steamcardapi.model.RenderedHttpResource;
import com.alextc.steamcardapi.model.SteamGame;
import com.alextc.steamcardapi.service.SteamCardService;
import com.alextc.steamcardapi.service.SteamGameService;
import com.alextc.steamcardapi.service.SteamImageService;
import com.alextc.steamcardapi.svg.SteamGameCardRenderer;
import com.alextc.steamcardapi.svg.SvgLayout;
import com.alextc.steamcardapi.svg.SvgTheme;
import com.alextc.steamcardapi.validation.SteamRequestValidator;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SteamGameController.class)
class SteamGameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SteamRequestValidator requestValidator;

    @MockBean
    private SteamGameService gameService;

    @MockBean
    private SteamImageService imageService;

    @MockBean
    private SteamStoreApiClient storeApiClient;

    @MockBean
    private SteamGameCardRenderer gameCardRenderer;

    @MockBean
    private SteamCardService cardService;

    private SteamGame game;

    @BeforeEach
    void setUp() {
        game = TestFixtures.game(730, "Counter-Strike 2", 600, false);
        when(requestValidator.appId("730")).thenReturn(730);
        when(requestValidator.language(any())).thenReturn("en");
    }

    @Test
    void returnsGameJson() throws Exception {
        when(gameService.getGame(730, "en", SvgLayout.SHOWCASE, GameImageType.PORTRAIT)).thenReturn(game);

        mockMvc.perform(get("/api/steam/game/730"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.appId").value(730))
                .andExpect(jsonPath("$.images.portraitCoverUrl").value(game.images().portraitCoverUrl()));
    }

    @Test
    void returnsGameCardSvgWithHeaders() throws Exception {
        when(gameService.getGame(730, "en", SvgLayout.SHOWCASE, GameImageType.AUTO)).thenReturn(game);
        when(imageService.renderableGameImageUrl(eq(game.images()), eq(GameImageType.AUTO), eq(game.name()), eq(730),
                eq(SvgTheme.GITHUB_DARK), eq(CardImageMode.EMBEDDED))).thenReturn("data:image/png;base64,COVER");
        when(gameCardRenderer.render(eq(game), eq(SvgTheme.GITHUB_DARK), eq(SvgLayout.SHOWCASE),
                any(), eq("data:image/png;base64,COVER"))).thenReturn("<svg>game</svg>");
        when(cardService.svg("<svg>game</svg>")).thenReturn(svgResource("<svg>game</svg>", "\"game\""));

        mockMvc.perform(get("/api/steam/game/730/card.svg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/svg+xml;charset=UTF-8"))
                .andExpect(header().string("ETag", "\"game\""))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void returnsResolvedCoverWithCacheHeaders() throws Exception {
        RenderedHttpResource cover = new RenderedHttpResource(new byte[]{1, 2, 3}, "image/jpeg", "\"cover\"");
        when(requestValidator.coverType("portrait")).thenReturn(GameImageType.PORTRAIT);
        when(storeApiClient.getAppDetails(730, "en")).thenReturn(Optional.empty());
        when(imageService.coverResource(730, GameImageType.PORTRAIT, Optional.empty(), SvgTheme.GITHUB_DARK))
                .thenReturn(cover);

        mockMvc.perform(get("/api/steam/game/730/cover").param("type", "portrait"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(header().string("Cache-Control", "max-age=3600, public"))
                .andExpect(header().string("ETag", "\"cover\""));
    }

    @Test
    void rejectsInvalidAppIdsWithJsonError() throws Exception {
        when(requestValidator.appId("bad")).thenThrow(new InvalidCardParameterException("appId must be a positive integer"));

        mockMvc.perform(get("/api/steam/game/bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request"))
                .andExpect(jsonPath("$.requestId").exists());
    }

    private RenderedHttpResource svgResource(String svg, String eTag) {
        return new RenderedHttpResource(svg.getBytes(StandardCharsets.UTF_8), "image/svg+xml;charset=UTF-8", eTag);
    }
}

package com.alextc.steamcardapi.controller;

import com.alextc.steamcardapi.exception.SteamApiException;
import com.alextc.steamcardapi.exception.SteamProfileNotFoundException;
import com.alextc.steamcardapi.model.RenderedHttpResource;
import com.alextc.steamcardapi.model.SteamCardData;
import com.alextc.steamcardapi.model.SteamCardRequest;
import com.alextc.steamcardapi.model.SteamGame;
import com.alextc.steamcardapi.model.SteamLibrarySummary;
import com.alextc.steamcardapi.model.SteamStatistics;
import com.alextc.steamcardapi.service.SteamCardService;
import com.alextc.steamcardapi.service.SteamProfileService;
import com.alextc.steamcardapi.svg.SteamProfileCardRenderer;
import com.alextc.steamcardapi.svg.SvgLayout;
import com.alextc.steamcardapi.svg.SvgTheme;
import com.alextc.steamcardapi.validation.SteamRequestValidator;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SteamProfileController {

    private final SteamRequestValidator requestValidator;
    private final SteamProfileService steamProfileService;
    private final SteamProfileCardRenderer profileCardRenderer;
    private final SteamCardService steamCardService;

    public SteamProfileController(
            SteamRequestValidator requestValidator,
            SteamProfileService steamProfileService,
            SteamProfileCardRenderer profileCardRenderer,
            SteamCardService steamCardService
    ) {
        this.requestValidator = requestValidator;
        this.steamProfileService = steamProfileService;
        this.profileCardRenderer = profileCardRenderer;
        this.steamCardService = steamCardService;
    }

    @GetMapping(value = "/api/steam/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public SteamCardData profile(
            @RequestParam(required = false) String steamId,
            @RequestParam(required = false) String vanity,
            @RequestParam(required = false) String lang
    ) {
        SteamCardRequest request = requestValidator.cardRequest(
                steamId, vanity, null, "showcase", lang, null, null, "external", "auto", false, "rounded");
        return steamProfileService.getProfileData(request);
    }

    @GetMapping(value = "/api/steam/library", produces = MediaType.APPLICATION_JSON_VALUE)
    public SteamLibrarySummary library(@RequestParam(required = false) String steamId,
                                       @RequestParam(required = false) String vanity,
                                       @RequestParam(required = false) String lang) {
        return profile(steamId, vanity, lang).librarySummary();
    }

    @GetMapping(value = "/api/steam/recent", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SteamGame> recent(@RequestParam(required = false) String steamId,
                                  @RequestParam(required = false) String vanity,
                                  @RequestParam(required = false) String lang) {
        return profile(steamId, vanity, lang).recentGames();
    }

    @GetMapping(value = "/api/steam/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public SteamStatistics stats(@RequestParam(required = false) String steamId,
                                 @RequestParam(required = false) String vanity,
                                 @RequestParam(required = false) String lang) {
        return profile(steamId, vanity, lang).statistics();
    }

    @GetMapping(value = "/api/steam/card.svg", produces = "image/svg+xml;charset=UTF-8")
    public ResponseEntity<byte[]> card(
            @RequestParam(required = false) String steamId,
            @RequestParam(required = false) String vanity,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String layout,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String accent,
            @RequestParam(required = false) String show,
            @RequestParam(required = false) String imageMode,
            @RequestParam(required = false) String gameImage,
            @RequestParam(required = false) Boolean animation,
            @RequestParam(required = false) String border,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch
    ) {
        SteamCardRequest request = requestValidator.cardRequest(steamId, vanity, theme, layout, lang,
                accent, show, imageMode, gameImage, animation, border);
        try {
            RenderedHttpResource resource = steamCardService.svg(profileCardRenderer.render(
                    steamProfileService.getCardData(request)));
            return HttpResourceResponses.cacheable(resource, ifNoneMatch, 300);
        } catch (SteamApiException | SteamProfileNotFoundException exception) {
            RenderedHttpResource fallback = steamCardService.svg(profileCardRenderer.renderFallback(
                    SvgTheme.parse(theme), SvgLayout.parse(layout)));
            return HttpResourceResponses.cacheable(fallback, ifNoneMatch, 300);
        }
    }
}

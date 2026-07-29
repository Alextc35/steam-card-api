package com.alextc.steamcardapi.controller;

import com.alextc.steamcardapi.client.SteamStoreApiClient;
import com.alextc.steamcardapi.model.BorderStyle;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SteamGameController {

    private final SteamRequestValidator requestValidator;
    private final SteamGameService steamGameService;
    private final SteamImageService steamImageService;
    private final SteamStoreApiClient steamStoreApiClient;
    private final SteamGameCardRenderer gameCardRenderer;
    private final SteamCardService steamCardService;

    public SteamGameController(
            SteamRequestValidator requestValidator,
            SteamGameService steamGameService,
            SteamImageService steamImageService,
            SteamStoreApiClient steamStoreApiClient,
            SteamGameCardRenderer gameCardRenderer,
            SteamCardService steamCardService
    ) {
        this.requestValidator = requestValidator;
        this.steamGameService = steamGameService;
        this.steamImageService = steamImageService;
        this.steamStoreApiClient = steamStoreApiClient;
        this.gameCardRenderer = gameCardRenderer;
        this.steamCardService = steamCardService;
    }

    @GetMapping(value = "/api/steam/game/{appId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SteamGame game(@PathVariable String appId, @RequestParam(required = false) String lang) {
        return steamGameService.getGame(requestValidator.appId(appId), requestValidator.language(lang),
                SvgLayout.SHOWCASE, GameImageType.PORTRAIT);
    }

    @GetMapping(value = "/api/steam/game/{appId}/card.svg", produces = "image/svg+xml;charset=UTF-8")
    public ResponseEntity<byte[]> gameCard(
            @PathVariable String appId,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String layout,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String imageMode,
            @RequestParam(required = false) String gameImage,
            @RequestParam(required = false) String border,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch
    ) {
        int parsedAppId = requestValidator.appId(appId);
        SvgTheme parsedTheme = SvgTheme.parse(theme);
        SvgLayout parsedLayout = SvgLayout.parse(layout);
        GameImageType parsedImage = GameImageType.parse(gameImage);
        CardImageMode parsedMode = CardImageMode.parse(imageMode);
        BorderStyle parsedBorder = BorderStyle.parse(border);
        SteamGame game = steamGameService.getGame(parsedAppId, requestValidator.language(lang), parsedLayout, parsedImage);
        String renderedImage = steamImageService.renderableGameImageUrl(
                game.images(),
                parsedImage,
                game.name(),
                game.appId(),
                parsedTheme,
                parsedMode);
        RenderedHttpResource resource = steamCardService.svg(gameCardRenderer.render(
                game, parsedTheme, parsedLayout, parsedBorder, renderedImage));
        return HttpResourceResponses.cacheable(resource, ifNoneMatch, 300);
    }

    @GetMapping("/api/steam/game/{appId}/cover")
    public ResponseEntity<byte[]> cover(
            @PathVariable String appId,
            @RequestParam(defaultValue = "portrait") String type,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String lang,
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch
    ) {
        int parsedAppId = requestValidator.appId(appId);
        GameImageType parsedType = requestValidator.coverType(type);
        RenderedHttpResource resource = steamImageService.coverResource(
                parsedAppId,
                parsedType,
                steamStoreApiClient.getAppDetails(parsedAppId, requestValidator.language(lang)),
                SvgTheme.parse(theme));
        return HttpResourceResponses.cacheable(resource, ifNoneMatch, 3600);
    }
}

package com.alextc.steamcardapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.alextc.steamcardapi.client.SteamCdnClient;
import com.alextc.steamcardapi.exception.SteamImageUnavailableException;
import com.alextc.steamcardapi.model.CardImageMode;
import com.alextc.steamcardapi.model.DownloadedImage;
import com.alextc.steamcardapi.model.GameImageType;
import com.alextc.steamcardapi.model.RenderedHttpResource;
import com.alextc.steamcardapi.model.SteamGameImages;
import com.alextc.steamcardapi.svg.SvgLayout;
import com.alextc.steamcardapi.svg.SvgTheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

@ExtendWith(MockitoExtension.class)
class SteamImageServiceTest {

    @Mock
    private SteamCdnClient cdnClient;

    private SteamImageService service;

    @BeforeEach
    void setUp() {
        service = new SteamImageService(cdnClient,
                new ConcurrentMapCacheManager("steamImageResolution", "steamEmbeddedImage"),
                new SteamCardService());
    }

    @Test
    void buildsDeterministicUrlsAndSelectsPortraitHeaderAndHero() {
        SteamGameImages portrait = service.resolveImages(730, "iconhash", java.util.Optional.empty(),
                SvgLayout.SHOWCASE, GameImageType.PORTRAIT);
        SteamGameImages header = service.resolveImages(730, "iconhash", java.util.Optional.empty(),
                SvgLayout.NORMAL, GameImageType.HEADER);
        SteamGameImages hero = service.resolveImages(730, "iconhash", java.util.Optional.empty(),
                SvgLayout.HERO, GameImageType.HERO);

        assertThat(portrait.portraitCoverUrl()).endsWith("/730/library_600x900.jpg");
        assertThat(header.primaryImageUrl()).endsWith("/730/header.jpg");
        assertThat(hero.primaryImageUrl()).endsWith("/730/library_hero.jpg");
        assertThat(portrait.iconUrl()).contains("/730/iconhash.jpg");
    }

    @Test
    void embedsAllowedImagesAndFallsBackToPlaceholder() {
        String url = "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/header.jpg";
        when(cdnClient.download(url)).thenReturn(new DownloadedImage(new byte[]{1, 2, 3}, "image/jpeg"));

        String embedded = service.renderableImageUrl(url, "Counter-Strike 2", 730, SvgTheme.DARK, CardImageMode.EMBEDDED);
        String fallback = service.renderableImageUrl(null, "Counter-Strike 2", 730, SvgTheme.DARK, CardImageMode.EMBEDDED);

        assertThat(embedded).startsWith("data:image/jpeg;base64,");
        assertThat(fallback).startsWith("data:image/svg+xml;base64,");
    }

    @Test
    void triesGameImageFallbacksBeforeUsingPlaceholder() {
        SteamGameImages images = service.resolveImages(730, "iconhash", java.util.Optional.empty(),
                SvgLayout.SHOWCASE, GameImageType.PORTRAIT);
        when(cdnClient.download(images.portraitCoverUrl()))
                .thenThrow(new SteamImageUnavailableException("missing portrait"));
        when(cdnClient.download(images.headerUrl()))
                .thenReturn(new DownloadedImage(new byte[]{4, 5, 6}, "image/jpeg"));

        String rendered = service.renderableGameImageUrl(images, GameImageType.PORTRAIT,
                "Counter-Strike 2", 730, SvgTheme.DARK, CardImageMode.EMBEDDED);

        assertThat(rendered).startsWith("data:image/jpeg;base64,");
    }

    @Test
    void doesNotUpscaleTinyIconsAsLargePortraitCovers() {
        SteamGameImages images = service.resolveImages(730, "iconhash", java.util.Optional.empty(),
                SvgLayout.SHOWCASE, GameImageType.PORTRAIT);
        when(cdnClient.download(images.portraitCoverUrl()))
                .thenThrow(new SteamImageUnavailableException("missing portrait"));
        when(cdnClient.download(images.headerUrl()))
                .thenThrow(new SteamImageUnavailableException("missing header"));
        when(cdnClient.download(images.heroUrl()))
                .thenThrow(new SteamImageUnavailableException("missing hero"));

        String rendered = service.renderableGameImageUrl(images, GameImageType.PORTRAIT,
                "Counter-Strike 2", 730, SvgTheme.DARK, CardImageMode.EMBEDDED);

        assertThat(rendered).startsWith("data:image/svg+xml;base64,");
        verify(cdnClient, never()).download(images.smallCapsuleUrl());
        verify(cdnClient, never()).download(images.iconUrl());
    }

    @Test
    void usesLargeHeroFallbackForPortraitCards() {
        SteamGameImages images = service.resolveImages(730, "iconhash", java.util.Optional.empty(),
                SvgLayout.SHOWCASE, GameImageType.PORTRAIT);
        when(cdnClient.download(images.portraitCoverUrl()))
                .thenThrow(new SteamImageUnavailableException("missing portrait"));
        when(cdnClient.download(images.headerUrl()))
                .thenThrow(new SteamImageUnavailableException("missing header"));
        when(cdnClient.download(images.heroUrl()))
                .thenReturn(new DownloadedImage(new byte[]{7, 8, 9}, "image/jpeg"));

        String rendered = service.renderableGameImageUrl(images, GameImageType.PORTRAIT,
                "Counter-Strike 2", 730, SvgTheme.DARK, CardImageMode.EMBEDDED);

        assertThat(rendered).startsWith("data:image/jpeg;base64,");
    }

    @Test
    void rejectsDisallowedHostsThroughCdnClient() {
        SteamCdnClient realClient = new SteamCdnClient(new com.alextc.steamcardapi.config.SteamProperties(
                "test-key", "alextc", "", java.time.Duration.ofMinutes(5), java.time.Duration.ofSeconds(15),
                java.time.Duration.ofHours(1), java.time.Duration.ofSeconds(1), java.time.Duration.ofSeconds(1), 1024));

        assertThatThrownBy(() -> realClient.validateUrl("https://example.com/image.jpg"))
                .isInstanceOf(SteamImageUnavailableException.class);
    }

    @Test
    void coverEndpointFallsBackToPlaceholderWhenDownloadFails() {
        RenderedHttpResource resource = service.coverResource(730, GameImageType.PORTRAIT,
                java.util.Optional.empty(), SvgTheme.DARK);

        assertThat(resource.contentType()).startsWith("image/svg+xml");
        assertThat(new String(resource.body())).contains("Artwork unavailable");
    }
}

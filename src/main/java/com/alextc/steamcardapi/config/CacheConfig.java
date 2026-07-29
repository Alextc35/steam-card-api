package com.alextc.steamcardapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String STEAM_PROFILE_CACHE = "steamProfile";
    public static final String STEAM_RECENT_GAMES_CACHE = "steamRecentGames";
    public static final String STEAM_LIBRARY_CACHE = "steamLibrary";
    public static final String STEAM_LEVEL_CACHE = "steamLevel";
    public static final String STEAM_FRIENDS_CACHE = "steamFriends";
    public static final String STEAM_STORE_CACHE = "steamStore";
    public static final String STEAM_IMAGE_RESOLUTION_CACHE = "steamImageResolution";
    public static final String STEAM_EMBEDDED_IMAGE_CACHE = "steamEmbeddedImage";
    public static final String STEAM_SVG_CACHE = "steamSvg";

    @Bean
    CacheManager cacheManager(SteamProperties steamProperties) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                cache(STEAM_PROFILE_CACHE, steamProperties.liveCacheTtl(), 1_000),
                cache(STEAM_RECENT_GAMES_CACHE, steamProperties.liveCacheTtl(), 1_000),
                cache(STEAM_LIBRARY_CACHE, steamProperties.liveCacheTtl(), 1_000),
                cache(STEAM_LEVEL_CACHE, steamProperties.cacheTtl(), 1_000),
                cache(STEAM_FRIENDS_CACHE, steamProperties.cacheTtl(), 1_000),
                cache(STEAM_STORE_CACHE, Duration.ofHours(6), 10_000),
                cache(STEAM_IMAGE_RESOLUTION_CACHE, Duration.ofHours(6), 10_000),
                cache(STEAM_EMBEDDED_IMAGE_CACHE, steamProperties.imageCacheTtl(), 2_000),
                cache(STEAM_SVG_CACHE, steamProperties.liveCacheTtl(), 2_000)));
        return cacheManager;
    }

    private CaffeineCache cache(String name, Duration ttl, long maxSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .recordStats()
                .maximumSize(maxSize)
                .build());
    }
}

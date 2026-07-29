package com.alextc.steamcardapi.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "steam")
public record SteamProperties(
        @NotBlank String apiKey,
        String defaultVanity,
        String defaultId,
        Duration cacheTtl,
        Duration liveCacheTtl,
        Duration imageCacheTtl,
        Duration httpConnectTimeout,
        Duration httpReadTimeout,
        @Min(1024) long maxEmbeddedImageBytes
) {

    @AssertTrue(message = "cacheTtl must be a positive duration")
    public boolean isCacheTtlPositive() {
        return cacheTtl != null && !cacheTtl.isZero() && !cacheTtl.isNegative();
    }

    @AssertTrue(message = "liveCacheTtl must be a positive duration")
    public boolean isLiveCacheTtlPositive() {
        return liveCacheTtl != null && !liveCacheTtl.isZero() && !liveCacheTtl.isNegative();
    }

    @AssertTrue(message = "imageCacheTtl must be a positive duration")
    public boolean isImageCacheTtlPositive() {
        return imageCacheTtl != null && !imageCacheTtl.isZero() && !imageCacheTtl.isNegative();
    }

    @AssertTrue(message = "httpConnectTimeout must be a positive duration")
    public boolean isHttpConnectTimeoutPositive() {
        return httpConnectTimeout != null && !httpConnectTimeout.isZero() && !httpConnectTimeout.isNegative();
    }

    @AssertTrue(message = "httpReadTimeout must be a positive duration")
    public boolean isHttpReadTimeoutPositive() {
        return httpReadTimeout != null && !httpReadTimeout.isZero() && !httpReadTimeout.isNegative();
    }
}

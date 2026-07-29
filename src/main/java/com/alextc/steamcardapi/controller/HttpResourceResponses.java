package com.alextc.steamcardapi.controller;

import com.alextc.steamcardapi.model.RenderedHttpResource;
import java.util.concurrent.TimeUnit;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

final class HttpResourceResponses {

    private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

    private HttpResourceResponses() {
    }

    static ResponseEntity<byte[]> cacheable(RenderedHttpResource resource, String ifNoneMatch, int maxAgeSeconds) {
        if (resource.eTag().equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(resource.eTag())
                    .cacheControl(CacheControl.maxAge(maxAgeSeconds, TimeUnit.SECONDS).cachePublic())
                    .header(X_CONTENT_TYPE_OPTIONS, "nosniff")
                    .build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resource.contentType()))
                .eTag(resource.eTag())
                .cacheControl(CacheControl.maxAge(maxAgeSeconds, TimeUnit.SECONDS).cachePublic())
                .header(X_CONTENT_TYPE_OPTIONS, "nosniff")
                .body(resource.body());
    }
}

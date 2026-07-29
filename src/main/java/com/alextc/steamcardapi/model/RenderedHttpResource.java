package com.alextc.steamcardapi.model;

public record RenderedHttpResource(byte[] body, String contentType, String eTag) {
}

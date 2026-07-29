package com.alextc.steamcardapi.service;

import com.alextc.steamcardapi.model.RenderedHttpResource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Service;

@Service
public class SteamCardService {

    public RenderedHttpResource svg(String svg) {
        byte[] bytes = svg.getBytes(StandardCharsets.UTF_8);
        return resource(bytes, "image/svg+xml;charset=UTF-8");
    }

    public RenderedHttpResource resource(byte[] bytes, String contentType) {
        return new RenderedHttpResource(bytes, contentType, eTag(bytes));
    }

    private String eTag(byte[] bytes) {
        try {
            return "\"" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)) + "\"";
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}

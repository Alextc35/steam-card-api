package com.alextc.steamcardapi.client;

import com.alextc.steamcardapi.config.SteamProperties;
import com.alextc.steamcardapi.exception.SteamImageUnavailableException;
import com.alextc.steamcardapi.model.DownloadedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SteamCdnClient {

    public static final Set<String> ALLOWED_IMAGE_HOSTS = Set.of(
            "shared.fastly.steamstatic.com",
            "shared.akamai.steamstatic.com",
            "media.steampowered.com",
            "steamcdn-a.akamaihd.net",
            "cdn.cloudflare.steamstatic.com",
            "avatars.steamstatic.com"
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final HttpClient httpClient;
    private final SteamProperties steamProperties;

    @Autowired
    public SteamCdnClient(SteamProperties steamProperties) {
        this(steamProperties, HttpClient.newBuilder()
                .connectTimeout(steamProperties.httpConnectTimeout())
                .followRedirects(HttpClient.Redirect.NEVER)
                .build());
    }

    SteamCdnClient(SteamProperties steamProperties, HttpClient httpClient) {
        this.steamProperties = steamProperties;
        this.httpClient = httpClient;
    }

    public DownloadedImage download(String url) {
        URI uri = validateUrl(url);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(min(steamProperties.httpReadTimeout(), Duration.ofSeconds(5)))
                .header("User-Agent", "Steam Card API/1.0 (+https://alextc.es)")
                .header("Accept", "image/jpeg,image/png,image/webp")
                .build();
        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new SteamImageUnavailableException("Steam image returned HTTP " + response.statusCode());
            }
            String contentType = response.headers()
                    .firstValue("Content-Type")
                    .map(value -> value.split(";")[0].trim().toLowerCase(Locale.ROOT))
                    .orElse("");
            if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
                throw new SteamImageUnavailableException("Steam image returned an unsupported content type");
            }
            long declaredLength = response.headers()
                    .firstValueAsLong("Content-Length")
                    .orElse(-1);
            if (declaredLength > steamProperties.maxEmbeddedImageBytes()) {
                throw new SteamImageUnavailableException("Steam image is too large");
            }
            try (InputStream body = response.body()) {
                return new DownloadedImage(readBounded(body, steamProperties.maxEmbeddedImageBytes()), contentType);
            }
        } catch (IOException exception) {
            throw new SteamImageUnavailableException("Steam image could not be downloaded", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SteamImageUnavailableException("Steam image download was interrupted", exception);
        }
    }

    public URI validateUrl(String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException exception) {
            throw new SteamImageUnavailableException("Invalid Steam image URL");
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new SteamImageUnavailableException("Steam image URL must use HTTPS");
        }
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_HOSTS.contains(host)) {
            throw new SteamImageUnavailableException("Steam image host is not allowed");
        }
        return uri;
    }

    private byte[] readBounded(InputStream inputStream, long maxBytes) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new SteamImageUnavailableException("Steam image is too large");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private Duration min(Duration first, Duration second) {
        return first.compareTo(second) <= 0 ? first : second;
    }
}

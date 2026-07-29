package com.alextc.steamcardapi.dto.store;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamStoreAppDetailsResponse {

    private final Map<String, AppEnvelope> apps = new LinkedHashMap<>();

    public Map<String, AppEnvelope> apps() {
        return apps;
    }

    @JsonAnySetter
    void addApp(String appId, AppEnvelope envelope) {
        apps.put(appId, envelope);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppEnvelope(Boolean success, AppData data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppData(
            String type,
            String name,
            @JsonProperty("short_description") String shortDescription,
            List<String> developers,
            List<String> publishers,
            List<Description> genres,
            List<Description> categories,
            @JsonProperty("release_date") ReleaseDate releaseDate,
            @JsonProperty("price_overview") PriceOverview priceOverview,
            @JsonProperty("is_free") Boolean freeToPlay,
            @JsonProperty("header_image") String headerImage,
            @JsonProperty("capsule_image") String capsuleImage,
            @JsonProperty("background") String background,
            @JsonProperty("background_raw") String backgroundRaw,
            List<Screenshot> screenshots,
            Metacritic metacritic,
            Recommendations recommendations,
            Platforms platforms
    ) {
        public List<String> genreDescriptions() {
            return descriptions(genres);
        }

        public List<String> categoryDescriptions() {
            return descriptions(categories);
        }

        private List<String> descriptions(List<Description> source) {
            if (source == null) {
                return List.of();
            }
            List<String> values = new ArrayList<>();
            for (Description description : source) {
                if (description != null && description.description() != null && !description.description().isBlank()) {
                    values.add(description.description());
                }
            }
            return List.copyOf(values);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Description(Integer id, String description) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReleaseDate(@JsonProperty("coming_soon") Boolean comingSoon, String date) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PriceOverview(@JsonProperty("final_formatted") String finalFormatted) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Screenshot(@JsonProperty("path_full") String pathFull) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Metacritic(Integer score) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Recommendations(Integer total) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Platforms(Boolean windows, Boolean mac, Boolean linux) {
    }
}

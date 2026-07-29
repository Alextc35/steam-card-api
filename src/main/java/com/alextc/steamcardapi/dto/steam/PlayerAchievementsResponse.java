package com.alextc.steamcardapi.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerAchievementsResponse(PlayerStats playerstats) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerStats(
            Boolean success,
            @JsonProperty("gameName") String gameName,
            List<Achievement> achievements
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Achievement(
            @JsonProperty("apiname") String apiName,
            Integer achieved
    ) {
    }
}

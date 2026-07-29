package com.alextc.steamcardapi.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SteamLevelResponse(Response response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("player_level") Integer playerLevel) {
    }
}

package com.alextc.steamcardapi.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerSummariesResponse(Response response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(List<Player> players) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Player(
            @JsonProperty("steamid") String steamId,
            @JsonProperty("personaname") String personaName,
            @JsonProperty("profileurl") String profileUrl,
            @JsonProperty("avatarfull") String avatarFull,
            @JsonProperty("personastate") Integer personaState,
            @JsonProperty("gameextrainfo") String gameExtraInfo,
            @JsonProperty("gameid") String gameId,
            @JsonProperty("lastlogoff") Long lastLogoff,
            @JsonProperty("timecreated") Long timeCreated,
            @JsonProperty("loccountrycode") String countryCode
    ) {
    }
}

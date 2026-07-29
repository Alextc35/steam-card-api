package com.alextc.steamcardapi.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RecentlyPlayedGamesResponse(Response response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            @JsonProperty("total_count") Integer totalCount,
            List<Game> games
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Game(
            @JsonProperty("appid") Integer appId,
            String name,
            @JsonProperty("playtime_2weeks") Integer playtimeTwoWeeks,
            @JsonProperty("playtime_forever") Integer playtimeForever,
            @JsonProperty("img_icon_url") String imageIconUrl,
            @JsonProperty("rtime_last_played") Long lastPlayed
    ) {
    }
}

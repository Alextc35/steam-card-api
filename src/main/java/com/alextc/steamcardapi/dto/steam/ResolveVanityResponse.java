package com.alextc.steamcardapi.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResolveVanityResponse(Response response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            @JsonProperty("steamid") String steamId,
            Integer success,
            String message
    ) {
    }
}

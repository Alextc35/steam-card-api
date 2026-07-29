package com.alextc.steamcardapi.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FriendListResponse(@JsonProperty("friendslist") FriendsList friendsList) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FriendsList(List<Friend> friends) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Friend(@JsonProperty("steamid") String steamId) {
    }
}

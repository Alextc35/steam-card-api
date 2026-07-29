package com.alextc.steamcardapi.model;

public record SteamSubject(String steamId, String vanity) {

    public String cacheKey() {
        return steamId != null && !steamId.isBlank() ? "id:" + steamId : "vanity:" + vanity;
    }
}

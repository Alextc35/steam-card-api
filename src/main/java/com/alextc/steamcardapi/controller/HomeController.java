package com.alextc.steamcardapi.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final String version;

    public HomeController(ObjectProvider<BuildProperties> buildProperties) {
        BuildProperties properties = buildProperties.getIfAvailable();
        this.version = properties == null ? "dev" : properties.getVersion();
    }

    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("name", "Steam Card API");
        response.put("status", "ok");
        response.put("version", version);
        response.put("documentation", "/");
        response.put("profile", "/api/steam/profile");
        response.put("library", "/api/steam/library");
        response.put("recent", "/api/steam/recent");
        response.put("stats", "/api/steam/stats");
        response.put("card", "/api/steam/card.svg");
        response.put("game", "/api/steam/game/{appId}");
        response.put("gameCard", "/api/steam/game/{appId}/card.svg");
        response.put("cover", "/api/steam/game/{appId}/cover");
        response.put("health", "/actuator/health");
        return response;
    }
}

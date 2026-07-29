package com.alextc.steamcardapi.mapper;

import com.alextc.steamcardapi.dto.steam.PlayerSummariesResponse;
import com.alextc.steamcardapi.model.SteamProfile;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SteamProfileMapper {

    public SteamProfile toProfile(
            PlayerSummariesResponse.Player player,
            Integer steamLevel,
            Integer friendCount,
            Integer gameCount
    ) {
        return new SteamProfile(
                player.steamId(),
                player.personaName(),
                player.profileUrl(),
                player.avatarFull(),
                readablePersonaState(player.personaState()),
                player.gameExtraInfo() != null && !player.gameExtraInfo().isBlank(),
                toInstant(player.timeCreated()),
                toInstant(player.lastLogoff()),
                player.countryCode(),
                steamLevel,
                friendCount,
                gameCount);
    }

    private Instant toInstant(Long epochSeconds) {
        if (epochSeconds == null || epochSeconds <= 0) {
            return null;
        }
        return Instant.ofEpochSecond(epochSeconds);
    }

    private String readablePersonaState(Integer state) {
        if (state == null) {
            return "Unknown";
        }
        return switch (state) {
            case 0 -> "Offline";
            case 1 -> "Online";
            case 2 -> "Busy";
            case 3 -> "Away";
            case 4 -> "Snooze";
            case 5 -> "Looking to trade";
            case 6 -> "Looking to play";
            default -> "Unknown";
        };
    }
}

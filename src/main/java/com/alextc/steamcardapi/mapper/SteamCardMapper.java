package com.alextc.steamcardapi.mapper;

import com.alextc.steamcardapi.model.SteamCardData;
import com.alextc.steamcardapi.model.SteamCardRequest;
import com.alextc.steamcardapi.model.SteamGame;
import com.alextc.steamcardapi.model.SteamGameImages;
import com.alextc.steamcardapi.model.SteamLibrarySummary;
import com.alextc.steamcardapi.model.SteamProfile;
import com.alextc.steamcardapi.model.SteamStatistics;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SteamCardMapper {

    public SteamCardData toCardData(
            SteamProfile profile,
            SteamGame selectedGame,
            List<SteamGame> recentGames,
            SteamLibrarySummary librarySummary,
            SteamStatistics statistics,
            SteamCardRequest request,
            SteamGameImages resolvedImages,
            String renderedAvatarUrl,
            String renderedPrimaryImageUrl
    ) {
        return new SteamCardData(
                profile,
                selectedGame,
                List.copyOf(recentGames),
                librarySummary == null || librarySummary.mostPlayedGame() == null
                        ? List.of()
                        : List.of(librarySummary.mostPlayedGame()),
                statistics,
                librarySummary,
                request.theme(),
                request.layout(),
                request.locale(),
                request.accent(),
                request.show(),
                request.border(),
                resolvedImages,
                renderedAvatarUrl,
                renderedPrimaryImageUrl);
    }
}

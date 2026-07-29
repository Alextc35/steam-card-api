package com.alextc.steamcardapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "steam.api-key=test-key",
        "steam.default-id=76561198000000000"
})
class SteamCardApiApplicationTest {

    @Test
    void contextLoads() {
    }
}

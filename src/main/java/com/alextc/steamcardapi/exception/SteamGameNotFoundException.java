package com.alextc.steamcardapi.exception;

public class SteamGameNotFoundException extends RuntimeException {

    public SteamGameNotFoundException(String message) {
        super(message);
    }
}

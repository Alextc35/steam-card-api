package com.alextc.steamcardapi.exception;

public class SteamProfileNotFoundException extends RuntimeException {

    public SteamProfileNotFoundException(String message) {
        super(message);
    }
}

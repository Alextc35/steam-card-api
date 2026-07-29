package com.alextc.steamcardapi.exception;

public class SteamImageUnavailableException extends RuntimeException {

    public SteamImageUnavailableException(String message) {
        super(message);
    }

    public SteamImageUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

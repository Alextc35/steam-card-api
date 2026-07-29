package com.alextc.steamcardapi.validation;

import com.alextc.steamcardapi.exception.InvalidCardParameterException;
import java.util.regex.Pattern;

public final class HexColorValidator {

    private static final Pattern HEX = Pattern.compile("[0-9a-fA-F]{6}");

    private HexColorValidator() {
    }

    public static String validate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() > 6 || !HEX.matcher(value).matches()) {
            throw new InvalidCardParameterException("accent must be a 6-character hexadecimal color without #");
        }
        return value.toLowerCase();
    }
}

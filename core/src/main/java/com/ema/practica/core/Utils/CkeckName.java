package com.ema.practica.core.Utils;

import java.util.regex.Pattern;

public class CkeckName {
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,20}$");

    public static boolean verifyUser(String name) {
        return name != null && VALID_NAME_PATTERN.matcher(name).matches();
    }

    public static boolean verifyLoadedUser(String name) {
        return name != null && !name.trim().isEmpty();
    }
}
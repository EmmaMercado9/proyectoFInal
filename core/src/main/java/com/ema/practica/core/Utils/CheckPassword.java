package com.ema.practica.core.Utils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class CheckPassword {
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{8,}$");

    public static boolean verifyPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
    public static boolean verifyLoadedPass(String password) {
        return password != null && !password.trim().isEmpty();
    }
}
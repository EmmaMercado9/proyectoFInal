package com.ema.practica.core.Utils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class CheckEmail {
        private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

        public static boolean verifyEmail(String email) {
            return email != null && EMAIL_PATTERN.matcher(email).matches();
        }

    public static boolean verifyLoadedMail(String mail) {
        return mail != null && !mail.trim().isEmpty();
    }
}
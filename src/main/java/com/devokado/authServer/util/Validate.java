package com.devokado.authServer.util;

import lombok.val;

import java.util.regex.Pattern;

public class Validate {
    public static boolean isValidMobile(String mobile) {
        if (mobile.isEmpty())
            return false;
        val chars = mobile.toCharArray();
        for (Character c : chars) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return mobile.length() == 11 && mobile.startsWith("09");
    }

    public static boolean isValidMail(String email) {
        String EMAIL_STRING = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(EMAIL_STRING).matcher(email).matches();
    }
}

package com.devokado.authServer.util;

import lombok.val;

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
}

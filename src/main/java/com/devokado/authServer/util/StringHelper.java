package com.devokado.authServer.util;

import java.util.Random;

/**
 * @author Alimodares
 * @since 2020-12-14
 */
public class StringHelper {
    public static String generateCode(int length) {
        return String.valueOf(length < 1 ? 0 : new Random()
                .nextInt((9 * (int) Math.pow(10, length - 1)) - 1)
                + (int) Math.pow(10, length - 1));
    }
}

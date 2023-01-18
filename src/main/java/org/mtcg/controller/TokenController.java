package org.mtcg.controller;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenController {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public static String generateNewAuthToken(String username) {
        return "Basic "+username+"-mtcgToken";
    }
    /* static String generateNewAuthToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }*/

}

package com.android.server.utils;

import java.security.SecureRandom;

public class RandomUtil {
    public static final String C2_KEY = "bf0af535386b84bd4ed4e870c1b3d4d0569764bc291af58aeb54d8f60de24249";

    public static byte[] random(int len) {
        byte[] aesKey = new byte[len];
        new SecureRandom().nextBytes(aesKey);
        return aesKey;
    }
}

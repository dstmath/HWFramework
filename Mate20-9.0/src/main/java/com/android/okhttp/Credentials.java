package com.android.okhttp;

import com.android.okhttp.okio.ByteString;
import java.io.UnsupportedEncodingException;

public final class Credentials {
    private Credentials() {
    }

    public static String basic(String userName, String password) {
        try {
            String encoded = ByteString.of((userName + ":" + password).getBytes("ISO-8859-1")).base64();
            return "Basic " + encoded;
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError();
        }
    }
}

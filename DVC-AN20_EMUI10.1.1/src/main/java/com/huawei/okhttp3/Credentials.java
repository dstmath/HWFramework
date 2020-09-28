package com.huawei.okhttp3;

import com.huawei.okhttp3.internal.Util;
import com.huawei.okio.ByteString;
import java.nio.charset.Charset;

public final class Credentials {
    private Credentials() {
    }

    public static String basic(String username, String password) {
        return basic(username, password, Util.ISO_8859_1);
    }

    public static String basic(String username, String password, Charset charset) {
        String encoded = ByteString.encodeString(username + ":" + password, charset).base64();
        return "Basic " + encoded;
    }
}

package com.android.okhttp.internal;

public final class Version {
    public static String userAgent() {
        String agent = System.getProperty("http.agent");
        return agent != null ? agent : "Java" + System.getProperty("java.version");
    }

    private Version() {
    }
}

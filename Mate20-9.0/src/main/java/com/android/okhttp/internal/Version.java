package com.android.okhttp.internal;

public final class Version {
    public static String userAgent() {
        String agent = System.getProperty("http.agent");
        if (agent != null) {
            return agent;
        }
        return "Java" + System.getProperty("java.version");
    }

    private Version() {
    }
}

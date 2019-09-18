package com.huawei.okhttp3.internal;

public final class Version {
    private static final String DATE = "190124";
    private static final String PREFIX = "okhttp3-3.11.0.hw.";
    private static final String VERSION = "okhttp3-3.11.0.hw.190124";

    public static String userAgent() {
        return VERSION;
    }

    private Version() {
    }
}

package com.huawei.okhttp3.internal;

@Deprecated
public final class Version {
    public static String userAgent() {
        return "okhttp/${project.version}";
    }

    private Version() {
    }
}

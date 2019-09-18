package com.android.server.security.securityprofile;

public interface SecurityProfileInternal {
    void handleActivityResuming(String str);

    void registerScreenshotProtector(ScreenshotProtectorCallback screenshotProtectorCallback);

    boolean shouldPreventInteraction(int i, String str, int i2, int i3, String str2, int i4);

    boolean shouldPreventMediaProjection(int i);

    void unregisterScreenshotProtector(ScreenshotProtectorCallback screenshotProtectorCallback);

    boolean verifyPackage(String str, String str2);
}

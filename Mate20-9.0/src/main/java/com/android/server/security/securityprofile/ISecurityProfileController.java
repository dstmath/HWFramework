package com.android.server.security.securityprofile;

public interface ISecurityProfileController {
    void handleActivityResuming(String str);

    boolean shouldPreventInteraction(int i, String str, int i2, int i3, String str2, int i4);

    boolean shouldPreventMediaProjection(int i);

    boolean verifyPackage(String str, String str2);
}

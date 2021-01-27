package com.android.server.security.securityprofile;

import java.io.File;

public interface ISecurityProfileController {
    @Deprecated
    void handleActivityResuming(String str);

    boolean shouldPreventInteraction(int i, String str, IntentCaller intentCaller, int i2);

    @Deprecated
    boolean shouldPreventMediaProjection(int i);

    boolean verifyPackage(String str, File file);
}

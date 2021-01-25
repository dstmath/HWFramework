package com.huawei.server.security.securityprofile;

import com.android.server.security.securityprofile.IntentCaller;
import java.io.File;

public interface SecurityProfileInternal {
    @Deprecated
    void handleActivityResuming(String str);

    @Deprecated
    void registerScreenshotProtector(ScreenshotProtectorCallback screenshotProtectorCallback);

    boolean shouldPreventInteraction(int i, String str, IntentCaller intentCaller, int i2);

    @Deprecated
    boolean shouldPreventMediaProjection(int i);

    @Deprecated
    void unregisterScreenshotProtector(ScreenshotProtectorCallback screenshotProtectorCallback);

    boolean verifyPackage(String str, File file);
}

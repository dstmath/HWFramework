package com.android.server.security.trustspace;

public interface TrustSpaceManagerInternal {
    boolean checkIntent(int i, String str, int i2, int i3, String str2, int i4);

    int getProtectionLevel(String str);

    void initTrustSpace();

    boolean isIntentProtectedApp(String str);
}

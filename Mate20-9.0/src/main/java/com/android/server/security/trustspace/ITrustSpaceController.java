package com.android.server.security.trustspace;

public interface ITrustSpaceController {
    public static final int TYPE_ACTIVITY = 0;
    public static final int TYPE_BROADCAST = 1;
    public static final int TYPE_PROVIDER = 3;
    public static final int TYPE_SERVICE = 2;

    boolean checkIntent(int i, String str, int i2, int i3, String str2, int i4);

    void initTrustSpace();

    boolean isIntentProtectedApp(String str);
}

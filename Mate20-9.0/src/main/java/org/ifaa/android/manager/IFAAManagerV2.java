package org.ifaa.android.manager;

import android.content.Context;

public abstract class IFAAManagerV2 extends IFAAManager {
    public static final int IFAA_AUTH_FACE = 4;
    public static final int IFAA_AUTH_FINGERPRINT = 1;
    public static final int IFAA_AUTH_IRIS = 2;
    public static final int IFAA_AUTH_NONE = 0;
    public static final int IFAA_AUTH_PIN = 8;
    public static final int IFAA_FAIL = -1;
    public static final int IFAA_OK = 0;
    public static final int IFAA_VERSION_BASIC = 2;

    public abstract byte[] processCmdV2(Context context, byte[] bArr);
}

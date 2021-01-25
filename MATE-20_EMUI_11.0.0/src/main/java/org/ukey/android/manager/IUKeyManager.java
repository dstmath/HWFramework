package org.ukey.android.manager;

import android.content.Context;

public abstract class IUKeyManager {
    public static final int FAILED = -1;
    public static final int SUCCESS = 0;
    public static final int SWITCH_OFF = 0;
    public static final int SWITCH_ON = 1;
    public static final int UKEY_VERSION_1 = 1;
    public static final int UKEY_VERSION_2 = 2;
    public static final int UNSUPPORT_UKEY = 0;

    public abstract int createUKey(String str, String str2, String str3, String str4);

    public abstract int deleteUKey(String str, String str2, String str3, String str4);

    public abstract int getSDKVersion();

    public abstract String getUKeyID();

    public abstract int getUKeyStatus(String str);

    public abstract int getUKeyVersion();

    public abstract void requestUKeyPermission(Context context, int i);

    public abstract int syncUKey(String str, String str2, String str3);
}

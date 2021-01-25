package com.huawei.server.security.fileprotect;

import android.content.Context;
import com.huawei.android.content.pm.PackageParserEx;

public class DefaultHwAppAuthManager {
    private static final Object LOCK = new Object();
    private static volatile DefaultHwAppAuthManager sInstance;

    public static DefaultHwAppAuthManager getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new DefaultHwAppAuthManager();
                }
            }
        }
        return sInstance;
    }

    public void checkFileProtect(PackageParserEx.Package pkg) {
    }

    public void notifyPMSReady(Context context) {
    }

    public void preSendPackageBroadcast(String action, String packageName, String targetPkg) {
    }
}

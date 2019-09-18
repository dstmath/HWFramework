package com.android.server.trust;

import android.os.SystemProperties;

public class HwCustTrustManagerServiceImpl extends HwCustTrustManagerService {
    private static final boolean SHOW_SMARTLOCK = SystemProperties.getBoolean("ro.config.show_smartlock", false);

    public boolean isShowSmartLcok() {
        return SHOW_SMARTLOCK;
    }
}

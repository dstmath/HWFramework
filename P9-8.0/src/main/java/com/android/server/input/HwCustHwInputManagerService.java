package com.android.server.input;

import android.content.Context;
import android.database.ContentObserver;

public class HwCustHwInputManagerService {
    static final String TAG = "HwCustHwInputManagerService";

    public HwCustHwInputManagerService(Context context) {
    }

    public int registerContentObserverForFingerprintNavigation(ContentObserver co) {
        return 0;
    }

    public boolean isFingerprintNavigationEnable() {
        return false;
    }
}

package com.android.server.fingerprint;

import android.content.Context;

public class HwCustFingerprintService {
    public static final int DEFAULT_MAX_FAILED_TIME = 5;

    public boolean isAtt() {
        return false;
    }

    public int getRemainingNum(int failedAttempts, Context context) {
        return 5 - failedAttempts;
    }

    public boolean inLockoutMode(int failedAttempts, Context context) {
        return failedAttempts >= 5;
    }

    public boolean isLockoutMode(int failedAttempts, Context context) {
        return failedAttempts % 5 == 0;
    }
}

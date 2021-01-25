package com.huawei.android.feature.fingerprint;

public class SignVerifyStrategyFactory {
    private static final int VERIFY_ROOT_CA = 2;
    private static final int VERIFY_SIGN_SHA256 = 1;

    public static SignVerifyStrategy getSignVerifyStrategy(int i) {
        switch (i) {
            case 1:
                return new HiPkgSignManager();
            case 2:
                return new CAVerifyManager();
            default:
                return null;
        }
    }
}

package com.huawei.server.fingerprint;

import android.content.Context;

public class HwPartFingerprintFactoryImpl extends HwPartFingerprintFactory {
    public FingerViewController getFingerViewController(Context context) {
        return FingerViewController.getInstance(context);
    }
}

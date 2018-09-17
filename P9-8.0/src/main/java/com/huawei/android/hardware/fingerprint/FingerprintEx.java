package com.huawei.android.hardware.fingerprint;

import android.hardware.fingerprint.Fingerprint;

public class FingerprintEx {
    private Fingerprint mFingerprint;

    public FingerprintEx(Fingerprint fingerprint) {
        this.mFingerprint = fingerprint;
    }

    public CharSequence getName() {
        if (this.mFingerprint == null) {
            return "";
        }
        return this.mFingerprint.getName();
    }

    public int getFingerId() {
        if (this.mFingerprint == null) {
            return 0;
        }
        return this.mFingerprint.getFingerId();
    }

    public Fingerprint getFingerprint() {
        return this.mFingerprint;
    }
}

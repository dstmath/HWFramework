package com.huawei.android.hardware.fingerprint;

import android.hardware.fingerprint.Fingerprint;

public class FingerprintEx {
    private Fingerprint mFingerprint;

    public FingerprintEx(Fingerprint fingerprint) {
        this.mFingerprint = fingerprint;
    }

    public CharSequence getName() {
        Fingerprint fingerprint = this.mFingerprint;
        if (fingerprint == null) {
            return "";
        }
        return fingerprint.getName();
    }

    public int getFingerId() {
        Fingerprint fingerprint = this.mFingerprint;
        if (fingerprint == null) {
            return 0;
        }
        return fingerprint.getBiometricId();
    }

    public Fingerprint getFingerprint() {
        return this.mFingerprint;
    }
}

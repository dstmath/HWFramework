package com.huawei.android.biometric;

import android.hardware.fingerprint.Fingerprint;

public class FingerprintEx {
    private Fingerprint mFingerprint;

    public FingerprintEx(CharSequence name, int groupId, int fingerId, long deviceId) {
        if (name != null) {
            this.mFingerprint = new Fingerprint(name, groupId, fingerId, deviceId);
        }
    }

    public Fingerprint getFingerprint() {
        return this.mFingerprint;
    }

    public void setFingerprint(Fingerprint fingerprint) {
        this.mFingerprint = fingerprint;
    }

    public int getBiometricId() {
        Fingerprint fingerprint = this.mFingerprint;
        if (fingerprint != null) {
            return fingerprint.getBiometricId();
        }
        return 0;
    }

    public CharSequence getName() {
        Fingerprint fingerprint = this.mFingerprint;
        if (fingerprint != null) {
            return fingerprint.getName();
        }
        return "";
    }
}

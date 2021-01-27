package com.huawei.android.biometric;

import android.hardware.biometrics.BiometricAuthenticator;

public class BiometricAuthenticatorEx {
    private BiometricAuthenticator mBiometricAuthenticator;
    private BiometricAuthenticator.Identifier mIdentifier;

    public BiometricAuthenticator getBiometricAuthenticator() {
        return this.mBiometricAuthenticator;
    }

    public void setBiometricAuthenticator(BiometricAuthenticator biometricAuthenticator) {
        this.mBiometricAuthenticator = biometricAuthenticator;
    }

    public BiometricAuthenticator.Identifier getIdentifier() {
        return this.mIdentifier;
    }

    public void setIdentifier(BiometricAuthenticator.Identifier identifier) {
        this.mIdentifier = identifier;
    }

    public int getBiometricId() {
        BiometricAuthenticator.Identifier identifier = this.mIdentifier;
        if (identifier != null) {
            return identifier.getBiometricId();
        }
        return 0;
    }

    public void setFingerprintEx(FingerprintEx fingerprintEx) {
        if (fingerprintEx != null) {
            this.mIdentifier = fingerprintEx.getFingerprint();
        }
    }
}

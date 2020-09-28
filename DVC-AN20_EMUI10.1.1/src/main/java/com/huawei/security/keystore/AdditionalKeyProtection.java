package com.huawei.security.keystore;

import android.support.annotation.NonNull;
import java.security.KeyStore;

public final class AdditionalKeyProtection implements KeyStore.ProtectionParameter {
    private boolean mBiometricAuthenticationRequired;
    private int mBiometricType;
    private boolean mIsInvalidatedBySystemRooting;
    private boolean mIsTemplateBound;

    public AdditionalKeyProtection(boolean mBiometricAuthenticationRequired2, int mBiometricType2, boolean mIsTemplateBound2, boolean mIsInvalidatedBySystemRooting2) {
        this.mBiometricAuthenticationRequired = mBiometricAuthenticationRequired2;
        this.mBiometricType = mBiometricType2;
        this.mIsTemplateBound = mIsTemplateBound2;
        this.mIsInvalidatedBySystemRooting = mIsInvalidatedBySystemRooting2;
    }

    public boolean getBiometricAuthenticationRequired() {
        return this.mBiometricAuthenticationRequired;
    }

    public int getBiometricType() {
        return this.mBiometricType;
    }

    public boolean isTemplateBound() {
        return this.mIsTemplateBound;
    }

    public boolean isInvalidatedBySystemRooting() {
        return this.mIsInvalidatedBySystemRooting;
    }

    public static final class Builder {
        private boolean mBiometricAuthenticationRequired;
        private int mBiometricType;
        private boolean mIsInvalidatedBySystemRooting;
        private boolean mIsTemplateBound;

        @NonNull
        public Builder setBiometricAuthenticationRequired(int type) {
            this.mBiometricType = type;
            return this;
        }

        @NonNull
        public Builder setBiometricAuthenticationRequired(int type, boolean bindTemplate) {
            this.mBiometricType = type;
            this.mIsTemplateBound = bindTemplate;
            return this;
        }

        @NonNull
        public Builder setInvalidatedBySystemRooting(boolean invalidateKey) {
            this.mIsInvalidatedBySystemRooting = invalidateKey;
            return this;
        }

        @NonNull
        public AdditionalKeyProtection build() {
            return new AdditionalKeyProtection(this.mBiometricAuthenticationRequired, this.mBiometricType, this.mIsTemplateBound, this.mIsInvalidatedBySystemRooting);
        }
    }
}

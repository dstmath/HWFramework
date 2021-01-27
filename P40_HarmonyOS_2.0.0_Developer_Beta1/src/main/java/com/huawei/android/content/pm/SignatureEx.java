package com.huawei.android.content.pm;

import android.content.pm.Signature;

public class SignatureEx {
    private Signature mSignature;

    public void setSignature(Signature signature) {
        this.mSignature = signature;
    }

    public Signature getSignature() {
        return this.mSignature;
    }

    public byte[] toByteArray() {
        return this.mSignature.toByteArray();
    }
}

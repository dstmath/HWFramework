package com.android.server.pm.permission;

import android.content.pm.Signature;
import com.huawei.android.content.pm.SignatureEx;

public class BasePermissionEx {
    private BasePermission mBasePermission;

    public BasePermission getBasePermission() {
        return this.mBasePermission;
    }

    public void setBasePermission(BasePermission basePermission) {
        this.mBasePermission = basePermission;
    }

    public boolean isNormal() {
        return this.mBasePermission.isNormal();
    }

    public boolean isRuntime() {
        return this.mBasePermission.isRuntime();
    }

    public boolean isSignature() {
        return this.mBasePermission.isSignature();
    }

    public SignatureEx[] getSignatures() {
        Signature[] signatures = this.mBasePermission.getSourcePackageSetting().getSignatures();
        SignatureEx[] signatureExes = new SignatureEx[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            signatureExes[i] = new SignatureEx();
            signatureExes[i].setSignature(signatures[i]);
        }
        return signatureExes;
    }
}

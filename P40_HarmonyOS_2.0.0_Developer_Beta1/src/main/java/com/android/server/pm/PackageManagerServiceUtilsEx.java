package com.android.server.pm;

import android.content.pm.Signature;
import com.huawei.android.content.pm.SignatureEx;

public class PackageManagerServiceUtilsEx {
    public static int compareSignatures(SignatureEx[] s1, SignatureEx[] s2) {
        return PackageManagerServiceUtils.compareSignatures(toSignature(s1), toSignature(s2));
    }

    private static Signature[] toSignature(SignatureEx[] sex) {
        Signature[] signatures = new Signature[sex.length];
        for (int i = 0; i < sex.length; i++) {
            signatures[i] = sex[i].getSignature();
        }
        return signatures;
    }
}

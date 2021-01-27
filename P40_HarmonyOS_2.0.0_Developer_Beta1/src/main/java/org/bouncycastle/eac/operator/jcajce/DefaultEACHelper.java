package org.bouncycastle.eac.operator.jcajce;

import java.security.NoSuchAlgorithmException;
import java.security.Signature;

class DefaultEACHelper extends EACHelper {
    DefaultEACHelper() {
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.eac.operator.jcajce.EACHelper
    public Signature createSignature(String str) throws NoSuchAlgorithmException {
        return Signature.getInstance(str);
    }
}

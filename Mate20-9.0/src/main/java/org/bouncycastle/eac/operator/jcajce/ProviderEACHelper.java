package org.bouncycastle.eac.operator.jcajce;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Signature;

class ProviderEACHelper extends EACHelper {
    private final Provider provider;

    ProviderEACHelper(Provider provider2) {
        this.provider = provider2;
    }

    /* access modifiers changed from: protected */
    public Signature createSignature(String str) throws NoSuchAlgorithmException {
        return Signature.getInstance(str, this.provider);
    }
}

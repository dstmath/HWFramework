package org.bouncycastle.eac.operator.jcajce;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;

class NamedEACHelper extends EACHelper {
    private final String providerName;

    NamedEACHelper(String str) {
        this.providerName = str;
    }

    /* access modifiers changed from: protected */
    public Signature createSignature(String str) throws NoSuchProviderException, NoSuchAlgorithmException {
        return Signature.getInstance(str, this.providerName);
    }
}

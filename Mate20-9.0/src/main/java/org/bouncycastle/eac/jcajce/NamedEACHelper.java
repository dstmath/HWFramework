package org.bouncycastle.eac.jcajce;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

class NamedEACHelper implements EACHelper {
    private final String providerName;

    NamedEACHelper(String str) {
        this.providerName = str;
    }

    public KeyFactory createKeyFactory(String str) throws NoSuchProviderException, NoSuchAlgorithmException {
        return KeyFactory.getInstance(str, this.providerName);
    }
}

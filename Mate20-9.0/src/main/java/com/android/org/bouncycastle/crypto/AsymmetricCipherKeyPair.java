package com.android.org.bouncycastle.crypto;

import com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class AsymmetricCipherKeyPair {
    private AsymmetricKeyParameter privateParam;
    private AsymmetricKeyParameter publicParam;

    public AsymmetricCipherKeyPair(AsymmetricKeyParameter publicParam2, AsymmetricKeyParameter privateParam2) {
        this.publicParam = publicParam2;
        this.privateParam = privateParam2;
    }

    public AsymmetricCipherKeyPair(CipherParameters publicParam2, CipherParameters privateParam2) {
        this.publicParam = (AsymmetricKeyParameter) publicParam2;
        this.privateParam = (AsymmetricKeyParameter) privateParam2;
    }

    public AsymmetricKeyParameter getPublic() {
        return this.publicParam;
    }

    public AsymmetricKeyParameter getPrivate() {
        return this.privateParam;
    }
}

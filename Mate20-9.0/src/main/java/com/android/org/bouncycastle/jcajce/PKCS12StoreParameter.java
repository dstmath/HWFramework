package com.android.org.bouncycastle.jcajce;

import java.io.OutputStream;
import java.security.KeyStore;

public class PKCS12StoreParameter implements KeyStore.LoadStoreParameter {
    private final boolean forDEREncoding;
    private final OutputStream out;
    private final KeyStore.ProtectionParameter protectionParameter;

    public PKCS12StoreParameter(OutputStream out2, char[] password) {
        this(out2, password, false);
    }

    public PKCS12StoreParameter(OutputStream out2, KeyStore.ProtectionParameter protectionParameter2) {
        this(out2, protectionParameter2, false);
    }

    public PKCS12StoreParameter(OutputStream out2, char[] password, boolean forDEREncoding2) {
        this(out2, (KeyStore.ProtectionParameter) new KeyStore.PasswordProtection(password), forDEREncoding2);
    }

    public PKCS12StoreParameter(OutputStream out2, KeyStore.ProtectionParameter protectionParameter2, boolean forDEREncoding2) {
        this.out = out2;
        this.protectionParameter = protectionParameter2;
        this.forDEREncoding = forDEREncoding2;
    }

    public OutputStream getOutputStream() {
        return this.out;
    }

    public KeyStore.ProtectionParameter getProtectionParameter() {
        return this.protectionParameter;
    }

    public boolean isForDEREncoding() {
        return this.forDEREncoding;
    }
}

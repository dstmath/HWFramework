package com.android.org.bouncycastle.jce.provider;

import java.io.OutputStream;
import java.security.KeyStore;

public class JDKPKCS12StoreParameter implements KeyStore.LoadStoreParameter {
    private OutputStream outputStream;
    private KeyStore.ProtectionParameter protectionParameter;
    private boolean useDEREncoding;

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    public KeyStore.ProtectionParameter getProtectionParameter() {
        return this.protectionParameter;
    }

    public boolean isUseDEREncoding() {
        return this.useDEREncoding;
    }

    public void setOutputStream(OutputStream outputStream2) {
        this.outputStream = outputStream2;
    }

    public void setPassword(char[] password) {
        this.protectionParameter = new KeyStore.PasswordProtection(password);
    }

    public void setProtectionParameter(KeyStore.ProtectionParameter protectionParameter2) {
        this.protectionParameter = protectionParameter2;
    }

    public void setUseDEREncoding(boolean useDEREncoding2) {
        this.useDEREncoding = useDEREncoding2;
    }
}

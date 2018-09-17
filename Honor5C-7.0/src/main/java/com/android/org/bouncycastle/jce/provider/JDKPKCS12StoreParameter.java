package com.android.org.bouncycastle.jce.provider;

import java.io.OutputStream;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;

public class JDKPKCS12StoreParameter implements LoadStoreParameter {
    private OutputStream outputStream;
    private ProtectionParameter protectionParameter;
    private boolean useDEREncoding;

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    public ProtectionParameter getProtectionParameter() {
        return this.protectionParameter;
    }

    public boolean isUseDEREncoding() {
        return this.useDEREncoding;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void setPassword(char[] password) {
        this.protectionParameter = new PasswordProtection(password);
    }

    public void setProtectionParameter(ProtectionParameter protectionParameter) {
        this.protectionParameter = protectionParameter;
    }

    public void setUseDEREncoding(boolean useDEREncoding) {
        this.useDEREncoding = useDEREncoding;
    }
}

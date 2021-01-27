package org.bouncycastle.jcajce;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;

public class BCLoadStoreParameter implements KeyStore.LoadStoreParameter {
    private final InputStream in;
    private final OutputStream out;
    private final KeyStore.ProtectionParameter protectionParameter;

    BCLoadStoreParameter(InputStream inputStream, OutputStream outputStream, KeyStore.ProtectionParameter protectionParameter2) {
        this.in = inputStream;
        this.out = outputStream;
        this.protectionParameter = protectionParameter2;
    }

    public BCLoadStoreParameter(InputStream inputStream, KeyStore.ProtectionParameter protectionParameter2) {
        this(inputStream, null, protectionParameter2);
    }

    public BCLoadStoreParameter(InputStream inputStream, char[] cArr) {
        this(inputStream, new KeyStore.PasswordProtection(cArr));
    }

    public BCLoadStoreParameter(OutputStream outputStream, KeyStore.ProtectionParameter protectionParameter2) {
        this(null, outputStream, protectionParameter2);
    }

    public BCLoadStoreParameter(OutputStream outputStream, char[] cArr) {
        this(outputStream, new KeyStore.PasswordProtection(cArr));
    }

    public InputStream getInputStream() {
        if (this.out == null) {
            return this.in;
        }
        throw new UnsupportedOperationException("parameter configured for storage OutputStream present");
    }

    public OutputStream getOutputStream() {
        OutputStream outputStream = this.out;
        if (outputStream != null) {
            return outputStream;
        }
        throw new UnsupportedOperationException("parameter not configured for storage - no OutputStream");
    }

    @Override // java.security.KeyStore.LoadStoreParameter
    public KeyStore.ProtectionParameter getProtectionParameter() {
        return this.protectionParameter;
    }
}

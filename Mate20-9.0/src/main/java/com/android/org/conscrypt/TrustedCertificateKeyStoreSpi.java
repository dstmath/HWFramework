package com.android.org.conscrypt;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStoreSpi;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;

public final class TrustedCertificateKeyStoreSpi extends KeyStoreSpi {
    private final TrustedCertificateStore store = new TrustedCertificateStore();

    public Key engineGetKey(String alias, char[] password) {
        if (alias != null) {
            return null;
        }
        throw new NullPointerException("alias == null");
    }

    public Certificate[] engineGetCertificateChain(String alias) {
        if (alias != null) {
            return null;
        }
        throw new NullPointerException("alias == null");
    }

    public Certificate engineGetCertificate(String alias) {
        return this.store.getCertificate(alias);
    }

    public Date engineGetCreationDate(String alias) {
        return this.store.getCreationDate(alias);
    }

    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) {
        throw new UnsupportedOperationException();
    }

    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) {
        throw new UnsupportedOperationException();
    }

    public void engineSetCertificateEntry(String alias, Certificate cert) {
        if (alias == null) {
            throw new NullPointerException("alias == null");
        }
        throw new UnsupportedOperationException();
    }

    public void engineDeleteEntry(String alias) {
        throw new UnsupportedOperationException();
    }

    public Enumeration<String> engineAliases() {
        return Collections.enumeration(this.store.aliases());
    }

    public boolean engineContainsAlias(String alias) {
        return this.store.containsAlias(alias);
    }

    public int engineSize() {
        return this.store.aliases().size();
    }

    public boolean engineIsKeyEntry(String alias) {
        if (alias != null) {
            return false;
        }
        throw new NullPointerException("alias == null");
    }

    public boolean engineIsCertificateEntry(String alias) {
        return engineContainsAlias(alias);
    }

    public String engineGetCertificateAlias(Certificate c) {
        return this.store.getCertificateAlias(c);
    }

    public void engineStore(OutputStream stream, char[] password) {
        throw new UnsupportedOperationException();
    }

    public void engineLoad(InputStream stream, char[] password) {
        if (stream != null) {
            throw new UnsupportedOperationException();
        }
    }
}

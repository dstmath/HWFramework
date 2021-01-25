package com.android.server.locksettings.recoverablekeystore;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class KeyStoreProxyImpl implements KeyStoreProxy {
    private static final String ANDROID_KEY_STORE_PROVIDER = "AndroidKeyStore";
    private final KeyStore mKeyStore;

    public KeyStoreProxyImpl(KeyStore keyStore) {
        this.mKeyStore = keyStore;
    }

    @Override // com.android.server.locksettings.recoverablekeystore.KeyStoreProxy
    public boolean containsAlias(String alias) throws KeyStoreException {
        return this.mKeyStore.containsAlias(alias);
    }

    @Override // com.android.server.locksettings.recoverablekeystore.KeyStoreProxy
    public Key getKey(String alias, char[] password) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return this.mKeyStore.getKey(alias, password);
    }

    @Override // com.android.server.locksettings.recoverablekeystore.KeyStoreProxy
    public void setEntry(String alias, KeyStore.Entry entry, KeyStore.ProtectionParameter protParam) throws KeyStoreException {
        this.mKeyStore.setEntry(alias, entry, protParam);
    }

    @Override // com.android.server.locksettings.recoverablekeystore.KeyStoreProxy
    public void deleteEntry(String alias) throws KeyStoreException {
        this.mKeyStore.deleteEntry(alias);
    }

    public static KeyStore getAndLoadAndroidKeyStore() throws KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_PROVIDER);
        try {
            keyStore.load(null);
            return keyStore;
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new KeyStoreException("Unable to load keystore.", e);
        }
    }
}

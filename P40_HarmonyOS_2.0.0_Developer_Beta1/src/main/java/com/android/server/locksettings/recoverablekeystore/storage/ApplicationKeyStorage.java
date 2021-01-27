package com.android.server.locksettings.recoverablekeystore.storage;

import android.os.ServiceSpecificException;
import android.security.KeyStore;
import android.security.keystore.KeyProtection;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.locksettings.recoverablekeystore.KeyStoreProxy;
import com.android.server.locksettings.recoverablekeystore.KeyStoreProxyImpl;
import com.android.server.slice.SliceClientPermissions;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Locale;
import javax.crypto.spec.SecretKeySpec;

public class ApplicationKeyStorage {
    private static final String APPLICATION_KEY_ALIAS_PREFIX = "com.android.server.locksettings.recoverablekeystore/application/";
    private static final String TAG = "RecoverableAppKeyStore";
    private final KeyStoreProxy mKeyStore;
    private final KeyStore mKeystoreService;

    public static ApplicationKeyStorage getInstance(KeyStore keystoreService) throws KeyStoreException {
        return new ApplicationKeyStorage(new KeyStoreProxyImpl(KeyStoreProxyImpl.getAndLoadAndroidKeyStore()), keystoreService);
    }

    @VisibleForTesting
    ApplicationKeyStorage(KeyStoreProxy keyStore, KeyStore keystoreService) {
        this.mKeyStore = keyStore;
        this.mKeystoreService = keystoreService;
    }

    public String getGrantAlias(int userId, int uid, String alias) {
        Log.i(TAG, String.format(Locale.US, "Get %d/%d/%s", Integer.valueOf(userId), Integer.valueOf(uid), alias));
        return this.mKeystoreService.grant("USRPKEY_" + getInternalAlias(userId, uid, alias), uid);
    }

    public void setSymmetricKeyEntry(int userId, int uid, String alias, byte[] secretKey) throws KeyStoreException {
        Log.i(TAG, String.format(Locale.US, "Set %d/%d/%s: %d bytes of key material", Integer.valueOf(userId), Integer.valueOf(uid), alias, Integer.valueOf(secretKey.length)));
        try {
            this.mKeyStore.setEntry(getInternalAlias(userId, uid, alias), new KeyStore.SecretKeyEntry(new SecretKeySpec(secretKey, "AES")), new KeyProtection.Builder(3).setBlockModes("GCM").setEncryptionPaddings("NoPadding").build());
        } catch (KeyStoreException e) {
            throw new ServiceSpecificException(22, e.getMessage());
        }
    }

    public void deleteEntry(int userId, int uid, String alias) {
        Log.i(TAG, String.format(Locale.US, "Del %d/%d/%s", Integer.valueOf(userId), Integer.valueOf(uid), alias));
        try {
            this.mKeyStore.deleteEntry(getInternalAlias(userId, uid, alias));
        } catch (KeyStoreException e) {
            throw new ServiceSpecificException(22, e.getMessage());
        }
    }

    private String getInternalAlias(int userId, int uid, String alias) {
        return APPLICATION_KEY_ALIAS_PREFIX + userId + SliceClientPermissions.SliceAuthority.DELIMITER + uid + SliceClientPermissions.SliceAuthority.DELIMITER + alias;
    }
}

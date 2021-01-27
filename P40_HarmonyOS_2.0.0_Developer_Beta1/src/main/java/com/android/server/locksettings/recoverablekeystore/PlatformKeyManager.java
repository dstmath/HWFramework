package com.android.server.locksettings.recoverablekeystore;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.RemoteException;
import android.security.GateKeeper;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProtection;
import android.service.gatekeeper.IGateKeeperService;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.locksettings.recoverablekeystore.storage.RecoverableKeyStoreDb;
import com.android.server.slice.SliceClientPermissions;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class PlatformKeyManager {
    private static final String ANDROID_KEY_STORE_PROVIDER = "AndroidKeyStore";
    private static final String DECRYPT_KEY_ALIAS_SUFFIX = "decrypt";
    private static final String ENCRYPT_KEY_ALIAS_SUFFIX = "encrypt";
    private static final byte[] GCM_INSECURE_NONCE_BYTES = new byte[12];
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final String KEY_ALGORITHM = "AES";
    private static final String KEY_ALIAS_PREFIX = "com.android.server.locksettings.recoverablekeystore/platform/";
    private static final int KEY_SIZE_BITS = 256;
    private static final String KEY_WRAP_CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String TAG = "PlatformKeyManager";
    private static final int USER_AUTHENTICATION_VALIDITY_DURATION_SECONDS = 15;
    private final Context mContext;
    private final RecoverableKeyStoreDb mDatabase;
    private final KeyStoreProxy mKeyStore;

    public static PlatformKeyManager getInstance(Context context, RecoverableKeyStoreDb database) throws KeyStoreException, NoSuchAlgorithmException {
        return new PlatformKeyManager(context.getApplicationContext(), new KeyStoreProxyImpl(getAndLoadAndroidKeyStore()), database);
    }

    @VisibleForTesting
    PlatformKeyManager(Context context, KeyStoreProxy keyStore, RecoverableKeyStoreDb database) {
        this.mKeyStore = keyStore;
        this.mContext = context;
        this.mDatabase = database;
    }

    public int getGenerationId(int userId) {
        return this.mDatabase.getPlatformKeyGenerationId(userId);
    }

    public boolean isAvailable(int userId) {
        return ((KeyguardManager) this.mContext.getSystemService(KeyguardManager.class)).isDeviceSecure(userId);
    }

    public void invalidatePlatformKey(int userId, int generationId) {
        if (generationId != -1) {
            try {
                this.mKeyStore.deleteEntry(getEncryptAlias(userId, generationId));
                this.mKeyStore.deleteEntry(getDecryptAlias(userId, generationId));
            } catch (KeyStoreException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void regenerate(int userId) throws NoSuchAlgorithmException, KeyStoreException, InsecureUserException, IOException, RemoteException {
        int nextId;
        if (isAvailable(userId)) {
            int generationId = getGenerationId(userId);
            if (generationId == -1) {
                nextId = 1;
            } else {
                invalidatePlatformKey(userId, generationId);
                nextId = generationId + 1;
            }
            generateAndLoadKey(userId, nextId);
            return;
        }
        throw new InsecureUserException(String.format(Locale.US, "%d does not have a lock screen set.", Integer.valueOf(userId)));
    }

    public PlatformEncryptionKey getEncryptKey(int userId) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, InsecureUserException, IOException, RemoteException {
        init(userId);
        try {
            getDecryptKeyInternal(userId);
            return getEncryptKeyInternal(userId);
        } catch (UnrecoverableKeyException e) {
            Log.i(TAG, String.format(Locale.US, "Regenerating permanently invalid Platform key for user %d.", Integer.valueOf(userId)));
            regenerate(userId);
            return getEncryptKeyInternal(userId);
        }
    }

    private PlatformEncryptionKey getEncryptKeyInternal(int userId) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, InsecureUserException {
        int generationId = getGenerationId(userId);
        String alias = getEncryptAlias(userId, generationId);
        if (isKeyLoaded(userId, generationId)) {
            return new PlatformEncryptionKey(generationId, this.mKeyStore.getKey(alias, null));
        }
        throw new UnrecoverableKeyException("KeyStore doesn't contain key " + alias);
    }

    public PlatformDecryptionKey getDecryptKey(int userId) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, InsecureUserException, IOException, RemoteException {
        init(userId);
        try {
            PlatformDecryptionKey decryptionKey = getDecryptKeyInternal(userId);
            ensureDecryptionKeyIsValid(userId, decryptionKey);
            return decryptionKey;
        } catch (UnrecoverableKeyException e) {
            Log.i(TAG, String.format(Locale.US, "Regenerating permanently invalid Platform key for user %d.", Integer.valueOf(userId)));
            regenerate(userId);
            return getDecryptKeyInternal(userId);
        }
    }

    private PlatformDecryptionKey getDecryptKeyInternal(int userId) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, InsecureUserException {
        int generationId = getGenerationId(userId);
        String alias = getDecryptAlias(userId, generationId);
        if (isKeyLoaded(userId, generationId)) {
            return new PlatformDecryptionKey(generationId, this.mKeyStore.getKey(alias, null));
        }
        throw new UnrecoverableKeyException("KeyStore doesn't contain key " + alias);
    }

    private void ensureDecryptionKeyIsValid(int userId, PlatformDecryptionKey decryptionKey) throws UnrecoverableKeyException {
        try {
            Cipher.getInstance(KEY_WRAP_CIPHER_ALGORITHM).init(4, (Key) decryptionKey.getKey(), (AlgorithmParameterSpec) new GCMParameterSpec(128, GCM_INSECURE_NONCE_BYTES));
        } catch (KeyPermanentlyInvalidatedException e) {
            Log.e(TAG, String.format(Locale.US, "The platform key for user %d became invalid.", Integer.valueOf(userId)));
            throw new UnrecoverableKeyException(e.getMessage());
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e2) {
        }
    }

    /* access modifiers changed from: package-private */
    public void init(int userId) throws KeyStoreException, NoSuchAlgorithmException, InsecureUserException, IOException, RemoteException {
        int generationId;
        if (isAvailable(userId)) {
            int generationId2 = getGenerationId(userId);
            if (isKeyLoaded(userId, generationId2)) {
                Log.i(TAG, String.format(Locale.US, "Platform key generation %d exists already.", Integer.valueOf(generationId2)));
                return;
            }
            if (generationId2 == -1) {
                Log.i(TAG, "Generating initial platform key generation ID.");
                generationId = 1;
            } else {
                Log.w(TAG, String.format(Locale.US, "Platform generation ID was %d but no entry was present in AndroidKeyStore. Generating fresh key.", Integer.valueOf(generationId2)));
                generationId = generationId2 + 1;
            }
            generateAndLoadKey(userId, generationId);
            return;
        }
        throw new InsecureUserException(String.format(Locale.US, "%d does not have a lock screen set.", Integer.valueOf(userId)));
    }

    private String getEncryptAlias(int userId, int generationId) {
        return KEY_ALIAS_PREFIX + userId + SliceClientPermissions.SliceAuthority.DELIMITER + generationId + SliceClientPermissions.SliceAuthority.DELIMITER + ENCRYPT_KEY_ALIAS_SUFFIX;
    }

    private String getDecryptAlias(int userId, int generationId) {
        return KEY_ALIAS_PREFIX + userId + SliceClientPermissions.SliceAuthority.DELIMITER + generationId + SliceClientPermissions.SliceAuthority.DELIMITER + DECRYPT_KEY_ALIAS_SUFFIX;
    }

    private void setGenerationId(int userId, int generationId) throws IOException {
        this.mDatabase.setPlatformKeyGenerationId(userId, generationId);
    }

    private boolean isKeyLoaded(int userId, int generationId) throws KeyStoreException {
        return this.mKeyStore.containsAlias(getEncryptAlias(userId, generationId)) && this.mKeyStore.containsAlias(getDecryptAlias(userId, generationId));
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public IGateKeeperService getGateKeeperService() {
        return GateKeeper.getService();
    }

    private void generateAndLoadKey(int userId, int generationId) throws NoSuchAlgorithmException, KeyStoreException, IOException, RemoteException {
        String encryptAlias = getEncryptAlias(userId, generationId);
        String decryptAlias = getDecryptAlias(userId, generationId);
        SecretKey secretKey = generateAesKey();
        long secureUserId = getGateKeeperService().getSecureUserId(userId);
        if (secureUserId == 0) {
            Log.e(TAG, "No SID available for user " + userId);
            return;
        }
        this.mKeyStore.setEntry(decryptAlias, new KeyStore.SecretKeyEntry(secretKey), new KeyProtection.Builder(2).setUserAuthenticationRequired(true).setUserAuthenticationValidityDurationSeconds(15).setBlockModes("GCM").setEncryptionPaddings("NoPadding").setBoundToSpecificSecureUserId(secureUserId).build());
        this.mKeyStore.setEntry(encryptAlias, new KeyStore.SecretKeyEntry(secretKey), new KeyProtection.Builder(1).setBlockModes("GCM").setEncryptionPaddings("NoPadding").build());
        setGenerationId(userId, generationId);
    }

    private static SecretKey generateAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    private static KeyStore getAndLoadAndroidKeyStore() throws KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_PROVIDER);
        try {
            keyStore.load(null);
            return keyStore;
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new KeyStoreException("Unable to load keystore.", e);
        }
    }
}

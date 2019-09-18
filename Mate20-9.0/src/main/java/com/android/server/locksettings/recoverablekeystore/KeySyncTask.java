package com.android.server.locksettings.recoverablekeystore;

import android.content.Context;
import android.security.Scrypt;
import android.security.keystore.recovery.KeyChainProtectionParams;
import android.security.keystore.recovery.KeyChainSnapshot;
import android.security.keystore.recovery.KeyDerivationParams;
import android.security.keystore.recovery.WrappedApplicationKey;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.server.locksettings.recoverablekeystore.storage.RecoverableKeyStoreDb;
import com.android.server.locksettings.recoverablekeystore.storage.RecoverySnapshotStorage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class KeySyncTask implements Runnable {
    private static final int LENGTH_PREFIX_BYTES = 4;
    private static final String LOCK_SCREEN_HASH_ALGORITHM = "SHA-256";
    private static final String RECOVERY_KEY_ALGORITHM = "AES";
    private static final int RECOVERY_KEY_SIZE_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    @VisibleForTesting
    static final int SCRYPT_PARAM_N = 4096;
    @VisibleForTesting
    static final int SCRYPT_PARAM_OUTLEN_BYTES = 32;
    @VisibleForTesting
    static final int SCRYPT_PARAM_P = 1;
    @VisibleForTesting
    static final int SCRYPT_PARAM_R = 8;
    private static final String TAG = "KeySyncTask";
    private static final int TRUSTED_HARDWARE_MAX_ATTEMPTS = 10;
    private final String mCredential;
    private final int mCredentialType;
    private final boolean mCredentialUpdated;
    private final PlatformKeyManager mPlatformKeyManager;
    private final RecoverableKeyStoreDb mRecoverableKeyStoreDb;
    private final RecoverySnapshotStorage mRecoverySnapshotStorage;
    private final Scrypt mScrypt;
    private final RecoverySnapshotListenersStorage mSnapshotListenersStorage;
    private final TestOnlyInsecureCertificateHelper mTestOnlyInsecureCertificateHelper;
    private final int mUserId;

    public static KeySyncTask newInstance(Context context, RecoverableKeyStoreDb recoverableKeyStoreDb, RecoverySnapshotStorage snapshotStorage, RecoverySnapshotListenersStorage recoverySnapshotListenersStorage, int userId, int credentialType, String credential, boolean credentialUpdated) throws NoSuchAlgorithmException, KeyStoreException, InsecureUserException {
        KeySyncTask keySyncTask = new KeySyncTask(recoverableKeyStoreDb, snapshotStorage, recoverySnapshotListenersStorage, userId, credentialType, credential, credentialUpdated, PlatformKeyManager.getInstance(context, recoverableKeyStoreDb), new TestOnlyInsecureCertificateHelper(), new Scrypt());
        return keySyncTask;
    }

    @VisibleForTesting
    KeySyncTask(RecoverableKeyStoreDb recoverableKeyStoreDb, RecoverySnapshotStorage snapshotStorage, RecoverySnapshotListenersStorage recoverySnapshotListenersStorage, int userId, int credentialType, String credential, boolean credentialUpdated, PlatformKeyManager platformKeyManager, TestOnlyInsecureCertificateHelper testOnlyInsecureCertificateHelper, Scrypt scrypt) {
        this.mSnapshotListenersStorage = recoverySnapshotListenersStorage;
        this.mRecoverableKeyStoreDb = recoverableKeyStoreDb;
        this.mUserId = userId;
        this.mCredentialType = credentialType;
        this.mCredential = credential;
        this.mCredentialUpdated = credentialUpdated;
        this.mPlatformKeyManager = platformKeyManager;
        this.mRecoverySnapshotStorage = snapshotStorage;
        this.mTestOnlyInsecureCertificateHelper = testOnlyInsecureCertificateHelper;
        this.mScrypt = scrypt;
    }

    public void run() {
        try {
            synchronized (KeySyncTask.class) {
                syncKeys();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected exception thrown during KeySyncTask", e);
        }
    }

    private void syncKeys() {
        if (this.mCredentialType == -1) {
            Log.w(TAG, "Credentials are not set for user " + this.mUserId);
            this.mPlatformKeyManager.invalidatePlatformKey(this.mUserId, this.mPlatformKeyManager.getGenerationId(this.mUserId));
        } else if (isCustomLockScreen() != 0) {
            Log.w(TAG, "Unsupported credential type " + this.mCredentialType + "for user " + this.mUserId);
            this.mRecoverableKeyStoreDb.invalidateKeysForUserIdOnCustomScreenLock(this.mUserId);
        } else {
            List<Integer> recoveryAgents = this.mRecoverableKeyStoreDb.getRecoveryAgents(this.mUserId);
            for (Integer intValue : recoveryAgents) {
                int uid = intValue.intValue();
                try {
                    syncKeysForAgent(uid);
                } catch (IOException e) {
                    Log.e(TAG, "IOException during sync for agent " + uid, e);
                }
            }
            if (recoveryAgents.isEmpty()) {
                Log.w(TAG, "No recovery agent initialized for user " + this.mUserId);
            }
        }
    }

    private boolean isCustomLockScreen() {
        return (this.mCredentialType == -1 || this.mCredentialType == 1 || this.mCredentialType == 2) ? false : true;
    }

    private void syncKeysForAgent(int recoveryAgentUid) throws IOException {
        PublicKey publicKey;
        byte[] localLskfHash;
        Long counterId;
        KeyDerivationParams keyDerivationParams;
        int i = recoveryAgentUid;
        boolean shouldRecreateCurrentVersion = false;
        if (!shouldCreateSnapshot(recoveryAgentUid)) {
            shouldRecreateCurrentVersion = this.mRecoverableKeyStoreDb.getSnapshotVersion(this.mUserId, i) != null && this.mRecoverySnapshotStorage.get(i) == null;
            if (shouldRecreateCurrentVersion) {
                Log.d(TAG, "Recreating most recent snapshot");
            } else {
                Log.d(TAG, "Key sync not needed.");
                return;
            }
        }
        boolean shouldRecreateCurrentVersion2 = shouldRecreateCurrentVersion;
        String rootCertAlias = this.mTestOnlyInsecureCertificateHelper.getDefaultCertificateAliasIfEmpty(this.mRecoverableKeyStoreDb.getActiveRootOfTrust(this.mUserId, i));
        CertPath certPath = this.mRecoverableKeyStoreDb.getRecoveryServiceCertPath(this.mUserId, i, rootCertAlias);
        if (certPath != null) {
            Log.d(TAG, "Using the public key in stored CertPath for syncing");
            publicKey = ((Certificate) certPath.getCertificates().get(0)).getPublicKey();
        } else {
            Log.d(TAG, "Using the stored raw public key for syncing");
            publicKey = this.mRecoverableKeyStoreDb.getRecoveryServicePublicKey(this.mUserId, i);
        }
        PublicKey publicKey2 = publicKey;
        if (publicKey2 == null) {
            Log.w(TAG, "Not initialized for KeySync: no public key set. Cancelling task.");
            return;
        }
        byte[] vaultHandle = this.mRecoverableKeyStoreDb.getServerParams(this.mUserId, i);
        if (vaultHandle == null) {
            Log.w(TAG, "No device ID set for user " + this.mUserId);
            return;
        }
        if (this.mTestOnlyInsecureCertificateHelper.isTestOnlyCertificateAlias(rootCertAlias)) {
            Log.w(TAG, "Insecure root certificate is used by recovery agent " + i);
            if (this.mTestOnlyInsecureCertificateHelper.doesCredentialSupportInsecureMode(this.mCredentialType, this.mCredential)) {
                Log.w(TAG, "Whitelisted credential is used to generate snapshot by recovery agent " + i);
            } else {
                Log.w(TAG, "Non whitelisted credential is used to generate recovery snapshot by " + i + " - ignore attempt.");
                return;
            }
        }
        boolean useScryptToHashCredential = shouldUseScryptToHashCredential();
        byte[] salt = generateSalt();
        if (useScryptToHashCredential) {
            localLskfHash = hashCredentialsByScrypt(salt, this.mCredential);
        } else {
            localLskfHash = hashCredentialsBySaltedSha256(salt, this.mCredential);
        }
        byte[] localLskfHash2 = localLskfHash;
        try {
            Map<String, SecretKey> rawKeys = getKeysToSync(recoveryAgentUid);
            if (this.mTestOnlyInsecureCertificateHelper.isTestOnlyCertificateAlias(rootCertAlias)) {
                rawKeys = this.mTestOnlyInsecureCertificateHelper.keepOnlyWhitelistedInsecureKeys(rawKeys);
            }
            Map<String, SecretKey> rawKeys2 = rawKeys;
            try {
                SecretKey recoveryKey = generateRecoveryKey();
                try {
                    Map<String, byte[]> encryptedApplicationKeys = KeySyncUtils.encryptKeysWithRecoveryKey(recoveryKey, rawKeys2);
                    if (this.mCredentialUpdated) {
                        String str = rootCertAlias;
                        counterId = Long.valueOf(generateAndStoreCounterId(recoveryAgentUid));
                    } else {
                        counterId = this.mRecoverableKeyStoreDb.getCounterId(this.mUserId, i);
                        if (counterId == null) {
                            counterId = Long.valueOf(generateAndStoreCounterId(recoveryAgentUid));
                        }
                    }
                    Long counterId2 = counterId;
                    CertPath certPath2 = certPath;
                    byte[] vaultParams = KeySyncUtils.packVaultParams(publicKey2, counterId2.longValue(), 10, vaultHandle);
                    try {
                        byte[] encryptedRecoveryKey = KeySyncUtils.thmEncryptRecoveryKey(publicKey2, localLskfHash2, vaultParams, recoveryKey);
                        if (useScryptToHashCredential) {
                            keyDerivationParams = KeyDerivationParams.createScryptParams(salt, 4096);
                        } else {
                            keyDerivationParams = KeyDerivationParams.createSha256Params(salt);
                        }
                        byte[] bArr = vaultParams;
                        PublicKey publicKey3 = publicKey2;
                        KeyDerivationParams keyDerivationParams2 = keyDerivationParams;
                        KeyDerivationParams keyDerivationParams3 = keyDerivationParams2;
                        KeyChainProtectionParams keyChainProtectionParams = new KeyChainProtectionParams.Builder().setUserSecretType(100).setLockScreenUiFormat(getUiFormat(this.mCredentialType, this.mCredential)).setKeyDerivationParams(keyDerivationParams2).setSecret(new byte[0]).build();
                        ArrayList arrayList = new ArrayList();
                        arrayList.add(keyChainProtectionParams);
                        KeyChainProtectionParams keyChainProtectionParams2 = keyChainProtectionParams;
                        boolean z = useScryptToHashCredential;
                        byte[] bArr2 = salt;
                        KeyChainSnapshot.Builder keyChainSnapshotBuilder = new KeyChainSnapshot.Builder().setSnapshotVersion(getSnapshotVersion(i, shouldRecreateCurrentVersion2)).setMaxAttempts(10).setCounterId(counterId2.longValue()).setServerParams(vaultHandle).setKeyChainProtectionParams(arrayList).setWrappedApplicationKeys(createApplicationKeyEntries(encryptedApplicationKeys)).setEncryptedRecoveryKeyBlob(encryptedRecoveryKey);
                        try {
                            keyChainSnapshotBuilder.setTrustedHardwareCertPath(certPath2);
                            this.mRecoverySnapshotStorage.put(i, keyChainSnapshotBuilder.build());
                            this.mSnapshotListenersStorage.recoverySnapshotAvailable(i);
                            this.mRecoverableKeyStoreDb.setShouldCreateSnapshot(this.mUserId, i, false);
                        } catch (CertificateException e) {
                            CertificateException certificateException = e;
                            Log.wtf(TAG, "Cannot serialize CertPath when calling setTrustedHardwareCertPath", e);
                        }
                    } catch (NoSuchAlgorithmException e2) {
                        byte[] bArr3 = vaultParams;
                        PublicKey publicKey4 = publicKey2;
                        boolean z2 = useScryptToHashCredential;
                        byte[] bArr4 = salt;
                        CertPath certPath3 = certPath2;
                        NoSuchAlgorithmException noSuchAlgorithmException = e2;
                        Log.wtf(TAG, "SecureBox encrypt algorithms unavailable", e2);
                    } catch (InvalidKeyException e3) {
                        byte[] bArr5 = vaultParams;
                        PublicKey publicKey5 = publicKey2;
                        boolean z3 = useScryptToHashCredential;
                        byte[] bArr6 = salt;
                        CertPath certPath4 = certPath2;
                        InvalidKeyException invalidKeyException = e3;
                        Log.e(TAG, "Could not encrypt with recovery key", e3);
                    }
                } catch (InvalidKeyException | NoSuchAlgorithmException e4) {
                    String str2 = rootCertAlias;
                    PublicKey publicKey6 = publicKey2;
                    boolean z4 = useScryptToHashCredential;
                    byte[] bArr7 = salt;
                    CertPath certPath5 = certPath;
                    GeneralSecurityException generalSecurityException = e4;
                    Log.wtf(TAG, "Should be impossible: could not encrypt application keys with random key", e4);
                }
            } catch (NoSuchAlgorithmException e5) {
                String str3 = rootCertAlias;
                PublicKey publicKey7 = publicKey2;
                boolean z5 = useScryptToHashCredential;
                byte[] bArr8 = salt;
                CertPath certPath6 = certPath;
                NoSuchAlgorithmException noSuchAlgorithmException2 = e5;
                Log.wtf("AES should never be unavailable", e5);
            }
        } catch (GeneralSecurityException e6) {
            String str4 = rootCertAlias;
            PublicKey publicKey8 = publicKey2;
            boolean z6 = useScryptToHashCredential;
            byte[] bArr9 = salt;
            CertPath certPath7 = certPath;
            GeneralSecurityException generalSecurityException2 = e6;
            Log.e(TAG, "Failed to load recoverable keys for sync", e6);
        } catch (InsecureUserException e7) {
            String str5 = rootCertAlias;
            PublicKey publicKey9 = publicKey2;
            boolean z7 = useScryptToHashCredential;
            byte[] bArr10 = salt;
            CertPath certPath8 = certPath;
            InsecureUserException insecureUserException = e7;
            Log.e(TAG, "A screen unlock triggered the key sync flow, so user must have lock screen. This should be impossible.", e7);
        } catch (BadPlatformKeyException e8) {
            String str6 = rootCertAlias;
            PublicKey publicKey10 = publicKey2;
            boolean z8 = useScryptToHashCredential;
            byte[] bArr11 = salt;
            CertPath certPath9 = certPath;
            BadPlatformKeyException badPlatformKeyException = e8;
            Log.e(TAG, "Loaded keys for same generation ID as platform key, so BadPlatformKeyException should be impossible.", e8);
        } catch (IOException e9) {
            String str7 = rootCertAlias;
            PublicKey publicKey11 = publicKey2;
            boolean z9 = useScryptToHashCredential;
            byte[] bArr12 = salt;
            CertPath certPath10 = certPath;
            IOException iOException = e9;
            Log.e(TAG, "Local database error.", e9);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getSnapshotVersion(int recoveryAgentUid, boolean shouldRecreateCurrentVersion) throws IOException {
        Long snapshotVersion;
        Long snapshotVersion2 = this.mRecoverableKeyStoreDb.getSnapshotVersion(this.mUserId, recoveryAgentUid);
        long j = 1;
        if (shouldRecreateCurrentVersion) {
            if (snapshotVersion2 != null) {
                j = snapshotVersion2.longValue();
            }
            snapshotVersion = Long.valueOf(j);
        } else {
            if (snapshotVersion2 != null) {
                j = 1 + snapshotVersion2.longValue();
            }
            snapshotVersion = Long.valueOf(j);
        }
        if (this.mRecoverableKeyStoreDb.setSnapshotVersion(this.mUserId, recoveryAgentUid, snapshotVersion.longValue()) >= 0) {
            return snapshotVersion.intValue();
        }
        Log.e(TAG, "Failed to set the snapshot version in the local DB.");
        throw new IOException("Failed to set the snapshot version in the local DB.");
    }

    private long generateAndStoreCounterId(int recoveryAgentUid) throws IOException {
        long counter = new SecureRandom().nextLong();
        if (this.mRecoverableKeyStoreDb.setCounterId(this.mUserId, recoveryAgentUid, counter) >= 0) {
            return counter;
        }
        Log.e(TAG, "Failed to set the snapshot version in the local DB.");
        throw new IOException("Failed to set counterId in the local DB.");
    }

    private Map<String, SecretKey> getKeysToSync(int recoveryAgentUid) throws InsecureUserException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPlatformKeyException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
        PlatformDecryptionKey decryptKey = this.mPlatformKeyManager.getDecryptKey(this.mUserId);
        return WrappedKey.unwrapKeys(decryptKey, this.mRecoverableKeyStoreDb.getAllKeys(this.mUserId, recoveryAgentUid, decryptKey.getGenerationId()));
    }

    private boolean shouldCreateSnapshot(int recoveryAgentUid) {
        if (!ArrayUtils.contains(this.mRecoverableKeyStoreDb.getRecoverySecretTypes(this.mUserId, recoveryAgentUid), 100)) {
            return false;
        }
        if (!this.mCredentialUpdated || this.mRecoverableKeyStoreDb.getSnapshotVersion(this.mUserId, recoveryAgentUid) == null) {
            return this.mRecoverableKeyStoreDb.getShouldCreateSnapshot(this.mUserId, recoveryAgentUid);
        }
        this.mRecoverableKeyStoreDb.setShouldCreateSnapshot(this.mUserId, recoveryAgentUid, true);
        return true;
    }

    @VisibleForTesting
    static int getUiFormat(int credentialType, String credential) {
        if (credentialType == 1) {
            return 3;
        }
        if (isPin(credential)) {
            return 1;
        }
        return 2;
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    @VisibleForTesting
    static boolean isPin(String credential) {
        if (credential == null) {
            return false;
        }
        int length = credential.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isDigit(credential.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @VisibleForTesting
    static byte[] hashCredentialsBySaltedSha256(byte[] salt, String credentials) {
        byte[] credentialsBytes = credentials.getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(salt.length + credentialsBytes.length + 8);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(salt.length);
        byteBuffer.put(salt);
        byteBuffer.putInt(credentialsBytes.length);
        byteBuffer.put(credentialsBytes);
        try {
            return MessageDigest.getInstance(LOCK_SCREEN_HASH_ALGORITHM).digest(byteBuffer.array());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] hashCredentialsByScrypt(byte[] salt, String credentials) {
        return this.mScrypt.scrypt(credentials.getBytes(StandardCharsets.UTF_8), salt, 4096, 8, 1, 32);
    }

    private static SecretKey generateRecoveryKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(RECOVERY_KEY_ALGORITHM);
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    private static List<WrappedApplicationKey> createApplicationKeyEntries(Map<String, byte[]> encryptedApplicationKeys) {
        ArrayList<WrappedApplicationKey> keyEntries = new ArrayList<>();
        for (String alias : encryptedApplicationKeys.keySet()) {
            keyEntries.add(new WrappedApplicationKey.Builder().setAlias(alias).setEncryptedKeyMaterial(encryptedApplicationKeys.get(alias)).build());
        }
        return keyEntries;
    }

    private boolean shouldUseScryptToHashCredential() {
        return this.mCredentialType == 2;
    }
}

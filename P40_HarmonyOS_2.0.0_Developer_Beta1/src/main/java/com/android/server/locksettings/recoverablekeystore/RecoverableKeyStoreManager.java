package com.android.server.locksettings.recoverablekeystore;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Binder;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.os.UserHandle;
import android.security.KeyStore;
import android.security.keystore.recovery.KeyChainProtectionParams;
import android.security.keystore.recovery.KeyChainSnapshot;
import android.security.keystore.recovery.RecoveryCertPath;
import android.security.keystore.recovery.WrappedApplicationKey;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.HexDump;
import com.android.internal.util.Preconditions;
import com.android.server.locksettings.recoverablekeystore.certificate.CertParsingException;
import com.android.server.locksettings.recoverablekeystore.certificate.CertUtils;
import com.android.server.locksettings.recoverablekeystore.certificate.CertValidationException;
import com.android.server.locksettings.recoverablekeystore.certificate.CertXml;
import com.android.server.locksettings.recoverablekeystore.certificate.SigXml;
import com.android.server.locksettings.recoverablekeystore.storage.ApplicationKeyStorage;
import com.android.server.locksettings.recoverablekeystore.storage.CleanupManager;
import com.android.server.locksettings.recoverablekeystore.storage.RecoverableKeyStoreDb;
import com.android.server.locksettings.recoverablekeystore.storage.RecoverySessionStorage;
import com.android.server.locksettings.recoverablekeystore.storage.RecoverySnapshotStorage;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.crypto.AEADBadTagException;

public class RecoverableKeyStoreManager {
    private static final String TAG = "RecoverableKeyStoreMgr";
    private static RecoverableKeyStoreManager mInstance;
    private final ApplicationKeyStorage mApplicationKeyStorage;
    private final CleanupManager mCleanupManager;
    private final Context mContext;
    private final RecoverableKeyStoreDb mDatabase;
    private final ExecutorService mExecutorService;
    private final RecoverySnapshotListenersStorage mListenersStorage;
    private final PlatformKeyManager mPlatformKeyManager;
    private final RecoverableKeyGenerator mRecoverableKeyGenerator;
    private final RecoverySessionStorage mRecoverySessionStorage;
    private final RecoverySnapshotStorage mSnapshotStorage;
    private final TestOnlyInsecureCertificateHelper mTestCertHelper;

    public static synchronized RecoverableKeyStoreManager getInstance(Context context, KeyStore keystore) {
        RecoverableKeyStoreManager recoverableKeyStoreManager;
        synchronized (RecoverableKeyStoreManager.class) {
            if (mInstance == null) {
                RecoverableKeyStoreDb db = RecoverableKeyStoreDb.newInstance(context);
                try {
                    PlatformKeyManager platformKeyManager = PlatformKeyManager.getInstance(context, db);
                    ApplicationKeyStorage applicationKeyStorage = ApplicationKeyStorage.getInstance(keystore);
                    RecoverySnapshotStorage snapshotStorage = RecoverySnapshotStorage.newInstance();
                    mInstance = new RecoverableKeyStoreManager(context.getApplicationContext(), db, new RecoverySessionStorage(), Executors.newSingleThreadExecutor(), snapshotStorage, new RecoverySnapshotListenersStorage(), platformKeyManager, applicationKeyStorage, new TestOnlyInsecureCertificateHelper(), CleanupManager.getInstance(context.getApplicationContext(), snapshotStorage, db, applicationKeyStorage));
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (KeyStoreException e2) {
                    throw new ServiceSpecificException(22, e2.getMessage());
                }
            }
            recoverableKeyStoreManager = mInstance;
        }
        return recoverableKeyStoreManager;
    }

    @VisibleForTesting
    RecoverableKeyStoreManager(Context context, RecoverableKeyStoreDb recoverableKeyStoreDb, RecoverySessionStorage recoverySessionStorage, ExecutorService executorService, RecoverySnapshotStorage snapshotStorage, RecoverySnapshotListenersStorage listenersStorage, PlatformKeyManager platformKeyManager, ApplicationKeyStorage applicationKeyStorage, TestOnlyInsecureCertificateHelper testOnlyInsecureCertificateHelper, CleanupManager cleanupManager) {
        this.mContext = context;
        this.mDatabase = recoverableKeyStoreDb;
        this.mRecoverySessionStorage = recoverySessionStorage;
        this.mExecutorService = executorService;
        this.mListenersStorage = listenersStorage;
        this.mSnapshotStorage = snapshotStorage;
        this.mPlatformKeyManager = platformKeyManager;
        this.mApplicationKeyStorage = applicationKeyStorage;
        this.mTestCertHelper = testOnlyInsecureCertificateHelper;
        this.mCleanupManager = cleanupManager;
        this.mCleanupManager.verifyKnownUsers();
        try {
            this.mRecoverableKeyGenerator = RecoverableKeyGenerator.newInstance(this.mDatabase);
        } catch (NoSuchAlgorithmException e) {
            Log.wtf(TAG, "AES keygen algorithm not available. AOSP must support this.", e);
            throw new ServiceSpecificException(22, e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void initRecoveryService(String rootCertificateAlias, byte[] recoveryServiceCertFile) throws RemoteException {
        CertificateEncodingException e;
        checkRecoverKeyStorePermission();
        int userId = UserHandle.getCallingUserId();
        int uid = Binder.getCallingUid();
        String rootCertificateAlias2 = this.mTestCertHelper.getDefaultCertificateAliasIfEmpty(rootCertificateAlias);
        if (this.mTestCertHelper.isValidRootCertificateAlias(rootCertificateAlias2)) {
            String activeRootAlias = this.mDatabase.getActiveRootOfTrust(userId, uid);
            if (activeRootAlias == null) {
                Log.d(TAG, "Root of trust for recovery agent + " + uid + " is assigned for the first time to " + rootCertificateAlias2);
            } else if (!activeRootAlias.equals(rootCertificateAlias2)) {
                Log.i(TAG, "Root of trust for recovery agent " + uid + " is changed to " + rootCertificateAlias2 + " from  " + activeRootAlias);
            }
            if (this.mDatabase.setActiveRootOfTrust(userId, uid, rootCertificateAlias2) >= 0) {
                try {
                    CertXml certXml = CertXml.parse(recoveryServiceCertFile);
                    long newSerial = certXml.getSerial();
                    Long oldSerial = this.mDatabase.getRecoveryServiceCertSerial(userId, uid, rootCertificateAlias2);
                    if (oldSerial == null || oldSerial.longValue() < newSerial || this.mTestCertHelper.isTestOnlyCertificateAlias(rootCertificateAlias2)) {
                        Log.i(TAG, "Updating the certificate with the new serial number " + newSerial);
                        X509Certificate rootCert = this.mTestCertHelper.getRootCertificate(rootCertificateAlias2);
                        try {
                            Log.d(TAG, "Getting and validating a random endpoint certificate");
                            CertPath certPath = certXml.getRandomEndpointCert(rootCert);
                            try {
                                Log.d(TAG, "Saving the randomly chosen endpoint certificate to database");
                                long updatedCertPathRows = this.mDatabase.setRecoveryServiceCertPath(userId, uid, rootCertificateAlias2, certPath);
                                if (updatedCertPathRows > 0) {
                                    try {
                                        if (this.mDatabase.setRecoveryServiceCertSerial(userId, uid, rootCertificateAlias2, newSerial) >= 0) {
                                            if (this.mDatabase.getSnapshotVersion(userId, uid) != null) {
                                                this.mDatabase.setShouldCreateSnapshot(userId, uid, true);
                                                Log.i(TAG, "This is a certificate change. Snapshot must be updated");
                                            } else {
                                                Log.i(TAG, "This is a certificate change. Snapshot didn't exist");
                                            }
                                            if (this.mDatabase.setCounterId(userId, uid, new SecureRandom().nextLong()) < 0) {
                                                Log.e(TAG, "Failed to set the counter id in the local DB.");
                                                return;
                                            }
                                            return;
                                        }
                                        throw new ServiceSpecificException(22, "Failed to set the certificate serial number in the local DB.");
                                    } catch (CertificateEncodingException e2) {
                                        e = e2;
                                        Log.e(TAG, "Failed to encode CertPath", e);
                                        throw new ServiceSpecificException(25, e.getMessage());
                                    }
                                } else if (updatedCertPathRows < 0) {
                                    throw new ServiceSpecificException(22, "Failed to set the certificate path in the local DB.");
                                }
                            } catch (CertificateEncodingException e3) {
                                e = e3;
                                Log.e(TAG, "Failed to encode CertPath", e);
                                throw new ServiceSpecificException(25, e.getMessage());
                            }
                        } catch (CertValidationException e4) {
                            Log.e(TAG, "Invalid endpoint cert", e4);
                            throw new ServiceSpecificException(28, e4.getMessage());
                        }
                    } else if (oldSerial.longValue() == newSerial) {
                        Log.i(TAG, "The cert file serial number is the same, so skip updating.");
                    } else {
                        Log.e(TAG, "The cert file serial number is older than the one in database.");
                        throw new ServiceSpecificException(29, "The cert file serial number is older than the one in database.");
                    }
                } catch (CertParsingException e5) {
                    Log.d(TAG, "Failed to parse the input as a cert file: " + HexDump.toHexString(recoveryServiceCertFile));
                    throw new ServiceSpecificException(25, e5.getMessage());
                }
            } else {
                throw new ServiceSpecificException(22, "Failed to set the root of trust in the local DB.");
            }
        } else {
            throw new ServiceSpecificException(28, "Invalid root certificate alias");
        }
    }

    public void initRecoveryServiceWithSigFile(String rootCertificateAlias, byte[] recoveryServiceCertFile, byte[] recoveryServiceSigFile) throws RemoteException {
        checkRecoverKeyStorePermission();
        String rootCertificateAlias2 = this.mTestCertHelper.getDefaultCertificateAliasIfEmpty(rootCertificateAlias);
        Preconditions.checkNotNull(recoveryServiceCertFile, "recoveryServiceCertFile is null");
        Preconditions.checkNotNull(recoveryServiceSigFile, "recoveryServiceSigFile is null");
        try {
            try {
                SigXml.parse(recoveryServiceSigFile).verifyFileSignature(this.mTestCertHelper.getRootCertificate(rootCertificateAlias2), recoveryServiceCertFile);
                initRecoveryService(rootCertificateAlias2, recoveryServiceCertFile);
            } catch (CertValidationException e) {
                Log.d(TAG, "The signature over the cert file is invalid. Cert: " + HexDump.toHexString(recoveryServiceCertFile) + " Sig: " + HexDump.toHexString(recoveryServiceSigFile));
                throw new ServiceSpecificException(28, e.getMessage());
            }
        } catch (CertParsingException e2) {
            Log.d(TAG, "Failed to parse the sig file: " + HexDump.toHexString(recoveryServiceSigFile));
            throw new ServiceSpecificException(25, e2.getMessage());
        }
    }

    public KeyChainSnapshot getKeyChainSnapshot() throws RemoteException {
        checkRecoverKeyStorePermission();
        KeyChainSnapshot snapshot = this.mSnapshotStorage.get(Binder.getCallingUid());
        if (snapshot != null) {
            return snapshot;
        }
        throw new ServiceSpecificException(21);
    }

    public void setSnapshotCreatedPendingIntent(PendingIntent intent) throws RemoteException {
        checkRecoverKeyStorePermission();
        this.mListenersStorage.setSnapshotListener(Binder.getCallingUid(), intent);
    }

    public void setServerParams(byte[] serverParams) throws RemoteException {
        checkRecoverKeyStorePermission();
        int userId = UserHandle.getCallingUserId();
        int uid = Binder.getCallingUid();
        byte[] currentServerParams = this.mDatabase.getServerParams(userId, uid);
        if (Arrays.equals(serverParams, currentServerParams)) {
            Log.v(TAG, "Not updating server params - same as old value.");
        } else if (this.mDatabase.setServerParams(userId, uid, serverParams) < 0) {
            throw new ServiceSpecificException(22, "Database failure trying to set server params.");
        } else if (currentServerParams == null) {
            Log.i(TAG, "Initialized server params.");
        } else if (this.mDatabase.getSnapshotVersion(userId, uid) != null) {
            this.mDatabase.setShouldCreateSnapshot(userId, uid, true);
            Log.i(TAG, "Updated server params. Snapshot must be updated");
        } else {
            Log.i(TAG, "Updated server params. Snapshot didn't exist");
        }
    }

    public void setRecoveryStatus(String alias, int status) throws RemoteException {
        checkRecoverKeyStorePermission();
        Preconditions.checkNotNull(alias, "alias is null");
        if (((long) this.mDatabase.setRecoveryStatus(Binder.getCallingUid(), alias, status)) < 0) {
            throw new ServiceSpecificException(22, "Failed to set the key recovery status in the local DB.");
        }
    }

    public Map<String, Integer> getRecoveryStatus() throws RemoteException {
        checkRecoverKeyStorePermission();
        return this.mDatabase.getStatusForAllKeys(Binder.getCallingUid());
    }

    public void setRecoverySecretTypes(int[] secretTypes) throws RemoteException {
        checkRecoverKeyStorePermission();
        Preconditions.checkNotNull(secretTypes, "secretTypes is null");
        int userId = UserHandle.getCallingUserId();
        int uid = Binder.getCallingUid();
        int[] currentSecretTypes = this.mDatabase.getRecoverySecretTypes(userId, uid);
        if (Arrays.equals(secretTypes, currentSecretTypes)) {
            Log.v(TAG, "Not updating secret types - same as old value.");
        } else if (this.mDatabase.setRecoverySecretTypes(userId, uid, secretTypes) < 0) {
            throw new ServiceSpecificException(22, "Database error trying to set secret types.");
        } else if (currentSecretTypes.length == 0) {
            Log.i(TAG, "Initialized secret types.");
        } else {
            Log.i(TAG, "Updated secret types. Snapshot pending.");
            if (this.mDatabase.getSnapshotVersion(userId, uid) != null) {
                this.mDatabase.setShouldCreateSnapshot(userId, uid, true);
                Log.i(TAG, "Updated secret types. Snapshot must be updated");
                return;
            }
            Log.i(TAG, "Updated secret types. Snapshot didn't exist");
        }
    }

    public int[] getRecoverySecretTypes() throws RemoteException {
        checkRecoverKeyStorePermission();
        return this.mDatabase.getRecoverySecretTypes(UserHandle.getCallingUserId(), Binder.getCallingUid());
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public byte[] startRecoverySession(String sessionId, byte[] verifierPublicKey, byte[] vaultParams, byte[] vaultChallenge, List<KeyChainProtectionParams> secrets) throws RemoteException {
        checkRecoverKeyStorePermission();
        int uid = Binder.getCallingUid();
        if (secrets.size() == 1) {
            try {
                PublicKey publicKey = KeySyncUtils.deserializePublicKey(verifierPublicKey);
                if (publicKeysMatch(publicKey, vaultParams)) {
                    byte[] keyClaimant = KeySyncUtils.generateKeyClaimant();
                    byte[] kfHash = secrets.get(0).getSecret();
                    this.mRecoverySessionStorage.add(uid, new RecoverySessionStorage.Entry(sessionId, kfHash, keyClaimant, vaultParams));
                    Log.i(TAG, "Received VaultParams for recovery: " + HexDump.toHexString(vaultParams));
                    try {
                        return KeySyncUtils.encryptRecoveryClaim(publicKey, vaultParams, vaultChallenge, KeySyncUtils.calculateThmKfHash(kfHash), keyClaimant);
                    } catch (NoSuchAlgorithmException e) {
                        Log.wtf(TAG, "SecureBox algorithm missing. AOSP must support this.", e);
                        throw new ServiceSpecificException(22, e.getMessage());
                    } catch (InvalidKeyException e2) {
                        throw new ServiceSpecificException(25, e2.getMessage());
                    }
                } else {
                    throw new ServiceSpecificException(28, "The public keys given in verifierPublicKey and vaultParams do not match.");
                }
            } catch (InvalidKeySpecException e3) {
                throw new ServiceSpecificException(25, e3.getMessage());
            }
        } else {
            throw new UnsupportedOperationException("Only a single KeyChainProtectionParams is supported");
        }
    }

    public byte[] startRecoverySessionWithCertPath(String sessionId, String rootCertificateAlias, RecoveryCertPath verifierCertPath, byte[] vaultParams, byte[] vaultChallenge, List<KeyChainProtectionParams> secrets) throws RemoteException {
        checkRecoverKeyStorePermission();
        String rootCertificateAlias2 = this.mTestCertHelper.getDefaultCertificateAliasIfEmpty(rootCertificateAlias);
        Preconditions.checkNotNull(sessionId, "invalid session");
        Preconditions.checkNotNull(verifierCertPath, "verifierCertPath is null");
        Preconditions.checkNotNull(vaultParams, "vaultParams is null");
        Preconditions.checkNotNull(vaultChallenge, "vaultChallenge is null");
        Preconditions.checkNotNull(secrets, "secrets is null");
        try {
            CertPath certPath = verifierCertPath.getCertPath();
            try {
                CertUtils.validateCertPath(this.mTestCertHelper.getRootCertificate(rootCertificateAlias2), certPath);
                byte[] verifierPublicKey = ((Certificate) certPath.getCertificates().get(0)).getPublicKey().getEncoded();
                if (verifierPublicKey != null) {
                    return startRecoverySession(sessionId, verifierPublicKey, vaultParams, vaultChallenge, secrets);
                }
                Log.e(TAG, "Failed to encode verifierPublicKey");
                throw new ServiceSpecificException(25, "Failed to encode verifierPublicKey");
            } catch (CertValidationException e) {
                Log.e(TAG, "Failed to validate the given cert path", e);
                throw new ServiceSpecificException(28, e.getMessage());
            }
        } catch (CertificateException e2) {
            throw new ServiceSpecificException(25, e2.getMessage());
        }
    }

    public Map<String, String> recoverKeyChainSnapshot(String sessionId, byte[] encryptedRecoveryKey, List<WrappedApplicationKey> applicationKeys) throws RemoteException {
        checkRecoverKeyStorePermission();
        int userId = UserHandle.getCallingUserId();
        int uid = Binder.getCallingUid();
        RecoverySessionStorage.Entry sessionEntry = this.mRecoverySessionStorage.get(uid, sessionId);
        if (sessionEntry != null) {
            try {
                Map<String, String> importKeyMaterials = importKeyMaterials(userId, uid, recoverApplicationKeys(decryptRecoveryKey(sessionEntry, encryptedRecoveryKey), applicationKeys));
                sessionEntry.destroy();
                this.mRecoverySessionStorage.remove(uid);
                return importKeyMaterials;
            } catch (KeyStoreException e) {
                throw new ServiceSpecificException(22, e.getMessage());
            } catch (Throwable th) {
                sessionEntry.destroy();
                this.mRecoverySessionStorage.remove(uid);
                throw th;
            }
        } else {
            throw new ServiceSpecificException(24, String.format(Locale.US, "Application uid=%d does not have pending session '%s'", Integer.valueOf(uid), sessionId));
        }
    }

    private Map<String, String> importKeyMaterials(int userId, int uid, Map<String, byte[]> keysByAlias) throws KeyStoreException {
        ArrayMap<String, String> grantAliasesByAlias = new ArrayMap<>(keysByAlias.size());
        for (String alias : keysByAlias.keySet()) {
            this.mApplicationKeyStorage.setSymmetricKeyEntry(userId, uid, alias, keysByAlias.get(alias));
            String grantAlias = getAlias(userId, uid, alias);
            Log.i(TAG, String.format(Locale.US, "Import %s -> %s", alias, grantAlias));
            grantAliasesByAlias.put(alias, grantAlias);
        }
        return grantAliasesByAlias;
    }

    private String getAlias(int userId, int uid, String alias) {
        return this.mApplicationKeyStorage.getGrantAlias(userId, uid, alias);
    }

    public void closeSession(String sessionId) throws RemoteException {
        checkRecoverKeyStorePermission();
        Preconditions.checkNotNull(sessionId, "invalid session");
        this.mRecoverySessionStorage.remove(Binder.getCallingUid(), sessionId);
    }

    public void removeKey(String alias) throws RemoteException {
        checkRecoverKeyStorePermission();
        Preconditions.checkNotNull(alias, "alias is null");
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getCallingUserId();
        if (this.mDatabase.removeKey(uid, alias)) {
            this.mDatabase.setShouldCreateSnapshot(userId, uid, true);
            this.mApplicationKeyStorage.deleteEntry(userId, uid, alias);
        }
    }

    @Deprecated
    public String generateKey(String alias) throws RemoteException {
        return generateKeyWithMetadata(alias, null);
    }

    public String generateKeyWithMetadata(String alias, byte[] metadata) throws RemoteException {
        checkRecoverKeyStorePermission();
        Preconditions.checkNotNull(alias, "alias is null");
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getCallingUserId();
        try {
            try {
                this.mApplicationKeyStorage.setSymmetricKeyEntry(userId, uid, alias, this.mRecoverableKeyGenerator.generateAndStoreKey(this.mPlatformKeyManager.getEncryptKey(userId), userId, uid, alias, metadata));
                return getAlias(userId, uid, alias);
            } catch (RecoverableKeyStorageException | InvalidKeyException | KeyStoreException e) {
                throw new ServiceSpecificException(22, e.getMessage());
            }
        } catch (NoSuchAlgorithmException e2) {
            throw new RuntimeException(e2);
        } catch (IOException | KeyStoreException | UnrecoverableKeyException e3) {
            throw new ServiceSpecificException(22, e3.getMessage());
        } catch (InsecureUserException e4) {
            throw new ServiceSpecificException(23, e4.getMessage());
        }
    }

    @Deprecated
    public String importKey(String alias, byte[] keyBytes) throws RemoteException {
        return importKeyWithMetadata(alias, keyBytes, null);
    }

    public String importKeyWithMetadata(String alias, byte[] keyBytes, byte[] metadata) throws RemoteException {
        checkRecoverKeyStorePermission();
        Preconditions.checkNotNull(alias, "alias is null");
        Preconditions.checkNotNull(keyBytes, "keyBytes is null");
        if (keyBytes.length == 32) {
            int uid = Binder.getCallingUid();
            int userId = UserHandle.getCallingUserId();
            try {
                try {
                    this.mRecoverableKeyGenerator.importKey(this.mPlatformKeyManager.getEncryptKey(userId), userId, uid, alias, keyBytes, metadata);
                    this.mApplicationKeyStorage.setSymmetricKeyEntry(userId, uid, alias, keyBytes);
                    return getAlias(userId, uid, alias);
                } catch (RecoverableKeyStorageException | InvalidKeyException | KeyStoreException e) {
                    throw new ServiceSpecificException(22, e.getMessage());
                }
            } catch (NoSuchAlgorithmException e2) {
                throw new RuntimeException(e2);
            } catch (IOException | KeyStoreException | UnrecoverableKeyException e3) {
                throw new ServiceSpecificException(22, e3.getMessage());
            } catch (InsecureUserException e4) {
                throw new ServiceSpecificException(23, e4.getMessage());
            }
        } else {
            Log.e(TAG, "The given key for import doesn't have the required length 256");
            throw new ServiceSpecificException(27, "The given key does not contain 256 bits.");
        }
    }

    public String getKey(String alias) throws RemoteException {
        checkRecoverKeyStorePermission();
        Preconditions.checkNotNull(alias, "alias is null");
        return getAlias(UserHandle.getCallingUserId(), Binder.getCallingUid(), alias);
    }

    private byte[] decryptRecoveryKey(RecoverySessionStorage.Entry sessionEntry, byte[] encryptedClaimResponse) throws RemoteException, ServiceSpecificException {
        try {
            try {
                return KeySyncUtils.decryptRecoveryKey(sessionEntry.getLskfHash(), KeySyncUtils.decryptRecoveryClaimResponse(sessionEntry.getKeyClaimant(), sessionEntry.getVaultParams(), encryptedClaimResponse));
            } catch (InvalidKeyException e) {
                Log.e(TAG, "Got InvalidKeyException during decrypting recovery key", e);
                throw new ServiceSpecificException(26, "Failed to decrypt recovery key " + e.getMessage());
            } catch (AEADBadTagException e2) {
                Log.e(TAG, "Got AEADBadTagException during decrypting recovery key", e2);
                throw new ServiceSpecificException(26, "Failed to decrypt recovery key " + e2.getMessage());
            } catch (NoSuchAlgorithmException e3) {
                throw new ServiceSpecificException(22, e3.getMessage());
            }
        } catch (InvalidKeyException e4) {
            Log.e(TAG, "Got InvalidKeyException during decrypting recovery claim response", e4);
            throw new ServiceSpecificException(26, "Failed to decrypt recovery key " + e4.getMessage());
        } catch (AEADBadTagException e5) {
            Log.e(TAG, "Got AEADBadTagException during decrypting recovery claim response", e5);
            throw new ServiceSpecificException(26, "Failed to decrypt recovery key " + e5.getMessage());
        } catch (NoSuchAlgorithmException e6) {
            throw new ServiceSpecificException(22, e6.getMessage());
        }
    }

    private Map<String, byte[]> recoverApplicationKeys(byte[] recoveryKey, List<WrappedApplicationKey> applicationKeys) throws RemoteException {
        HashMap<String, byte[]> keyMaterialByAlias = new HashMap<>();
        for (WrappedApplicationKey applicationKey : applicationKeys) {
            String alias = applicationKey.getAlias();
            try {
                keyMaterialByAlias.put(alias, KeySyncUtils.decryptApplicationKey(recoveryKey, applicationKey.getEncryptedKeyMaterial(), applicationKey.getMetadata()));
            } catch (NoSuchAlgorithmException e) {
                Log.wtf(TAG, "Missing SecureBox algorithm. AOSP required to support this.", e);
                throw new ServiceSpecificException(22, e.getMessage());
            } catch (InvalidKeyException e2) {
                Log.e(TAG, "Got InvalidKeyException during decrypting application key with alias: " + alias, e2);
                throw new ServiceSpecificException(26, "Failed to recover key with alias '" + alias + "': " + e2.getMessage());
            } catch (AEADBadTagException e3) {
                Log.e(TAG, "Got AEADBadTagException during decrypting application key with alias: " + alias, e3);
            }
        }
        if (applicationKeys.isEmpty() || !keyMaterialByAlias.isEmpty()) {
            return keyMaterialByAlias;
        }
        Log.e(TAG, "Failed to recover any of the application keys.");
        throw new ServiceSpecificException(26, "Failed to recover any of the application keys.");
    }

    public void lockScreenSecretAvailable(int storedHashType, byte[] credential, int userId) {
        try {
            this.mExecutorService.execute(KeySyncTask.newInstance(this.mContext, this.mDatabase, this.mSnapshotStorage, this.mListenersStorage, userId, storedHashType, credential, false));
        } catch (NoSuchAlgorithmException e) {
            Log.wtf(TAG, "Should never happen - algorithm unavailable for KeySync", e);
        } catch (KeyStoreException e2) {
            Log.e(TAG, "Key store error encountered during recoverable key sync", e2);
        } catch (InsecureUserException e3) {
            Log.wtf(TAG, "Impossible - insecure user, but user just entered lock screen", e3);
        }
    }

    public void lockScreenSecretChanged(int storedHashType, byte[] credential, int userId) {
        try {
            this.mExecutorService.execute(KeySyncTask.newInstance(this.mContext, this.mDatabase, this.mSnapshotStorage, this.mListenersStorage, userId, storedHashType, credential, true));
        } catch (NoSuchAlgorithmException e) {
            Log.wtf(TAG, "Should never happen - algorithm unavailable for KeySync", e);
        } catch (KeyStoreException e2) {
            Log.e(TAG, "Key store error encountered during recoverable key sync", e2);
        } catch (InsecureUserException e3) {
            Log.e(TAG, "InsecureUserException during lock screen secret update", e3);
        }
    }

    private void checkRecoverKeyStorePermission() {
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission("android.permission.RECOVER_KEYSTORE", "Caller " + Binder.getCallingUid() + " doesn't have RecoverKeyStore permission.");
        this.mCleanupManager.registerRecoveryAgent(UserHandle.getCallingUserId(), Binder.getCallingUid());
    }

    private boolean publicKeysMatch(PublicKey publicKey, byte[] vaultParams) {
        byte[] encodedPublicKey = SecureBox.encodePublicKey(publicKey);
        return Arrays.equals(encodedPublicKey, Arrays.copyOf(vaultParams, encodedPublicKey.length));
    }
}

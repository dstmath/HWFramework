package android.security;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.face.FaceManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.UserHandle;
import android.security.keymaster.ExportResult;
import android.security.keymaster.KeyCharacteristics;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterBlob;
import android.security.keymaster.KeymasterCertificateChain;
import android.security.keymaster.KeymasterDefs;
import android.security.keymaster.OperationResult;
import android.security.keystore.IKeystoreCertificateChainCallback;
import android.security.keystore.IKeystoreExportKeyCallback;
import android.security.keystore.IKeystoreKeyCharacteristicsCallback;
import android.security.keystore.IKeystoreOperationResultCallback;
import android.security.keystore.IKeystoreResponseCallback;
import android.security.keystore.IKeystoreService;
import android.security.keystore.KeyExpiredException;
import android.security.keystore.KeyNotYetValidException;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeystoreResponse;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.Log;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

public class KeyStore {
    public static final int CANNOT_ATTEST_IDS = -66;
    public static final int CONFIRMATIONUI_ABORTED = 2;
    public static final int CONFIRMATIONUI_CANCELED = 1;
    public static final int CONFIRMATIONUI_IGNORED = 4;
    public static final int CONFIRMATIONUI_OK = 0;
    public static final int CONFIRMATIONUI_OPERATION_PENDING = 3;
    public static final int CONFIRMATIONUI_SYSTEM_ERROR = 5;
    public static final int CONFIRMATIONUI_UIERROR = 65536;
    public static final int CONFIRMATIONUI_UIERROR_MALFORMED_UTF8_ENCODING = 65539;
    public static final int CONFIRMATIONUI_UIERROR_MESSAGE_TOO_LONG = 65538;
    public static final int CONFIRMATIONUI_UIERROR_MISSING_GLYPH = 65537;
    public static final int CONFIRMATIONUI_UNEXPECTED = 7;
    public static final int CONFIRMATIONUI_UNIMPLEMENTED = 6;
    public static final int FLAG_CRITICAL_TO_DEVICE_ENCRYPTION = 8;
    public static final int FLAG_ENCRYPTED = 1;
    public static final int FLAG_NONE = 0;
    public static final int FLAG_SOFTWARE = 2;
    public static final int FLAG_STRONGBOX = 16;
    public static final int HARDWARE_TYPE_UNAVAILABLE = -68;
    private static final String KEYSTORE_SERVICE_NAME = "android.security.keystore";
    public static final int KEY_ALREADY_EXISTS = 16;
    public static final int KEY_NOT_FOUND = 7;
    public static final int KEY_PERMANENTLY_INVALIDATED = 17;
    public static final int LOCKED = 2;
    @UnsupportedAppUsage
    public static final int NO_ERROR = 1;
    public static final int OP_AUTH_NEEDED = 15;
    public static final int PERMISSION_DENIED = 6;
    public static final int PROTOCOL_ERROR = 5;
    public static final int SYSTEM_ERROR = 4;
    private static final String TAG = "AndroidSecurityKeyStore";
    public static final int UID_SELF = -1;
    public static final int UNDEFINED_ACTION = 9;
    public static final int UNINITIALIZED = 3;
    public static final int VALUE_CORRUPTED = 8;
    public static final int WRONG_PASSWORD = 10;
    private IKeystoreService mBinder;
    private final Context mContext;
    private int mError = 1;
    private IBinder mToken;

    public enum State {
        UNLOCKED,
        LOCKED,
        UNINITIALIZED
    }

    private KeyStore(IKeystoreService binder) {
        this.mBinder = binder;
        this.mContext = getApplicationContext();
    }

    public void onUserLockedStateChanged(int userHandle, boolean locked) {
        try {
            if (this.mBinder != null) {
                this.mBinder.onKeyguardVisibilityChanged(locked, userHandle);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to update user locked state " + userHandle, e);
        }
    }

    @UnsupportedAppUsage
    public static Context getApplicationContext() {
        Application application = ActivityThread.currentApplication();
        if (application != null) {
            return application;
        }
        throw new IllegalStateException("Failed to obtain application Context from ActivityThread");
    }

    @UnsupportedAppUsage
    public static KeyStore getInstance() {
        IKeystoreService binder = IKeystoreService.Stub.asInterface(ServiceManager.getService(KEYSTORE_SERVICE_NAME));
        if (binder == null) {
            Log.e(TAG, "can not get keystore service.");
        }
        return new KeyStore(binder);
    }

    private synchronized IBinder getToken() {
        if (this.mToken == null) {
            this.mToken = new Binder();
        }
        return this.mToken;
    }

    @UnsupportedAppUsage
    public State state(int userId) {
        try {
            int ret = this.mBinder.getState(userId);
            if (ret == 1) {
                return State.UNLOCKED;
            }
            if (ret == 2) {
                return State.LOCKED;
            }
            if (ret == 3) {
                return State.UNINITIALIZED;
            }
            throw new AssertionError(this.mError);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            throw new AssertionError(e);
        }
    }

    @UnsupportedAppUsage
    public State state() {
        return state(UserHandle.myUserId());
    }

    public boolean isUnlocked() {
        return state() == State.UNLOCKED ? true : false;
    }

    public byte[] get(String key, int uid) {
        return get(key, uid, false);
    }

    @UnsupportedAppUsage
    public byte[] get(String key) {
        return get(key, -1);
    }

    public byte[] get(String key, int uid, boolean suppressKeyNotFoundWarning) {
        try {
            return this.mBinder.get(key != null ? key : "", uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        } catch (ServiceSpecificException e2) {
            if (!suppressKeyNotFoundWarning || e2.errorCode != 7) {
                Log.w(TAG, "KeyStore exception", e2);
            }
            return null;
        }
    }

    public byte[] get(String key, boolean suppressKeyNotFoundWarning) {
        return get(key, -1, suppressKeyNotFoundWarning);
    }

    public boolean put(String key, byte[] value, int uid, int flags) {
        return insert(key, value, uid, flags) == 1 ? true : false;
    }

    public int insert(String key, byte[] value, int uid, int flags) {
        if (value == null) {
            try {
                value = new byte[0];
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to keystore", e);
                return 4;
            }
        }
        int error = this.mBinder.insert(key, value, uid, flags);
        if (error != 16) {
            return error;
        }
        this.mBinder.del(key, uid);
        return this.mBinder.insert(key, value, uid, flags);
    }

    /* access modifiers changed from: package-private */
    public int delete2(String key, int uid) {
        try {
            return this.mBinder.del(key, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        }
    }

    public boolean delete(String key, int uid) {
        int ret = delete2(key, uid);
        return (ret == 1 || ret == 7) ? true : false;
    }

    @UnsupportedAppUsage
    public boolean delete(String key) {
        return delete(key, -1);
    }

    public boolean contains(String key, int uid) {
        try {
            if (this.mBinder == null) {
                Log.w(TAG, "KeyStore binder is null, key = " + key + ", uid = " + uid);
                return false;
            } else if (this.mBinder.exist(key, uid) == 1) {
                return true;
            } else {
                return false;
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean contains(String key) {
        return contains(key, -1);
    }

    public String[] list(String prefix, int uid) {
        try {
            return this.mBinder.list(prefix, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        } catch (ServiceSpecificException e2) {
            Log.w(TAG, "KeyStore exception", e2);
            return null;
        }
    }

    @UnsupportedAppUsage
    public int[] listUidsOfAuthBoundKeys() {
        List<String> uidsOut = new ArrayList<>();
        try {
            int rc = this.mBinder.listUidsOfAuthBoundKeys(uidsOut);
            if (rc == 1) {
                return uidsOut.stream().mapToInt($$Lambda$wddj3hVVrg0MkscpMtYt3BzY8Y.INSTANCE).toArray();
            }
            Log.w(TAG, String.format("listUidsOfAuthBoundKeys failed with error code %d", Integer.valueOf(rc)));
            return null;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        } catch (ServiceSpecificException e2) {
            Log.w(TAG, "KeyStore exception", e2);
            return null;
        }
    }

    public String[] list(String prefix) {
        return list(prefix, -1);
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public boolean reset() {
        try {
            return this.mBinder.reset() == 1 ? true : false;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean lock(int userId) {
        try {
            return this.mBinder.lock(userId) == 1 ? true : false;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean lock() {
        return lock(UserHandle.myUserId());
    }

    public boolean unlock(int userId, String password) {
        try {
            this.mError = this.mBinder.unlock(userId, password != null ? password : "");
            if (this.mError == 1) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    @UnsupportedAppUsage
    public boolean unlock(String password) {
        return unlock(UserHandle.getUserId(Process.myUid()), password);
    }

    public boolean isEmpty(int userId) {
        try {
            return this.mBinder.isEmpty(userId) != 0 ? true : false;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public boolean isEmpty() {
        return isEmpty(UserHandle.myUserId());
    }

    public String grant(String key, int uid) {
        try {
            String grantAlias = this.mBinder.grant(key, uid);
            if (grantAlias == "") {
                return null;
            }
            return grantAlias;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public boolean ungrant(String key, int uid) {
        try {
            return this.mBinder.ungrant(key, uid) == 1 ? true : false;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public long getmtime(String key, int uid) {
        try {
            long millis = this.mBinder.getmtime(key, uid);
            if (millis == -1) {
                return -1;
            }
            return 1000 * millis;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return -1;
        }
    }

    public long getmtime(String key) {
        return getmtime(key, -1);
    }

    public boolean isHardwareBacked() {
        return isHardwareBacked(KeyProperties.KEY_ALGORITHM_RSA);
    }

    public boolean isHardwareBacked(String keyType) {
        try {
            return this.mBinder.is_hardware_backed(keyType.toUpperCase(Locale.US)) == 1 ? true : false;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean clearUid(int uid) {
        try {
            return this.mBinder.clear_uid((long) uid) == 1 ? true : false;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public int getLastError() {
        return this.mError;
    }

    public boolean addRngEntropy(byte[] data, int flags) {
        KeystoreResultPromise promise = new KeystoreResultPromise();
        try {
            this.mBinder.asBinder().linkToDeath(promise, 0);
            boolean z = true;
            if (this.mBinder.addRngEntropy(promise, data, flags) == 1) {
                if (promise.getFuture().get().getErrorCode() != 1) {
                    z = false;
                }
                return z;
            }
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            return false;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        } catch (InterruptedException | ExecutionException e2) {
            Log.e(TAG, "AddRngEntropy completed with exception", e2);
            return false;
        } finally {
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
        }
    }

    /* access modifiers changed from: private */
    public class KeyCharacteristicsCallbackResult {
        private KeyCharacteristics keyCharacteristics;
        private KeystoreResponse keystoreResponse;

        public KeyCharacteristicsCallbackResult(KeystoreResponse keystoreResponse2, KeyCharacteristics keyCharacteristics2) {
            this.keystoreResponse = keystoreResponse2;
            this.keyCharacteristics = keyCharacteristics2;
        }

        public KeystoreResponse getKeystoreResponse() {
            return this.keystoreResponse;
        }

        public void setKeystoreResponse(KeystoreResponse keystoreResponse2) {
            this.keystoreResponse = keystoreResponse2;
        }

        public KeyCharacteristics getKeyCharacteristics() {
            return this.keyCharacteristics;
        }

        public void setKeyCharacteristics(KeyCharacteristics keyCharacteristics2) {
            this.keyCharacteristics = keyCharacteristics2;
        }
    }

    /* access modifiers changed from: private */
    public class KeyCharacteristicsPromise extends IKeystoreKeyCharacteristicsCallback.Stub implements IBinder.DeathRecipient {
        private final CompletableFuture<KeyCharacteristicsCallbackResult> future;

        private KeyCharacteristicsPromise() {
            this.future = new CompletableFuture<>();
        }

        @Override // android.security.keystore.IKeystoreKeyCharacteristicsCallback
        public void onFinished(KeystoreResponse keystoreResponse, KeyCharacteristics keyCharacteristics) throws RemoteException {
            Log.i(KeyStore.TAG, "keystore characteristics promise future onFinished.");
            this.future.complete(new KeyCharacteristicsCallbackResult(keystoreResponse, keyCharacteristics));
        }

        public final CompletableFuture<KeyCharacteristicsCallbackResult> getFuture() {
            return this.future;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            this.future.completeExceptionally(new RemoteException("Keystore died"));
        }
    }

    private int generateKeyInternal(String alias, KeymasterArguments args, byte[] entropy, int uid, int flags, KeyCharacteristics outCharacteristics) throws RemoteException, ExecutionException, InterruptedException {
        KeyCharacteristicsPromise promise = new KeyCharacteristicsPromise();
        try {
            this.mBinder.asBinder().linkToDeath(promise, 0);
            int error = this.mBinder.generateKey(promise, alias, args, entropy, uid, flags);
            if (error != 1) {
                Log.e(TAG, "generateKeyInternal failed on request " + error);
                return error;
            }
            KeyCharacteristicsCallbackResult result = promise.getFuture().get();
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            int error2 = result.getKeystoreResponse().getErrorCode();
            if (error2 != 1) {
                Log.e(TAG, "generateKeyInternal failed on response " + error2);
                return error2;
            }
            KeyCharacteristics characteristics = result.getKeyCharacteristics();
            if (characteristics == null) {
                Log.e(TAG, "generateKeyInternal got empty key cheractariestics " + error2);
                return 4;
            }
            outCharacteristics.shallowCopyFrom(characteristics);
            return 1;
        } finally {
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
        }
    }

    public int generateKey(String alias, KeymasterArguments args, byte[] entropy, int uid, int flags, KeyCharacteristics outCharacteristics) {
        byte[] entropy2;
        KeymasterArguments args2;
        if (entropy != null) {
            entropy2 = entropy;
        } else {
            try {
                entropy2 = new byte[0];
            } catch (RemoteException e) {
                e = e;
                Log.w(TAG, "Cannot connect to keystore", e);
                return 4;
            } catch (InterruptedException | ExecutionException e2) {
                e = e2;
                Log.e(TAG, "generateKey completed with exception", e);
                return 4;
            }
        }
        if (args != null) {
            args2 = args;
        } else {
            try {
                args2 = new KeymasterArguments();
            } catch (RemoteException e3) {
                e = e3;
                Log.w(TAG, "Cannot connect to keystore", e);
                return 4;
            } catch (InterruptedException | ExecutionException e4) {
                e = e4;
                Log.e(TAG, "generateKey completed with exception", e);
                return 4;
            }
        }
        try {
            int error = generateKeyInternal(alias, args2, entropy2, uid, flags, outCharacteristics);
            if (error != 16) {
                return error;
            }
            try {
            } catch (RemoteException e5) {
                e = e5;
                Log.w(TAG, "Cannot connect to keystore", e);
                return 4;
            } catch (InterruptedException | ExecutionException e6) {
                e = e6;
                Log.e(TAG, "generateKey completed with exception", e);
                return 4;
            }
            try {
                this.mBinder.del(alias, uid);
                return generateKeyInternal(alias, args2, entropy2, uid, flags, outCharacteristics);
            } catch (RemoteException e7) {
                e = e7;
                Log.w(TAG, "Cannot connect to keystore", e);
                return 4;
            } catch (InterruptedException | ExecutionException e8) {
                e = e8;
                Log.e(TAG, "generateKey completed with exception", e);
                return 4;
            }
        } catch (RemoteException e9) {
            e = e9;
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        } catch (InterruptedException | ExecutionException e10) {
            e = e10;
            Log.e(TAG, "generateKey completed with exception", e);
            return 4;
        }
    }

    public int generateKey(String alias, KeymasterArguments args, byte[] entropy, int flags, KeyCharacteristics outCharacteristics) {
        return generateKey(alias, args, entropy, -1, flags, outCharacteristics);
    }

    public int getKeyCharacteristics(String alias, KeymasterBlob clientId, KeymasterBlob appId, int uid, KeyCharacteristics outCharacteristics) {
        RemoteException e;
        Throwable e2;
        KeymasterBlob appId2;
        KeyCharacteristicsPromise promise = new KeyCharacteristicsPromise();
        try {
            this.mBinder.asBinder().linkToDeath(promise, 0);
            KeymasterBlob clientId2 = clientId != null ? clientId : new KeymasterBlob(new byte[0]);
            if (appId != null) {
                appId2 = appId;
            } else {
                try {
                    appId2 = new KeymasterBlob(new byte[0]);
                } catch (RemoteException e3) {
                    e = e3;
                    Log.w(TAG, "Cannot connect to keystore", e);
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return 4;
                } catch (InterruptedException | ExecutionException e4) {
                    e2 = e4;
                    try {
                        Log.e(TAG, "GetKeyCharacteristics completed with exception", e2);
                        this.mBinder.asBinder().unlinkToDeath(promise, 0);
                        return 4;
                    } catch (Throwable th) {
                        e = th;
                        this.mBinder.asBinder().unlinkToDeath(promise, 0);
                        throw e;
                    }
                }
            }
            try {
                int error = this.mBinder.getKeyCharacteristics(promise, alias, clientId2, appId2, uid);
                if (error != 1) {
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return error;
                }
                try {
                    KeyCharacteristicsCallbackResult result = promise.getFuture().get();
                    int error2 = result.getKeystoreResponse().getErrorCode();
                    if (error2 != 1) {
                        this.mBinder.asBinder().unlinkToDeath(promise, 0);
                        return error2;
                    }
                    KeyCharacteristics characteristics = result.getKeyCharacteristics();
                    if (characteristics == null) {
                        this.mBinder.asBinder().unlinkToDeath(promise, 0);
                        return 4;
                    }
                    outCharacteristics.shallowCopyFrom(characteristics);
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return 1;
                } catch (InterruptedException | ExecutionException e5) {
                    e2 = e5;
                    Log.e(TAG, "GetKeyCharacteristics completed with exception", e2);
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return 4;
                }
            } catch (RemoteException e6) {
                e = e6;
                Log.w(TAG, "Cannot connect to keystore", e);
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return 4;
            } catch (Throwable th2) {
                e = th2;
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                throw e;
            }
        } catch (RemoteException e7) {
            e = e7;
            Log.w(TAG, "Cannot connect to keystore", e);
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            return 4;
        } catch (InterruptedException | ExecutionException e8) {
            e2 = e8;
            Log.e(TAG, "GetKeyCharacteristics completed with exception", e2);
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            return 4;
        } catch (Throwable th3) {
            e = th3;
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            throw e;
        }
    }

    public int getKeyCharacteristics(String alias, KeymasterBlob clientId, KeymasterBlob appId, KeyCharacteristics outCharacteristics) {
        return getKeyCharacteristics(alias, clientId, appId, -1, outCharacteristics);
    }

    private int importKeyInternal(String alias, KeymasterArguments args, int format, byte[] keyData, int uid, int flags, KeyCharacteristics outCharacteristics) throws RemoteException, ExecutionException, InterruptedException {
        KeyCharacteristicsPromise promise = new KeyCharacteristicsPromise();
        this.mBinder.asBinder().linkToDeath(promise, 0);
        try {
            int error = this.mBinder.importKey(promise, alias, args, format, keyData, uid, flags);
            if (error != 1) {
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return error;
            }
            KeyCharacteristicsCallbackResult result = promise.getFuture().get();
            int error2 = result.getKeystoreResponse().getErrorCode();
            if (error2 != 1) {
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return error2;
            }
            KeyCharacteristics characteristics = result.getKeyCharacteristics();
            if (characteristics == null) {
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return 4;
            }
            try {
                outCharacteristics.shallowCopyFrom(characteristics);
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return 1;
            } catch (Throwable th) {
                th = th;
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            throw th;
        }
    }

    public int importKey(String alias, KeymasterArguments args, int format, byte[] keyData, int uid, int flags, KeyCharacteristics outCharacteristics) {
        try {
            int error = importKeyInternal(alias, args, format, keyData, uid, flags, outCharacteristics);
            if (error != 16) {
                return error;
            }
            this.mBinder.del(alias, uid);
            return importKeyInternal(alias, args, format, keyData, uid, flags, outCharacteristics);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        } catch (InterruptedException | ExecutionException e2) {
            Log.e(TAG, "ImportKey completed with exception", e2);
            return 4;
        }
    }

    public int importKey(String alias, KeymasterArguments args, int format, byte[] keyData, int flags, KeyCharacteristics outCharacteristics) {
        return importKey(alias, args, format, keyData, -1, flags, outCharacteristics);
    }

    private String getAlgorithmFromPKCS8(byte[] keyData) {
        try {
            return new AlgorithmId(new ObjectIdentifier(PrivateKeyInfo.getInstance(new ASN1InputStream(new ByteArrayInputStream(keyData)).readObject()).getPrivateKeyAlgorithm().getAlgorithm().getId())).getName();
        } catch (IOException e) {
            Log.e(TAG, "getAlgorithmFromPKCS8 Failed to parse key data");
            Log.e(TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    private KeymasterArguments makeLegacyArguments(String algorithm) {
        KeymasterArguments args = new KeymasterArguments();
        args.addEnum(KeymasterDefs.KM_TAG_ALGORITHM, KeyProperties.KeyAlgorithm.toKeymasterAsymmetricKeyAlgorithm(algorithm));
        args.addEnum(KeymasterDefs.KM_TAG_PURPOSE, 2);
        args.addEnum(KeymasterDefs.KM_TAG_PURPOSE, 3);
        args.addEnum(KeymasterDefs.KM_TAG_PURPOSE, 0);
        args.addEnum(KeymasterDefs.KM_TAG_PURPOSE, 1);
        args.addEnum(KeymasterDefs.KM_TAG_PADDING, 1);
        if (algorithm.equalsIgnoreCase(KeyProperties.KEY_ALGORITHM_RSA) == 1) {
            args.addEnum(KeymasterDefs.KM_TAG_PADDING, 2);
            args.addEnum(KeymasterDefs.KM_TAG_PADDING, 4);
            args.addEnum(KeymasterDefs.KM_TAG_PADDING, 5);
            args.addEnum(KeymasterDefs.KM_TAG_PADDING, 3);
        }
        args.addEnum(KeymasterDefs.KM_TAG_DIGEST, 0);
        args.addEnum(KeymasterDefs.KM_TAG_DIGEST, 1);
        args.addEnum(KeymasterDefs.KM_TAG_DIGEST, 2);
        args.addEnum(KeymasterDefs.KM_TAG_DIGEST, 3);
        args.addEnum(KeymasterDefs.KM_TAG_DIGEST, 4);
        args.addEnum(KeymasterDefs.KM_TAG_DIGEST, 5);
        args.addEnum(KeymasterDefs.KM_TAG_DIGEST, 6);
        args.addBoolean(KeymasterDefs.KM_TAG_NO_AUTH_REQUIRED);
        args.addDate(KeymasterDefs.KM_TAG_ORIGINATION_EXPIRE_DATETIME, new Date(Long.MAX_VALUE));
        args.addDate(KeymasterDefs.KM_TAG_USAGE_EXPIRE_DATETIME, new Date(Long.MAX_VALUE));
        args.addDate(KeymasterDefs.KM_TAG_ACTIVE_DATETIME, new Date(0));
        return args;
    }

    public boolean importKey(String alias, byte[] keyData, int uid, int flags) {
        String algorithm = getAlgorithmFromPKCS8(keyData);
        if (algorithm == null) {
            return false;
        }
        int result = importKey(alias, makeLegacyArguments(algorithm), 1, keyData, uid, flags, new KeyCharacteristics());
        if (result == 1) {
            return true;
        }
        Log.e(TAG, Log.getStackTraceString(new KeyStoreException(result, "legacy key import failed")));
        return false;
    }

    private int importWrappedKeyInternal(String wrappedKeyAlias, byte[] wrappedKey, String wrappingKeyAlias, byte[] maskingKey, KeymasterArguments args, long rootSid, long fingerprintSid, KeyCharacteristics outCharacteristics) throws RemoteException, ExecutionException, InterruptedException {
        KeyCharacteristicsPromise promise = new KeyCharacteristicsPromise();
        this.mBinder.asBinder().linkToDeath(promise, 0);
        try {
            int error = this.mBinder.importWrappedKey(promise, wrappedKeyAlias, wrappedKey, wrappingKeyAlias, maskingKey, args, rootSid, fingerprintSid);
            if (error != 1) {
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return error;
            }
            KeyCharacteristicsCallbackResult result = promise.getFuture().get();
            int error2 = result.getKeystoreResponse().getErrorCode();
            if (error2 != 1) {
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return error2;
            }
            KeyCharacteristics characteristics = result.getKeyCharacteristics();
            if (characteristics == null) {
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return 4;
            }
            try {
                outCharacteristics.shallowCopyFrom(characteristics);
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return 1;
            } catch (Throwable th) {
                th = th;
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            throw th;
        }
    }

    public int importWrappedKey(String wrappedKeyAlias, byte[] wrappedKey, String wrappingKeyAlias, byte[] maskingKey, KeymasterArguments args, long rootSid, long fingerprintSid, int uid, KeyCharacteristics outCharacteristics) {
        try {
            int error = importWrappedKeyInternal(wrappedKeyAlias, wrappedKey, wrappingKeyAlias, maskingKey, args, rootSid, fingerprintSid, outCharacteristics);
            if (error != 16) {
                return error;
            }
            try {
            } catch (RemoteException e) {
                e = e;
                Log.w(TAG, "Cannot connect to keystore", e);
                return 4;
            } catch (InterruptedException | ExecutionException e2) {
                e = e2;
                Log.e(TAG, "ImportWrappedKey completed with exception", e);
                return 4;
            }
            try {
                this.mBinder.del(wrappedKeyAlias, -1);
                return importWrappedKeyInternal(wrappedKeyAlias, wrappedKey, wrappingKeyAlias, maskingKey, args, rootSid, fingerprintSid, outCharacteristics);
            } catch (RemoteException e3) {
                e = e3;
                Log.w(TAG, "Cannot connect to keystore", e);
                return 4;
            } catch (InterruptedException | ExecutionException e4) {
                e = e4;
                Log.e(TAG, "ImportWrappedKey completed with exception", e);
                return 4;
            }
        } catch (RemoteException e5) {
            e = e5;
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        } catch (InterruptedException | ExecutionException e6) {
            e = e6;
            Log.e(TAG, "ImportWrappedKey completed with exception", e);
            return 4;
        }
    }

    /* access modifiers changed from: private */
    public class ExportKeyPromise extends IKeystoreExportKeyCallback.Stub implements IBinder.DeathRecipient {
        private final CompletableFuture<ExportResult> future;

        private ExportKeyPromise() {
            this.future = new CompletableFuture<>();
        }

        @Override // android.security.keystore.IKeystoreExportKeyCallback
        public void onFinished(ExportResult exportKeyResult) throws RemoteException {
            Log.i(KeyStore.TAG, "keystore export key promise future onFinished.");
            this.future.complete(exportKeyResult);
        }

        public final CompletableFuture<ExportResult> getFuture() {
            return this.future;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            this.future.completeExceptionally(new RemoteException("Keystore died"));
        }
    }

    public ExportResult exportKey(String alias, int format, KeymasterBlob clientId, KeymasterBlob appId, int uid) {
        KeymasterBlob appId2;
        ExportKeyPromise promise = new ExportKeyPromise();
        try {
            this.mBinder.asBinder().linkToDeath(promise, 0);
            KeymasterBlob clientId2 = clientId != null ? clientId : new KeymasterBlob(new byte[0]);
            if (appId != null) {
                appId2 = appId;
            } else {
                try {
                    appId2 = new KeymasterBlob(new byte[0]);
                } catch (RemoteException e) {
                    e = e;
                    Log.w(TAG, "Cannot connect to keystore", e);
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return null;
                } catch (InterruptedException | ExecutionException e2) {
                    e = e2;
                    try {
                        Log.e(TAG, "ExportKey completed with exception", e);
                        this.mBinder.asBinder().unlinkToDeath(promise, 0);
                        return null;
                    } catch (Throwable th) {
                        e = th;
                        this.mBinder.asBinder().unlinkToDeath(promise, 0);
                        throw e;
                    }
                } catch (Throwable th2) {
                    e = th2;
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    throw e;
                }
            }
            try {
                int error = this.mBinder.exportKey(promise, alias, format, clientId2, appId2, uid);
                if (error == 1) {
                    ExportResult exportResult = promise.getFuture().get();
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return exportResult;
                }
                try {
                    ExportResult exportResult2 = new ExportResult(error);
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return exportResult2;
                } catch (InterruptedException | ExecutionException e3) {
                    e = e3;
                    Log.e(TAG, "ExportKey completed with exception", e);
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return null;
                }
            } catch (RemoteException e4) {
                e = e4;
                Log.w(TAG, "Cannot connect to keystore", e);
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return null;
            }
        } catch (RemoteException e5) {
            e = e5;
            Log.w(TAG, "Cannot connect to keystore", e);
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            return null;
        } catch (InterruptedException | ExecutionException e6) {
            e = e6;
            Log.e(TAG, "ExportKey completed with exception", e);
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            return null;
        } catch (Throwable th3) {
            e = th3;
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            throw e;
        }
    }

    public ExportResult exportKey(String alias, int format, KeymasterBlob clientId, KeymasterBlob appId) {
        return exportKey(alias, format, clientId, appId, -1);
    }

    /* access modifiers changed from: private */
    public class OperationPromise extends IKeystoreOperationResultCallback.Stub implements IBinder.DeathRecipient {
        private final CompletableFuture<OperationResult> future;

        private OperationPromise() {
            this.future = new CompletableFuture<>();
        }

        @Override // android.security.keystore.IKeystoreOperationResultCallback
        public void onFinished(OperationResult operationResult) throws RemoteException {
            Log.i(KeyStore.TAG, "keystore operation promise future onFinished.");
            this.future.complete(operationResult);
        }

        public final CompletableFuture<OperationResult> getFuture() {
            return this.future;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            this.future.completeExceptionally(new RemoteException("Keystore died"));
        }
    }

    public OperationResult begin(String alias, int purpose, boolean pruneable, KeymasterArguments args, byte[] entropy, int uid) {
        byte[] entropy2;
        OperationPromise promise = new OperationPromise();
        try {
            this.mBinder.asBinder().linkToDeath(promise, 0);
            KeymasterArguments args2 = args != null ? args : new KeymasterArguments();
            if (entropy != null) {
                entropy2 = entropy;
            } else {
                try {
                    entropy2 = new byte[0];
                } catch (RemoteException e) {
                    e = e;
                    Log.w(TAG, "Cannot connect to keystore", e);
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return null;
                } catch (InterruptedException | ExecutionException e2) {
                    e = e2;
                    try {
                        Log.e(TAG, "Begin completed with exception", e);
                        this.mBinder.asBinder().unlinkToDeath(promise, 0);
                        return null;
                    } catch (Throwable th) {
                        e = th;
                        this.mBinder.asBinder().unlinkToDeath(promise, 0);
                        throw e;
                    }
                } catch (Throwable th2) {
                    e = th2;
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    throw e;
                }
            }
            try {
                int errorCode = this.mBinder.begin(promise, getToken(), alias, purpose, pruneable, args2, entropy2, uid);
                if (errorCode == 1) {
                    OperationResult operationResult = promise.getFuture().get();
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return operationResult;
                }
                try {
                    OperationResult operationResult2 = new OperationResult(errorCode);
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return operationResult2;
                } catch (InterruptedException | ExecutionException e3) {
                    e = e3;
                    Log.e(TAG, "Begin completed with exception", e);
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return null;
                }
            } catch (RemoteException e4) {
                e = e4;
                Log.w(TAG, "Cannot connect to keystore", e);
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return null;
            }
        } catch (RemoteException e5) {
            e = e5;
            Log.w(TAG, "Cannot connect to keystore", e);
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            return null;
        } catch (InterruptedException | ExecutionException e6) {
            e = e6;
            Log.e(TAG, "Begin completed with exception", e);
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            return null;
        } catch (Throwable th3) {
            e = th3;
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            throw e;
        }
    }

    public OperationResult begin(String alias, int purpose, boolean pruneable, KeymasterArguments args, byte[] entropy) {
        return begin(alias, purpose, pruneable, args != null ? args : new KeymasterArguments(), entropy != null ? entropy : new byte[0], -1);
    }

    public OperationResult update(IBinder token, KeymasterArguments arguments, byte[] input) {
        OperationPromise promise = new OperationPromise();
        try {
            this.mBinder.asBinder().linkToDeath(promise, 0);
            int errorCode = this.mBinder.update(promise, token, arguments != null ? arguments : new KeymasterArguments(), input != null ? input : new byte[0]);
            if (errorCode == 1) {
                OperationResult operationResult = promise.getFuture().get();
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return operationResult;
            }
            try {
                return new OperationResult(errorCode);
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, "Update completed with exception", e);
                return null;
            } finally {
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
            }
        } catch (RemoteException e2) {
            Log.w(TAG, "Cannot connect to keystore", e2);
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            return null;
        }
    }

    public OperationResult finish(IBinder token, KeymasterArguments arguments, byte[] signature, byte[] entropy) {
        RemoteException e;
        Throwable e2;
        byte[] entropy2;
        byte[] signature2;
        OperationPromise promise = new OperationPromise();
        try {
            this.mBinder.asBinder().linkToDeath(promise, 0);
            KeymasterArguments arguments2 = arguments != null ? arguments : new KeymasterArguments();
            if (entropy != null) {
                entropy2 = entropy;
            } else {
                try {
                    entropy2 = new byte[0];
                } catch (RemoteException e3) {
                    e = e3;
                    Log.w(TAG, "Cannot connect to keystore", e);
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return null;
                } catch (InterruptedException | ExecutionException e4) {
                    e2 = e4;
                    try {
                        Log.e(TAG, "Finish completed with exception", e2);
                        this.mBinder.asBinder().unlinkToDeath(promise, 0);
                        return null;
                    } catch (Throwable th) {
                        e = th;
                        this.mBinder.asBinder().unlinkToDeath(promise, 0);
                        throw e;
                    }
                }
            }
            if (signature != null) {
                signature2 = signature;
            } else {
                try {
                    signature2 = new byte[0];
                } catch (RemoteException e5) {
                    e = e5;
                    Log.w(TAG, "Cannot connect to keystore", e);
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return null;
                } catch (InterruptedException | ExecutionException e6) {
                    e2 = e6;
                    Log.e(TAG, "Finish completed with exception", e2);
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return null;
                } catch (Throwable th2) {
                    e = th2;
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    throw e;
                }
            }
            try {
                int errorCode = this.mBinder.finish(promise, token, arguments2, signature2, entropy2);
                if (errorCode == 1) {
                    OperationResult operationResult = promise.getFuture().get();
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return operationResult;
                }
                try {
                    OperationResult operationResult2 = new OperationResult(errorCode);
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return operationResult2;
                } catch (InterruptedException | ExecutionException e7) {
                    e2 = e7;
                    Log.e(TAG, "Finish completed with exception", e2);
                    this.mBinder.asBinder().unlinkToDeath(promise, 0);
                    return null;
                }
            } catch (RemoteException e8) {
                e = e8;
                Log.w(TAG, "Cannot connect to keystore", e);
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return null;
            } catch (Throwable th3) {
                e = th3;
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                throw e;
            }
        } catch (RemoteException e9) {
            e = e9;
            Log.w(TAG, "Cannot connect to keystore", e);
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            return null;
        } catch (InterruptedException | ExecutionException e10) {
            e2 = e10;
            Log.e(TAG, "Finish completed with exception", e2);
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            return null;
        } catch (Throwable th4) {
            e = th4;
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            throw e;
        }
    }

    public OperationResult finish(IBinder token, KeymasterArguments arguments, byte[] signature) {
        return finish(token, arguments, signature, null);
    }

    private class KeystoreResultPromise extends IKeystoreResponseCallback.Stub implements IBinder.DeathRecipient {
        private final CompletableFuture<KeystoreResponse> future;

        private KeystoreResultPromise() {
            this.future = new CompletableFuture<>();
        }

        @Override // android.security.keystore.IKeystoreResponseCallback
        public void onFinished(KeystoreResponse keystoreResponse) throws RemoteException {
            Log.i(KeyStore.TAG, "keystore result promise future onFinished.");
            this.future.complete(keystoreResponse);
        }

        public final CompletableFuture<KeystoreResponse> getFuture() {
            return this.future;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            this.future.completeExceptionally(new RemoteException("Keystore died"));
        }
    }

    public int abort(IBinder token) {
        KeystoreResultPromise promise = new KeystoreResultPromise();
        try {
            this.mBinder.asBinder().linkToDeath(promise, 0);
            int errorCode = this.mBinder.abort(promise, token);
            if (errorCode == 1) {
                return promise.getFuture().get().getErrorCode();
            }
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            return errorCode;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        } catch (InterruptedException | ExecutionException e2) {
            Log.e(TAG, "Abort completed with exception", e2);
            return 4;
        } finally {
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
        }
    }

    public int addAuthToken(byte[] authToken) {
        try {
            return this.mBinder.addAuthToken(authToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        }
    }

    public boolean onUserPasswordChanged(int userId, String newPassword) {
        if (newPassword == null) {
            newPassword = "";
        }
        try {
            return this.mBinder.onUserPasswordChanged(userId, newPassword) == 1 ? true : false;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public void onUserAdded(int userId, int parentId) {
        try {
            this.mBinder.onUserAdded(userId, parentId);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
        }
    }

    public void onUserAdded(int userId) {
        onUserAdded(userId, -1);
    }

    public void onUserRemoved(int userId) {
        try {
            this.mBinder.onUserRemoved(userId);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
        }
    }

    public boolean onUserPasswordChanged(String newPassword) {
        return onUserPasswordChanged(UserHandle.getUserId(Process.myUid()), newPassword);
    }

    /* access modifiers changed from: private */
    public class KeyAttestationCallbackResult {
        private KeymasterCertificateChain certificateChain;
        private KeystoreResponse keystoreResponse;

        public KeyAttestationCallbackResult(KeystoreResponse keystoreResponse2, KeymasterCertificateChain certificateChain2) {
            this.keystoreResponse = keystoreResponse2;
            this.certificateChain = certificateChain2;
        }

        public KeystoreResponse getKeystoreResponse() {
            return this.keystoreResponse;
        }

        public void setKeystoreResponse(KeystoreResponse keystoreResponse2) {
            this.keystoreResponse = keystoreResponse2;
        }

        public KeymasterCertificateChain getCertificateChain() {
            return this.certificateChain;
        }

        public void setCertificateChain(KeymasterCertificateChain certificateChain2) {
            this.certificateChain = certificateChain2;
        }
    }

    /* access modifiers changed from: private */
    public class CertificateChainPromise extends IKeystoreCertificateChainCallback.Stub implements IBinder.DeathRecipient {
        private final CompletableFuture<KeyAttestationCallbackResult> future;

        private CertificateChainPromise() {
            this.future = new CompletableFuture<>();
        }

        @Override // android.security.keystore.IKeystoreCertificateChainCallback
        public void onFinished(KeystoreResponse keystoreResponse, KeymasterCertificateChain certificateChain) throws RemoteException {
            Log.i(KeyStore.TAG, "keystore certificate chain future onFinished.");
            this.future.complete(new KeyAttestationCallbackResult(keystoreResponse, certificateChain));
        }

        public final CompletableFuture<KeyAttestationCallbackResult> getFuture() {
            return this.future;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            this.future.completeExceptionally(new RemoteException("Keystore died"));
        }
    }

    public int attestKey(String alias, KeymasterArguments params, KeymasterCertificateChain outChain) {
        CertificateChainPromise promise = new CertificateChainPromise();
        try {
            this.mBinder.asBinder().linkToDeath(promise, 0);
            if (params == null) {
                params = new KeymasterArguments();
            }
            if (outChain == null) {
                outChain = new KeymasterCertificateChain();
            }
            int error = this.mBinder.attestKey(promise, alias, params);
            if (error != 1) {
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return error;
            }
            try {
                KeyAttestationCallbackResult result = promise.getFuture().get();
                int error2 = result.getKeystoreResponse().getErrorCode();
                if (error2 == 1) {
                    outChain.shallowCopyFrom(result.getCertificateChain());
                }
                return error2;
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, "AttestKey completed with exception", e);
                return 4;
            } finally {
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
            }
        } catch (RemoteException e2) {
            Log.w(TAG, "Cannot connect to keystore", e2);
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            return 4;
        }
    }

    public int attestDeviceIds(KeymasterArguments params, KeymasterCertificateChain outChain) {
        CertificateChainPromise promise = new CertificateChainPromise();
        try {
            this.mBinder.asBinder().linkToDeath(promise, 0);
            if (params == null) {
                params = new KeymasterArguments();
            }
            if (outChain == null) {
                outChain = new KeymasterCertificateChain();
            }
            int error = this.mBinder.attestDeviceIds(promise, params);
            if (error != 1) {
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
                return error;
            }
            try {
                KeyAttestationCallbackResult result = promise.getFuture().get();
                int error2 = result.getKeystoreResponse().getErrorCode();
                if (error2 == 1) {
                    outChain.shallowCopyFrom(result.getCertificateChain());
                }
                return error2;
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, "AttestDevicdeIds completed with exception", e);
                return 4;
            } finally {
                this.mBinder.asBinder().unlinkToDeath(promise, 0);
            }
        } catch (RemoteException e2) {
            Log.w(TAG, "Cannot connect to keystore", e2);
            this.mBinder.asBinder().unlinkToDeath(promise, 0);
            return 4;
        }
    }

    public void onDeviceOffBody() {
        try {
            this.mBinder.onDeviceOffBody();
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
        }
    }

    public int presentConfirmationPrompt(IBinder listener, String promptText, byte[] extraData, String locale, int uiOptionsAsFlags) {
        try {
            return this.mBinder.presentConfirmationPrompt(listener, promptText, extraData, locale, uiOptionsAsFlags);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 5;
        }
    }

    public int cancelConfirmationPrompt(IBinder listener) {
        try {
            return this.mBinder.cancelConfirmationPrompt(listener);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 5;
        }
    }

    public boolean isConfirmationPromptSupported() {
        try {
            return this.mBinder.isConfirmationPromptSupported();
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    @UnsupportedAppUsage
    public static KeyStoreException getKeyStoreException(int errorCode) {
        if (errorCode > 0) {
            if (errorCode == 1) {
                return new KeyStoreException(errorCode, "OK");
            }
            if (errorCode == 2) {
                return new KeyStoreException(errorCode, "User authentication required");
            }
            if (errorCode == 3) {
                return new KeyStoreException(errorCode, "Keystore not initialized");
            }
            if (errorCode == 4) {
                return new KeyStoreException(errorCode, "System error");
            }
            if (errorCode == 6) {
                return new KeyStoreException(errorCode, "Permission denied");
            }
            if (errorCode == 7) {
                return new KeyStoreException(errorCode, "Key not found");
            }
            if (errorCode == 8) {
                return new KeyStoreException(errorCode, "Key blob corrupted");
            }
            if (errorCode == 15) {
                return new KeyStoreException(errorCode, "Operation requires authorization");
            }
            if (errorCode != 17) {
                return new KeyStoreException(errorCode, String.valueOf(errorCode));
            }
            return new KeyStoreException(errorCode, "Key permanently invalidated");
        } else if (errorCode != -16) {
            return new KeyStoreException(errorCode, KeymasterDefs.getErrorMessage(errorCode));
        } else {
            return new KeyStoreException(errorCode, "Invalid user authentication validity duration");
        }
    }

    public InvalidKeyException getInvalidKeyException(String keystoreKeyAlias, int uid, KeyStoreException e) {
        int errorCode = e.getErrorCode();
        if (errorCode == 2) {
            return new UserNotAuthenticatedException();
        }
        if (errorCode == 3) {
            return new KeyPermanentlyInvalidatedException();
        }
        if (errorCode != 15) {
            switch (errorCode) {
                case -26:
                    break;
                case -25:
                    return new KeyExpiredException();
                case -24:
                    return new KeyNotYetValidException();
                default:
                    return new InvalidKeyException("Keystore operation failed", e);
            }
        }
        KeyCharacteristics keyCharacteristics = new KeyCharacteristics();
        int getKeyCharacteristicsErrorCode = getKeyCharacteristics(keystoreKeyAlias, null, null, uid, keyCharacteristics);
        if (getKeyCharacteristicsErrorCode != 1) {
            return new InvalidKeyException("Failed to obtained key characteristics", getKeyStoreException(getKeyCharacteristicsErrorCode));
        }
        List<BigInteger> keySids = keyCharacteristics.getUnsignedLongs(KeymasterDefs.KM_TAG_USER_SECURE_ID);
        if (keySids.isEmpty() == 1) {
            return new KeyPermanentlyInvalidatedException();
        }
        long rootSid = GateKeeper.getSecureUserId();
        if (rootSid != 0 && keySids.contains(KeymasterArguments.toUint64(rootSid))) {
            return new UserNotAuthenticatedException();
        }
        long fingerprintOnlySid = getFingerprintOnlySid();
        if (fingerprintOnlySid != 0 && keySids.contains(KeymasterArguments.toUint64(fingerprintOnlySid))) {
            return new UserNotAuthenticatedException();
        }
        long faceOnlySid = getFaceOnlySid();
        if (faceOnlySid == 0 || !keySids.contains(KeymasterArguments.toUint64(faceOnlySid))) {
            return new KeyPermanentlyInvalidatedException();
        }
        return new UserNotAuthenticatedException();
    }

    private long getFaceOnlySid() {
        FaceManager faceManager;
        if (this.mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FACE) == 1 && (faceManager = (FaceManager) this.mContext.getSystemService(FaceManager.class)) != null) {
            return faceManager.getAuthenticatorId();
        }
        return 0;
    }

    private long getFingerprintOnlySid() {
        FingerprintManager fingerprintManager;
        if (this.mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) == 1 && (fingerprintManager = (FingerprintManager) this.mContext.getSystemService(FingerprintManager.class)) != null) {
            return fingerprintManager.getAuthenticatorId();
        }
        return 0;
    }

    public InvalidKeyException getInvalidKeyException(String keystoreKeyAlias, int uid, int errorCode) {
        return getInvalidKeyException(keystoreKeyAlias, uid, getKeyStoreException(errorCode));
    }
}

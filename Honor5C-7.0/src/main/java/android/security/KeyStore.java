package android.security;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ProxyInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.security.IKeystoreService.Stub;
import android.security.keymaster.ExportResult;
import android.security.keymaster.KeyCharacteristics;
import android.security.keymaster.KeymasterArguments;
import android.security.keymaster.KeymasterBlob;
import android.security.keymaster.KeymasterCertificateChain;
import android.security.keymaster.KeymasterDefs;
import android.security.keymaster.OperationResult;
import android.security.keystore.KeyExpiredException;
import android.security.keystore.KeyNotYetValidException;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.Log;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.Locale;

public class KeyStore {
    public static final int FLAG_ENCRYPTED = 1;
    public static final int FLAG_NONE = 0;
    public static final int KEY_NOT_FOUND = 7;
    public static final int LOCKED = 2;
    public static final int NO_ERROR = 1;
    public static final int OP_AUTH_NEEDED = 15;
    public static final int PERMISSION_DENIED = 6;
    public static final int PROTOCOL_ERROR = 5;
    public static final int SYSTEM_ERROR = 4;
    private static final String TAG = "KeyStore";
    public static final int UID_SELF = -1;
    public static final int UNDEFINED_ACTION = 9;
    public static final int UNINITIALIZED = 3;
    public static final int VALUE_CORRUPTED = 8;
    public static final int WRONG_PASSWORD = 10;
    private final IKeystoreService mBinder;
    private final Context mContext;
    private int mError;
    private IBinder mToken;

    public enum State {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.security.KeyStore.State.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.security.KeyStore.State.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.security.KeyStore.State.<clinit>():void");
        }
    }

    private KeyStore(IKeystoreService binder) {
        this.mError = NO_ERROR;
        this.mBinder = binder;
        this.mContext = getApplicationContext();
    }

    public static Context getApplicationContext() {
        Application application = ActivityThread.currentApplication();
        if (application != null) {
            return application;
        }
        throw new IllegalStateException("Failed to obtain application Context from ActivityThread");
    }

    public static KeyStore getInstance() {
        return new KeyStore(Stub.asInterface(ServiceManager.getService("android.security.keystore")));
    }

    private synchronized IBinder getToken() {
        if (this.mToken == null) {
            this.mToken = new Binder();
        }
        return this.mToken;
    }

    public State state(int userId) {
        try {
            switch (this.mBinder.getState(userId)) {
                case NO_ERROR /*1*/:
                    return State.UNLOCKED;
                case LOCKED /*2*/:
                    return State.LOCKED;
                case UNINITIALIZED /*3*/:
                    return State.UNINITIALIZED;
                default:
                    throw new AssertionError(this.mError);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            throw new AssertionError(e);
        }
    }

    public State state() {
        return state(UserHandle.myUserId());
    }

    public boolean isUnlocked() {
        return state() == State.UNLOCKED;
    }

    public byte[] get(String key, int uid) {
        try {
            return this.mBinder.get(key, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public byte[] get(String key) {
        return get(key, UID_SELF);
    }

    public boolean put(String key, byte[] value, int uid, int flags) {
        return insert(key, value, uid, flags) == NO_ERROR;
    }

    public int insert(String key, byte[] value, int uid, int flags) {
        try {
            return this.mBinder.insert(key, value, uid, flags);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return SYSTEM_ERROR;
        }
    }

    public boolean delete(String key, int uid) {
        boolean z = true;
        try {
            int ret = this.mBinder.del(key, uid);
            if (!(ret == NO_ERROR || ret == KEY_NOT_FOUND)) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean delete(String key) {
        return delete(key, UID_SELF);
    }

    public boolean contains(String key, int uid) {
        boolean z = true;
        try {
            if (this.mBinder == null) {
                Log.w(TAG, "KeyStore binder is null, key = " + key + ", uid = " + uid);
                return false;
            }
            if (this.mBinder.exist(key, uid) != NO_ERROR) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean contains(String key) {
        return contains(key, UID_SELF);
    }

    public String[] list(String prefix, int uid) {
        try {
            return this.mBinder.list(prefix, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public String[] list(String prefix) {
        return list(prefix, UID_SELF);
    }

    public boolean reset() {
        boolean z = true;
        try {
            if (this.mBinder.reset() != NO_ERROR) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean lock(int userId) {
        boolean z = true;
        try {
            if (this.mBinder.lock(userId) != NO_ERROR) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean lock() {
        return lock(UserHandle.myUserId());
    }

    public boolean unlock(int userId, String password) {
        boolean z = true;
        try {
            this.mError = this.mBinder.unlock(userId, password);
            if (this.mError != NO_ERROR) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean unlock(String password) {
        return unlock(UserHandle.getUserId(Process.myUid()), password);
    }

    public boolean isEmpty(int userId) {
        boolean z = false;
        try {
            if (this.mBinder.isEmpty(userId) != 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean isEmpty() {
        return isEmpty(UserHandle.myUserId());
    }

    public boolean generate(String key, int uid, int keyType, int keySize, int flags, byte[][] args) {
        try {
            return this.mBinder.generate(key, uid, keyType, keySize, flags, new KeystoreArguments(args)) == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean importKey(String keyName, byte[] key, int uid, int flags) {
        boolean z = true;
        try {
            if (this.mBinder.import_key(keyName, key, uid, flags) != NO_ERROR) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public byte[] sign(String key, byte[] data) {
        try {
            return this.mBinder.sign(key, data);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public boolean verify(String key, byte[] data, byte[] signature) {
        boolean z = true;
        try {
            if (this.mBinder.verify(key, data, signature) != NO_ERROR) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean grant(String key, int uid) {
        boolean z = true;
        try {
            if (this.mBinder.grant(key, uid) != NO_ERROR) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean ungrant(String key, int uid) {
        boolean z = true;
        try {
            if (this.mBinder.ungrant(key, uid) != NO_ERROR) {
                z = false;
            }
            return z;
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
        return getmtime(key, UID_SELF);
    }

    public boolean duplicate(String srcKey, int srcUid, String destKey, int destUid) {
        boolean z = true;
        try {
            if (this.mBinder.duplicate(srcKey, srcUid, destKey, destUid) != NO_ERROR) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean isHardwareBacked() {
        return isHardwareBacked(KeyProperties.KEY_ALGORITHM_RSA);
    }

    public boolean isHardwareBacked(String keyType) {
        boolean z = true;
        try {
            if (this.mBinder.is_hardware_backed(keyType.toUpperCase(Locale.US)) != NO_ERROR) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean clearUid(int uid) {
        boolean z = true;
        try {
            if (this.mBinder.clear_uid((long) uid) != NO_ERROR) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public int getLastError() {
        return this.mError;
    }

    public boolean addRngEntropy(byte[] data) {
        boolean z = true;
        try {
            if (this.mBinder.addRngEntropy(data) != NO_ERROR) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public int generateKey(String alias, KeymasterArguments args, byte[] entropy, int uid, int flags, KeyCharacteristics outCharacteristics) {
        try {
            return this.mBinder.generateKey(alias, args, entropy, uid, flags, outCharacteristics);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return SYSTEM_ERROR;
        }
    }

    public int generateKey(String alias, KeymasterArguments args, byte[] entropy, int flags, KeyCharacteristics outCharacteristics) {
        return generateKey(alias, args, entropy, UID_SELF, flags, outCharacteristics);
    }

    public int getKeyCharacteristics(String alias, KeymasterBlob clientId, KeymasterBlob appId, int uid, KeyCharacteristics outCharacteristics) {
        try {
            return this.mBinder.getKeyCharacteristics(alias, clientId, appId, uid, outCharacteristics);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return SYSTEM_ERROR;
        }
    }

    public int getKeyCharacteristics(String alias, KeymasterBlob clientId, KeymasterBlob appId, KeyCharacteristics outCharacteristics) {
        return getKeyCharacteristics(alias, clientId, appId, UID_SELF, outCharacteristics);
    }

    public int importKey(String alias, KeymasterArguments args, int format, byte[] keyData, int uid, int flags, KeyCharacteristics outCharacteristics) {
        try {
            return this.mBinder.importKey(alias, args, format, keyData, uid, flags, outCharacteristics);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return SYSTEM_ERROR;
        }
    }

    public int importKey(String alias, KeymasterArguments args, int format, byte[] keyData, int flags, KeyCharacteristics outCharacteristics) {
        return importKey(alias, args, format, keyData, UID_SELF, flags, outCharacteristics);
    }

    public ExportResult exportKey(String alias, int format, KeymasterBlob clientId, KeymasterBlob appId, int uid) {
        try {
            return this.mBinder.exportKey(alias, format, clientId, appId, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public ExportResult exportKey(String alias, int format, KeymasterBlob clientId, KeymasterBlob appId) {
        return exportKey(alias, format, clientId, appId, UID_SELF);
    }

    public OperationResult begin(String alias, int purpose, boolean pruneable, KeymasterArguments args, byte[] entropy, int uid) {
        try {
            return this.mBinder.begin(getToken(), alias, purpose, pruneable, args, entropy, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public OperationResult begin(String alias, int purpose, boolean pruneable, KeymasterArguments args, byte[] entropy) {
        return begin(alias, purpose, pruneable, args, entropy, UID_SELF);
    }

    public OperationResult update(IBinder token, KeymasterArguments arguments, byte[] input) {
        try {
            return this.mBinder.update(token, arguments, input);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public OperationResult finish(IBinder token, KeymasterArguments arguments, byte[] signature, byte[] entropy) {
        try {
            return this.mBinder.finish(token, arguments, signature, entropy);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public OperationResult finish(IBinder token, KeymasterArguments arguments, byte[] signature) {
        return finish(token, arguments, signature, null);
    }

    public int abort(IBinder token) {
        try {
            return this.mBinder.abort(token);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return SYSTEM_ERROR;
        }
    }

    public boolean isOperationAuthorized(IBinder token) {
        try {
            return this.mBinder.isOperationAuthorized(token);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public int addAuthToken(byte[] authToken) {
        try {
            return this.mBinder.addAuthToken(authToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return SYSTEM_ERROR;
        }
    }

    public boolean onUserPasswordChanged(int userId, String newPassword) {
        boolean z = true;
        if (newPassword == null) {
            newPassword = ProxyInfo.LOCAL_EXCL_LIST;
        }
        try {
            if (this.mBinder.onUserPasswordChanged(userId, newPassword) != NO_ERROR) {
                z = false;
            }
            return z;
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
        onUserAdded(userId, UID_SELF);
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

    public int attestKey(String alias, KeymasterArguments params, KeymasterCertificateChain outChain) {
        try {
            return this.mBinder.attestKey(alias, params, outChain);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return SYSTEM_ERROR;
        }
    }

    public static KeyStoreException getKeyStoreException(int errorCode) {
        if (errorCode > 0) {
            switch (errorCode) {
                case NO_ERROR /*1*/:
                    return new KeyStoreException(errorCode, "OK");
                case LOCKED /*2*/:
                    return new KeyStoreException(errorCode, "User authentication required");
                case UNINITIALIZED /*3*/:
                    return new KeyStoreException(errorCode, "Keystore not initialized");
                case SYSTEM_ERROR /*4*/:
                    return new KeyStoreException(errorCode, "System error");
                case PERMISSION_DENIED /*6*/:
                    return new KeyStoreException(errorCode, "Permission denied");
                case KEY_NOT_FOUND /*7*/:
                    return new KeyStoreException(errorCode, "Key not found");
                case VALUE_CORRUPTED /*8*/:
                    return new KeyStoreException(errorCode, "Key blob corrupted");
                case OP_AUTH_NEEDED /*15*/:
                    return new KeyStoreException(errorCode, "Operation requires authorization");
                default:
                    return new KeyStoreException(errorCode, String.valueOf(errorCode));
            }
        }
        switch (errorCode) {
            case KeymasterDefs.KM_ERROR_INVALID_AUTHORIZATION_TIMEOUT /*-16*/:
                return new KeyStoreException(errorCode, "Invalid user authentication validity duration");
            default:
                return new KeyStoreException(errorCode, KeymasterDefs.getErrorMessage(errorCode));
        }
    }

    public InvalidKeyException getInvalidKeyException(String keystoreKeyAlias, int uid, KeyStoreException e) {
        switch (e.getErrorCode()) {
            case KeymasterDefs.KM_ERROR_KEY_USER_NOT_AUTHENTICATED /*-26*/:
            case OP_AUTH_NEEDED /*15*/:
                KeyCharacteristics keyCharacteristics = new KeyCharacteristics();
                int getKeyCharacteristicsErrorCode = getKeyCharacteristics(keystoreKeyAlias, null, null, uid, keyCharacteristics);
                if (getKeyCharacteristicsErrorCode != NO_ERROR) {
                    return new InvalidKeyException("Failed to obtained key characteristics", getKeyStoreException(getKeyCharacteristicsErrorCode));
                }
                List<BigInteger> keySids = keyCharacteristics.getUnsignedLongs(KeymasterDefs.KM_TAG_USER_SECURE_ID);
                if (keySids.isEmpty()) {
                    return new KeyPermanentlyInvalidatedException();
                }
                long rootSid = GateKeeper.getSecureUserId();
                if (rootSid != 0 && keySids.contains(KeymasterArguments.toUint64(rootSid))) {
                    return new UserNotAuthenticatedException();
                }
                long fingerprintOnlySid = getFingerprintOnlySid();
                if (fingerprintOnlySid == 0 || !keySids.contains(KeymasterArguments.toUint64(fingerprintOnlySid))) {
                    return new KeyPermanentlyInvalidatedException();
                }
                return new UserNotAuthenticatedException();
            case KeymasterDefs.KM_ERROR_KEY_EXPIRED /*-25*/:
                return new KeyExpiredException();
            case KeymasterDefs.KM_ERROR_KEY_NOT_YET_VALID /*-24*/:
                return new KeyNotYetValidException();
            case LOCKED /*2*/:
                return new UserNotAuthenticatedException();
            default:
                return new InvalidKeyException("Keystore operation failed", e);
        }
    }

    private long getFingerprintOnlySid() {
        FingerprintManager fingerprintManager = (FingerprintManager) this.mContext.getSystemService(FingerprintManager.class);
        if (fingerprintManager == null) {
            return 0;
        }
        return fingerprintManager.getAuthenticatorId();
    }

    public InvalidKeyException getInvalidKeyException(String keystoreKeyAlias, int uid, int errorCode) {
        return getInvalidKeyException(keystoreKeyAlias, uid, getKeyStoreException(errorCode));
    }
}

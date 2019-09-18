package android.security;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.UserHandle;
import android.security.IKeystoreService;
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

    public static Context getApplicationContext() {
        Application application = ActivityThread.currentApplication();
        if (application != null) {
            return application;
        }
        throw new IllegalStateException("Failed to obtain application Context from ActivityThread");
    }

    public static KeyStore getInstance() {
        return new KeyStore(IKeystoreService.Stub.asInterface(ServiceManager.getService("android.security.keystore")));
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
                case 1:
                    return State.UNLOCKED;
                case 2:
                    return State.LOCKED;
                case 3:
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
            return this.mBinder.get(key != null ? key : "", uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        } catch (ServiceSpecificException e2) {
            Log.w(TAG, "KeyStore exception", e2);
            return null;
        }
    }

    public byte[] get(String key) {
        return get(key, -1);
    }

    public boolean put(String key, byte[] value, int uid, int flags) {
        return insert(key, value, uid, flags) == 1;
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
        return this.mBinder.insert(key, value, uid, flags);
    }

    public boolean delete(String key, int uid) {
        boolean z = false;
        try {
            int ret = this.mBinder.del(key, uid);
            if (ret == 1 || ret == 7) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean delete(String key) {
        return delete(key, -1);
    }

    public boolean contains(String key, int uid) {
        boolean z = false;
        try {
            if (this.mBinder == null) {
                Log.w(TAG, "KeyStore binder is null, key = " + key + ", uid = " + uid);
                return false;
            }
            if (this.mBinder.exist(key, uid) == 1) {
                z = true;
            }
            return z;
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

    public String[] list(String prefix) {
        return list(prefix, -1);
    }

    public boolean reset() {
        boolean z = false;
        try {
            if (this.mBinder.reset() == 1) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean lock(int userId) {
        boolean z = false;
        try {
            if (this.mBinder.lock(userId) == 1) {
                z = true;
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
        boolean z = false;
        try {
            this.mError = this.mBinder.unlock(userId, password != null ? password : "");
            if (this.mError == 1) {
                z = true;
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
        boolean z = false;
        try {
            if (this.mBinder.generate(key, uid, keyType, keySize, flags, new KeystoreArguments(args)) == 1) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean importKey(String keyName, byte[] key, int uid, int flags) {
        boolean z = false;
        try {
            if (this.mBinder.import_key(keyName, key, uid, flags) == 1) {
                z = true;
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
        } catch (ServiceSpecificException e2) {
            Log.w(TAG, "KeyStore exception", e2);
            return null;
        }
    }

    public boolean verify(String key, byte[] data, byte[] signature) {
        byte[] signature2;
        boolean z = false;
        if (signature != null) {
            signature2 = signature;
        } else {
            try {
                signature2 = new byte[0];
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to keystore", e);
                return false;
            } catch (ServiceSpecificException e2) {
                Log.w(TAG, "KeyStore exception", e2);
                return false;
            }
        }
        if (this.mBinder.verify(key, data, signature2) == 1) {
            z = true;
        }
        return z;
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
        boolean z = false;
        try {
            if (this.mBinder.ungrant(key, uid) == 1) {
                z = true;
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
        return getmtime(key, -1);
    }

    public boolean isHardwareBacked() {
        return isHardwareBacked(KeyProperties.KEY_ALGORITHM_RSA);
    }

    public boolean isHardwareBacked(String keyType) {
        boolean z = false;
        try {
            if (this.mBinder == null) {
                Log.w(TAG, "KeyStore binder is null, keyType = " + keyType);
                return false;
            }
            if (this.mBinder.is_hardware_backed(keyType.toUpperCase(Locale.US)) == 1) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean clearUid(int uid) {
        boolean z = false;
        try {
            if (this.mBinder.clear_uid((long) uid) == 1) {
                z = true;
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

    public boolean addRngEntropy(byte[] data, int flags) {
        boolean z = false;
        try {
            if (this.mBinder.addRngEntropy(data, flags) == 1) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
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
                KeymasterArguments keymasterArguments = args;
                byte[] bArr = entropy;
                e = e;
                Log.w(TAG, "Cannot connect to keystore", e);
                return 4;
            }
        }
        if (args != null) {
            args2 = args;
        } else {
            try {
                args2 = new KeymasterArguments();
            } catch (RemoteException e2) {
                KeymasterArguments keymasterArguments2 = args;
                e = e2;
                Log.w(TAG, "Cannot connect to keystore", e);
                return 4;
            }
        }
        try {
            return this.mBinder.generateKey(alias, args2, entropy2, uid, flags, outCharacteristics);
        } catch (RemoteException e3) {
            e = e3;
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        }
    }

    public int generateKey(String alias, KeymasterArguments args, byte[] entropy, int flags, KeyCharacteristics outCharacteristics) {
        return generateKey(alias, args, entropy, -1, flags, outCharacteristics);
    }

    public int getKeyCharacteristics(String alias, KeymasterBlob clientId, KeymasterBlob appId, int uid, KeyCharacteristics outCharacteristics) {
        KeymasterBlob clientId2;
        KeymasterBlob appId2;
        if (clientId != null) {
            clientId2 = clientId;
        } else {
            try {
                clientId2 = new KeymasterBlob(new byte[0]);
            } catch (RemoteException e) {
                KeymasterBlob keymasterBlob = clientId;
                e = e;
                Log.w(TAG, "Cannot connect to keystore", e);
                return 4;
            }
        }
        if (appId != null) {
            appId2 = appId;
        } else {
            try {
                appId2 = new KeymasterBlob(new byte[0]);
            } catch (RemoteException e2) {
                e = e2;
                Log.w(TAG, "Cannot connect to keystore", e);
                return 4;
            }
        }
        try {
            return this.mBinder.getKeyCharacteristics(alias, clientId2, appId2, uid, outCharacteristics);
        } catch (RemoteException e3) {
            e = e3;
            KeymasterBlob keymasterBlob2 = appId2;
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        }
    }

    public int getKeyCharacteristics(String alias, KeymasterBlob clientId, KeymasterBlob appId, KeyCharacteristics outCharacteristics) {
        return getKeyCharacteristics(alias, clientId, appId, -1, outCharacteristics);
    }

    public int importKey(String alias, KeymasterArguments args, int format, byte[] keyData, int uid, int flags, KeyCharacteristics outCharacteristics) {
        try {
            return this.mBinder.importKey(alias, args, format, keyData, uid, flags, outCharacteristics);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        }
    }

    public int importKey(String alias, KeymasterArguments args, int format, byte[] keyData, int flags, KeyCharacteristics outCharacteristics) {
        return importKey(alias, args, format, keyData, -1, flags, outCharacteristics);
    }

    public int importWrappedKey(String wrappedKeyAlias, byte[] wrappedKey, String wrappingKeyAlias, byte[] maskingKey, KeymasterArguments args, long rootSid, long fingerprintSid, int uid, KeyCharacteristics outCharacteristics) {
        try {
            return this.mBinder.importWrappedKey(wrappedKeyAlias, wrappedKey, wrappingKeyAlias, maskingKey, args, rootSid, fingerprintSid, outCharacteristics);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
        }
    }

    public ExportResult exportKey(String alias, int format, KeymasterBlob clientId, KeymasterBlob appId, int uid) {
        KeymasterBlob clientId2;
        KeymasterBlob appId2;
        if (clientId != null) {
            clientId2 = clientId;
        } else {
            try {
                clientId2 = new KeymasterBlob(new byte[0]);
            } catch (RemoteException e) {
                KeymasterBlob keymasterBlob = clientId;
                e = e;
                Log.w(TAG, "Cannot connect to keystore", e);
                return null;
            }
        }
        if (appId != null) {
            appId2 = appId;
        } else {
            try {
                appId2 = new KeymasterBlob(new byte[0]);
            } catch (RemoteException e2) {
                e = e2;
                Log.w(TAG, "Cannot connect to keystore", e);
                return null;
            }
        }
        try {
            return this.mBinder.exportKey(alias, format, clientId2, appId2, uid);
        } catch (RemoteException e3) {
            e = e3;
            KeymasterBlob keymasterBlob2 = appId2;
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public ExportResult exportKey(String alias, int format, KeymasterBlob clientId, KeymasterBlob appId) {
        return exportKey(alias, format, clientId, appId, -1);
    }

    public OperationResult begin(String alias, int purpose, boolean pruneable, KeymasterArguments args, byte[] entropy, int uid) {
        KeymasterArguments args2;
        byte[] entropy2;
        if (args != null) {
            args2 = args;
        } else {
            try {
                args2 = new KeymasterArguments();
            } catch (RemoteException e) {
                KeymasterArguments keymasterArguments = args;
                byte[] bArr = entropy;
                e = e;
                Log.w(TAG, "Cannot connect to keystore", e);
                return null;
            }
        }
        if (entropy != null) {
            entropy2 = entropy;
        } else {
            try {
                entropy2 = new byte[0];
            } catch (RemoteException e2) {
                e = e2;
                byte[] bArr2 = entropy;
                Log.w(TAG, "Cannot connect to keystore", e);
                return null;
            }
        }
        try {
            return this.mBinder.begin(getToken(), alias, purpose, pruneable, args2, entropy2, uid);
        } catch (RemoteException e3) {
            e = e3;
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public OperationResult begin(String alias, int purpose, boolean pruneable, KeymasterArguments args, byte[] entropy) {
        return begin(alias, purpose, pruneable, args != null ? args : new KeymasterArguments(), entropy != null ? entropy : new byte[0], -1);
    }

    public OperationResult update(IBinder token, KeymasterArguments arguments, byte[] input) {
        KeymasterArguments arguments2;
        if (arguments != null) {
            arguments2 = arguments;
        } else {
            try {
                arguments2 = new KeymasterArguments();
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to keystore", e);
                return null;
            }
        }
        return this.mBinder.update(token, arguments2, input != null ? input : new byte[0]);
    }

    public OperationResult finish(IBinder token, KeymasterArguments arguments, byte[] signature, byte[] entropy) {
        KeymasterArguments arguments2;
        if (arguments != null) {
            arguments2 = arguments;
        } else {
            try {
                arguments2 = new KeymasterArguments();
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to keystore", e);
                return null;
            }
        }
        return this.mBinder.finish(token, arguments2, signature != null ? signature : new byte[0], entropy != null ? entropy : new byte[0]);
    }

    public OperationResult finish(IBinder token, KeymasterArguments arguments, byte[] signature) {
        return finish(token, arguments, signature, null);
    }

    public int abort(IBinder token) {
        try {
            return this.mBinder.abort(token);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return 4;
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
            return 4;
        }
    }

    public boolean onUserPasswordChanged(int userId, String newPassword) {
        if (newPassword == null) {
            newPassword = "";
        }
        boolean z = false;
        try {
            if (this.mBinder.onUserPasswordChanged(userId, newPassword) == 1) {
                z = true;
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

    public int attestKey(String alias, KeymasterArguments params, KeymasterCertificateChain outChain) {
        if (params == null) {
            try {
                params = new KeymasterArguments();
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to keystore", e);
                return 4;
            }
        }
        if (outChain == null) {
            outChain = new KeymasterCertificateChain();
        }
        return this.mBinder.attestKey(alias, params, outChain);
    }

    public int attestDeviceIds(KeymasterArguments params, KeymasterCertificateChain outChain) {
        if (params == null) {
            try {
                params = new KeymasterArguments();
            } catch (RemoteException e) {
                Log.w(TAG, "Cannot connect to keystore", e);
                return 4;
            }
        }
        if (outChain == null) {
            outChain = new KeymasterCertificateChain();
        }
        return this.mBinder.attestDeviceIds(params, outChain);
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

    public static KeyStoreException getKeyStoreException(int errorCode) {
        if (errorCode > 0) {
            if (errorCode == 15) {
                return new KeyStoreException(errorCode, "Operation requires authorization");
            }
            switch (errorCode) {
                case 1:
                    return new KeyStoreException(errorCode, "OK");
                case 2:
                    return new KeyStoreException(errorCode, "User authentication required");
                case 3:
                    return new KeyStoreException(errorCode, "Keystore not initialized");
                case 4:
                    return new KeyStoreException(errorCode, "System error");
                default:
                    switch (errorCode) {
                        case 6:
                            return new KeyStoreException(errorCode, "Permission denied");
                        case 7:
                            return new KeyStoreException(errorCode, "Key not found");
                        case 8:
                            return new KeyStoreException(errorCode, "Key blob corrupted");
                        default:
                            return new KeyStoreException(errorCode, String.valueOf(errorCode));
                    }
            }
        } else if (errorCode != -16) {
            return new KeyStoreException(errorCode, KeymasterDefs.getErrorMessage(errorCode));
        } else {
            return new KeyStoreException(errorCode, "Invalid user authentication validity duration");
        }
    }

    public InvalidKeyException getInvalidKeyException(String keystoreKeyAlias, int uid, KeyStoreException e) {
        int errorCode = e.getErrorCode();
        if (errorCode != 15) {
            switch (errorCode) {
                case KeymasterDefs.KM_ERROR_KEY_USER_NOT_AUTHENTICATED:
                    break;
                case KeymasterDefs.KM_ERROR_KEY_EXPIRED:
                    return new KeyExpiredException();
                case KeymasterDefs.KM_ERROR_KEY_NOT_YET_VALID:
                    return new KeyNotYetValidException();
                default:
                    switch (errorCode) {
                        case 2:
                            return new UserNotAuthenticatedException();
                        case 3:
                            return new KeyPermanentlyInvalidatedException();
                        default:
                            return new InvalidKeyException("Keystore operation failed", e);
                    }
            }
        }
        KeyCharacteristics keyCharacteristics = new KeyCharacteristics();
        int getKeyCharacteristicsErrorCode = getKeyCharacteristics(keystoreKeyAlias, null, null, uid, keyCharacteristics);
        if (getKeyCharacteristicsErrorCode != 1) {
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
    }

    private long getFingerprintOnlySid() {
        if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.fingerprint")) {
            return 0;
        }
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

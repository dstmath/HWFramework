package com.huawei.security;

import android.app.Application;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.security.keystore.KeyExpiredException;
import android.security.keystore.KeyNotYetValidException;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.security.IHwKeystoreService;
import com.huawei.security.hwassetmanager.IHwAssetObserver;
import com.huawei.security.keymaster.HwExportResult;
import com.huawei.security.keymaster.HwKeyCharacteristics;
import com.huawei.security.keymaster.HwKeymasterArguments;
import com.huawei.security.keymaster.HwKeymasterBlob;
import com.huawei.security.keymaster.HwKeymasterCertificateChain;
import com.huawei.security.keymaster.HwKeymasterDefs;
import com.huawei.security.keymaster.HwOperationResult;
import com.huawei.security.keystore.ArrayUtils;
import com.huawei.security.keystore.HwUniversalKeyStoreException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HwKeystoreManager {
    public static final int AUTH_TEMPLATE_ID_BIND_OVERFLOW = 100002;
    public static final int AUTH_TEMPLATE_ID_BIND_REPEAT = 100001;
    public static final int AUTH_TYPE_UNSUPPORT = 100000;
    private static final String DEFAULT_CREDENTIAL = "";
    public static final int EIMA_KEY_ANTIROOT = 67;
    public static final int FLAG_ENCRYPTED = 1;
    public static final int FLAG_NONE = 0;
    public static final String HW_KEYSTORE_SDK_VERSION = "10.0.0.1";
    private static final String INTERFACE_NAME = "com.huawei.security.IHwKeystoreService";
    public static final int KEY_COUNT_OVERFLOW = 100;
    public static final int KEY_NOT_FOUND = 7;
    public static final int KM_KEY_FORMAT_X509 = 0;
    public static final int LOCKED = 2;
    private static final int NORMAL_RPC_FLAG = 0;
    public static final int NOT_SUPPORT = 101;
    public static final int NO_ERROR = 1;
    public static final int OP_AUTH_NEEDED = 15;
    public static final int PERMISSION_DENIED = 6;
    public static final int PROTOCOL_ERROR = 5;
    private static final int REMOTE_EXCEPTION_ERROR = -1;
    private static final String SHA_256_METHOD = "SHA-256";
    public static final int SYSTEM_ERROR = 4;
    private static final String TAG = "HwKeystoreManager";
    public static final int UID_SELF = -1;
    public static final int UNDEFINED_ACTION = 9;
    public static final int UNINITIALIZED = 3;
    private static final int UNLOCK_BINDER_ID = 23;
    private static final int USER_CREDENTIAL_BINDER_ID = 22;
    public static final int VALUE_CORRUPTED = 8;
    public static final int VERSION_ERROR = -1;
    public static final int WRONG_PASSWORD = 10;
    private static HwKeystoreManager sHwKeystoreManager = null;
    private final IHwKeystoreService mBinder;
    private IBinder mToken;

    public enum State {
        UNLOCKED,
        LOCKED,
        UNINITIALIZED
    }

    private HwKeystoreManager(IHwKeystoreService binder) {
        this.mBinder = binder;
    }

    public static Context getApplicationContext() {
        Application application = ActivityThreadEx.currentApplication();
        if (application != null) {
            return application;
        }
        throw new IllegalStateException("Failed to obtain application Context from ActivityThread");
    }

    public static HwKeystoreManager getInstance() {
        IHwKeystoreService binder = IHwKeystoreService.Stub.asInterface(ServiceManagerEx.getService(INTERFACE_NAME));
        if (binder == null) {
            Log.e(TAG, "getInstance IHwKeystoreService binder is null");
        }
        return new HwKeystoreManager(binder);
    }

    public static String getHwKeystoreSdkVersion() {
        return HW_KEYSTORE_SDK_VERSION;
    }

    public static HwUniversalKeyStoreException getKeyStoreException(int errorCode) {
        if (errorCode > 0) {
            if (errorCode == 1) {
                return new HwUniversalKeyStoreException(errorCode, "OK");
            }
            if (errorCode == 2) {
                return new HwUniversalKeyStoreException(errorCode, "User authentication required");
            }
            if (errorCode == 3) {
                return new HwUniversalKeyStoreException(errorCode, "Keystore not initialized");
            }
            if (errorCode == 4) {
                return new HwUniversalKeyStoreException(errorCode, "System error");
            }
            if (errorCode == 6) {
                return new HwUniversalKeyStoreException(errorCode, "Permission denied");
            }
            if (errorCode == 7) {
                return new HwUniversalKeyStoreException(errorCode, "Key not found");
            }
            if (errorCode == 8) {
                return new HwUniversalKeyStoreException(errorCode, "Key blob corrupted");
            }
            if (errorCode == 15) {
                return new HwUniversalKeyStoreException(errorCode, "Operation requires authorization");
            }
            if (errorCode == 67) {
                return new HwUniversalKeyStoreException(errorCode, "System has been rooted");
            }
            if (errorCode == 100) {
                return new HwUniversalKeyStoreException(errorCode, "Key count is overflowed");
            }
            if (errorCode == 101) {
                return new HwUniversalKeyStoreException(errorCode, "Not support HwPKI");
            }
            switch (errorCode) {
                case AUTH_TYPE_UNSUPPORT /* 100000 */:
                    return new HwUniversalKeyStoreException(errorCode, "Auth type unsupport");
                case AUTH_TEMPLATE_ID_BIND_REPEAT /* 100001 */:
                    return new HwUniversalKeyStoreException(errorCode, "Bound auth template ID repeat");
                case AUTH_TEMPLATE_ID_BIND_OVERFLOW /* 100002 */:
                    return new HwUniversalKeyStoreException(errorCode, "Bound auth template ID overflow");
                default:
                    return new HwUniversalKeyStoreException(errorCode, String.valueOf(errorCode));
            }
        } else if (errorCode != -16) {
            return new HwUniversalKeyStoreException(errorCode, HwKeymasterDefs.getErrorMessage(errorCode));
        } else {
            return new HwUniversalKeyStoreException(errorCode, "Invalid user authentication validity duration");
        }
    }

    public State state(int userId) {
        return State.UNLOCKED;
    }

    public State state() {
        return state(UserHandleEx.myUserId());
    }

    public boolean put(String key, byte[] value, int uid, int flags) {
        return insert(key, value, uid, flags) == 1;
    }

    public int insert(String key, byte[] value, int uid, int flags) {
        return set(key, new HwKeymasterBlob(value), uid);
    }

    public boolean delete(String key, int uid) {
        Log.i(TAG, "delete");
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return false;
        }
        try {
            int ret = iHwKeystoreService.del(key, uid);
            if (ret == 1 || ret == 7) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return false;
        }
    }

    public boolean delete(String key) {
        return delete(key, -1);
    }

    public boolean contains(String key, int uid) {
        if (key == null || key.isEmpty()) {
            Log.e(TAG, "contains key is null");
            return false;
        }
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return false;
        }
        try {
            if (iHwKeystoreService.contains(key) == 1) {
                Log.i(TAG, "contains return true, uid:" + uid);
                return true;
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
        }
        return false;
    }

    public boolean contains(String key) {
        return contains(key, -1);
    }

    public String[] list(String prefix, int uid) {
        return ArrayUtils.EmptyArray.STRING;
    }

    public String[] list(String prefix) {
        return list(prefix, -1);
    }

    public int getLastError() {
        return 1;
    }

    public int generateKey(String alias, HwKeymasterArguments args, byte[] entropy, int uid, int flags, HwKeyCharacteristics outCharacteristics) {
        Log.i(TAG, "generateKey");
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return 4;
        }
        try {
            return iHwKeystoreService.generateKey(alias, args, entropy, uid, flags, outCharacteristics);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return 4;
        }
    }

    public int generateKey(String alias, HwKeymasterArguments args, byte[] entropy, int flags, HwKeyCharacteristics outCharacteristics) {
        return generateKey(alias, args, entropy, -1, flags, outCharacteristics);
    }

    public int getKeyCharacteristics(String alias, HwKeymasterBlob clientId, HwKeymasterBlob appId, int uid, HwKeyCharacteristics outCharacteristics) {
        Log.i(TAG, "getKeyCharacteristics");
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return 4;
        }
        try {
            return iHwKeystoreService.getKeyCharacteristics(alias, clientId, appId, uid, outCharacteristics);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return 4;
        }
    }

    public int getKeyCharacteristics(String alias, HwKeymasterBlob clientId, HwKeymasterBlob appId, HwKeyCharacteristics outCharacteristics) {
        return getKeyCharacteristics(alias, clientId, appId, -1, outCharacteristics);
    }

    public HwExportResult exportKey(String alias, int format, HwKeymasterBlob clientId, HwKeymasterBlob appId, int uid) {
        Log.i(TAG, "exportKey");
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return null;
        }
        try {
            return iHwKeystoreService.exportKey(alias, format, clientId, appId, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return null;
        }
    }

    public HwExportResult exportKey(String alias, int format, HwKeymasterBlob clientId, HwKeymasterBlob appId) {
        return exportKey(alias, format, clientId, appId, -1);
    }

    public int importKey(String alias, HwKeymasterArguments args, HwKeymasterBlob keyData) {
        Log.i(TAG, "import key");
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.e(TAG, "mBinder is not exist.");
            return 4;
        }
        try {
            return Integer.parseInt(String.valueOf(iHwKeystoreService.getClass().getMethod("importKey", String.class, HwKeymasterArguments.class, HwKeymasterBlob.class).invoke(this.mBinder, alias, args, keyData)));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.e(TAG, "Cannot connect to HwKeystoreManager");
            return 4;
        }
    }

    public HwOperationResult begin(String alias, int purpose, boolean pruneable, HwKeymasterArguments args, byte[] entropy, int uid) {
        Log.i(TAG, "begin");
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return null;
        }
        try {
            return iHwKeystoreService.begin(getToken(), alias, purpose, pruneable, args, entropy, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return null;
        }
    }

    public HwOperationResult begin(String alias, int purpose, boolean pruneable, HwKeymasterArguments args, byte[] entropy) {
        return begin(alias, purpose, pruneable, args, entropy, -1);
    }

    public HwOperationResult update(IBinder token, HwKeymasterArguments arguments, byte[] input) {
        Log.i(TAG, "update");
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return null;
        }
        try {
            return iHwKeystoreService.update(token, arguments, input);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return null;
        }
    }

    public HwOperationResult finish(IBinder token, HwKeymasterArguments arguments, byte[] signature, byte[] entropy) {
        Log.i(TAG, "finish");
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return null;
        }
        try {
            return iHwKeystoreService.finish(token, arguments, signature, entropy);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return null;
        }
    }

    public HwOperationResult finish(IBinder token, HwKeymasterArguments arguments, byte[] signature) {
        return finish(token, arguments, signature, null);
    }

    public int abort(IBinder token) {
        Log.i(TAG, "abort");
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return 4;
        }
        try {
            return iHwKeystoreService.abort(token);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return 4;
        }
    }

    public int attestKey(String alias, int uid, HwKeymasterArguments params, HwKeymasterCertificateChain outChain) {
        Log.i(TAG, "attestKey");
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return 4;
        }
        try {
            return iHwKeystoreService.attestKey(alias, uid, params, outChain);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return 4;
        }
    }

    public int attestDeviceIds(HwKeymasterArguments params, HwKeymasterCertificateChain outChain) {
        Log.i(TAG, "attestDeviceIds() was called");
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return 4;
        }
        try {
            return iHwKeystoreService.attestDeviceIds(params, outChain);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return 4;
        }
    }

    public int assetHandleReq(HwKeymasterArguments params, HwKeymasterCertificateChain outResult) {
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return -1;
        }
        try {
            return iHwKeystoreService.assetHandleReq(params, outResult);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return -1;
        }
    }

    private synchronized IBinder getToken() {
        if (this.mToken == null) {
            this.mToken = new Binder();
        }
        return this.mToken;
    }

    public HwExportResult get(String alias, int uid) {
        if (this.mBinder == null) {
            Log.w(TAG, "mBinder is not exist.");
            return null;
        }
        try {
            Log.i(TAG, "get");
            return this.mBinder.get(alias, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return null;
        }
    }

    public int set(String alias, HwKeymasterBlob blob, int uid) {
        if (this.mBinder == null) {
            Log.w(TAG, "mBinder is not exist.");
            return 4;
        }
        try {
            Log.i(TAG, "set");
            return this.mBinder.set(alias, blob, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return 4;
        }
    }

    public String getHuksServiceVersion() {
        if (this.mBinder == null) {
            Log.w(TAG, "mBinder is not exist.");
            return "Get Huks service version failed!";
        }
        try {
            Log.i(TAG, "getHuksServiceVersion");
            return this.mBinder.getHuksServiceVersion();
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return "Get Huks service version failed!";
        }
    }

    public int exportTrustCert(HwKeymasterCertificateChain outChain) {
        if (this.mBinder == null) {
            Log.w(TAG, "mBinder is not exist.");
            return 4;
        }
        try {
            Log.i(TAG, "exportTrustCert");
            return this.mBinder.exportTrustCert(outChain);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return 4;
        }
    }

    public int setKeyProtection(String alias, HwKeymasterArguments args) {
        if (this.mBinder == null) {
            Log.w(TAG, "mBinder is not exist.");
            return 4;
        }
        try {
            Log.i(TAG, "setKeyProtection");
            return this.mBinder.setKeyProtection(alias, args);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return 4;
        }
    }

    public boolean registerObserver(IHwAssetObserver observer, String dataOwner, int event, int dataType) {
        Log.d(TAG, "registerObserver start, dataOwner:" + dataOwner + ",event:" + event + ",dataType:" + dataType);
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return false;
        }
        try {
            int result = iHwKeystoreService.registerObserver(observer, dataOwner, event, dataType);
            if (result != 1) {
                Log.e(TAG, "unRegisterObserver, error code = " + result);
                return false;
            }
            Log.d(TAG, "registerObserver end");
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "registerObserver: Cannot connect to HwKeystoreService", e);
            return false;
        }
    }

    public boolean unRegisterObserver(String dataOwner, int event, int dataType) {
        Log.d(TAG, "unRegisterObserver start");
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return false;
        }
        try {
            int result = iHwKeystoreService.unRegisterObserver(dataOwner, event, dataType);
            if (result != 1) {
                Log.e(TAG, "unRegisterObserver, error code = " + result);
                return false;
            }
            Log.d(TAG, "unRegisterObserver end");
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "unRegisterObserver: Cannot connect to HwKeystoreService", e);
            return false;
        }
    }

    public InvalidKeyException getInvalidKeyException(String keystoreKeyAlias, int uid, HwUniversalKeyStoreException e) {
        int errorCode = e.getErrorCode();
        if (errorCode == 2) {
            return new UserNotAuthenticatedException("User is not authenticated");
        }
        if (errorCode != 15) {
            switch (errorCode) {
                case HwKeymasterDefs.KM_ERROR_KEY_USER_NOT_AUTHENTICATED /* -26 */:
                    break;
                case -25:
                    return new KeyExpiredException("Key is expired");
                case HwKeymasterDefs.KM_ERROR_KEY_NOT_YET_VALID /* -24 */:
                    return new KeyNotYetValidException("Key is not valid");
                default:
                    return new InvalidKeyException("Keystore operation failed", e);
            }
        }
        HwKeyCharacteristics keyCharacteristics = new HwKeyCharacteristics();
        int getKeyCharacteristicsErrorCode = getKeyCharacteristics(keystoreKeyAlias, null, null, uid, keyCharacteristics);
        if (getKeyCharacteristicsErrorCode != 1) {
            return new InvalidKeyException("Failed to obtain key characteristics", getKeyStoreException(getKeyCharacteristicsErrorCode));
        }
        if (keyCharacteristics.getUnsignedLongs(HwKeymasterDefs.KM_TAG_USER_SECURE_ID).isEmpty()) {
            return new KeyPermanentlyInvalidatedException("key SIDs is empty.");
        }
        return new KeyPermanentlyInvalidatedException("None of the key SIDs can ever be authenticated");
    }

    public InvalidKeyException getInvalidKeyException(String keystoreKeyAlias, int uid, int errorCode) {
        return getInvalidKeyException(keystoreKeyAlias, uid, getKeyStoreException(errorCode));
    }

    public int getSecurityCapabilities(HwKeymasterArguments params, HwKeymasterCertificateChain outChain) {
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return -1;
        }
        try {
            return iHwKeystoreService.getSecurityCapabilities(params, outChain);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return -1;
        }
    }

    public int getSecurityChallenge(HwKeymasterArguments params, HwKeymasterCertificateChain outChain) {
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return -1;
        }
        try {
            return iHwKeystoreService.getSecurityChallenge(params, outChain);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return -1;
        }
    }

    public int verifySecurityChallenge(HwKeymasterArguments params) {
        IHwKeystoreService iHwKeystoreService = this.mBinder;
        if (iHwKeystoreService == null) {
            Log.w(TAG, "mBinder is not exist.");
            return -1;
        }
        try {
            if (iHwKeystoreService.verifySecurityChallenge(params) == 1) {
                return 0;
            }
            return -1;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to HwKeystoreManager", e);
            return -1;
        }
    }

    public int onUserCredentialChanged(int userId, String newCredential) {
        if (this.mBinder == null) {
            Log.w(TAG, "mBinder is not exist.");
            return -1;
        }
        String tempCredential = newCredential == null ? "" : newCredential;
        try {
            byte[] credentialBytes = new byte[0];
            if (!"".equals(tempCredential)) {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(tempCredential.getBytes(StandardCharsets.ISO_8859_1));
                credentialBytes = md.digest();
            }
            return onUserCredentialChangedInner(userId, credentialBytes);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "onUserCredentialChanged: no algorithm");
            return -1;
        }
    }

    public int unlock(int userId, String credential) {
        if (this.mBinder == null) {
            Log.w(TAG, "mBinder is not exist.");
            return -1;
        } else if (credential == null || credential.isEmpty()) {
            Log.w(TAG, "credential is not exist.");
            return 10;
        } else {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(credential.getBytes(StandardCharsets.ISO_8859_1));
                return unlockInner(userId, md.digest());
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "unlock: no algorithm");
                return -1;
            }
        }
    }

    private int onUserCredentialChangedInner(int userId, byte[] newCredential) {
        int result = 0;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(INTERFACE_NAME);
            data.writeInt(userId);
            data.writeByteArray(newCredential);
            this.mBinder.asBinder().transact(23, data, reply, 0);
            reply.readException();
            result = reply.readInt();
        } catch (RemoteException e) {
            Log.e(TAG, "onUserCredentialChangedInner RemoteException");
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    private int unlockInner(int userId, byte[] credential) {
        int result = 0;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(INTERFACE_NAME);
            data.writeInt(userId);
            data.writeByteArray(credential);
            this.mBinder.asBinder().transact(24, data, reply, 0);
            reply.readException();
            result = reply.readInt();
        } catch (RemoteException e) {
            Log.e(TAG, "unlockInner RemoteException");
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return result;
    }
}

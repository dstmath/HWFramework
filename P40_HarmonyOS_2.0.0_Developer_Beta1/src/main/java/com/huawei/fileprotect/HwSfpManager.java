package com.huawei.fileprotect;

import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.UserHandleEx;
import huawei.android.security.IHwLockStateChangeCallback;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.IHwSfpService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwSfpManager {
    private static final int DEFAULT_CAPACITY = 4;
    private static final int DEFAULT_VERSION = 2;
    private static final String EMPTY_STRING = "";
    public static final int ERROR_CODE_FAILED = -1;
    public static final int ERROR_CODE_INVALID_PARAM = -3;
    public static final int ERROR_CODE_LISTENER_NOT_REGISTERED = -6;
    public static final int ERROR_CODE_LISTENER_REGISTERED = -5;
    public static final int ERROR_CODE_OK = 0;
    public static final int ERROR_CODE_REMOTE_ERROR = -4;
    public static final int ERROR_CODE_SERVICE_NOT_FOUND = -2;
    public static final int FBE_VER_NO_2 = 2;
    public static final int FBE_VER_NO_3 = 3;
    public static final int FLAG_LOCAL_STATE = 1;
    private static final int HW_DATA_ENCRYPTION_PLUGIN_ID = 11;
    private static final Object INSTANCE_SYNC = new Object();
    public static final int LOCK_STATE_ERROR = -1;
    public static final int LOCK_STATE_LOCKED = 1;
    public static final int LOCK_STATE_UNLOCKED = 0;
    private static final String SECURITY_SERVICE = "securityserver";
    public static final int STORAGE_ECE_TYPE = 2;
    private static final int STORAGE_GET_FBE_VER = 10;
    public static final int STORAGE_SECE_TYPE = 3;
    private static final String TAG = "HwSfpManager";
    private static HwSfpManager sHwSfpManager = null;
    private static IHwSfpService sHwSfpService;
    private final Map<ILockStateChangedListener, IHwLockStateChangeCallback> mCallbackMap = new HashMap(4);

    public interface ILockStateChangedListener {
        void onLockStateChanged(int i, int i2);
    }

    private HwSfpManager() {
    }

    public static HwSfpManager getDefault() {
        HwSfpManager hwSfpManager;
        synchronized (HwSfpManager.class) {
            if (sHwSfpManager == null) {
                sHwSfpManager = new HwSfpManager();
            }
            hwSfpManager = sHwSfpManager;
        }
        return hwSfpManager;
    }

    public String getKeyDesc(int userId, int storageType) {
        IHwSfpService hwSfpService = getService();
        if (hwSfpService == null) {
            return null;
        }
        try {
            return hwSfpService.getKeyDesc(userId, storageType);
        } catch (RemoteException e) {
            Log.e(TAG, "getKeyDesc: RemoteException occurs!");
            return null;
        }
    }

    public int getFbeVersion() {
        IHwSfpService hwSfpService = getService();
        if (hwSfpService == null) {
            return 2;
        }
        String version = "";
        try {
            version = hwSfpService.getKeyDesc(0, 10);
        } catch (RemoteException e) {
            Log.e(TAG, "getFbeVersion: remote service error!");
        }
        if (TextUtils.isEmpty(version)) {
            return 2;
        }
        try {
            int versionNum = Integer.parseInt(version.trim());
            Log.i(TAG, "getFbeVersion version = " + versionNum);
            if (versionNum >= 3) {
                return versionNum;
            }
            return 2;
        } catch (NumberFormatException e2) {
            Log.e(TAG, "getFbeVersion: version number is null!");
            return 2;
        }
    }

    public List<String> getSensitiveDataPolicyList() {
        IHwSfpService hwSfpService = getService();
        if (hwSfpService == null) {
            return null;
        }
        try {
            return hwSfpService.getSensitiveDataPolicyList();
        } catch (RemoteException e) {
            Log.e(TAG, "getSensitiveDataPolicyList: RemoteException occurs!");
            return null;
        }
    }

    public int getLockState(int userId, int flag) {
        Log.i(TAG, "getLockState");
        if (flag != 1) {
            Log.w(TAG, "getLockState: input the invalid flag!");
            return -3;
        }
        IHwSfpService service = getService();
        if (service == null) {
            Log.e(TAG, "getLockState: service error!");
            return -2;
        }
        try {
            return service.getLockState(userId, flag);
        } catch (RemoteException e) {
            Log.e(TAG, "getLockState: failed!");
            return -4;
        }
    }

    public int registerLockStateChangeCallback(int flag, ILockStateChangedListener lockStateChangedListener) {
        Log.i(TAG, "registerLockStateChangeCallback");
        if (flag != 1 || lockStateChangedListener == null) {
            Log.e(TAG, "registerLockStateChangeCallback: invalid param!");
            return -3;
        }
        IHwSfpService service = getService();
        if (service == null) {
            Log.e(TAG, "registerLockStateChangeCallback: service error!");
            return -2;
        }
        try {
            return service.registerLockStateChangeCallback(flag, getOrCreateListenerStub(lockStateChangedListener));
        } catch (RemoteException e) {
            Log.e(TAG, "registerLockStateChangeCallback: RemoteException occurs!");
            return -4;
        }
    }

    public int unregisterLockStateChangeCallback(ILockStateChangedListener lockStateChangedListener) {
        Log.i(TAG, "unregisterLockStateChangeCallback");
        if (lockStateChangedListener == null) {
            Log.e(TAG, "unregisterLockStateChangeCallback: input is null!");
            return -3;
        }
        IHwSfpService service = getService();
        if (service == null) {
            Log.e(TAG, "unregisterLockStateChangeCallback: service error!");
            return -2;
        }
        synchronized (this.mCallbackMap) {
            if (!this.mCallbackMap.containsKey(lockStateChangedListener)) {
                return -6;
            }
            try {
                int errorCode = service.unregisterLockStateChangeCallback(this.mCallbackMap.get(lockStateChangedListener));
                if (errorCode != 0) {
                    return errorCode;
                }
                this.mCallbackMap.remove(lockStateChangedListener);
                return 0;
            } catch (RemoteException e) {
                Log.e(TAG, "unregisterLockStateChangeCallback: RemoteException occurs!");
                return -4;
            }
        }
    }

    public int executePolicy(String path) {
        Log.i(TAG, "executePolicy");
        IHwSfpService service = getService();
        if (service == null) {
            Log.e(TAG, "executePolicy: service error!");
            return -2;
        }
        try {
            return service.executePolicy(path, UserHandleEx.myUserId());
        } catch (RemoteException e) {
            Log.e(TAG, "executePolicy: RemoteException occurs!");
            return -4;
        }
    }

    private IHwSfpService getService() {
        synchronized (INSTANCE_SYNC) {
            if (sHwSfpService != null) {
                return sHwSfpService;
            }
            IHwSecurityService secService = IHwSecurityService.Stub.asInterface(ServiceManagerEx.getService(SECURITY_SERVICE));
            if (secService != null) {
                try {
                    sHwSfpService = IHwSfpService.Stub.asInterface(secService.querySecurityInterface(11));
                } catch (RemoteException e) {
                    Log.e(TAG, "getService: RemoteException occurs!");
                }
            }
            return sHwSfpService;
        }
    }

    private IHwLockStateChangeCallback newListener(final ILockStateChangedListener resultListener) {
        return new IHwLockStateChangeCallback.Stub() {
            /* class com.huawei.fileprotect.HwSfpManager.AnonymousClass1 */

            @Override // huawei.android.security.IHwLockStateChangeCallback
            public void onLockStateChanged(int userId, int state) throws RemoteException {
                if (resultListener != null) {
                    Log.i(HwSfpManager.TAG, "on callback");
                    resultListener.onLockStateChanged(userId, state);
                }
            }

            @Override // huawei.android.security.IHwLockStateChangeCallback
            public int innerHashCode() throws RemoteException {
                ILockStateChangedListener iLockStateChangedListener = resultListener;
                if (iLockStateChangedListener != null) {
                    return iLockStateChangedListener.hashCode();
                }
                return -1;
            }

            @Override // huawei.android.security.IHwLockStateChangeCallback
            public boolean isInnerEquals(IHwLockStateChangeCallback otherCallback) throws RemoteException {
                return equals(otherCallback);
            }

            @Override // huawei.android.security.IHwLockStateChangeCallback.Stub, android.os.IInterface
            public IBinder asBinder() {
                return this;
            }
        };
    }

    private IHwLockStateChangeCallback getOrCreateListenerStub(ILockStateChangedListener resultListener) {
        synchronized (this.mCallbackMap) {
            if (this.mCallbackMap.containsKey(resultListener)) {
                return this.mCallbackMap.get(resultListener);
            }
            IHwLockStateChangeCallback callback = newListener(resultListener);
            this.mCallbackMap.put(resultListener, callback);
            return callback;
        }
    }
}

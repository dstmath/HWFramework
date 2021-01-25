package com.huawei.fileprotect;

import android.os.IBinder;
import android.os.RemoteException;
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
    private static final int DEFAULT_MAP_SIZE = 4;
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
    private static final int HWDATAENCRYPTION_PLUGIN_ID = 11;
    public static final int LOCK_STATE_ERROR = -1;
    public static final int LOCK_STATE_LOCKED = 1;
    public static final int LOCK_STATE_UNLOCKED = 0;
    private static final String SECURITY_SERVICE = "securityserver";
    public static final int STORAGE_ECE_TYPE = 2;
    private static final int STORAGE_GET_FEB_VER = 10;
    public static final int STORAGE_SECE_TYPE = 3;
    private static final String TAG = "HwSfpManager";
    private static final Object mInstanceSync = new Object();
    private static IHwSfpService sHwSfpService;
    private static HwSfpManager sSelf = null;
    private final Map<ILockStateChangedListener, IHwLockStateChangeCallback> mCallbackMap = new HashMap(4);

    public interface ILockStateChangedListener {
        void onLockStateChanged(int i, int i2);
    }

    private HwSfpManager() {
    }

    public static HwSfpManager getDefault() {
        HwSfpManager hwSfpManager;
        synchronized (HwSfpManager.class) {
            if (sSelf == null) {
                sSelf = new HwSfpManager();
            }
            hwSfpManager = sSelf;
        }
        return hwSfpManager;
    }

    private static IHwSfpService getService() {
        synchronized (mInstanceSync) {
            if (sHwSfpService != null) {
                return sHwSfpService;
            }
            IHwSecurityService secService = IHwSecurityService.Stub.asInterface(ServiceManagerEx.getService(SECURITY_SERVICE));
            if (secService != null) {
                try {
                    sHwSfpService = IHwSfpService.Stub.asInterface(secService.querySecurityInterface(11));
                } catch (RemoteException e) {
                    Log.e(TAG, "remote service error");
                    return null;
                }
            }
            return sHwSfpService;
        }
    }

    public String getKeyDesc(int userId, int storageType) {
        if (getService() == null) {
            return null;
        }
        try {
            return sHwSfpService.getKeyDesc(userId, storageType);
        } catch (RemoteException e) {
            Log.e(TAG, "remote service error");
            return null;
        }
    }

    public int getFbeVersion() {
        if (getService() != null) {
            try {
                String version = sHwSfpService.getKeyDesc(0, 10);
                if (version == null) {
                    return 2;
                }
                Integer ver = Integer.valueOf(version.trim());
                if (ver == null) {
                    Log.e(TAG, "getFbeVersion ver is null!");
                    return 2;
                }
                Log.i(TAG, "getFbeVersion version = " + ver.intValue());
                if (ver.intValue() >= 3) {
                    return ver.intValue();
                }
                return 2;
            } catch (RemoteException | NumberFormatException e) {
                Log.e(TAG, "remote service error");
            }
        }
        return 2;
    }

    public List<String> getSensitiveDataPolicyList() {
        if (getService() == null) {
            return null;
        }
        try {
            return sHwSfpService.getSensitiveDataPolicyList();
        } catch (RemoteException e) {
            Log.e(TAG, "remote service error");
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
            return service.registerLockStateChangeCallback(flag, getListener(lockStateChangedListener));
        } catch (RemoteException e) {
            Log.e(TAG, "registerLockStateChangeCallback: failed!");
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
        try {
            int errorCode = service.unregisterLockStateChangeCallback(getListener(lockStateChangedListener));
            if (errorCode != 0) {
                return errorCode;
            }
            synchronized (this.mCallbackMap) {
                this.mCallbackMap.remove(lockStateChangedListener);
            }
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterLockStateChangeCallback: failed!");
            return -4;
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
            Log.e(TAG, "executePolicy: failed!");
            return -4;
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

    private IHwLockStateChangeCallback getListener(ILockStateChangedListener resultListener) {
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

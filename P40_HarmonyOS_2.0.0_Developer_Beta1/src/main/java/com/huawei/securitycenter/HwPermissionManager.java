package com.huawei.securitycenter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import huawei.android.security.IHwPermissionManager;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.IOnHwPermissionChangeListener;
import java.util.Map;

public class HwPermissionManager {
    private static final int ARRAY_MAP_SIZE = 16;
    private static final int HW_PERM_PLUGIN_ID = 17;
    private static final int ILLEGAL = -1;
    private static final String KEY_CALLER = "caller";
    private static final String KEY_OPERATION = "operation";
    private static final String KEY_PACKAGE_NAME = "pkgName";
    private static final String KEY_PERM_TYPE = "permType";
    private static final Object LOCK = new Object();
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "HwPermissionManager";
    private static volatile HwPermissionManager sSelf = null;
    private IHwPermissionManager mIHwPermissionManager;
    private final Map<OnHwPermissionChangeListener, IOnHwPermissionChangeListener> mPermissionListeners = new ArrayMap(16);

    public interface OnHwPermissionChangeListener {
        void onPermissionChanged(String str, String str2, long j, int i);
    }

    private HwPermissionManager() {
        Log.i(TAG, "create HwPermissionManager");
    }

    public static HwPermissionManager getInstance() {
        if (sSelf == null) {
            synchronized (HwPermissionManager.class) {
                if (sSelf == null) {
                    sSelf = new HwPermissionManager();
                }
            }
        }
        return sSelf;
    }

    private IHwPermissionManager getHwPermissionService() {
        synchronized (LOCK) {
            if (this.mIHwPermissionManager != null) {
                return this.mIHwPermissionManager;
            }
            IHwSecurityService secService = IHwSecurityService.Stub.asInterface(ServiceManagerEx.getService(SECURITY_SERVICE));
            if (secService != null) {
                try {
                    this.mIHwPermissionManager = IHwPermissionManager.Stub.asInterface(secService.querySecurityInterface(17));
                } catch (RemoteException e) {
                    Log.e(TAG, "Get HwPermissionService failed!");
                }
            }
            return this.mIHwPermissionManager;
        }
    }

    public Bundle getHwPermissionInfo(String pkgName, int userId, long permType, Bundle params) {
        if (getHwPermissionService() != null) {
            try {
                return this.mIHwPermissionManager.getHwPermissionInfo(pkgName, userId, permType, params);
            } catch (RemoteException e) {
                Log.e(TAG, "getHwPermissionInfo failed!");
            }
        }
        return new Bundle();
    }

    public void setHwPermissionInfo(int userId, Bundle params) {
        if (getHwPermissionService() != null) {
            try {
                this.mIHwPermissionManager.setHwPermissionInfo(userId, params);
            } catch (RemoteException e) {
                Log.e(TAG, "setHwPermissionInfo failed!");
            }
        }
    }

    public void removeHwPermissionInfo(String pkgName, int userId) {
        if (getHwPermissionService() != null) {
            try {
                this.mIHwPermissionManager.removeHwPermissionInfo(pkgName, userId);
            } catch (RemoteException e) {
                Log.e(TAG, "setHwPermissionInfo failed!");
            }
        }
    }

    public String getSmsAuthPackageName() {
        if (getHwPermissionService() == null) {
            return null;
        }
        try {
            return this.mIHwPermissionManager.getSmsAuthPackageName();
        } catch (RemoteException e) {
            Log.e(TAG, "getSmsAuthPackageName failed!");
            return null;
        }
    }

    public void setUserAuthResult(int uid, int userSelection, long permissionType) {
        if (getHwPermissionService() != null) {
            try {
                this.mIHwPermissionManager.setUserAuthResult(uid, userSelection, permissionType);
            } catch (RemoteException e) {
                Log.e(TAG, "getSmsAuthAppName failed!");
            }
        }
    }

    public void addOnPermissionsChangeListener(OnHwPermissionChangeListener listener) {
        synchronized (LOCK) {
            if (this.mPermissionListeners.get(listener) != null) {
                Log.i(TAG, "listener has contained in Listeners, return");
                return;
            }
            OnPermissionsChangeListenerDelegate delegate = new OnPermissionsChangeListenerDelegate(listener, Looper.getMainLooper());
            try {
                Log.i(TAG, "HwPermissionManager addOnPermissionsChangeListener");
                getHwPermissionService().addOnPermissionsChangeListener(delegate);
                this.mPermissionListeners.put(listener, delegate);
            } catch (RemoteException e) {
                Log.e(TAG, "addOnPermissionsChangeListener failed!");
            }
        }
    }

    public void removeOnPermissionsChangeListener(OnHwPermissionChangeListener listener) {
        synchronized (LOCK) {
            IOnHwPermissionChangeListener delegate = this.mPermissionListeners.get(listener);
            if (delegate != null) {
                try {
                    Log.i(TAG, "HwPermissionManager removeOnPermissionsChangeListener");
                    getHwPermissionService().removeOnPermissionsChangeListener(delegate);
                    this.mPermissionListeners.remove(listener);
                } catch (RemoteException e) {
                    Log.e(TAG, "removeOnPermissionsChangeListener failed!");
                }
            } else {
                Log.w(TAG, "listener is null, can not remove");
            }
        }
    }

    public int verifyHwAuthCeritification(String packagePath) {
        return 1;
    }

    class OnPermissionsChangeListenerDelegate extends IOnHwPermissionChangeListener.Stub implements Handler.Callback {
        private static final int MSG_PERMISSIONS_CHANGED = 1;
        private final Handler mHandler;
        private final OnHwPermissionChangeListener mListener;

        OnPermissionsChangeListenerDelegate(OnHwPermissionChangeListener listener, Looper looper) {
            this.mListener = listener;
            this.mHandler = new Handler(looper, this);
        }

        public void onPermissionChanged(String caller, String pkgName, long permType, int operation) {
            Bundle bundle = new Bundle();
            bundle.putString(HwPermissionManager.KEY_CALLER, caller);
            bundle.putString(HwPermissionManager.KEY_PACKAGE_NAME, pkgName);
            bundle.putLong(HwPermissionManager.KEY_PERM_TYPE, permType);
            bundle.putInt(HwPermissionManager.KEY_OPERATION, operation);
            this.mHandler.obtainMessage(1, bundle).sendToTarget();
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            Bundle bundle;
            if (msg.what != 1 || (bundle = (Bundle) msg.obj) == null) {
                return false;
            }
            String caller = bundle.getString(HwPermissionManager.KEY_CALLER);
            String pkgName = bundle.getString(HwPermissionManager.KEY_PACKAGE_NAME);
            long permType = bundle.getLong(HwPermissionManager.KEY_PERM_TYPE);
            int operation = bundle.getInt(HwPermissionManager.KEY_OPERATION);
            this.mListener.onPermissionChanged(caller, pkgName, permType, operation);
            Log.i(HwPermissionManager.TAG, "onPermissionChanged, caller: " + caller + ", pkgName" + pkgName + ", permType" + permType + ", operation" + operation);
            return true;
        }
    }
}

package com.huawei.security.behaviorauth;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.security.behaviorauth.IBehaviorCollectService;
import huawei.android.security.IHwSecurityService;

public class HwBehaviorManager {
    private static final int ERROR_AUTH_REMOTE_EXCEPTION = -5;
    private static final int ERROR_BEHAVIOR_DATA = -6;
    private static final int ERROR_BEHAVIOR_MODEL = -7;
    private static final int ERROR_BIND_ATUH_SERVICE = -4;
    private static final int ERROR_FAILED_BIND_SERVICE = -10;
    private static final int ERROR_GET_COLLECT_SERVICE = -11;
    private static final int ERROR_ILLEGAL_PARAMETER = -1;
    private static final int ERROR_NOT_INITIAL = -9;
    private static final int ERROR_PACKAGE_INCLUDE = -2;
    private static final int ERROR_PACKAGE_NOT_INCLUDE = -3;
    private static final int ERROR_UNKNOW_EXCEPTION = -12;
    private static final int ERROR_UNMARSHAL_DATA = -8;
    private static final Object GET_PLUGIN_LOCK = new Object();
    private static final int HW_BEHAVIOR_AUTH_PLUGIN_ID = 22;
    private static final Object INSTANCE_LOCK = new Object();
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "HwBehaviorManager";
    private static volatile HwBehaviorManager sBehaviorManager;
    private IHwSecurityService mSecurityService;

    private HwBehaviorManager(Context context) {
        IBinder binder = ServiceManagerEx.getService(SECURITY_SERVICE);
        if (binder == null) {
            Log.e(TAG, "binder is null");
        } else {
            Log.i(TAG, "binder is not null");
        }
        this.mSecurityService = IHwSecurityService.Stub.asInterface(binder);
        if (this.mSecurityService == null) {
            Log.e(TAG, "mSecurityService is null");
        }
    }

    public static HwBehaviorManager getInstance(Context context) {
        HwBehaviorManager hwBehaviorManager;
        synchronized (INSTANCE_LOCK) {
            if (sBehaviorManager == null) {
                sBehaviorManager = new HwBehaviorManager(context);
            }
            hwBehaviorManager = sBehaviorManager;
        }
        return hwBehaviorManager;
    }

    private IBehaviorCollectService getBehaviorCollectPlugin() {
        IBehaviorCollectService behaviorCollectService;
        if (this.mSecurityService == null) {
            Log.e(TAG, "mSecurityService is null in getBehaviorCollectPlugin");
            return null;
        }
        synchronized (GET_PLUGIN_LOCK) {
            try {
                behaviorCollectService = IBehaviorCollectService.Stub.asInterface(this.mSecurityService.querySecurityInterface(22));
                if (behaviorCollectService == null) {
                    Log.e(TAG, "behaviorCollectService is null");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "occur RemoteException in getBehaviorCollectPlugin");
                behaviorCollectService = null;
            }
        }
        return behaviorCollectService;
    }

    public int initBotDetect(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            Log.e(TAG, "invalid input parameter in initBotDetect");
            return -1;
        }
        IBehaviorCollectService behaviorCollectService = getBehaviorCollectPlugin();
        if (behaviorCollectService != null) {
            try {
                return behaviorCollectService.initBotDetect(pkgName);
            } catch (RemoteException e) {
                Log.e(TAG, "occur RemoteException in initBotDetect");
                return -5;
            } catch (Exception e2) {
                Log.e(TAG, "occur Exception in initBotDetect");
                return -12;
            }
        } else {
            Log.e(TAG, "get behaviorCollectPlugin failed in initBotDetect");
            return -11;
        }
    }

    public float getBotDetectResult(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            Log.e(TAG, "invalid input parameter in getBotDetectResult");
            return -1.0f;
        }
        IBehaviorCollectService behaviorCollectService = getBehaviorCollectPlugin();
        if (behaviorCollectService != null) {
            try {
                return behaviorCollectService.getBotDetectResult(pkgName);
            } catch (RemoteException e) {
                Log.e(TAG, "occur RemoteException in getBotDetectResult");
                return -5.0f;
            } catch (Exception e2) {
                Log.e(TAG, "occur Exception in getBotDetectResult");
                return -12.0f;
            }
        } else {
            Log.e(TAG, "get behaviorCollectPlugin failed in getBotDetectResult");
            return -11.0f;
        }
    }

    public int releaseBotDetect(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            Log.e(TAG, "invalid input parameter in release");
            return -1;
        }
        IBehaviorCollectService behaviorCollectService = getBehaviorCollectPlugin();
        if (behaviorCollectService != null) {
            try {
                return behaviorCollectService.releaseBotDetect(pkgName);
            } catch (RemoteException e) {
                Log.e(TAG, "occur RemoteException in releaseBotDetect");
                return -5;
            } catch (Exception e2) {
                Log.e(TAG, "occur Exception in releaseBotDetect");
                return -12;
            }
        } else {
            Log.e(TAG, "get behaviorCollectPlugin failed in releaseBotDetect");
            return -11;
        }
    }
}

package com.huawei.iaware;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.app.IUserSwitchObserver;
import android.content.Context;
import android.content.Intent;
import android.hidl.base.V1_0.DebugInfo;
import android.os.Bundle;
import android.os.IMWThirdpartyCallback;
import android.os.IRemoteCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.app.IGameObserver;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.pgmng.plug.PGSdk;

public class IAwareCallbackAL {
    static final String FOLD_TYPE_KEY = "FoldType";
    static final String GAME_OB_KEY = "GameObserver";
    static final String PACKAGEMONITOR_OB_KEY = "PackageMonitorCallBack";
    static final String PM_EXTERNAL_STORAGE = "ExternalStorage";
    static final int PM_USER_DEFAULT = 9999;
    static final String PM_USER_HANDLE = "UserHandle";
    static final String PROCESS_OB_KEY = "ProcessObserver";
    static final int REGISTER_FAILED = -1;
    static final int REGISTER_SUCCESS = 0;
    static final String SINK_KEY = "Sink";
    static final String SINK_STATETYPE = "stateType";
    private static final String TAG = "IAwareCallbackAL";
    static final String THIRD_PARTY_KEY = "thirdPartyCallBack";
    static final String USERSWITCH_OB_KEY = "UserSwitchObserver";
    static final String USERSWITCH_OB_NAME = "UserSwitchObName";
    static final String VRSTATE_CALLBACKS_KEY = "VrStateCallbacks";
    private FoldableStateListenerIAL mFoldStateCallback = null;
    private GameObserverIAL mGameObserver = null;
    /* access modifiers changed from: private */
    public Stub mIAwareCallback = null;
    private MultiWinCallBackHandlerIAL mMultiWinCallback = null;
    private PGSdk mPGSdk = null;
    private PackageMonitorCallBackIAL mPackageMonitorCallBack = null;
    private ProcessObserverIAL mProcessObserver = null;
    private SinkIAL mSinkIAL = null;
    private UserSwitchObserverIAL mUserSwitchObserver = null;
    private IVrManager mVRManager = null;
    private VrStateCallbacksIAL mVrStateCallbacks = null;

    private class FoldableStateListenerIAL implements HwFoldScreenManagerEx.FoldableStateListener {
        private FoldableStateListenerIAL() {
        }

        public void onStateChange(Bundle extra) {
            if (IAwareCallbackAL.this.mIAwareCallback != null) {
                IAwareCallbackAL.this.mIAwareCallback.onFoldStateChanged(extra);
            }
        }
    }

    private class GameObserverIAL extends IGameObserver.Stub {
        private GameObserverIAL() {
        }

        public void onGameListChanged() {
            if (IAwareCallbackAL.this.mIAwareCallback != null) {
                IAwareCallbackAL.this.mIAwareCallback.gameOnGameListChanged();
            }
        }

        public void onGameStatusChanged(String packageName, int event) {
            if (IAwareCallbackAL.this.mIAwareCallback != null) {
                IAwareCallbackAL.this.mIAwareCallback.gameOnGameStatusChanged(packageName, event);
            }
        }
    }

    private class MultiWinCallBackHandlerIAL extends IMWThirdpartyCallback.Stub {
        private MultiWinCallBackHandlerIAL() {
        }

        public void onModeChanged(boolean aMWStatus) {
            IAwareCallbackAL.this.mIAwareCallback.thirdPartyOnModeChanged(aMWStatus);
        }

        public void onZoneChanged() {
            IAwareCallbackAL.this.mIAwareCallback.thirdPartyOnZoneChanged();
        }

        public void onSizeChanged() {
            IAwareCallbackAL.this.mIAwareCallback.thirdPartyOnSizeChanged();
        }
    }

    private class PackageMonitorCallBackIAL extends PackageMonitor {
        private PackageMonitorCallBackIAL() {
        }

        public void onPackageAdded(String packageName, int uid) {
            IAwareCallbackAL.this.mIAwareCallback.onPackageAdded(packageName, uid);
        }

        public void onPackageRemoved(String packageName, int uid) {
            IAwareCallbackAL.this.mIAwareCallback.onPackageRemoved(packageName, uid);
        }

        public void onPackageUpdateStarted(String packageName, int uid) {
            IAwareCallbackAL.this.mIAwareCallback.onPackageUpdateStarted(packageName, uid);
        }

        public void onPackageUpdateFinished(String packageName, int uid) {
            IAwareCallbackAL.this.mIAwareCallback.onPackageUpdateFinished(packageName, uid);
        }
    }

    private class ProcessObserverIAL extends IProcessObserver.Stub {
        private ProcessObserverIAL() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (IAwareCallbackAL.this.mIAwareCallback != null) {
                IAwareCallbackAL.this.mIAwareCallback.processOnForegroundActivitiesChanged(pid, uid, foregroundActivities);
            }
        }

        public void onProcessDied(int pid, int uid) {
            if (IAwareCallbackAL.this.mIAwareCallback != null) {
                IAwareCallbackAL.this.mIAwareCallback.processOnProcessDied(pid, uid);
            }
        }
    }

    private class SinkIAL implements PGSdk.Sink {
        private SinkIAL() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            if (IAwareCallbackAL.this.mIAwareCallback != null) {
                IAwareCallbackAL.this.mIAwareCallback.sinkOnStateChanged(stateType, eventType, pid, pkg, uid);
            }
        }
    }

    public static abstract class Stub {
        private static final String TAG_1 = "AwareLog";
        private static final String TAG_2 = "Stub";

        public void sinkOnStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            Log.e(TAG_1, "Stub: sinkOnStateChanged has not been overridden!");
        }

        public void processOnForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            Log.e(TAG_1, "Stub: processOnForegroundActivitiesChanged has not been overridden!");
        }

        public void processOnProcessDied(int pid, int uid) {
            Log.e(TAG_1, "Stub: processProcessDied has not been overridden!");
        }

        public void switchOnUserSwitching(int newUserId, Object reply) {
            Log.e(TAG_1, "Stub: switchOnUserSwitching has not been overridden!");
        }

        public void switchOnLockedBootComplete(int userId) {
            Log.e(TAG_1, "Stub: switchOnLockedBootComplete has not been overridden!");
        }

        public void switchOnUserSwitchComplete(int newUserId) {
            Log.e(TAG_1, "Stub: switchOnUserSwitchComplete has not been overridden!");
        }

        public void switchOnForegroundProfileSwitch(int newProfileId) {
            Log.e(TAG_1, "Stub: switchOnForegroundProfileSwitch has not been overridden!");
        }

        public void thirdPartyOnModeChanged(boolean aMWStatus) {
            Log.e(TAG_1, "Stub: onModeChanged has not been overridden!");
        }

        public void thirdPartyOnZoneChanged() {
            Log.e(TAG_1, "Stub: onZoneChanged has not been overridden!");
        }

        public void thirdPartyOnSizeChanged() {
            Log.e(TAG_1, "Stub: onSizeChanged has not been overridden!");
        }

        public void gameOnGameListChanged() {
            Log.e(TAG_1, "Stub: gameOnGameListChanged has not been overridden!");
        }

        public void gameOnGameStatusChanged(String packageName, int event) {
            Log.e(TAG_1, "Stub: gameOnGameStatusChanged has not been overridden!");
        }

        public void onFoldStateChanged(Bundle extra) {
            Log.e(TAG_1, "Stub:onFoldStateChanged has not been overridden");
        }

        public void onPackageAdded(String packageName, int uid) {
            Log.e(TAG_1, "Stub: onPackageAdded has not been overridden!");
        }

        public void onPackageRemoved(String packageName, int uid) {
            Log.e(TAG_1, "Stub: onPackageRemoved has not been overridden!");
        }

        public void onPackageUpdateStarted(String packageName, int uid) {
            Log.e(TAG_1, "Stub: onPackageUpdateStarted has not been overridden!");
        }

        public void onPackageUpdateFinished(String packageName, int uid) {
            Log.e(TAG_1, "Stub: onPackageUpdateFinished has not been overridden!");
        }

        public void vrOnVrStateChanged(boolean enable) {
            Log.e(TAG_1, "Stub: vrOnVrStateChanged has not been overridden!");
        }
    }

    private class UserSwitchObserverIAL extends IUserSwitchObserver.Stub {
        private UserSwitchObserverIAL() {
        }

        public void onUserSwitching(int newUserId, IRemoteCallback reply) {
            if (IAwareCallbackAL.this.mIAwareCallback != null) {
                IAwareCallbackAL.this.mIAwareCallback.switchOnUserSwitching(newUserId, reply);
            }
        }

        public void onLockedBootComplete(int userId) {
            if (IAwareCallbackAL.this.mIAwareCallback != null) {
                IAwareCallbackAL.this.mIAwareCallback.switchOnLockedBootComplete(userId);
            }
        }

        public void onUserSwitchComplete(int newUserId) throws RemoteException {
            if (IAwareCallbackAL.this.mIAwareCallback != null) {
                IAwareCallbackAL.this.mIAwareCallback.switchOnUserSwitchComplete(newUserId);
            }
        }

        public void onForegroundProfileSwitch(int newProfileId) {
            if (IAwareCallbackAL.this.mIAwareCallback != null) {
                IAwareCallbackAL.this.mIAwareCallback.switchOnForegroundProfileSwitch(newProfileId);
            }
        }
    }

    private class VrStateCallbacksIAL extends IVrStateCallbacks.Stub {
        private VrStateCallbacksIAL() {
        }

        public void onVrStateChanged(boolean enable) throws RemoteException {
            if (IAwareCallbackAL.this.mIAwareCallback != null) {
                IAwareCallbackAL.this.mIAwareCallback.vrOnVrStateChanged(enable);
            }
        }
    }

    public int register(String key, Stub callback, Intent intent, Context context) {
        int ret;
        char c = 65535;
        if (key == null || callback == null) {
            return REGISTER_FAILED;
        }
        if (this.mIAwareCallback == null) {
            this.mIAwareCallback = callback;
        }
        switch (key.hashCode()) {
            case 2577075:
                if (key.equals(SINK_KEY)) {
                    c = 0;
                    break;
                }
                break;
            case 153382645:
                if (key.equals(USERSWITCH_OB_KEY)) {
                    c = 2;
                    break;
                }
                break;
            case 354516281:
                if (key.equals(VRSTATE_CALLBACKS_KEY)) {
                    c = 6;
                    break;
                }
                break;
            case 445232964:
                if (key.equals(THIRD_PARTY_KEY)) {
                    c = 3;
                    break;
                }
                break;
            case 668157192:
                if (key.equals(GAME_OB_KEY)) {
                    c = 4;
                    break;
                }
                break;
            case 1013952069:
                if (key.equals(PROCESS_OB_KEY)) {
                    c = 1;
                    break;
                }
                break;
            case 1166350041:
                if (key.equals(PACKAGEMONITOR_OB_KEY)) {
                    c = 5;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                ret = registerPGListener(intent);
                break;
            case DebugInfo.Architecture.IS_64BIT:
                ret = registerProcessObserver();
                break;
            case DebugInfo.Architecture.IS_32BIT:
                ret = registerUserSwitchObserver(intent);
                break;
            case 3:
                ret = registerThirdPartyCallBack();
                break;
            case 4:
                ret = registerGameObserver();
                break;
            case 5:
                ret = registerPackageMonitor(intent, context);
                break;
            case 6:
                ret = registerVRListener();
                break;
            default:
                ret = registerOthers(key);
                break;
        }
        return ret;
    }

    private int registerOthers(String key) {
        if (((key.hashCode() == 358255355 && key.equals(FOLD_TYPE_KEY)) ? (char) 0 : 65535) == 0) {
            return registerFoldStateCallback();
        }
        Log.e(TAG, "register wrong key " + key);
        return REGISTER_FAILED;
    }

    public int unregister(String key, Intent intent) {
        char c = 65535;
        if (key == null) {
            return REGISTER_FAILED;
        }
        int ret = REGISTER_FAILED;
        switch (key.hashCode()) {
            case 2577075:
                if (key.equals(SINK_KEY)) {
                    c = 0;
                    break;
                }
                break;
            case 153382645:
                if (key.equals(USERSWITCH_OB_KEY)) {
                    c = 2;
                    break;
                }
                break;
            case 354516281:
                if (key.equals(VRSTATE_CALLBACKS_KEY)) {
                    c = 6;
                    break;
                }
                break;
            case 358255355:
                if (key.equals(FOLD_TYPE_KEY)) {
                    c = 7;
                    break;
                }
                break;
            case 445232964:
                if (key.equals(THIRD_PARTY_KEY)) {
                    c = 3;
                    break;
                }
                break;
            case 668157192:
                if (key.equals(GAME_OB_KEY)) {
                    c = 4;
                    break;
                }
                break;
            case 1013952069:
                if (key.equals(PROCESS_OB_KEY)) {
                    c = 1;
                    break;
                }
                break;
            case 1166350041:
                if (key.equals(PACKAGEMONITOR_OB_KEY)) {
                    c = 5;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                ret = unregisterPGListener(intent);
                break;
            case DebugInfo.Architecture.IS_64BIT:
                ret = unregisterProcessObserver();
                break;
            case DebugInfo.Architecture.IS_32BIT:
                ret = unregisterUserSwitchObserver();
                break;
            case 3:
                ret = unregisterThirdPartyCallBack();
                break;
            case 4:
                ret = unregisterGameObserver();
                break;
            case 5:
                ret = unregisterPackageMonitor();
                break;
            case 6:
                ret = unregisterVRListener();
                break;
            case 7:
                ret = unregisterFoldStateCallback();
                break;
            default:
                Log.e(TAG, "unregister wrong key " + key);
                break;
        }
        return ret;
    }

    private boolean initPG(Intent intent) {
        boolean z = false;
        if (intent == null) {
            return false;
        }
        if (this.mPGSdk == null) {
            this.mPGSdk = PGSdk.getInstance();
        }
        if (this.mSinkIAL == null) {
            this.mSinkIAL = new SinkIAL();
        }
        if (this.mPGSdk != null) {
            z = true;
        }
        return z;
    }

    private int registerPGListener(Intent intent) {
        if (!initPG(intent)) {
            return REGISTER_FAILED;
        }
        int i = 0;
        boolean enable = false;
        try {
            int statetype = intent.getIntExtra(SINK_STATETYPE, REGISTER_FAILED);
            if (statetype != REGISTER_FAILED) {
                enable = this.mPGSdk.enableStateEvent(this.mSinkIAL, statetype);
            }
        } catch (RemoteException e) {
            this.mPGSdk = null;
            Log.e(TAG, "registerPGListener sink RemoteException!");
        }
        if (!enable) {
            i = REGISTER_FAILED;
        }
        return i;
    }

    private int unregisterPGListener(Intent intent) {
        if (this.mPGSdk == null || this.mSinkIAL == null || intent == null) {
            return REGISTER_FAILED;
        }
        int i = 0;
        boolean disable = false;
        try {
            int statetype = intent.getIntExtra(SINK_STATETYPE, REGISTER_FAILED);
            if (statetype != REGISTER_FAILED) {
                disable = this.mPGSdk.disableStateEvent(this.mSinkIAL, statetype);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterPGListener happend RemoteException!");
        }
        if (!disable) {
            i = REGISTER_FAILED;
        }
        return i;
    }

    private int registerProcessObserver() {
        if (this.mProcessObserver != null) {
            return REGISTER_FAILED;
        }
        this.mProcessObserver = new ProcessObserverIAL();
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
            return 0;
        } catch (RemoteException e) {
            this.mProcessObserver = null;
            Log.e(TAG, "register process observer failed");
            return REGISTER_FAILED;
        }
    }

    private int unregisterProcessObserver() {
        if (this.mProcessObserver == null) {
            return REGISTER_FAILED;
        }
        try {
            ActivityManagerNative.getDefault().unregisterProcessObserver(this.mProcessObserver);
            this.mProcessObserver = null;
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "unregister process observer failed");
            return REGISTER_FAILED;
        }
    }

    private int registerUserSwitchObserver(Intent intent) {
        int ret = REGISTER_FAILED;
        if (intent == null) {
            return REGISTER_FAILED;
        }
        if (this.mUserSwitchObserver == null) {
            this.mUserSwitchObserver = new UserSwitchObserverIAL();
            try {
                String name = intent.getStringExtra(USERSWITCH_OB_NAME);
                if (name != null && !"".equals(name)) {
                    ActivityManagerNative.getDefault().registerUserSwitchObserver(this.mUserSwitchObserver, name);
                    ret = 0;
                }
            } catch (RemoteException e) {
                this.mUserSwitchObserver = null;
                Log.e(TAG, "registerUserSwitchObserver failed!");
            }
        }
        return ret;
    }

    private int unregisterUserSwitchObserver() {
        if (this.mUserSwitchObserver == null) {
            return REGISTER_FAILED;
        }
        try {
            ActivityManagerNative.getDefault().unregisterUserSwitchObserver(this.mUserSwitchObserver);
            this.mUserSwitchObserver = null;
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterUserSwitchObserver failed!");
            return REGISTER_FAILED;
        }
    }

    private int registerThirdPartyCallBack() {
        if (this.mMultiWinCallback != null) {
            return REGISTER_FAILED;
        }
        this.mMultiWinCallback = new MultiWinCallBackHandlerIAL();
        if (HwActivityManager.registerThirdPartyCallBack(this.mMultiWinCallback)) {
            return 0;
        }
        this.mMultiWinCallback = null;
        return REGISTER_FAILED;
    }

    private int unregisterThirdPartyCallBack() {
        if (this.mMultiWinCallback == null || !HwActivityManager.unregisterThirdPartyCallBack(this.mMultiWinCallback)) {
            return REGISTER_FAILED;
        }
        this.mMultiWinCallback = null;
        return 0;
    }

    private int registerPackageMonitor(Intent intent, Context context) {
        int ret = REGISTER_FAILED;
        if (intent == null || context == null) {
            return REGISTER_FAILED;
        }
        if (this.mPackageMonitorCallBack == null) {
            this.mPackageMonitorCallBack = new PackageMonitorCallBackIAL();
            if (REGISTER_FAILED == intent.getIntExtra(PM_USER_HANDLE, PM_USER_DEFAULT)) {
                this.mPackageMonitorCallBack.register(context, null, UserHandle.ALL, intent.getBooleanExtra(PM_EXTERNAL_STORAGE, true));
            }
            ret = 0;
        }
        return ret;
    }

    private int unregisterPackageMonitor() {
        if (this.mPackageMonitorCallBack == null) {
            return REGISTER_FAILED;
        }
        this.mPackageMonitorCallBack.unregister();
        return 0;
    }

    private int registerGameObserver() {
        if (this.mGameObserver != null) {
            return REGISTER_FAILED;
        }
        this.mGameObserver = new GameObserverIAL();
        ActivityManagerEx.registerGameObserver(this.mGameObserver);
        return 0;
    }

    private int unregisterGameObserver() {
        if (this.mGameObserver == null) {
            return REGISTER_FAILED;
        }
        ActivityManagerEx.unregisterGameObserver(this.mGameObserver);
        this.mGameObserver = null;
        return 0;
    }

    private int registerVRListener() {
        int ret = REGISTER_FAILED;
        if (this.mVRManager == null) {
            this.mVRManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
        }
        if (this.mVRManager == null) {
            return REGISTER_FAILED;
        }
        if (this.mVrStateCallbacks == null) {
            this.mVrStateCallbacks = new VrStateCallbacksIAL();
            try {
                this.mVRManager.registerListener(this.mVrStateCallbacks);
                ret = 0;
            } catch (RemoteException e) {
                this.mVrStateCallbacks = null;
                Log.e(TAG, "failed to register vr mode state listener:" + e.getMessage());
            }
        }
        return ret;
    }

    private int unregisterVRListener() {
        int ret = REGISTER_FAILED;
        if (this.mVRManager == null || this.mVrStateCallbacks == null) {
            return REGISTER_FAILED;
        }
        try {
            this.mVRManager.unregisterListener(this.mVrStateCallbacks);
            this.mVrStateCallbacks = null;
            ret = 0;
        } catch (RemoteException e) {
            Log.e(TAG, "failed to unregister vr mode state listener:" + e.getMessage());
        }
        return ret;
    }

    private int registerFoldStateCallback() {
        if (this.mFoldStateCallback != null) {
            return REGISTER_FAILED;
        }
        this.mFoldStateCallback = new FoldableStateListenerIAL();
        try {
            HwFoldScreenManagerEx.registerFoldableState(this.mFoldStateCallback, 1);
            return 0;
        } catch (RuntimeException e) {
            this.mFoldStateCallback = null;
            Log.e(TAG, "failed to register fold state listener: " + e.getMessage());
            return REGISTER_FAILED;
        }
    }

    private int unregisterFoldStateCallback() {
        int ret = REGISTER_FAILED;
        if (this.mFoldStateCallback == null) {
            return REGISTER_FAILED;
        }
        try {
            HwFoldScreenManagerEx.unregisterFoldableState(this.mFoldStateCallback);
            this.mFoldStateCallback = null;
            ret = 0;
        } catch (RuntimeException e) {
            Log.e(TAG, "failed to unregister fold state listener:" + e.getMessage());
        }
        return ret;
    }
}

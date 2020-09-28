package com.huawei.iaware;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.app.IUserSwitchObserver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IMWThirdpartyCallback;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.content.PackageMonitor;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.app.IGameObserver;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.pgmng.plug.PowerKit;
import java.util.Iterator;

public class IAwareCallbackAL {
    private static final int BG_CHANAGE_MSG = 1;
    private static final int BOOT_COMPLETE_MSG = 4;
    private static final int FG_CHANAGE_MSG = 0;
    private static final int FG_PROFILE_SWITCH = 6;
    private static final int FOLD_CHANGE_MSG = 18;
    public static final String FOLD_TYPE_KEY = "FoldType";
    private static final int GAME_LSIT_CHANGE_MSG = 15;
    public static final String GAME_OB_KEY = "GameObserver";
    private static final int GAME_STATUS_CHANGE_MSG = 16;
    private static final Object LOCK = new Object();
    private static final int MODE_CHANGE_MSG = 7;
    public static final String PACKAGEMONITOR_OB_KEY = "PackageMonitorCallBack";
    private static final int PG_SINK_MSG = 17;
    private static final int PKG_ADD_MSG = 10;
    private static final int PKG_REMOVE_MSG = 11;
    private static final int PKG_UPDATE_FINISH_MSG = 13;
    private static final int PKG_UPDATE_START_MSG = 12;
    public static final String PM_EXTERNAL_STORAGE = "ExternalStorage";
    public static final int PM_USER_DEFAULT = 9999;
    public static final String PM_USER_HANDLE = "UserHandle";
    private static final int PROCESS_DIE_MSG = 2;
    public static final String PROCESS_OB_KEY = "ProcessObserver";
    public static final int REGISTER_FAILED = -1;
    public static final int REGISTER_SUCCESS = 0;
    public static final String SINK_KEY = "Sink";
    public static final String SINK_STATETYPE = "stateType";
    private static final int SIZE_CHANGE_MSG = 9;
    private static final String TAG = "IAwareCallbackAL";
    public static final String THIRD_PARTY_KEY = "thirdPartyCallBack";
    public static final String USERSWITCH_OB_KEY = "UserSwitchObserver";
    public static final String USERSWITCH_OB_NAME = "UserSwitchObName";
    private static final int USER_SWITCH_COMPLETE_MSG = 5;
    private static final int USER_SWITCH_MSG = 3;
    public static final String VRSTATE_CALLBACKS_KEY = "VrStateCallbacks";
    private static final int VR_STATE_CHANGE_MSG = 14;
    private static final int ZONE_CHANGE_MSG = 8;
    private static IAwareCallbackAL sInstance = null;
    private FoldableStateListenerIAL mFoldStateCallback = null;
    private GameObserverIAL mGameObserver = null;
    private IAwareCallbackHandler mHandler = null;
    private final ArrayMap<String, ArraySet<Stub>> mIAwareCallbacks = new ArrayMap<>();
    private MultiWinCallBackHandlerIAL mMultiWinCallback = null;
    private PowerKit mPGSdk = null;
    private PackageMonitorCallBackIAL mPackageMonitorCallBack = null;
    private ProcessObserverIAL mProcessObserver = null;
    private SinkIAL mSinkIAL = null;
    private UserSwitchObserverIAL mUserSwitchObserver = null;
    private IVrManager mVRManager = null;
    private VrStateCallbacksIAL mVrStateCallbacks = null;

    public IAwareCallbackAL() {
        Looper looper = IAwareFunctionAL.getBackgroundThreadLooper();
        if (looper != null) {
            this.mHandler = new IAwareCallbackHandler(looper);
        }
    }

    public static IAwareCallbackAL getInstance() {
        IAwareCallbackAL iAwareCallbackAL;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new IAwareCallbackAL();
            }
            iAwareCallbackAL = sInstance;
        }
        return iAwareCallbackAL;
    }

    public int register(String key, Stub callback, Intent intent, Context context) {
        if (key == null || callback == null) {
            return -1;
        }
        int ret = registerIAwareCallBack(key, callback, intent, context);
        Log.d(TAG, "register ret: " + ret + ", key: " + key);
        return ret;
    }

    public int unregister(String key, Stub callback, Intent intent) {
        if (key == null) {
            return -1;
        }
        int ret = unregisterIAwareCallBack(key, callback, intent);
        Log.d(TAG, "unregister ret: " + ret + ", key: " + key);
        return ret;
    }

    public int unregister(String key, Intent intent) {
        if (key == null) {
            return -1;
        }
        Log.d(TAG, "this method is not actually unregister since its usage for down compatible");
        return -1;
    }

    private int registerEvent(String key, Intent intent, Context context) {
        char c = 65535;
        if (key == null) {
            return -1;
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
                return registerPGListener(intent);
            case 1:
                return registerProcessObserver();
            case 2:
                return registerUserSwitchObserver(intent);
            case USER_SWITCH_MSG /*{ENCODED_INT: 3}*/:
                return registerThirdPartyCallBack();
            case BOOT_COMPLETE_MSG /*{ENCODED_INT: 4}*/:
                return registerGameObserver();
            case USER_SWITCH_COMPLETE_MSG /*{ENCODED_INT: 5}*/:
                return registerPackageMonitor(intent, context);
            case FG_PROFILE_SWITCH /*{ENCODED_INT: 6}*/:
                return registerVRListener();
            default:
                return registerOthers(key);
        }
    }

    private int registerOthers(String key) {
        if (((key.hashCode() == 358255355 && key.equals(FOLD_TYPE_KEY)) ? (char) 0 : 65535) == 0) {
            return registerFoldStateCallback();
        }
        Log.e(TAG, "register wrong key " + key);
        return -1;
    }

    private int unregisterEvent(String key, Intent intent) {
        char c = 65535;
        if (key == null) {
            return -1;
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
                return unregisterPGListener(intent);
            case 1:
                return unregisterProcessObserver();
            case 2:
                return unregisterUserSwitchObserver();
            case USER_SWITCH_MSG /*{ENCODED_INT: 3}*/:
                return unregisterThirdPartyCallBack();
            case BOOT_COMPLETE_MSG /*{ENCODED_INT: 4}*/:
                return unregisterGameObserver();
            case USER_SWITCH_COMPLETE_MSG /*{ENCODED_INT: 5}*/:
                return unregisterPackageMonitor();
            case FG_PROFILE_SWITCH /*{ENCODED_INT: 6}*/:
                return unregisterVRListener();
            case MODE_CHANGE_MSG /*{ENCODED_INT: 7}*/:
                return unregisterFoldStateCallback();
            default:
                Log.e(TAG, "unregister wrong key " + key);
                return -1;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x004f, code lost:
        if (com.huawei.iaware.IAwareCallbackAL.SINK_KEY.equals(r7) == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0051, code lost:
        registerEvent(r7, r9, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        return 0;
     */
    private int registerIAwareCallBack(String key, Stub callback, Intent intent, Context context) {
        synchronized (this.mIAwareCallbacks) {
            ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(key);
            if (callbacks == null) {
                ArraySet<Stub> callbacks2 = new ArraySet<>();
                callbacks2.add(callback);
                this.mIAwareCallbacks.put(key, callbacks2);
                return registerEvent(key, intent, context);
            }
            callbacks.add(callback);
            Log.d(TAG, "callback key: " + key + ", size: " + callbacks.size());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002b, code lost:
        if (com.huawei.iaware.IAwareCallbackAL.SINK_KEY.equals(r5) == false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002d, code lost:
        unregisterEvent(r5, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        return 0;
     */
    private int unregisterIAwareCallBack(String key, Stub callback, Intent intent) {
        synchronized (this.mIAwareCallbacks) {
            ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(key);
            if (callbacks == null) {
                return -1;
            }
            callbacks.remove(callback);
            if (callbacks.isEmpty()) {
                this.mIAwareCallbacks.remove(key);
                return unregisterEvent(key, intent);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMsgToHandler(int msgId, Object obj) {
        IAwareCallbackHandler iAwareCallbackHandler = this.mHandler;
        if (iAwareCallbackHandler != null) {
            Message msg = iAwareCallbackHandler.obtainMessage();
            msg.what = msgId;
            msg.obj = obj;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMsgToHandler(int msgId, String paramStr, boolean param) {
        IAwareCallbackHandler iAwareCallbackHandler = this.mHandler;
        if (iAwareCallbackHandler != null) {
            Message msg = iAwareCallbackHandler.obtainMessage();
            msg.what = msgId;
            msg.getData().putBoolean(paramStr, param);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMsgToHandler(int msgId) {
        IAwareCallbackHandler iAwareCallbackHandler = this.mHandler;
        if (iAwareCallbackHandler != null) {
            Message msg = iAwareCallbackHandler.obtainMessage();
            msg.what = msgId;
            this.mHandler.removeMessages(msgId);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public class SinkIAL implements PowerKit.Sink {
        private SinkIAL() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.PG_SINK_MSG, new StateInfo(stateType, eventType, pid, pkg, uid));
        }
    }

    private boolean initPG(Intent intent) {
        if (intent == null) {
            return false;
        }
        if (this.mPGSdk == null) {
            this.mPGSdk = PowerKit.getInstance();
        }
        if (this.mSinkIAL == null) {
            this.mSinkIAL = new SinkIAL();
        }
        if (this.mPGSdk != null) {
            return true;
        }
        return false;
    }

    private int registerPGListener(Intent intent) {
        if (!initPG(intent)) {
            return -1;
        }
        boolean enable = false;
        try {
            int statetype = intent.getIntExtra(SINK_STATETYPE, -1);
            if (statetype != -1) {
                enable = this.mPGSdk.enableStateEvent(this.mSinkIAL, statetype);
                Log.d(TAG, "register pg status: " + enable);
            }
        } catch (RemoteException e) {
            this.mPGSdk = null;
            Log.e(TAG, "registerPGListener sink RemoteException!");
        }
        if (enable) {
            return 0;
        }
        return -1;
    }

    private int unregisterPGListener(Intent intent) {
        if (this.mPGSdk == null || this.mSinkIAL == null || intent == null) {
            return -1;
        }
        boolean disable = false;
        try {
            int statetype = intent.getIntExtra(SINK_STATETYPE, -1);
            if (statetype != -1) {
                disable = this.mPGSdk.disableStateEvent(this.mSinkIAL, statetype);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterPGListener happend RemoteException!");
        }
        if (disable) {
            return 0;
        }
        return -1;
    }

    private int registerProcessObserver() {
        if (this.mProcessObserver != null) {
            return -1;
        }
        this.mProcessObserver = new ProcessObserverIAL();
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
            Log.d(TAG, "register process observer success!");
            return 0;
        } catch (RemoteException e) {
            this.mProcessObserver = null;
            Log.e(TAG, "register process observer failed");
            return -1;
        }
    }

    private int unregisterProcessObserver() {
        if (this.mProcessObserver == null) {
            return -1;
        }
        try {
            ActivityManagerNative.getDefault().unregisterProcessObserver(this.mProcessObserver);
            this.mProcessObserver = null;
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "unregister process observer failed");
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public class ProcessObserverIAL extends IProcessObserver.Stub {
        private ProcessObserverIAL() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            PidInfo pidInfo = new PidInfo(pid, uid);
            if (foregroundActivities) {
                IAwareCallbackAL.this.sendMsgToHandler(0, pidInfo);
            } else {
                IAwareCallbackAL.this.sendMsgToHandler(1, pidInfo);
            }
        }

        public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) {
        }

        public void onProcessDied(int pid, int uid) {
            IAwareCallbackAL.this.sendMsgToHandler(2, new PidInfo(pid, uid));
        }
    }

    private int registerUserSwitchObserver(Intent intent) {
        if (intent == null || this.mUserSwitchObserver != null) {
            return -1;
        }
        this.mUserSwitchObserver = new UserSwitchObserverIAL();
        try {
            String name = intent.getStringExtra(USERSWITCH_OB_NAME);
            if (name == null || "".equals(name)) {
                return -1;
            }
            ActivityManagerNative.getDefault().registerUserSwitchObserver(this.mUserSwitchObserver, name);
            Log.d(TAG, "register userswitch observer success!");
            return 0;
        } catch (RemoteException e) {
            this.mUserSwitchObserver = null;
            Log.e(TAG, "registerUserSwitchObserver failed!");
            return -1;
        }
    }

    private int unregisterUserSwitchObserver() {
        if (this.mUserSwitchObserver == null) {
            return -1;
        }
        try {
            ActivityManagerNative.getDefault().unregisterUserSwitchObserver(this.mUserSwitchObserver);
            this.mUserSwitchObserver = null;
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterUserSwitchObserver failed!");
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public class UserSwitchObserverIAL extends IUserSwitchObserver.Stub {
        private UserSwitchObserverIAL() {
        }

        public void onUserSwitching(int newUserId, IRemoteCallback reply) {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.USER_SWITCH_MSG, new UserSwicthInfo(newUserId, reply, 0));
        }

        public void onLockedBootComplete(int userId) {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.BOOT_COMPLETE_MSG, new UserSwicthInfo(userId, null, 0));
        }

        public void onUserSwitchComplete(int newUserId) throws RemoteException {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.USER_SWITCH_COMPLETE_MSG, new UserSwicthInfo(newUserId, null, 0));
        }

        public void onForegroundProfileSwitch(int newProfileId) {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.FG_PROFILE_SWITCH, new UserSwicthInfo(0, null, newProfileId));
        }
    }

    private int registerThirdPartyCallBack() {
        if (this.mMultiWinCallback != null) {
            return -1;
        }
        this.mMultiWinCallback = new MultiWinCallBackHandlerIAL();
        if (HwActivityTaskManager.registerThirdPartyCallBack(this.mMultiWinCallback)) {
            Log.d(TAG, "register thirdParty success!");
            return 0;
        }
        this.mMultiWinCallback = null;
        return -1;
    }

    private int unregisterThirdPartyCallBack() {
        MultiWinCallBackHandlerIAL multiWinCallBackHandlerIAL = this.mMultiWinCallback;
        if (multiWinCallBackHandlerIAL == null || !HwActivityTaskManager.unregisterThirdPartyCallBack(multiWinCallBackHandlerIAL)) {
            return -1;
        }
        this.mMultiWinCallback = null;
        return 0;
    }

    /* access modifiers changed from: private */
    public class MultiWinCallBackHandlerIAL extends IMWThirdpartyCallback.Stub {
        private MultiWinCallBackHandlerIAL() {
        }

        public void onModeChanged(boolean aMWStatus) {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.MODE_CHANGE_MSG, "aMWStatus", aMWStatus);
        }

        public void onZoneChanged() {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.ZONE_CHANGE_MSG);
        }

        public void onSizeChanged() {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.SIZE_CHANGE_MSG);
        }
    }

    private int registerPackageMonitor(Intent intent, Context context) {
        if (intent == null || context == null || this.mPackageMonitorCallBack != null) {
            return -1;
        }
        this.mPackageMonitorCallBack = new PackageMonitorCallBackIAL();
        if (-1 == intent.getIntExtra(PM_USER_HANDLE, PM_USER_DEFAULT)) {
            this.mPackageMonitorCallBack.register(context, null, UserHandle.ALL, intent.getBooleanExtra(PM_EXTERNAL_STORAGE, true));
            Log.d(TAG, "register pms monitor success!");
        }
        return 0;
    }

    private int unregisterPackageMonitor() {
        PackageMonitorCallBackIAL packageMonitorCallBackIAL = this.mPackageMonitorCallBack;
        if (packageMonitorCallBackIAL == null) {
            return -1;
        }
        packageMonitorCallBackIAL.unregister();
        return 0;
    }

    /* access modifiers changed from: private */
    public class PackageMonitorCallBackIAL extends PackageMonitor {
        private PackageMonitorCallBackIAL() {
        }

        public void onPackageAdded(String packageName, int uid) {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.PKG_ADD_MSG, new PkgInfo(packageName, uid));
        }

        public void onPackageRemoved(String packageName, int uid) {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.PKG_REMOVE_MSG, new PkgInfo(packageName, uid));
        }

        public void onPackageUpdateStarted(String packageName, int uid) {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.PKG_UPDATE_START_MSG, new PkgInfo(packageName, uid));
        }

        public void onPackageUpdateFinished(String packageName, int uid) {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.PKG_UPDATE_FINISH_MSG, new PkgInfo(packageName, uid));
        }
    }

    private int registerGameObserver() {
        if (this.mGameObserver != null) {
            return -1;
        }
        this.mGameObserver = new GameObserverIAL();
        ActivityManagerEx.registerGameObserver(this.mGameObserver);
        Log.d(TAG, "register game observer success!");
        return 0;
    }

    private int unregisterGameObserver() {
        GameObserverIAL gameObserverIAL = this.mGameObserver;
        if (gameObserverIAL == null) {
            return -1;
        }
        ActivityManagerEx.unregisterGameObserver(gameObserverIAL);
        this.mGameObserver = null;
        return 0;
    }

    /* access modifiers changed from: private */
    public class GameObserverIAL extends IGameObserver.Stub {
        private GameObserverIAL() {
        }

        public void onGameListChanged() {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.GAME_LSIT_CHANGE_MSG);
        }

        public void onGameStatusChanged(String packageName, int event) {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.GAME_STATUS_CHANGE_MSG, new GameInfo(packageName, event));
        }
    }

    private int registerVRListener() {
        if (this.mVRManager == null) {
            this.mVRManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
        }
        if (this.mVRManager == null || this.mVrStateCallbacks != null) {
            return -1;
        }
        this.mVrStateCallbacks = new VrStateCallbacksIAL();
        try {
            this.mVRManager.registerListener(this.mVrStateCallbacks);
            Log.d(TAG, "register vr listener success!");
            return 0;
        } catch (RemoteException e) {
            this.mVrStateCallbacks = null;
            Log.e(TAG, "failed to register vr mode state listener:" + e.getMessage());
            return -1;
        }
    }

    private int unregisterVRListener() {
        VrStateCallbacksIAL vrStateCallbacksIAL;
        IVrManager iVrManager = this.mVRManager;
        if (iVrManager == null || (vrStateCallbacksIAL = this.mVrStateCallbacks) == null) {
            return -1;
        }
        try {
            iVrManager.unregisterListener(vrStateCallbacksIAL);
            this.mVrStateCallbacks = null;
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "failed to unregister vr mode state listener:" + e.getMessage());
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public class VrStateCallbacksIAL extends IVrStateCallbacks.Stub {
        private VrStateCallbacksIAL() {
        }

        public void onVrStateChanged(boolean enable) throws RemoteException {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.VR_STATE_CHANGE_MSG, "enable", enable);
        }
    }

    private int registerFoldStateCallback() {
        if (this.mFoldStateCallback != null) {
            return -1;
        }
        this.mFoldStateCallback = new FoldableStateListenerIAL();
        try {
            HwFoldScreenManagerEx.registerFoldableState(this.mFoldStateCallback, 1);
            Log.d(TAG, "register fold state listener success!");
            return 0;
        } catch (RuntimeException e) {
            this.mFoldStateCallback = null;
            Log.e(TAG, "failed to register fold state listener: " + e.getMessage());
            return -1;
        }
    }

    private int unregisterFoldStateCallback() {
        FoldableStateListenerIAL foldableStateListenerIAL = this.mFoldStateCallback;
        if (foldableStateListenerIAL == null) {
            return -1;
        }
        try {
            HwFoldScreenManagerEx.unregisterFoldableState(foldableStateListenerIAL);
            this.mFoldStateCallback = null;
            return 0;
        } catch (RuntimeException e) {
            Log.e(TAG, "failed to unregister fold state listener:" + e.getMessage());
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public class FoldableStateListenerIAL implements HwFoldScreenManagerEx.FoldableStateListener {
        private FoldableStateListenerIAL() {
        }

        public void onStateChange(Bundle extra) {
            IAwareCallbackAL.this.sendMsgToHandler(IAwareCallbackAL.FOLD_CHANGE_MSG, extra);
        }
    }

    public static abstract class Stub {
        private static final String STRING_STUB = "Stub";
        private static final String TAG_AWARELOG = "AwareLog";

        public void sinkOnStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            Log.e(TAG_AWARELOG, "Stub: sinkOnStateChanged has not been overridden!");
        }

        public void processOnForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            Log.e(TAG_AWARELOG, "Stub: processOnForegroundActivitiesChanged has not been overridden!");
        }

        public void processOnProcessDied(int pid, int uid) {
            Log.e(TAG_AWARELOG, "Stub: processProcessDied has not been overridden!");
        }

        public void switchOnUserSwitching(int newUserId, Object reply) {
            Log.e(TAG_AWARELOG, "Stub: switchOnUserSwitching has not been overridden!");
        }

        public void switchOnLockedBootComplete(int userId) {
            Log.e(TAG_AWARELOG, "Stub: switchOnLockedBootComplete has not been overridden!");
        }

        public void switchOnUserSwitchComplete(int newUserId) {
            Log.e(TAG_AWARELOG, "Stub: switchOnUserSwitchComplete has not been overridden!");
        }

        public void switchOnForegroundProfileSwitch(int newProfileId) {
            Log.e(TAG_AWARELOG, "Stub: switchOnForegroundProfileSwitch has not been overridden!");
        }

        public void thirdPartyOnModeChanged(boolean aMWStatus) {
            Log.e(TAG_AWARELOG, "Stub: onModeChanged has not been overridden!");
        }

        public void thirdPartyOnZoneChanged() {
            Log.e(TAG_AWARELOG, "Stub: onZoneChanged has not been overridden!");
        }

        public void thirdPartyOnSizeChanged() {
            Log.e(TAG_AWARELOG, "Stub: onSizeChanged has not been overridden!");
        }

        public void gameOnGameListChanged() {
            Log.e(TAG_AWARELOG, "Stub: gameOnGameListChanged has not been overridden!");
        }

        public void gameOnGameStatusChanged(String packageName, int event) {
            Log.e(TAG_AWARELOG, "Stub: gameOnGameStatusChanged has not been overridden!");
        }

        public void onFoldStateChanged(Bundle extra) {
            Log.e(TAG_AWARELOG, "Stub:onFoldStateChanged has not been overridden!");
        }

        public void onPackageAdded(String packageName, int uid) {
            Log.e(TAG_AWARELOG, "Stub: onPackageAdded has not been overridden!");
        }

        public void onPackageRemoved(String packageName, int uid) {
            Log.e(TAG_AWARELOG, "Stub: onPackageRemoved has not been overridden!");
        }

        public void onPackageUpdateStarted(String packageName, int uid) {
            Log.e(TAG_AWARELOG, "Stub: onPackageUpdateStarted has not been overridden!");
        }

        public void onPackageUpdateFinished(String packageName, int uid) {
            Log.e(TAG_AWARELOG, "Stub: onPackageUpdateFinished has not been overridden!");
        }

        public void vrOnVrStateChanged(boolean enable) {
            Log.e(TAG_AWARELOG, "Stub: vrOnVrStateChanged has not been overridden!");
        }
    }

    /* access modifiers changed from: private */
    public static class GameInfo {
        public int eventId;
        public String pkgName;

        public GameInfo(String pkgName2, int eventId2) {
            this.pkgName = pkgName2;
            this.eventId = eventId2;
        }
    }

    /* access modifiers changed from: private */
    public static class PkgInfo {
        public String pkgName;
        public int uid;

        public PkgInfo(String pkgName2, int uid2) {
            this.pkgName = pkgName2;
            this.uid = uid2;
        }
    }

    /* access modifiers changed from: private */
    public static class PidInfo {
        public int pid;
        public int uid;

        public PidInfo(int pid2, int uid2) {
            this.pid = pid2;
            this.uid = uid2;
        }
    }

    /* access modifiers changed from: private */
    public static class UserSwicthInfo {
        public int profileId;
        public IRemoteCallback reply;
        public int userId;

        public UserSwicthInfo(int userId2, IRemoteCallback reply2, int newProfileId) {
            this.userId = userId2;
            this.reply = reply2;
            this.profileId = newProfileId;
        }
    }

    /* access modifiers changed from: private */
    public static class StateInfo {
        public int eventType;
        public int pid;
        public String pkg;
        public int stateType;
        public int uid;

        public StateInfo(int stateType2, int eventType2, int pid2, String pkg2, int uid2) {
            this.stateType = stateType2;
            this.eventType = eventType2;
            this.pid = pid2;
            this.pkg = pkg2;
            this.uid = uid2;
        }
    }

    /* access modifiers changed from: private */
    public class IAwareCallbackHandler extends Handler {
        public IAwareCallbackHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg == null) {
                Log.d(IAwareCallbackAL.TAG, "msg is null, error");
                return;
            }
            switch (msg.what) {
                case 0:
                    IAwareCallbackAL.this.handleForeGroundChange(msg, true);
                    return;
                case 1:
                    IAwareCallbackAL.this.handleForeGroundChange(msg, false);
                    return;
                case 2:
                    IAwareCallbackAL.this.handleProcessDie(msg);
                    return;
                case IAwareCallbackAL.USER_SWITCH_MSG /*{ENCODED_INT: 3}*/:
                    IAwareCallbackAL.this.handleUserSwitch(msg);
                    return;
                case IAwareCallbackAL.BOOT_COMPLETE_MSG /*{ENCODED_INT: 4}*/:
                    IAwareCallbackAL.this.handleBootComplete(msg);
                    return;
                case IAwareCallbackAL.USER_SWITCH_COMPLETE_MSG /*{ENCODED_INT: 5}*/:
                    IAwareCallbackAL.this.handleSwitchComplete(msg);
                    return;
                case IAwareCallbackAL.FG_PROFILE_SWITCH /*{ENCODED_INT: 6}*/:
                    IAwareCallbackAL.this.handleFgProfileSwitch(msg);
                    return;
                case IAwareCallbackAL.MODE_CHANGE_MSG /*{ENCODED_INT: 7}*/:
                    IAwareCallbackAL.this.handleModeChange(msg);
                    return;
                case IAwareCallbackAL.ZONE_CHANGE_MSG /*{ENCODED_INT: 8}*/:
                    IAwareCallbackAL.this.handleZoneChange();
                    return;
                case IAwareCallbackAL.SIZE_CHANGE_MSG /*{ENCODED_INT: 9}*/:
                    IAwareCallbackAL.this.handleSizeChange();
                    return;
                default:
                    handleMessageEx(msg);
                    return;
            }
        }

        private void handleMessageEx(Message msg) {
            switch (msg.what) {
                case IAwareCallbackAL.PKG_ADD_MSG /*{ENCODED_INT: 10}*/:
                    IAwareCallbackAL.this.handlePkgAdd(msg);
                    return;
                case IAwareCallbackAL.PKG_REMOVE_MSG /*{ENCODED_INT: 11}*/:
                    IAwareCallbackAL.this.handlePkgRemove(msg);
                    return;
                case IAwareCallbackAL.PKG_UPDATE_START_MSG /*{ENCODED_INT: 12}*/:
                    IAwareCallbackAL.this.handlePkgUpdateStart(msg);
                    return;
                case IAwareCallbackAL.PKG_UPDATE_FINISH_MSG /*{ENCODED_INT: 13}*/:
                    IAwareCallbackAL.this.handlePkgUpdateFinish(msg);
                    return;
                case IAwareCallbackAL.VR_STATE_CHANGE_MSG /*{ENCODED_INT: 14}*/:
                    IAwareCallbackAL.this.handleVrStateChange(msg);
                    return;
                case IAwareCallbackAL.GAME_LSIT_CHANGE_MSG /*{ENCODED_INT: 15}*/:
                    IAwareCallbackAL.this.handleGameListChange();
                    return;
                case IAwareCallbackAL.GAME_STATUS_CHANGE_MSG /*{ENCODED_INT: 16}*/:
                    IAwareCallbackAL.this.handleGameStatusChange(msg);
                    return;
                case IAwareCallbackAL.PG_SINK_MSG /*{ENCODED_INT: 17}*/:
                    IAwareCallbackAL.this.handlePgSink(msg);
                    return;
                case IAwareCallbackAL.FOLD_CHANGE_MSG /*{ENCODED_INT: 18}*/:
                    IAwareCallbackAL.this.handleFoldStateChange(msg);
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFoldStateChange(Message msg) {
        if (msg.obj instanceof Bundle) {
            Bundle extra = (Bundle) msg.obj;
            synchronized (this.mIAwareCallbacks) {
                ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(FOLD_TYPE_KEY);
                if (callbacks != null) {
                    Iterator<Stub> it = callbacks.iterator();
                    while (it.hasNext()) {
                        it.next().onFoldStateChanged(extra);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePgSink(Message msg) {
        if (msg.obj instanceof StateInfo) {
            StateInfo stateInfo = (StateInfo) msg.obj;
            synchronized (this.mIAwareCallbacks) {
                ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(SINK_KEY);
                if (callbacks != null) {
                    Iterator<Stub> it = callbacks.iterator();
                    while (it.hasNext()) {
                        it.next().sinkOnStateChanged(stateInfo.stateType, stateInfo.eventType, stateInfo.pid, stateInfo.pkg, stateInfo.uid);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGameStatusChange(Message msg) {
        if (msg.obj instanceof GameInfo) {
            GameInfo gameInfo = (GameInfo) msg.obj;
            synchronized (this.mIAwareCallbacks) {
                ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(GAME_OB_KEY);
                if (callbacks != null) {
                    Iterator<Stub> it = callbacks.iterator();
                    while (it.hasNext()) {
                        it.next().gameOnGameStatusChanged(gameInfo.pkgName, gameInfo.eventId);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGameListChange() {
        synchronized (this.mIAwareCallbacks) {
            ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(GAME_OB_KEY);
            if (callbacks != null) {
                Iterator<Stub> it = callbacks.iterator();
                while (it.hasNext()) {
                    it.next().gameOnGameListChanged();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleVrStateChange(Message msg) {
        boolean enable = msg.getData().getBoolean("enable");
        synchronized (this.mIAwareCallbacks) {
            ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(VRSTATE_CALLBACKS_KEY);
            if (callbacks != null) {
                Iterator<Stub> it = callbacks.iterator();
                while (it.hasNext()) {
                    it.next().vrOnVrStateChanged(enable);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePkgUpdateFinish(Message msg) {
        if (msg.obj instanceof PkgInfo) {
            PkgInfo pkgInfo = (PkgInfo) msg.obj;
            synchronized (this.mIAwareCallbacks) {
                ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(PACKAGEMONITOR_OB_KEY);
                if (callbacks != null) {
                    Iterator<Stub> it = callbacks.iterator();
                    while (it.hasNext()) {
                        it.next().onPackageUpdateFinished(pkgInfo.pkgName, pkgInfo.uid);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePkgUpdateStart(Message msg) {
        if (msg.obj instanceof PkgInfo) {
            PkgInfo pkgInfo = (PkgInfo) msg.obj;
            synchronized (this.mIAwareCallbacks) {
                ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(PACKAGEMONITOR_OB_KEY);
                if (callbacks != null) {
                    Iterator<Stub> it = callbacks.iterator();
                    while (it.hasNext()) {
                        it.next().onPackageUpdateStarted(pkgInfo.pkgName, pkgInfo.uid);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePkgRemove(Message msg) {
        if (msg.obj instanceof PkgInfo) {
            PkgInfo pkgInfo = (PkgInfo) msg.obj;
            synchronized (this.mIAwareCallbacks) {
                ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(PACKAGEMONITOR_OB_KEY);
                if (callbacks != null) {
                    Iterator<Stub> it = callbacks.iterator();
                    while (it.hasNext()) {
                        it.next().onPackageRemoved(pkgInfo.pkgName, pkgInfo.uid);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePkgAdd(Message msg) {
        if (msg.obj instanceof PkgInfo) {
            PkgInfo pkgInfo = (PkgInfo) msg.obj;
            synchronized (this.mIAwareCallbacks) {
                ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(PACKAGEMONITOR_OB_KEY);
                if (callbacks != null) {
                    Iterator<Stub> it = callbacks.iterator();
                    while (it.hasNext()) {
                        it.next().onPackageAdded(pkgInfo.pkgName, pkgInfo.uid);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSizeChange() {
        synchronized (this.mIAwareCallbacks) {
            ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(THIRD_PARTY_KEY);
            if (callbacks != null) {
                Iterator<Stub> it = callbacks.iterator();
                while (it.hasNext()) {
                    it.next().thirdPartyOnSizeChanged();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleZoneChange() {
        synchronized (this.mIAwareCallbacks) {
            ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(THIRD_PARTY_KEY);
            if (callbacks != null) {
                Iterator<Stub> it = callbacks.iterator();
                while (it.hasNext()) {
                    it.next().thirdPartyOnZoneChanged();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleModeChange(Message msg) {
        boolean aMWStatus = msg.getData().getBoolean("aMWStatus");
        synchronized (this.mIAwareCallbacks) {
            ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(THIRD_PARTY_KEY);
            if (callbacks != null) {
                Iterator<Stub> it = callbacks.iterator();
                while (it.hasNext()) {
                    it.next().thirdPartyOnModeChanged(aMWStatus);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFgProfileSwitch(Message msg) {
        if (msg.obj instanceof UserSwicthInfo) {
            UserSwicthInfo userSwicthInfo = (UserSwicthInfo) msg.obj;
            synchronized (this.mIAwareCallbacks) {
                ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(USERSWITCH_OB_KEY);
                if (callbacks != null) {
                    Iterator<Stub> it = callbacks.iterator();
                    while (it.hasNext()) {
                        it.next().switchOnForegroundProfileSwitch(userSwicthInfo.profileId);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSwitchComplete(Message msg) {
        if (msg.obj instanceof UserSwicthInfo) {
            UserSwicthInfo userSwicthInfo = (UserSwicthInfo) msg.obj;
            synchronized (this.mIAwareCallbacks) {
                ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(USERSWITCH_OB_KEY);
                if (callbacks != null) {
                    Iterator<Stub> it = callbacks.iterator();
                    while (it.hasNext()) {
                        it.next().switchOnUserSwitchComplete(userSwicthInfo.userId);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBootComplete(Message msg) {
        if (msg.obj instanceof UserSwicthInfo) {
            UserSwicthInfo userSwicthInfo = (UserSwicthInfo) msg.obj;
            synchronized (this.mIAwareCallbacks) {
                ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(USERSWITCH_OB_KEY);
                if (callbacks != null) {
                    Iterator<Stub> it = callbacks.iterator();
                    while (it.hasNext()) {
                        it.next().switchOnLockedBootComplete(userSwicthInfo.userId);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUserSwitch(Message msg) {
        if (msg.obj instanceof UserSwicthInfo) {
            UserSwicthInfo userSwicthInfo = (UserSwicthInfo) msg.obj;
            synchronized (this.mIAwareCallbacks) {
                ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(USERSWITCH_OB_KEY);
                if (callbacks != null) {
                    Iterator<Stub> it = callbacks.iterator();
                    while (it.hasNext()) {
                        it.next().switchOnUserSwitching(userSwicthInfo.userId, userSwicthInfo.reply);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleProcessDie(Message msg) {
        if (msg.obj instanceof PidInfo) {
            PidInfo pidInfo = (PidInfo) msg.obj;
            synchronized (this.mIAwareCallbacks) {
                ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(PROCESS_OB_KEY);
                if (callbacks != null) {
                    Iterator<Stub> it = callbacks.iterator();
                    while (it.hasNext()) {
                        it.next().processOnProcessDied(pidInfo.pid, pidInfo.uid);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleForeGroundChange(Message msg, boolean isForeground) {
        if (msg.obj instanceof PidInfo) {
            PidInfo pidInfo = (PidInfo) msg.obj;
            synchronized (this.mIAwareCallbacks) {
                ArraySet<Stub> callbacks = this.mIAwareCallbacks.get(PROCESS_OB_KEY);
                if (callbacks != null) {
                    Iterator<Stub> it = callbacks.iterator();
                    while (it.hasNext()) {
                        it.next().processOnForegroundActivitiesChanged(pidInfo.pid, pidInfo.uid, isForeground);
                    }
                }
            }
        }
    }
}

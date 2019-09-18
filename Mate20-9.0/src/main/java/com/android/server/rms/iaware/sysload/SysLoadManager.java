package com.android.server.rms.iaware.sysload;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.DeviceInfo;
import android.rms.iaware.ISceneCallback;
import android.util.ArrayMap;
import com.android.internal.os.BackgroundThread;
import com.android.server.input.HwInputManagerService;
import com.android.server.pfw.autostartup.comm.XmlConst;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SysLoadManager {
    private static final int FINGERSENSE_OPT = 4;
    private static final int GAME_SCENE_DELAYED = 0;
    private static final int INPUT_FILTER_OPT = 1;
    private static final int MSG_BASE_VALUE = 100;
    private static final int MSG_CHECK_STATUS = 108;
    private static final int MSG_CLOSE_FEATURE = 107;
    private static final int MSG_ENTER_GAME_SCENE = 101;
    private static final int MSG_EXIT_GAME_SCENE = 102;
    private static final int MSG_SINGLE_HAND_OFF = 106;
    private static final int MSG_SINGLE_HAND_ON = 105;
    private static final int MSG_WAKEUP_LOCK = 103;
    private static final int MSG_WAKEUP_RELEASE = 104;
    private static final int PROPERTY_OPT = 8;
    private static final int SINGLE_HAND_OPT = 2;
    private static final int SKIP_USER_ACTIVITY = 16;
    private static final String SYSLOAD_SINGLEHAND_TYPE = "LazyMode";
    private static final String TAG = "SysLoadManager";
    private static final AtomicBoolean mIsFeatureEnable = new AtomicBoolean(false);
    private static SysLoadManager sInstance;
    private static Object syncObject = new Object();
    private final ArrayMap<CallbackRecord, Integer> mCallbacks;
    private Context mContext;
    private HighLoadHandler mHighLoadHandler;
    private HwInputManagerService mInputManagerService;
    /* access modifiers changed from: private */
    public int mInputStatus;
    private int mInputStatusCache;
    /* access modifiers changed from: private */
    public AtomicBoolean mIsGameScene;
    /* access modifiers changed from: private */
    public int mLockNum;
    private PowerManager mPowerManager;
    private int mSingleMode;
    private final SyncRoot mSyncRoot;

    private final class CallbackRecord implements IBinder.DeathRecipient {
        private final ISceneCallback mCallback;
        public final int mUid;

        public CallbackRecord(int uid, ISceneCallback callback) {
            this.mUid = uid;
            this.mCallback = callback;
        }

        public void binderDied() {
            AwareLog.d(SysLoadManager.TAG, "GameScene listener for uid " + this.mUid + " died.");
            SysLoadManager.this.onCallbackDied(this);
        }

        public void notifySceneChangedAsync(boolean start, int scene) {
            try {
                this.mCallback.onSceneChanged(scene, start, 0, 0, "");
            } catch (RemoteException e) {
                AwareLog.e(SysLoadManager.TAG, "Failed to notify application " + this.mUid + " that game scene changed, assuming it died.");
                binderDied();
            }
        }
    }

    private class HighLoadHandler extends Handler {
        public HighLoadHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (AwareConstant.CURRENT_USER_TYPE == 3) {
                AwareLog.i(SysLoadManager.TAG, "handleMessage what = " + msg.what + " mLockNum:" + SysLoadManager.this.mLockNum + " gameMode:" + SysLoadManager.this.mIsGameScene.get());
            }
            switch (msg.what) {
                case 101:
                    SysLoadManager.this.setGameScene();
                    return;
                case 102:
                    SysLoadManager.this.resetGameScene();
                    return;
                case 103:
                    int unused = SysLoadManager.this.mLockNum = SysLoadManager.this.mLockNum + 1;
                    if (SysLoadManager.this.mLockNum == 1) {
                        SysLoadManager.this.setIawareGameMode(true);
                        return;
                    }
                    return;
                case 104:
                    SysLoadManager.access$010(SysLoadManager.this);
                    if (SysLoadManager.this.mLockNum <= 0) {
                        SysLoadManager.this.setIawareGameMode(false);
                        int unused2 = SysLoadManager.this.mLockNum = 0;
                        return;
                    }
                    return;
                case 105:
                    SysLoadManager.this.sendInputAwareSingleMode(true);
                    return;
                case 106:
                    SysLoadManager.this.sendInputAwareSingleMode(false);
                    return;
                case 107:
                    int unused3 = SysLoadManager.this.mLockNum = 0;
                    return;
                case 108:
                    if (SysLoadManager.this.mIsGameScene.get()) {
                        AwareLog.w(SysLoadManager.TAG, "check status current is game mode");
                        SysLoadManager.this.resetGameScene();
                        return;
                    } else if (SysLoadManager.this.mLockNum > 0) {
                        int unused4 = SysLoadManager.this.mLockNum = 0;
                        int unused5 = SysLoadManager.this.mInputStatus = SysLoadManager.this.mInputStatus & -17;
                        SysLoadManager.this.inputOptDisable();
                        return;
                    } else {
                        return;
                    }
                default:
                    AwareLog.e(SysLoadManager.TAG, "error msg what = " + msg.what);
                    return;
            }
        }
    }

    public static final class SyncRoot {
    }

    static /* synthetic */ int access$010(SysLoadManager x0) {
        int i = x0.mLockNum;
        x0.mLockNum = i - 1;
        return i;
    }

    private SysLoadManager() {
        this.mHighLoadHandler = null;
        this.mIsGameScene = new AtomicBoolean(false);
        this.mLockNum = 0;
        this.mCallbacks = new ArrayMap<>();
        this.mSyncRoot = new SyncRoot();
        this.mInputManagerService = null;
        this.mPowerManager = null;
        this.mContext = null;
        this.mInputStatus = 2;
        this.mInputStatusCache = -1;
        this.mSingleMode = 0;
        this.mHighLoadHandler = new HighLoadHandler(BackgroundThread.get().getLooper());
    }

    public static SysLoadManager getInstance() {
        SysLoadManager sysLoadManager;
        synchronized (syncObject) {
            if (sInstance == null) {
                sInstance = new SysLoadManager();
            }
            sysLoadManager = sInstance;
        }
        return sysLoadManager;
    }

    /* access modifiers changed from: private */
    public final void setIawareGameMode(boolean lock) {
        if (this.mIsGameScene.get()) {
            if (lock) {
                this.mInputStatus |= 16;
            } else {
                this.mInputStatus &= -17;
            }
            sendInputIawareMode(this.mInputStatus);
        }
    }

    /* access modifiers changed from: private */
    public final void sendInputAwareSingleMode(boolean singleMode) {
        if (singleMode) {
            this.mInputStatus &= -3;
        } else {
            this.mInputStatus |= 2;
        }
        sendInputIawareMode(this.mInputStatus);
    }

    private final void sendInputIawareMode(int status) {
        if (this.mInputManagerService != null && this.mInputStatusCache != status) {
            if (AwareConstant.CURRENT_USER_TYPE == 3) {
                AwareLog.i(TAG, "send input mode finished cur mode:" + this.mInputStatusCache + " to mode:" + status);
            }
            this.mInputStatusCache = status;
            this.mInputManagerService.setIawareGameMode(this.mInputStatusCache);
        }
    }

    public void enable() {
        if (mIsFeatureEnable.get()) {
            AwareLog.d(TAG, "SysLoadManager has already enable!");
        } else if (DeviceInfo.getDeviceLevel() < 0) {
            AwareLog.e(TAG, "Device Level unknow!");
        } else {
            mIsFeatureEnable.set(true);
        }
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public void disable() {
        if (!mIsFeatureEnable.get()) {
            AwareLog.d(TAG, "SysLoadManager has already disable!");
            return;
        }
        mIsFeatureEnable.set(false);
        this.mHighLoadHandler.sendEmptyMessageDelayed(107, 0);
        if (this.mIsGameScene.get()) {
            inputOptDisable();
        }
    }

    public void enterGameSceneMsg() {
        if (mIsFeatureEnable.get()) {
            this.mHighLoadHandler.removeMessages(101);
            this.mHighLoadHandler.sendEmptyMessageDelayed(101, 0);
        }
    }

    public void exitGameSceneMsg() {
        if (mIsFeatureEnable.get()) {
            this.mHighLoadHandler.removeMessages(102);
            this.mHighLoadHandler.sendEmptyMessageDelayed(102, 0);
        }
    }

    public void enterLauncher() {
        if (mIsFeatureEnable.get()) {
            this.mHighLoadHandler.sendEmptyMessageDelayed(108, 0);
        }
    }

    public void setInputManagerService(HwInputManagerService inputManagerService) {
        this.mInputManagerService = inputManagerService;
    }

    private void inputOptEnable() {
        if (DeviceInfo.getDeviceLevel() > 1) {
            this.mInputStatus |= 4;
        }
        if (this.mLockNum > 0) {
            this.mInputStatus |= 16;
        }
        sendInputIawareMode(this.mInputStatus);
    }

    /* access modifiers changed from: private */
    public void inputOptDisable() {
        if (DeviceInfo.getDeviceLevel() > 1) {
            this.mInputStatus &= -5;
        }
        this.mLockNum = 0;
        this.mInputStatus &= -17;
        sendInputIawareMode(this.mInputStatus);
    }

    public void notifyWakeLock(int uid, int pid, String packageName, String tag) {
        if (!mIsFeatureEnable.get()) {
            AwareLog.d(TAG, "SysLoadOpt has already disable!");
        } else if (this.mInputManagerService != null) {
            AwareLog.d(TAG, "acquire wakelock, pid: " + pid + ", uid: " + uid + ", packageName: " + packageName + ", tag: " + tag);
            this.mHighLoadHandler.sendEmptyMessageDelayed(103, 0);
        }
    }

    public void notifyWakeLockRelease(int uid, int pid, String packageName, String tag) {
        if (!mIsFeatureEnable.get()) {
            AwareLog.d(TAG, "SysLoadOpt has already disable!");
        } else if (this.mInputManagerService != null) {
            AwareLog.d(TAG, "release wakelock, pid: " + pid + ", uid: " + uid + ", packageName: " + packageName + ", tag: " + tag);
            if (this.mIsGameScene.get()) {
                notifyToPMSUserActivity();
            }
            this.mHighLoadHandler.sendEmptyMessageDelayed(104, 0);
        }
    }

    private void notifyToPMSUserActivity() {
        if (this.mPowerManager == null && this.mContext != null) {
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        }
        if (this.mPowerManager != null) {
            this.mPowerManager.userActivity(SystemClock.uptimeMillis(), 2, 0);
        } else {
            AwareLog.e(TAG, "power manager service is null");
        }
    }

    /* access modifiers changed from: private */
    public void setGameScene() {
        if (mIsFeatureEnable.get()) {
            this.mIsGameScene.set(true);
            dispatchGameSceneChanged(true);
            inputOptEnable();
        }
    }

    /* access modifiers changed from: private */
    public void resetGameScene() {
        if (mIsFeatureEnable.get()) {
            if (this.mLockNum > 0) {
                notifyToPMSUserActivity();
            }
            this.mIsGameScene.set(false);
            dispatchGameSceneChanged(false);
            inputOptDisable();
        }
    }

    public boolean isScene(int scene) {
        if (scene != 2) {
            return false;
        }
        return this.mIsGameScene.get();
    }

    public void reportData(CollectData data) {
        if (data != null) {
            Bundle bundle = data.getBundle();
            if (bundle != null) {
                this.mSingleMode = bundle.getInt(SYSLOAD_SINGLEHAND_TYPE);
                if (this.mSingleMode == 0) {
                    this.mHighLoadHandler.sendEmptyMessageDelayed(106, 0);
                } else {
                    this.mHighLoadHandler.sendEmptyMessageDelayed(105, 0);
                }
                StringBuilder sb = new StringBuilder();
                sb.append("current SingleMode:");
                sb.append(this.mSingleMode == 0 ? "off" : XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                AwareLog.d(TAG, sb.toString());
            }
        }
    }

    public boolean isLiteSysLoadEnable() {
        return mIsFeatureEnable.get();
    }

    public void registerCallback(ISceneCallback callback, int scene) {
        if (mIsFeatureEnable.get() && callback != null) {
            int callingUid = Binder.getCallingUid();
            AwareLog.d(TAG, "game scene registerCallback callback " + callback + " callingUid " + callingUid + " callingPid " + Binder.getCallingPid());
            synchronized (this.mSyncRoot) {
                CallbackRecord record = new CallbackRecord(callingUid, callback);
                try {
                    callback.asBinder().linkToDeath(record, 0);
                } catch (RemoteException e) {
                    AwareLog.e(TAG, "Couldn't register for the death!!!!!");
                }
                this.mCallbacks.put(record, Integer.valueOf(scene));
                if ((scene & 3) != 0) {
                    record.notifySceneChangedAsync(this.mIsGameScene.get(), 2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onCallbackDied(CallbackRecord record) {
        synchronized (this.mSyncRoot) {
            this.mCallbacks.remove(record);
        }
    }

    private void dispatchGameSceneChanged(boolean start) {
        ArrayList<CallbackRecord> tempCallbacks = new ArrayList<>();
        synchronized (this.mSyncRoot) {
            tempCallbacks.clear();
            for (Map.Entry<CallbackRecord, Integer> m : this.mCallbacks.entrySet()) {
                CallbackRecord callback = m.getKey();
                Integer secens = m.getValue();
                if (!(secens == null || (secens.intValue() & 3) == 0)) {
                    tempCallbacks.add(callback);
                }
            }
        }
        int count = tempCallbacks.size();
        for (int i = 0; i < count; i++) {
            tempCallbacks.get(i).notifySceneChangedAsync(start, 2);
        }
        tempCallbacks.clear();
        AwareLog.d(TAG, "dispatchGameSceneChanged count " + count);
    }
}

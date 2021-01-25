package com.android.server.rms.iaware.sysload;

import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.DeviceInfo;
import android.rms.iaware.ISceneCallback;
import android.util.ArrayMap;
import com.android.server.input.InputManagerServiceEx;
import com.huawei.android.os.PowerManagerExt;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SysLoadManager {
    private static final int GAME_SCENE_DELAYED = 0;
    private static final Object LOCK = new Object();
    private static final int MSG_BASE_VALUE = 100;
    private static final int MSG_CHECK_STATUS = 106;
    private static final int MSG_CLOSE_FEATURE = 105;
    private static final int MSG_ENTER_GAME_SCENE = 101;
    private static final int MSG_EXIT_GAME_SCENE = 102;
    private static final int MSG_WAKEUP_LOCK = 103;
    private static final int MSG_WAKEUP_RELEASE = 104;
    private static final int SKIP_USER_ACTIVITY = 16;
    private static final String TAG = "SysLoadManager";
    private static SysLoadManager sInstance;
    private static AtomicBoolean sIsFeatureEnable = new AtomicBoolean(false);
    private final ArrayMap<CallbackRecord, Integer> mCallbacks = new ArrayMap<>();
    private Context mContext = null;
    private HighLoadHandler mHighLoadHandler = null;
    private InputManagerServiceEx mInputManagerService = null;
    private int mInputStatus = 0;
    private int mInputStatusCache = -1;
    private AtomicBoolean mIsGameScene = new AtomicBoolean(false);
    private int mLockNum = 0;
    private PowerManager mPowerManager = null;
    private final SyncRoot mSyncRoot = new SyncRoot();

    public static final class SyncRoot {
    }

    static /* synthetic */ int access$008(SysLoadManager x0) {
        int i = x0.mLockNum;
        x0.mLockNum = i + 1;
        return i;
    }

    static /* synthetic */ int access$010(SysLoadManager x0) {
        int i = x0.mLockNum;
        x0.mLockNum = i - 1;
        return i;
    }

    private SysLoadManager() {
        initHandler();
    }

    public static SysLoadManager getInstance() {
        SysLoadManager sysLoadManager;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new SysLoadManager();
            }
            sysLoadManager = sInstance;
        }
        return sysLoadManager;
    }

    private void initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mHighLoadHandler = new HighLoadHandler(looper);
        } else {
            this.mHighLoadHandler = new HighLoadHandler(BackgroundThreadEx.getLooper());
        }
    }

    /* access modifiers changed from: private */
    public class HighLoadHandler extends Handler {
        public HighLoadHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (AwareConstant.CURRENT_USER_TYPE == 3) {
                AwareLog.i(SysLoadManager.TAG, "handleMessage what = " + msg.what + " mLockNum: " + SysLoadManager.this.mLockNum + " gameMode: " + SysLoadManager.this.mIsGameScene.get());
            }
            switch (msg.what) {
                case 101:
                    SysLoadManager.this.setGameScene();
                    return;
                case 102:
                    SysLoadManager.this.resetGameScene();
                    return;
                case 103:
                    SysLoadManager.access$008(SysLoadManager.this);
                    if (SysLoadManager.this.mLockNum == 1) {
                        SysLoadManager.this.setIawareGameMode(true);
                        return;
                    }
                    return;
                case 104:
                    SysLoadManager.access$010(SysLoadManager.this);
                    if (SysLoadManager.this.mLockNum <= 0) {
                        SysLoadManager.this.setIawareGameMode(false);
                        SysLoadManager.this.mLockNum = 0;
                        return;
                    }
                    return;
                case 105:
                    SysLoadManager.this.mLockNum = 0;
                    return;
                case 106:
                    if (SysLoadManager.this.mIsGameScene.get()) {
                        AwareLog.w(SysLoadManager.TAG, "check status current is game mode");
                        SysLoadManager.this.resetGameScene();
                        return;
                    } else if (SysLoadManager.this.mLockNum > 0) {
                        SysLoadManager.this.mLockNum = 0;
                        SysLoadManager.this.mInputStatus &= -17;
                        SysLoadManager.this.inputOptDisable();
                        return;
                    } else {
                        AwareLog.w(SysLoadManager.TAG, "check status do nothing");
                        return;
                    }
                default:
                    AwareLog.e(SysLoadManager.TAG, "error msg what = " + msg.what);
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setIawareGameMode(boolean lock) {
        if (this.mIsGameScene.get()) {
            if (lock) {
                this.mInputStatus |= 16;
            } else {
                this.mInputStatus &= -17;
            }
            sendInputIawareMode(this.mInputStatus);
        }
    }

    private void sendInputIawareMode(int status) {
        if (this.mInputManagerService != null && this.mInputStatusCache != status) {
            if (AwareConstant.CURRENT_USER_TYPE == 3) {
                AwareLog.i(TAG, "send input mode finished cur mode: " + this.mInputStatusCache + " to mode: " + status);
            }
            this.mInputStatusCache = status;
            this.mInputManagerService.setIawareGameMode(this.mInputStatusCache);
        }
    }

    public void enable() {
        if (sIsFeatureEnable.get()) {
            AwareLog.d(TAG, "SysLoadManager has already enable!");
        } else if (DeviceInfo.getDeviceLevel() < 0) {
            AwareLog.e(TAG, "Device Level unknow!");
        } else {
            sIsFeatureEnable.set(true);
        }
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public void disable() {
        if (!sIsFeatureEnable.get()) {
            AwareLog.d(TAG, "SysLoadManager has already disable!");
            return;
        }
        sIsFeatureEnable.set(false);
        this.mHighLoadHandler.sendEmptyMessageDelayed(105, 0);
        if (this.mIsGameScene.get()) {
            inputOptDisable();
        }
    }

    public void enterGameSceneMsg() {
        if (sIsFeatureEnable.get()) {
            this.mHighLoadHandler.removeMessages(101);
            this.mHighLoadHandler.sendEmptyMessageDelayed(101, 0);
        }
    }

    public void exitGameSceneMsg() {
        if (sIsFeatureEnable.get()) {
            this.mHighLoadHandler.removeMessages(102);
            this.mHighLoadHandler.sendEmptyMessageDelayed(102, 0);
        }
    }

    public void enterLauncher() {
        if (sIsFeatureEnable.get()) {
            this.mHighLoadHandler.sendEmptyMessageDelayed(106, 0);
        }
    }

    public void setInputManagerService(InputManagerServiceEx inputManagerService) {
        this.mInputManagerService = inputManagerService;
    }

    private void inputOptEnable() {
        if (this.mLockNum > 0) {
            this.mInputStatus |= 16;
        }
        sendInputIawareMode(this.mInputStatus);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void inputOptDisable() {
        this.mLockNum = 0;
        this.mInputStatus &= -17;
        sendInputIawareMode(this.mInputStatus);
    }

    public void notifyWakeLock(int uid, int pid, String packageName, String tag) {
        if (!sIsFeatureEnable.get()) {
            AwareLog.d(TAG, "SysLoadOpt has already disable!");
        } else if (this.mInputManagerService != null) {
            AwareLog.d(TAG, "acquire wakelock, pid: " + pid + ", uid: " + uid + ", packageName: " + packageName + ", tag: " + tag);
            this.mHighLoadHandler.sendEmptyMessageDelayed(103, 0);
        }
    }

    public void notifyWakeLockRelease(int uid, int pid, String packageName, String tag) {
        if (!sIsFeatureEnable.get()) {
            AwareLog.d(TAG, "SysLoadOpt has already disable!");
        } else if (this.mInputManagerService != null) {
            AwareLog.d(TAG, "release wakelock, pid: " + pid + ", uid: " + uid + ", packageName: " + packageName + ", tag: " + tag);
            if (this.mIsGameScene.get()) {
                notifyToPmsUserActivity();
            }
            this.mHighLoadHandler.sendEmptyMessageDelayed(104, 0);
        }
    }

    private void notifyToPmsUserActivity() {
        Context context;
        if (this.mPowerManager == null && (context = this.mContext) != null) {
            Object obj = context.getSystemService("power");
            if (obj instanceof PowerManager) {
                this.mPowerManager = (PowerManager) obj;
            }
        }
        PowerManager powerManager = this.mPowerManager;
        if (powerManager != null) {
            PowerManagerExt.userActivity(powerManager, SystemClock.uptimeMillis(), 2, 0);
        } else {
            AwareLog.e(TAG, "power manager service is null");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setGameScene() {
        if (sIsFeatureEnable.get()) {
            this.mIsGameScene.set(true);
            dispatchGameSceneChanged(true);
            inputOptEnable();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetGameScene() {
        if (sIsFeatureEnable.get()) {
            if (this.mLockNum > 0) {
                notifyToPmsUserActivity();
            }
            this.mIsGameScene.set(false);
            dispatchGameSceneChanged(false);
            inputOptDisable();
        }
    }

    public void registerCallback(ISceneCallback callback, int scene) {
        if (sIsFeatureEnable.get() && callback != null) {
            int callingUid = Binder.getCallingUid();
            AwareLog.d(TAG, "game scene registerCallback callback " + callback + " callingUid " + callingUid + " callingPid " + Binder.getCallingPid());
            synchronized (this.mSyncRoot) {
                CallbackRecord record = new CallbackRecord(callingUid, callback);
                try {
                    callback.asBinder().linkToDeath(record, 0);
                } catch (RemoteException e) {
                    AwareLog.e(TAG, "Couldn't register for the death!");
                }
                this.mCallbacks.put(record, Integer.valueOf(scene));
                if ((scene & 3) != 0) {
                    record.notifySceneChangedAsync(this.mIsGameScene.get(), 2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCallbackDied(CallbackRecord record) {
        synchronized (this.mSyncRoot) {
            this.mCallbacks.remove(record);
        }
    }

    private void dispatchGameSceneChanged(boolean start) {
        ArrayList<CallbackRecord> tempCallbacks = new ArrayList<>();
        synchronized (this.mSyncRoot) {
            tempCallbacks.clear();
            for (Map.Entry<CallbackRecord, Integer> entry : this.mCallbacks.entrySet()) {
                CallbackRecord callback = entry.getKey();
                Integer secens = entry.getValue();
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

    /* access modifiers changed from: private */
    public final class CallbackRecord implements IBinder.DeathRecipient {
        private final ISceneCallback mCallback;
        private final int mUid;

        public CallbackRecord(int uid, ISceneCallback callback) {
            this.mUid = uid;
            this.mCallback = callback;
        }

        @Override // android.os.IBinder.DeathRecipient
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
}

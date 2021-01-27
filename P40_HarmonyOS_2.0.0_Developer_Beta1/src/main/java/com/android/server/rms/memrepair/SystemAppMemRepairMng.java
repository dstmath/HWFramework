package com.android.server.rms.memrepair;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.rms.iaware.memory.data.handle.DataAppHandle;
import com.android.server.rms.iaware.memory.policy.AbsMemoryExecutor;
import com.android.server.rms.iaware.memory.policy.SystemTrimPolicy;
import com.huawei.android.app.HwActivityTaskManagerAdapter;
import com.huawei.android.os.IMWThirdpartyCallbackEx;
import com.huawei.iaware.AwareServiceThread;
import com.huawei.internal.os.BackgroundThreadEx;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SystemAppMemRepairMng {
    private static final long KEEP_ALIVE_TIME = 30;
    private static final Object LOCK = new Object();
    private static final long MAX_EXCUTE_INTERVAL = 1440;
    private static final long MAX_THRESHOLD = 1048576;
    private static final long MILLISECOND_OF_MINUTE = 60000;
    private static final long MIN_EXCUTE_INTERVAL = 1;
    private static final long MIN_THRESHOLD = 0;
    private static final int MSG_LOW_MEMORY = 103;
    private static final int MSG_MID_NIGHT = 101;
    private static final int MSG_SCREEN_OFF = 102;
    private static final int MSG_SYSTEM_MAMANGER_CLEAN = 104;
    public static final int POLICY_KILL = 1;
    public static final int POLICY_TRIM = 0;
    private static final int SCENE_TYPE_LOW_MEMORY = 4;
    private static final int SCENE_TYPE_MID_NIGHT = 1;
    private static final int SCENE_TYPE_MIN = 1;
    private static final int SCENE_TYPE_SCREEN_OFF = 2;
    private static final int SCENE_TYPE_SYSTEM_MANAGER_CLEAN = 8;
    private static final String TAG = "SystemAppMemRepairMng";
    private static SystemAppMemRepairMng sInstance = null;
    private static long sLastExecuteTime = 0;
    private final Map<MemRepairScene, SystemAppMemRepairDefaultAction> mActions = new ArrayMap();
    private Context mContext = null;
    private final AtomicBoolean mEnable = new AtomicBoolean(false);
    private Handler mHandler = null;
    private final AtomicBoolean mInterrupted = new AtomicBoolean(false);
    private long mInterval = 10;
    private AtomicBoolean mIsMultiWindow = new AtomicBoolean(false);
    private ThreadPoolExecutor mMemPrepairExecutor = null;
    private SysMemCallBackHandler mSysMemCallBackHandler = new SysMemCallBackHandler(this, null);

    public enum Policy {
        TRIM("on-trim"),
        KILL("kill-9");
        
        private String mDescription;

        private Policy(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    public enum MemRepairScene {
        NONE("none"),
        MID_NIGHT("mid-night"),
        SCREEN_OFF("screen-off"),
        LOW_MEMORY("low-memory"),
        SYSTEM_MANAGER_CLEAN("system_manager_clean");
        
        private String mDescription;

        private MemRepairScene(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    private SystemAppMemRepairMng() {
        initHandler();
        this.mMemPrepairExecutor = new ThreadPoolExecutor(0, 1, (long) KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue(1), new AbsMemoryExecutor.MemThreadFactory("iaware.mem.prepair"));
        synchronized (this.mActions) {
            this.mActions.put(MemRepairScene.LOW_MEMORY, new SystemAppMemRepairDefaultAction(MemRepairScene.LOW_MEMORY.description()));
            this.mActions.put(MemRepairScene.MID_NIGHT, new SystemAppMemRepairDefaultAction(MemRepairScene.MID_NIGHT.description()));
            this.mActions.put(MemRepairScene.SCREEN_OFF, new SystemAppMemRepairDefaultAction(MemRepairScene.SCREEN_OFF.description()));
            this.mActions.put(MemRepairScene.SYSTEM_MANAGER_CLEAN, new SystemAppMemRepairDefaultAction(MemRepairScene.SYSTEM_MANAGER_CLEAN.description()));
        }
    }

    private void initHandler() {
        Looper looper = AwareServiceThread.getInstance().getLooper();
        if (looper != null) {
            this.mHandler = new SystemMemRepairHandler(looper);
        } else {
            this.mHandler = new SystemMemRepairHandler(BackgroundThreadEx.getLooper());
        }
    }

    public static SystemAppMemRepairMng getInstance() {
        SystemAppMemRepairMng systemAppMemRepairMng;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new SystemAppMemRepairMng();
            }
            systemAppMemRepairMng = sInstance;
        }
        return systemAppMemRepairMng;
    }

    public void enable() {
        if (!this.mEnable.get()) {
            SystemTrimPolicy.getInstance().enable();
            MultiTaskManagerService mtmService = MultiTaskManagerService.self();
            if (mtmService != null) {
                this.mContext = mtmService.context();
            }
            if (this.mContext != null) {
                this.mEnable.set(true);
            } else {
                AwareLog.w(TAG, "Get MtmServices failed, set feature disable");
            }
            HwActivityTaskManagerAdapter.registerThirdPartyCallBack(this.mSysMemCallBackHandler);
        }
    }

    public void disable() {
        if (this.mEnable.get()) {
            SystemTrimPolicy.getInstance().disable();
            this.mEnable.set(false);
            HwActivityTaskManagerAdapter.unregisterThirdPartyCallBack(this.mSysMemCallBackHandler);
        }
    }

    public void setTriggerInterval(long interval) {
        if (interval <= MAX_EXCUTE_INTERVAL && interval >= MIN_EXCUTE_INTERVAL) {
            this.mInterval = interval;
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(TAG, "mInterval is : " + this.mInterval + "min");
            }
        }
    }

    public void interrupt(boolean interrupt) {
        this.mInterrupted.set(interrupt);
    }

    public void configSystemAppThreshold(String packageName, long threshold, int policy, int scene) {
        if (packageName == null || policy < 0) {
            AwareLog.w(TAG, "packageName or policy is invalid");
        } else if (threshold <= 0 || threshold > 1048576) {
            AwareLog.w(TAG, "threshold is invalid");
        } else if (policy == Policy.TRIM.ordinal()) {
            configTrimPolicyThreshold(packageName, threshold);
        } else if (policy == Policy.KILL.ordinal()) {
            configKillPolicyThreshold(packageName, threshold, policy, scene);
        } else {
            AwareLog.w(TAG, "policy is invalid");
        }
    }

    public void reportData(int eventId) {
        if (this.mEnable.get()) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = -1;
            if (eventId == 20011) {
                this.mInterrupted.set(true);
            } else if (eventId == 20027) {
                msg.what = 101;
            } else if (eventId == 20029) {
                msg.what = 103;
            } else if (eventId == 20031) {
                msg.what = 104;
            } else if (eventId == 90011) {
                msg.what = 102;
                this.mInterrupted.set(true);
            }
            if (msg.what != -1) {
                this.mHandler.sendMessage(msg);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetInterrupt() {
        if (this.mInterrupted.get()) {
            interrupt(false);
        }
    }

    private void configTrimPolicyThreshold(String packageName, long threshold) {
        SystemTrimPolicy.getInstance().updateProcThreshold(packageName, threshold);
    }

    private void configKillPolicyThreshold(String packageName, long threshold, int policy, int sceneValue) {
        if (sceneValue < 0) {
            AwareLog.w(TAG, "scene is invalid");
            return;
        }
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "configKillPolicyThreshold:packageName=" + packageName + ",threshold=" + threshold + ",policy=" + policy + ",sceneValue=" + sceneValue);
        }
        synchronized (this.mActions) {
            if ((sceneValue & 1) != 0) {
                try {
                    this.mActions.get(MemRepairScene.MID_NIGHT).updateThreshold(packageName, threshold);
                } catch (Throwable th) {
                    throw th;
                }
            }
            if ((sceneValue & 2) != 0) {
                this.mActions.get(MemRepairScene.SCREEN_OFF).updateThreshold(packageName, threshold);
            }
            if ((sceneValue & 4) != 0) {
                this.mActions.get(MemRepairScene.LOW_MEMORY).updateThreshold(packageName, threshold);
            }
            if ((sceneValue & 8) != 0) {
                this.mActions.get(MemRepairScene.SYSTEM_MANAGER_CLEAN).updateThreshold(packageName, threshold);
            }
        }
    }

    /* access modifiers changed from: private */
    public class SystemMemRepairHandler extends Handler {
        SystemMemRepairHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            long interval = SystemClock.elapsedRealtime() - SystemAppMemRepairMng.sLastExecuteTime;
            AwareLog.i(SystemAppMemRepairMng.TAG, "SysMemMng ReportData Msg.what = " + msg.what);
            if (SystemAppMemRepairMng.sLastExecuteTime != 0 && interval < SystemAppMemRepairMng.this.mInterval * 60000) {
                AwareLog.d(SystemAppMemRepairMng.TAG, "last excute was at " + SystemAppMemRepairMng.sLastExecuteTime + " ms  currentInterval is " + interval + " ms  ConfigInterval is " + SystemAppMemRepairMng.this.mInterval + " mins ");
            } else if (SystemAppMemRepairMng.this.mIsMultiWindow.get()) {
                AwareLog.d(SystemAppMemRepairMng.TAG, "trigger abort cause in multiWindow mode");
            } else {
                MemRepairScene scene = null;
                switch (msg.what) {
                    case 101:
                        scene = MemRepairScene.MID_NIGHT;
                        break;
                    case 102:
                        scene = MemRepairScene.SCREEN_OFF;
                        break;
                    case 103:
                        scene = MemRepairScene.LOW_MEMORY;
                        break;
                    case 104:
                        scene = MemRepairScene.SYSTEM_MANAGER_CLEAN;
                        break;
                }
                if (scene != null) {
                    AwareLog.i(SystemAppMemRepairMng.TAG, "handleMessage now,scene = " + scene);
                    SystemAppMemRepairMng.this.resetInterrupt();
                    SystemAppMemRepairMng.this.mMemPrepairExecutor.execute(new MemRepairRunnable(scene, DataAppHandle.getInstance().createBundleFromAppInfo()));
                    long unused = SystemAppMemRepairMng.sLastExecuteTime = SystemClock.elapsedRealtime();
                }
            }
        }
    }

    private final class MemRepairRunnable implements Runnable {
        private Bundle mExtras;
        private MemRepairScene mScene;

        MemRepairRunnable(MemRepairScene scene, Bundle extras) {
            this.mScene = scene;
            this.mExtras = extras;
        }

        @Override // java.lang.Runnable
        public void run() {
            SystemAppMemRepairDefaultAction action;
            synchronized (SystemAppMemRepairMng.this.mActions) {
                action = (SystemAppMemRepairDefaultAction) SystemAppMemRepairMng.this.mActions.get(this.mScene);
            }
            if (SystemAppMemRepairMng.this.mEnable.get()) {
                if (action != null) {
                    action.excute(SystemAppMemRepairMng.this.mContext, this.mExtras, SystemAppMemRepairMng.this.mInterrupted);
                }
                if (AnonymousClass1.$SwitchMap$com$android$server$rms$memrepair$SystemAppMemRepairMng$MemRepairScene[this.mScene.ordinal()] == 1) {
                    NativeAppMemRepair.getInstance().doMemRepair(SystemAppMemRepairMng.this.mInterrupted);
                }
                SystemAppMemRepairMng.this.resetInterrupt();
                return;
            }
            AwareLog.i(SystemAppMemRepairMng.TAG, "System app Mem prepair feature is disable");
        }
    }

    /* renamed from: com.android.server.rms.memrepair.SystemAppMemRepairMng$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$rms$memrepair$SystemAppMemRepairMng$MemRepairScene = new int[MemRepairScene.values().length];

        static {
            try {
                $SwitchMap$com$android$server$rms$memrepair$SystemAppMemRepairMng$MemRepairScene[MemRepairScene.MID_NIGHT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    private class SysMemCallBackHandler extends IMWThirdpartyCallbackEx {
        private SysMemCallBackHandler() {
        }

        /* synthetic */ SysMemCallBackHandler(SystemAppMemRepairMng x0, AnonymousClass1 x1) {
            this();
        }

        public void onModeChanged(boolean status) {
            SystemAppMemRepairMng.this.mIsMultiWindow.set(status);
            if (AwareLog.getDebugLogSwitch()) {
                AwareLog.d(SystemAppMemRepairMng.TAG, "MultiWindowMode is " + SystemAppMemRepairMng.this.mIsMultiWindow.get());
            }
        }

        public void onZoneChanged() {
        }

        public void onSizeChanged() {
        }
    }
}

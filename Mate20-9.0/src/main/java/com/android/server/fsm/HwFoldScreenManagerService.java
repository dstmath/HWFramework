package com.android.server.fsm;

import android.app.ActivityManagerInternal;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Point;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.CoordinationModeUtils;
import android.util.Slog;
import com.android.internal.util.DumpUtils;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import com.android.server.policy.WindowManagerPolicy;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.fsm.IFoldDisplayModeListener;
import com.huawei.android.fsm.IFoldableStateListener;
import com.huawei.android.fsm.IHwFoldScreenManager;
import huawei.android.hardware.tp.HwTpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class HwFoldScreenManagerService extends SystemService implements Watchdog.Monitor {
    public static final int EYE_PROTECTIION_OFF = 0;
    public static final int EYE_PROTECTIION_ON = 1;
    private static final int FSM_FORCE_WAKEUP_CMD = 3;
    private static final int FSM_MAGNETOMETER_NOTIFY_SLEEP = 5;
    private static final int FSM_MAGNETOMETER_TURNOFF_SENSOR = 4;
    private static final int FSM_NOTIFY_DISPLAYMODE_CHANGE_CMD = 2;
    private static final int FSM_NOTIFY_FOLDSTATE_CHANGE_CMD = 1;
    private static final int FSM_NOTIFY_POSTURE_CHANGE_CMD = 0;
    public static final String KEY_EYES_PROTECTION = "eyes_protection_mode";
    private static final String PERMISSION_FOLD_SCREEN = "com.huawei.permission.MANAGE_FOLD_SCREEN";
    private static final String TAG = "Fsm_FoldScreenManagerService";
    private ContentObserver mContentObserver;
    /* access modifiers changed from: private */
    public final Context mContext;
    final RemoteCallbackList<IFoldDisplayModeListener> mDisplayModeListeners = new RemoteCallbackList<>();
    private DoubleClickWakeupManager mDoubleClickWakeup;
    /* access modifiers changed from: private */
    public boolean mFingerPrintReady;
    private FingerPrintWakeupManager mFingerprintWakeup;
    final RemoteCallbackList<IFoldableStateListener> mFoldableStateListeners = new RemoteCallbackList<>();
    final HwFoldScreenManagerHandler mHandler;
    final ServiceThread mHandlerThread;
    private boolean mIsDisplayLocked;
    boolean mIsIntelligent = true;
    private MagnetometerWakeupManager mMagnetometerWakeup;
    private WindowManagerPolicy mPolicy;
    final PostureStateMachine mPostureSM;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    private PowerWakeupManager mPowerWakeup;
    final PosturePreprocessManager mPreprocess;

    private final class BinderService extends IHwFoldScreenManager.Stub {
        private BinderService() {
        }

        public int getPosture() {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                return HwFoldScreenManagerService.this.getPostureInner();
            }
            throw new SecurityException("Requires MANAGE_FOLD_SCREEN permission");
        }

        public int getFoldableState() {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                return HwFoldScreenManagerService.this.getFoldableStateInner();
            }
            throw new SecurityException("Requires MANAGE_FOLD_SCREEN permission");
        }

        public void registerFoldableState(IFoldableStateListener listener, int type) {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                synchronized (this) {
                    HwFoldScreenManagerService.this.mFoldableStateListeners.register(listener, new Integer(type));
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_FOLD_SCREEN permission");
        }

        public void unregisterFoldableState(IFoldableStateListener listener) {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                synchronized (this) {
                    HwFoldScreenManagerService.this.mFoldableStateListeners.unregister(listener);
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_FOLD_SCREEN permission");
        }

        public int setDisplayMode(int mode) {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                Slog.i("Fsm_FoldScreenManagerService", "setDisplayMode mode=" + mode);
                return HwFoldScreenManagerService.this.setDisplayModeInner(mode);
            }
            throw new SecurityException("Requires MANAGE_FOLD_SCREEN permission");
        }

        public int getDisplayMode() {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                return HwFoldScreenManagerService.this.getDisplayModeInner();
            }
            throw new SecurityException("Requires MANAGE_FOLD_SCREEN permission");
        }

        public int lockDisplayMode(int mode) {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                return 0;
            }
            throw new SecurityException("Requires MANAGE_FOLD_SCREEN permission");
        }

        public int unlockDisplayMode() {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                return 0;
            }
            throw new SecurityException("Requires MANAGE_FOLD_SCREEN permission");
        }

        public void registerFoldDisplayMode(IFoldDisplayModeListener listener) {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                synchronized (this) {
                    HwFoldScreenManagerService.this.mDisplayModeListeners.register(listener);
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_FOLD_SCREEN permission");
        }

        public void unregisterFoldDisplayMode(IFoldDisplayModeListener listener) {
            if (Binder.getCallingUid() == 1000 || HwFoldScreenManagerService.this.mContext.checkCallingPermission(HwFoldScreenManagerService.PERMISSION_FOLD_SCREEN) == 0) {
                synchronized (this) {
                    HwFoldScreenManagerService.this.mDisplayModeListeners.unregister(listener);
                }
                return;
            }
            throw new SecurityException("Requires MANAGE_FOLD_SCREEN permission");
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(HwFoldScreenManagerService.this.mContext, "Fsm_FoldScreenManagerService", pw)) {
                HwFoldScreenManagerService.this.doDump(fd, pw, args);
                pw.println("HwFoldScreenManagerService is running...");
            }
        }
    }

    private final class HwFoldScreenManagerHandler extends Handler {
        public HwFoldScreenManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    HwFoldScreenManagerService.this.notifyPostureChangeInner(msg.arg1);
                    return;
                case 1:
                    HwFoldScreenManagerService.this.notifyFoldStateChangeInner(msg.arg1);
                    HwFoldScreenManagerService.this.notifyFoldStateChangeToTp(msg.arg1);
                    return;
                case 2:
                    HwFoldScreenManagerService.this.notifyDisplayModeChangeInner(msg.arg1);
                    HwFoldScreenManagerService.this.notifyDisplayModeChangeToTp(msg.arg1);
                    return;
                case 3:
                    Slog.i("Fsm_FoldScreenManagerService", "sensor error, force wakeup");
                    HwFoldScreenManagerService.this.mPostureSM.setPosture(HwFoldScreenManagerService.this.getPostureInner());
                    return;
                case 4:
                    Slog.i("Fsm_FoldScreenManagerService", "magnetometer timeout, turnoff sensor");
                    HwFoldScreenManagerService.this.mPreprocess.stop();
                    return;
                case 5:
                    Slog.i("Fsm_FoldScreenManagerService", "fingerPrint timeout, turnoff sensor and init stateMachine");
                    HwFoldScreenManagerService.this.mHandler.removeMessages(3);
                    HwFoldScreenManagerService.this.mPreprocess.stop();
                    HwFoldScreenManagerService.this.mPostureSM.notifySleep();
                    boolean unused = HwFoldScreenManagerService.this.mFingerPrintReady = false;
                    return;
                default:
                    return;
            }
        }
    }

    private final class LocalService extends HwFoldScreenManagerInternal {
        private LocalService() {
        }

        public int getPosture() {
            return HwFoldScreenManagerService.this.getPostureInner();
        }

        public int getFoldableState() {
            return HwFoldScreenManagerService.this.getFoldableStateInner();
        }

        public int setDisplayMode(int mode) {
            return HwFoldScreenManagerService.this.setDisplayModeInner(mode);
        }

        public int getDisplayMode() {
            return HwFoldScreenManagerService.this.getDisplayModeInner();
        }

        public int lockDisplayMode(int mode) {
            return HwFoldScreenManagerService.this.lockDisplayModeInner(mode);
        }

        public int unlockDisplayMode() {
            return HwFoldScreenManagerService.this.unlockDisplayModeInner();
        }

        public int doubleClickToSetDisplayMode(Point point) {
            return HwFoldScreenManagerService.this.mPostureSM.doubleClickToSetDisplayMode(HwFoldScreenStateImpl.getDisplayRect(point));
        }

        public void notifySleep() {
            HwFoldScreenManagerService.this.mHandler.removeMessages(3);
            HwFoldScreenManagerService.this.mHandler.removeMessages(5);
            HwFoldScreenManagerService.this.mHandler.removeMessages(4);
            HwFoldScreenManagerService.this.mPreprocess.stop();
            HwFoldScreenManagerService.this.mPostureSM.notifySleep();
            boolean unused = HwFoldScreenManagerService.this.mFingerPrintReady = false;
        }

        public void prepareWakeup(int wakeupType, Bundle extra) {
            if (HwFoldScreenManagerService.this.mPowerManager == null) {
                PowerManager unused = HwFoldScreenManagerService.this.mPowerManager = (PowerManager) HwFoldScreenManagerService.this.mContext.getSystemService("power");
            }
            if (HwFoldScreenManagerService.this.mPowerManager.isScreenOn()) {
                Slog.w("Fsm_FoldScreenManagerService", "screen is on");
                return;
            }
            notifySleep();
            int uid = extra.getInt("uid");
            String opPackageName = extra.getString("opPackageName");
            String reason = extra.getString("reason");
            if (wakeupType == 5) {
                HwFoldScreenManagerService.this.mPostureSM.setDisplayRectForDoubleClick(HwFoldScreenStateImpl.getDisplayRect((Point) extra.getParcelable("position")));
            }
            if (wakeupType == 4) {
                HwFoldScreenManagerService.this.mHandler.sendEmptyMessageDelayed(4, 5000);
            } else {
                if (wakeupType == 3) {
                    boolean unused2 = HwFoldScreenManagerService.this.mFingerPrintReady = true;
                    HwFoldScreenManagerService.this.mHandler.sendEmptyMessageDelayed(5, 3000);
                }
                HwFoldScreenManagerService.this.mHandler.sendEmptyMessageDelayed(3, 1000);
            }
            HwFoldScreenManagerService.this.mPreprocess.start(wakeupType);
            WakeupManager wakeupManager = HwFoldScreenManagerService.this.getWakeupManagerInner(wakeupType);
            if (wakeupManager != null) {
                wakeupManager.setWakeUpInfo(uid, opPackageName, reason);
                HwFoldScreenStateImpl.setWakeUpManager(wakeupManager);
            }
        }

        public void startWakeup(int wakeupType, Bundle extra) {
            if (HwFoldScreenManagerService.this.mFingerPrintReady) {
                HwFoldScreenManagerService.this.mHandler.removeMessages(5);
                int uid = extra.getInt("uid");
                String opPackageName = extra.getString("opPackageName");
                String reason = extra.getString("reason");
                WakeupManager wakeupManager = HwFoldScreenManagerService.this.getWakeupManagerInner(wakeupType);
                if (wakeupManager != null) {
                    wakeupManager.setFingerprintReady();
                    wakeupManager.setWakeUpInfo(uid, opPackageName, reason);
                    wakeupManager.wakeup();
                }
            }
        }

        public void notifyFlip() {
            synchronized (this) {
                if (HwFoldScreenManagerService.this.mIsIntelligent) {
                    HwFoldScreenManagerService.this.mPostureSM.handleFlipPosture();
                }
            }
        }
    }

    public HwFoldScreenManagerService(Context context) {
        super(context);
        boolean z = false;
        this.mIsDisplayLocked = false;
        this.mFingerPrintReady = false;
        this.mContentObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                HwFoldScreenManagerService.this.changeIntelligentMode(Settings.System.getIntForUser(HwFoldScreenManagerService.this.mContext.getContentResolver(), "eyes_protection_mode", 1, -2));
            }
        };
        this.mContext = context;
        this.mHandlerThread = new ServiceThread("Fsm_FoldScreenManagerService", -4, false);
        this.mHandlerThread.start();
        this.mHandler = new HwFoldScreenManagerHandler(this.mHandlerThread.getLooper());
        this.mPostureSM = PostureStateMachine.getInstance();
        this.mPostureSM.init(this);
        this.mPostureSM.start();
        this.mIsIntelligent = Settings.System.getIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", 1, -2) == 1 ? true : z;
        this.mPreprocess = PosturePreprocessManager.getInstance();
        this.mPreprocess.init(context, this.mIsIntelligent);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("eyes_protection_mode"), true, this.mContentObserver, -1);
        this.mPowerWakeup = new PowerWakeupManager(this.mContext);
        this.mFingerprintWakeup = new FingerPrintWakeupManager(this.mContext);
        this.mDoubleClickWakeup = new DoubleClickWakeupManager(this.mContext);
        this.mMagnetometerWakeup = new MagnetometerWakeupManager(this.mContext);
        this.mMagnetometerWakeup.initSensorListener();
        this.mPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [android.os.IBinder, com.android.server.fsm.HwFoldScreenManagerService$BinderService] */
    public void onStart() {
        publishBinderService("fold_screen", new BinderService());
        publishLocalService(HwFoldScreenManagerInternal.class, new LocalService());
        Watchdog.getInstance().addMonitor(this);
        Watchdog.getInstance().addThread(this.mHandler);
    }

    public void onBootPhase(int phase) {
    }

    public void monitor() {
        synchronized (this) {
        }
    }

    public void systemReady() {
    }

    /* access modifiers changed from: private */
    public void notifyFoldStateChangeInner(int foldState) {
        int i = this.mFoldableStateListeners.beginBroadcast();
        while (i > 0) {
            i--;
            IFoldableStateListener listener = this.mFoldableStateListeners.getBroadcastItem(i);
            Integer type = (Integer) this.mFoldableStateListeners.getBroadcastCookie(i);
            if (!(listener == null || type == null)) {
                Bundle extra = new Bundle();
                try {
                    if (1 == type.intValue()) {
                        extra.putInt("android.intent.extra.REASON", 1);
                        extra.putInt("fold_state", foldState);
                        listener.onStateChange(extra);
                    }
                } catch (RemoteException e) {
                    Slog.e("Fsm_FoldScreenManagerService", "notifyFoldStateChangeInner RemoteException");
                }
            }
        }
        this.mFoldableStateListeners.finishBroadcast();
    }

    /* access modifiers changed from: private */
    public void notifyPostureChangeInner(int posture) {
        int i = this.mFoldableStateListeners.beginBroadcast();
        while (i > 0) {
            i--;
            IFoldableStateListener listener = this.mFoldableStateListeners.getBroadcastItem(i);
            Integer type = (Integer) this.mFoldableStateListeners.getBroadcastCookie(i);
            if (!(listener == null || type == null)) {
                Bundle extra = new Bundle();
                try {
                    if (2 == type.intValue()) {
                        extra.putInt("android.intent.extra.REASON", 2);
                        extra.putInt("posture_mode", posture);
                        listener.onStateChange(extra);
                    }
                } catch (RemoteException e) {
                    Slog.e("Fsm_FoldScreenManagerService", "notifyPostureChangeInner RemoteException");
                }
            }
        }
        this.mFoldableStateListeners.finishBroadcast();
    }

    /* access modifiers changed from: private */
    public void notifyDisplayModeChangeInner(int displayMode) {
        int i = this.mDisplayModeListeners.beginBroadcast();
        while (i > 0) {
            i--;
            IFoldDisplayModeListener listener = this.mDisplayModeListeners.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.onScreenDisplayModeChange(displayMode);
                } catch (RemoteException e) {
                    Slog.e("Fsm_FoldScreenManagerService", "notifyDisplayModeChangeInner RemoteException");
                }
            }
        }
        this.mDisplayModeListeners.finishBroadcast();
        if (this.mPolicy != null) {
            this.mPolicy.setDisplayMode(displayMode);
        }
    }

    /* access modifiers changed from: private */
    public void notifyFoldStateChangeToTp(int foldState) {
        String config;
        Slog.d("Fsm_FoldScreenManagerService", "notifyFoldStateChangeToTp foldState:" + foldState);
        switch (foldState) {
            case 1:
                config = "version:3+expand";
                break;
            case 2:
                config = "version:3+folder";
                break;
            case 3:
                config = "version:3+trestle";
                break;
            default:
                Slog.w("Fsm_FoldScreenManagerService", "Invalid foldState=" + foldState);
                return;
        }
        HwTpManager.getInstance().hwTsSetAftConfig(config);
    }

    /* access modifiers changed from: private */
    public void notifyDisplayModeChangeToTp(int displayMode) {
        String config;
        Slog.d("Fsm_FoldScreenManagerService", "notifyDisplayModeChangeToTp displayMode:" + displayMode);
        switch (displayMode) {
            case 1:
                config = "version:3+whole";
                break;
            case 2:
                config = "version:3+main";
                break;
            case 3:
                config = "version:3+minor";
                break;
            case 4:
                config = "version:3+s_main";
                if (CoordinationModeUtils.getInstance(this.mContext).getCoordinationCreateMode() == 3) {
                    config = "version:3+s_minor";
                    break;
                }
                break;
            default:
                Slog.w("Fsm_FoldScreenManagerService", "Invalid displayMode=" + displayMode);
                return;
        }
        HwTpManager.getInstance().hwTsSetAftConfig(config);
    }

    /* access modifiers changed from: package-private */
    public void notifyFoldStateChange(int foldState) {
        this.mHandler.sendMessage(Message.obtain(this.mHandler, 1, foldState, 0));
    }

    /* access modifiers changed from: package-private */
    public void notifyPostureChange(int posture) {
        this.mHandler.sendMessage(Message.obtain(this.mHandler, 0, posture, 0));
    }

    /* access modifiers changed from: package-private */
    public void notifyDisplayModeChange(int displayMode) {
        this.mHandler.sendMessage(Message.obtain(this.mHandler, 2, displayMode, 0));
    }

    /* access modifiers changed from: private */
    public int getPostureInner() {
        if (this.mPostureSM != null) {
            return this.mPostureSM.getPosture();
        }
        return 100;
    }

    /* access modifiers changed from: private */
    public int getFoldableStateInner() {
        if (this.mPostureSM != null) {
            return this.mPostureSM.getFoldableState();
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public int setDisplayModeInner(int mode) {
        int displayModeBeforeSet = getDisplayModeInner();
        if (this.mPowerManager == null) {
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        }
        if (!this.mPowerManager.isScreenOn()) {
            Slog.w("Fsm_FoldScreenManagerService", "can not set display mode when screenoff");
            return displayModeBeforeSet;
        } else if (mode < 1 || mode > 4 || mode == displayModeBeforeSet) {
            return displayModeBeforeSet;
        } else {
            if (this.mIsDisplayLocked) {
                Slog.w("Fsm_FoldScreenManagerService", "Display mode already be locked.");
                return displayModeBeforeSet;
            } else if (this.mPostureSM != null) {
                return this.mPostureSM.setDisplayMode(mode);
            } else {
                return displayModeBeforeSet;
            }
        }
    }

    /* access modifiers changed from: private */
    public int getDisplayModeInner() {
        if (this.mPostureSM != null) {
            return this.mPostureSM.getDisplayMode();
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public int lockDisplayModeInner(int mode) {
        if (this.mIsDisplayLocked) {
            Slog.w("Fsm_FoldScreenManagerService", "Display mode already be locked by ...");
            return getDisplayModeInner();
        }
        int displayModeNow = setDisplayModeInner(mode);
        if (displayModeNow == mode) {
            this.mIsDisplayLocked = true;
        } else {
            Slog.w("Fsm_FoldScreenManagerService", "set display mode failed, so can not do lock now");
        }
        return displayModeNow;
    }

    /* access modifiers changed from: private */
    public int unlockDisplayModeInner() {
        this.mIsDisplayLocked = false;
        return getDisplayModeInner();
    }

    private void dumpAllInfo(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("FOLD SCREEN MANAGER STARTER (dumpsys fold_screen)");
        pw.println("  " + "StateMachine");
        this.mPostureSM.dump(fd, pw, args);
        pw.println("");
        pw.println("  " + "PreprocessManager");
        this.mPreprocess.dump("  ", pw);
        pw.println("");
        pw.println("  " + "DisplayMode = " + getDisplayModeInner());
    }

    /* access modifiers changed from: private */
    public void doDump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int len = args.length;
        if (len > 1) {
            String cmd = args[0];
            if ("setDisplayMode".equals(cmd)) {
                setDisplayModeInner(Integer.parseInt(args[1]));
            } else if ("setPosture".equals(cmd)) {
                int posture = Integer.parseInt(args[1]);
                if (this.mPostureSM != null) {
                    this.mPostureSM.setPosture(posture);
                }
            }
        } else if (1 == len) {
            pw.println("FOLD SCREEN MANAGER STARTER: args number error");
            pw.println("  Example: dumpsys fold_screen setDisplayMode 1");
        } else if (len == 0) {
            dumpAllInfo(fd, pw, args);
        }
    }

    /* access modifiers changed from: private */
    public void changeIntelligentMode(int intelligent) {
        synchronized (this) {
            boolean isIntelligentNow = true;
            if (intelligent != 1) {
                isIntelligentNow = false;
            }
            if (isIntelligentNow != this.mIsIntelligent) {
                this.mIsIntelligent = isIntelligentNow;
                Slog.i("Fsm_FoldScreenManagerService", "changeIntelligentMode mIsIntelligent = " + this.mIsIntelligent);
                this.mPreprocess.updatePolicy(this.mIsIntelligent);
            }
        }
    }

    /* access modifiers changed from: private */
    public WakeupManager getWakeupManagerInner(int type) {
        if (type == 0) {
            return this.mPowerWakeup;
        }
        switch (type) {
            case 3:
                return this.mFingerprintWakeup;
            case 4:
                return this.mMagnetometerWakeup;
            case 5:
                return this.mDoubleClickWakeup;
            default:
                return null;
        }
    }

    /* access modifiers changed from: protected */
    public void exitCoordinationDisplayMode() {
        ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).exitCoordinationModeInner(true, false);
    }

    /* access modifiers changed from: protected */
    public void removeForceWakeUp() {
        this.mHandler.removeMessages(3);
        this.mHandler.removeMessages(4);
    }
}

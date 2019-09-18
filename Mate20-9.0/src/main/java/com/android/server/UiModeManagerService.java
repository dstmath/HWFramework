package com.android.server;

import android.app.ActivityManager;
import android.app.IUiModeManager;
import android.app.StatusBarManager;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.dreams.Sandman;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.Slog;
import com.android.internal.util.DumpUtils;
import com.android.server.pm.DumpState;
import com.android.server.twilight.TwilightListener;
import com.android.server.twilight.TwilightManager;
import com.android.server.twilight.TwilightState;
import java.io.FileDescriptor;
import java.io.PrintWriter;

final class UiModeManagerService extends SystemService {
    private static final boolean ENABLE_LAUNCH_DESK_DOCK_APP = true;
    private static final boolean LOG = false;
    /* access modifiers changed from: private */
    public static final String TAG = UiModeManager.class.getSimpleName();
    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean unused = UiModeManagerService.this.mCharging = intent.getIntExtra("plugged", 0) != 0;
            synchronized (UiModeManagerService.this.mLock) {
                if (UiModeManagerService.this.mSystemReady) {
                    UiModeManagerService.this.updateLocked(0, 0);
                }
            }
        }
    };
    private int mCarModeEnableFlags;
    private boolean mCarModeEnabled = false;
    private boolean mCarModeKeepsScreenOn;
    /* access modifiers changed from: private */
    public boolean mCharging = false;
    private boolean mComputedNightMode;
    private Configuration mConfiguration = new Configuration();
    int mCurUiMode = 0;
    private int mDefaultUiModeType;
    private boolean mDeskModeKeepsScreenOn;
    private final BroadcastReceiver mDockModeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            UiModeManagerService.this.updateDockState(intent.getIntExtra("android.intent.extra.DOCK_STATE", 0));
        }
    };
    private int mDockState = 0;
    private boolean mEnableCarDockLaunch = true;
    private final Handler mHandler = new Handler();
    private boolean mHoldingConfiguration = false;
    private int mLastBroadcastState = 0;
    final Object mLock = new Object();
    /* access modifiers changed from: private */
    public int mNightMode = 1;
    /* access modifiers changed from: private */
    public boolean mNightModeLocked = false;
    private final BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (getResultCode() == -1) {
                int enableFlags = intent.getIntExtra("enableFlags", 0);
                int disableFlags = intent.getIntExtra("disableFlags", 0);
                synchronized (UiModeManagerService.this.mLock) {
                    UiModeManagerService.this.updateAfterBroadcastLocked(intent.getAction(), enableFlags, disableFlags);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final IUiModeManager.Stub mService = new IUiModeManager.Stub() {
        public void enableCarMode(int flags) {
            if (isUiModeLocked()) {
                Slog.e(UiModeManagerService.TAG, "enableCarMode while UI mode is locked");
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (UiModeManagerService.this.mLock) {
                    UiModeManagerService.this.setCarModeLocked(true, flags);
                    if (UiModeManagerService.this.mSystemReady) {
                        UiModeManagerService.this.updateLocked(flags, 0);
                    }
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }

        public void disableCarMode(int flags) {
            if (isUiModeLocked()) {
                Slog.e(UiModeManagerService.TAG, "disableCarMode while UI mode is locked");
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (UiModeManagerService.this.mLock) {
                    UiModeManagerService.this.setCarModeLocked(false, 0);
                    if (UiModeManagerService.this.mSystemReady) {
                        UiModeManagerService.this.updateLocked(0, flags);
                    }
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }

        public int getCurrentModeType() {
            int i;
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (UiModeManagerService.this.mLock) {
                    i = UiModeManagerService.this.mCurUiMode & 15;
                }
                Binder.restoreCallingIdentity(ident);
                return i;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }

        public void setNightMode(int mode) {
            if (!isNightModeLocked() || UiModeManagerService.this.getContext().checkCallingOrSelfPermission("android.permission.MODIFY_DAY_NIGHT_MODE") == 0) {
                switch (mode) {
                    case 0:
                    case 1:
                    case 2:
                        long ident = Binder.clearCallingIdentity();
                        try {
                            synchronized (UiModeManagerService.this.mLock) {
                                if (UiModeManagerService.this.mNightMode != mode) {
                                    Settings.Secure.putInt(UiModeManagerService.this.getContext().getContentResolver(), "ui_night_mode", mode);
                                    int unused = UiModeManagerService.this.mNightMode = mode;
                                    UiModeManagerService.this.updateLocked(0, 0);
                                }
                            }
                            Binder.restoreCallingIdentity(ident);
                            return;
                        } catch (Throwable th) {
                            Binder.restoreCallingIdentity(ident);
                            throw th;
                        }
                    default:
                        throw new IllegalArgumentException("Unknown mode: " + mode);
                }
            } else {
                Slog.e(UiModeManagerService.TAG, "Night mode locked, requires MODIFY_DAY_NIGHT_MODE permission");
            }
        }

        public int getNightMode() {
            int access$300;
            synchronized (UiModeManagerService.this.mLock) {
                access$300 = UiModeManagerService.this.mNightMode;
            }
            return access$300;
        }

        public boolean isUiModeLocked() {
            boolean access$700;
            synchronized (UiModeManagerService.this.mLock) {
                access$700 = UiModeManagerService.this.mUiModeLocked;
            }
            return access$700;
        }

        public boolean isNightModeLocked() {
            boolean access$800;
            synchronized (UiModeManagerService.this.mLock) {
                access$800 = UiModeManagerService.this.mNightModeLocked;
            }
            return access$800;
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new Shell(UiModeManagerService.this.mService).exec(UiModeManagerService.this.mService, in, out, err, args, callback, resultReceiver);
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(UiModeManagerService.this.getContext(), UiModeManagerService.TAG, pw)) {
                UiModeManagerService.this.dumpImpl(pw);
            }
        }
    };
    private int mSetUiMode = 0;
    private StatusBarManager mStatusBarManager;
    boolean mSystemReady;
    private boolean mTelevision;
    private final TwilightListener mTwilightListener = new TwilightListener() {
        public void onTwilightStateChanged(TwilightState state) {
            synchronized (UiModeManagerService.this.mLock) {
                if (UiModeManagerService.this.mNightMode == 0) {
                    UiModeManagerService.this.updateComputedNightModeLocked();
                    UiModeManagerService.this.updateLocked(0, 0);
                }
            }
        }
    };
    private TwilightManager mTwilightManager;
    /* access modifiers changed from: private */
    public boolean mUiModeLocked = false;
    /* access modifiers changed from: private */
    public boolean mVrHeadset;
    private final IVrStateCallbacks mVrStateCallbacks = new IVrStateCallbacks.Stub() {
        public void onVrStateChanged(boolean enabled) {
            synchronized (UiModeManagerService.this.mLock) {
                boolean unused = UiModeManagerService.this.mVrHeadset = enabled;
                if (UiModeManagerService.this.mSystemReady) {
                    UiModeManagerService.this.updateLocked(0, 0);
                }
            }
        }
    };
    private PowerManager.WakeLock mWakeLock;
    private boolean mWatch;

    private static class Shell extends ShellCommand {
        public static final String NIGHT_MODE_STR_AUTO = "auto";
        public static final String NIGHT_MODE_STR_NO = "no";
        public static final String NIGHT_MODE_STR_UNKNOWN = "unknown";
        public static final String NIGHT_MODE_STR_YES = "yes";
        private final IUiModeManager mInterface;

        Shell(IUiModeManager iface) {
            this.mInterface = iface;
        }

        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("UiModeManager service (uimode) commands:");
            pw.println("  help");
            pw.println("    Print this help text.");
            pw.println("  night [yes|no|auto]");
            pw.println("    Set or read night mode.");
        }

        /* JADX WARNING: Removed duplicated region for block: B:13:0x0020 A[Catch:{ RemoteException -> 0x002a }] */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0025 A[Catch:{ RemoteException -> 0x002a }] */
        public int onCommand(String cmd) {
            boolean z;
            if (cmd == null) {
                return handleDefaultCommands(cmd);
            }
            try {
                if (cmd.hashCode() == 104817688) {
                    if (cmd.equals("night")) {
                        z = false;
                        if (!z) {
                            return handleDefaultCommands(cmd);
                        }
                        return handleNightMode();
                    }
                }
                z = true;
                if (!z) {
                }
            } catch (RemoteException e) {
                getErrPrintWriter().println("Remote exception: " + e);
                return -1;
            }
        }

        private int handleNightMode() throws RemoteException {
            PrintWriter err = getErrPrintWriter();
            String modeStr = getNextArg();
            if (modeStr == null) {
                printCurrentNightMode();
                return 0;
            }
            int mode = strToNightMode(modeStr);
            if (mode >= 0) {
                this.mInterface.setNightMode(mode);
                printCurrentNightMode();
                return 0;
            }
            err.println("Error: mode must be 'yes', 'no', or 'auto'");
            return -1;
        }

        private void printCurrentNightMode() throws RemoteException {
            PrintWriter pw = getOutPrintWriter();
            String currModeStr = nightModeToStr(this.mInterface.getNightMode());
            pw.println("Night mode: " + currModeStr);
        }

        private static String nightModeToStr(int mode) {
            switch (mode) {
                case 0:
                    return NIGHT_MODE_STR_AUTO;
                case 1:
                    return NIGHT_MODE_STR_NO;
                case 2:
                    return NIGHT_MODE_STR_YES;
                default:
                    return NIGHT_MODE_STR_UNKNOWN;
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:17:0x003b A[RETURN] */
        /* JADX WARNING: Removed duplicated region for block: B:18:0x003c A[RETURN] */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x003d A[RETURN] */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x003e A[RETURN] */
        private static int strToNightMode(String modeStr) {
            boolean z;
            int hashCode = modeStr.hashCode();
            if (hashCode == 3521) {
                if (modeStr.equals(NIGHT_MODE_STR_NO)) {
                    z = true;
                    switch (z) {
                        case false:
                            break;
                        case true:
                            break;
                        case true:
                            break;
                    }
                }
            } else if (hashCode == 119527) {
                if (modeStr.equals(NIGHT_MODE_STR_YES)) {
                    z = false;
                    switch (z) {
                        case false:
                            break;
                        case true:
                            break;
                        case true:
                            break;
                    }
                }
            } else if (hashCode == 3005871 && modeStr.equals(NIGHT_MODE_STR_AUTO)) {
                z = true;
                switch (z) {
                    case false:
                        return 2;
                    case true:
                        return 1;
                    case true:
                        return 0;
                    default:
                        return -1;
                }
            }
            z = true;
            switch (z) {
                case false:
                    break;
                case true:
                    break;
                case true:
                    break;
            }
        }
    }

    public UiModeManagerService(Context context) {
        super(context);
    }

    private static Intent buildHomeIntent(String category) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory(category);
        intent.setFlags(270532608);
        return intent;
    }

    public void onStart() {
        Context context = getContext();
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(26, TAG);
        context.registerReceiver(this.mDockModeReceiver, new IntentFilter("android.intent.action.DOCK_EVENT"));
        context.registerReceiver(this.mBatteryReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        this.mConfiguration.setToDefaults();
        Resources res = context.getResources();
        this.mDefaultUiModeType = res.getInteger(17694773);
        boolean z = false;
        this.mCarModeKeepsScreenOn = res.getInteger(17694754) == 1;
        this.mDeskModeKeepsScreenOn = res.getInteger(17694775) == 1;
        this.mEnableCarDockLaunch = res.getBoolean(17956951);
        this.mUiModeLocked = res.getBoolean(17956991);
        this.mNightModeLocked = res.getBoolean(17956990);
        PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature("android.hardware.type.television") || pm.hasSystemFeature("android.software.leanback")) {
            z = true;
        }
        this.mTelevision = z;
        this.mWatch = pm.hasSystemFeature("android.hardware.type.watch");
        this.mNightMode = Settings.Secure.getInt(context.getContentResolver(), "ui_night_mode", res.getInteger(17694768));
        SystemServerInitThreadPool.get().submit(new Runnable() {
            public final void run() {
                UiModeManagerService.lambda$onStart$0(UiModeManagerService.this);
            }
        }, TAG + ".onStart");
        publishBinderService("uimode", this.mService);
    }

    public static /* synthetic */ void lambda$onStart$0(UiModeManagerService uiModeManagerService) {
        synchronized (uiModeManagerService.mLock) {
            uiModeManagerService.updateConfigurationLocked();
            uiModeManagerService.sendConfigurationLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpImpl(PrintWriter pw) {
        synchronized (this.mLock) {
            pw.println("Current UI Mode Service state:");
            pw.print("  mDockState=");
            pw.print(this.mDockState);
            pw.print(" mLastBroadcastState=");
            pw.println(this.mLastBroadcastState);
            pw.print("  mNightMode=");
            pw.print(this.mNightMode);
            pw.print(" mNightModeLocked=");
            pw.print(this.mNightModeLocked);
            pw.print(" mCarModeEnabled=");
            pw.print(this.mCarModeEnabled);
            pw.print(" mComputedNightMode=");
            pw.print(this.mComputedNightMode);
            pw.print(" mCarModeEnableFlags=");
            pw.print(this.mCarModeEnableFlags);
            pw.print(" mEnableCarDockLaunch=");
            pw.println(this.mEnableCarDockLaunch);
            pw.print("  mCurUiMode=0x");
            pw.print(Integer.toHexString(this.mCurUiMode));
            pw.print(" mUiModeLocked=");
            pw.print(this.mUiModeLocked);
            pw.print(" mSetUiMode=0x");
            pw.println(Integer.toHexString(this.mSetUiMode));
            pw.print("  mHoldingConfiguration=");
            pw.print(this.mHoldingConfiguration);
            pw.print(" mSystemReady=");
            pw.println(this.mSystemReady);
            if (this.mTwilightManager != null) {
                pw.print("  mTwilightService.getLastTwilightState()=");
                pw.println(this.mTwilightManager.getLastTwilightState());
            }
        }
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            synchronized (this.mLock) {
                this.mTwilightManager = (TwilightManager) getLocalService(TwilightManager.class);
                boolean z = true;
                this.mSystemReady = true;
                if (this.mDockState != 2) {
                    z = false;
                }
                this.mCarModeEnabled = z;
                updateComputedNightModeLocked();
                registerVrStateListener();
                updateLocked(0, 0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setCarModeLocked(boolean enabled, int flags) {
        if (this.mCarModeEnabled != enabled) {
            this.mCarModeEnabled = enabled;
        }
        this.mCarModeEnableFlags = flags;
    }

    /* access modifiers changed from: private */
    public void updateDockState(int newState) {
        synchronized (this.mLock) {
            if (newState != this.mDockState) {
                this.mDockState = newState;
                setCarModeLocked(this.mDockState == 2, 0);
                if (this.mSystemReady) {
                    updateLocked(1, 0);
                }
            }
        }
    }

    private static boolean isDeskDockState(int state) {
        if (state != 1) {
            switch (state) {
                case 3:
                case 4:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    private void updateConfigurationLocked() {
        int uiMode;
        int i;
        int uiMode2 = this.mDefaultUiModeType;
        if (!this.mUiModeLocked) {
            if (this.mTelevision) {
                uiMode2 = 4;
            } else if (this.mWatch) {
                uiMode2 = 6;
            } else if (this.mCarModeEnabled) {
                uiMode2 = 3;
            } else if (isDeskDockState(this.mDockState)) {
                uiMode2 = 2;
            } else if (this.mVrHeadset) {
                uiMode2 = 7;
            }
        }
        if (this.mNightMode == 0) {
            if (this.mTwilightManager != null) {
                this.mTwilightManager.registerListener(this.mTwilightListener, this.mHandler);
            }
            updateComputedNightModeLocked();
            if (this.mComputedNightMode) {
                i = 32;
            } else {
                i = 16;
            }
            uiMode = uiMode2 | i;
        } else {
            if (this.mTwilightManager != null) {
                this.mTwilightManager.unregisterListener(this.mTwilightListener);
            }
            uiMode = uiMode2 | (this.mNightMode << 4);
        }
        this.mCurUiMode = uiMode;
        if (!this.mHoldingConfiguration) {
            this.mConfiguration.uiMode = uiMode;
        }
    }

    private void sendConfigurationLocked() {
        if (this.mSetUiMode != this.mConfiguration.uiMode) {
            this.mSetUiMode = this.mConfiguration.uiMode;
            try {
                ActivityManager.getService().updateConfiguration(this.mConfiguration);
            } catch (RemoteException e) {
                Slog.w(TAG, "Failure communicating with activity manager", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateLocked(int enableFlags, int disableFlags) {
        int i = enableFlags;
        int i2 = disableFlags;
        String action = null;
        String oldAction = null;
        if (this.mLastBroadcastState == 2) {
            adjustStatusBarCarModeLocked();
            oldAction = UiModeManager.ACTION_EXIT_CAR_MODE;
        } else if (isDeskDockState(this.mLastBroadcastState)) {
            oldAction = UiModeManager.ACTION_EXIT_DESK_MODE;
        }
        if (this.mCarModeEnabled) {
            if (this.mLastBroadcastState != 2) {
                adjustStatusBarCarModeLocked();
                if (oldAction != null) {
                    sendForegroundBroadcastToAllUsers(oldAction);
                }
                this.mLastBroadcastState = 2;
                action = UiModeManager.ACTION_ENTER_CAR_MODE;
            }
        } else if (!isDeskDockState(this.mDockState)) {
            this.mLastBroadcastState = 0;
            action = oldAction;
        } else if (!isDeskDockState(this.mLastBroadcastState)) {
            if (oldAction != null) {
                sendForegroundBroadcastToAllUsers(oldAction);
            }
            this.mLastBroadcastState = this.mDockState;
            action = UiModeManager.ACTION_ENTER_DESK_MODE;
        }
        boolean keepScreenOn = true;
        if (action != null) {
            Intent intent = new Intent(action);
            intent.putExtra("enableFlags", i);
            intent.putExtra("disableFlags", i2);
            intent.addFlags(268435456);
            getContext().sendOrderedBroadcastAsUser(intent, UserHandle.CURRENT, null, this.mResultReceiver, null, -1, null, null);
            this.mHoldingConfiguration = true;
            updateConfigurationLocked();
        } else {
            String category = null;
            if (this.mCarModeEnabled) {
                if (this.mEnableCarDockLaunch && (i & 1) != 0) {
                    category = "android.intent.category.CAR_DOCK";
                }
            } else if (isDeskDockState(this.mDockState)) {
                if ((i & 1) != 0) {
                    category = "android.intent.category.DESK_DOCK";
                }
            } else if ((i2 & 1) != 0) {
                category = "android.intent.category.HOME";
            }
            sendConfigurationAndStartDreamOrDockAppLocked(category);
        }
        if (!this.mCharging || ((!this.mCarModeEnabled || !this.mCarModeKeepsScreenOn || (this.mCarModeEnableFlags & 2) != 0) && (this.mCurUiMode != 2 || !this.mDeskModeKeepsScreenOn))) {
            keepScreenOn = false;
        }
        if (keepScreenOn == this.mWakeLock.isHeld()) {
            return;
        }
        if (keepScreenOn) {
            this.mWakeLock.acquire();
        } else {
            this.mWakeLock.release();
        }
    }

    private void sendForegroundBroadcastToAllUsers(String action) {
        getContext().sendBroadcastAsUser(new Intent(action).addFlags(268435456), UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    public void updateAfterBroadcastLocked(String action, int enableFlags, int disableFlags) {
        String category = null;
        if (UiModeManager.ACTION_ENTER_CAR_MODE.equals(action)) {
            if (this.mEnableCarDockLaunch && (enableFlags & 1) != 0) {
                category = "android.intent.category.CAR_DOCK";
            }
        } else if (UiModeManager.ACTION_ENTER_DESK_MODE.equals(action)) {
            if ((enableFlags & 1) != 0) {
                category = "android.intent.category.DESK_DOCK";
            }
        } else if ((disableFlags & 1) != 0) {
            category = "android.intent.category.HOME";
        }
        sendConfigurationAndStartDreamOrDockAppLocked(category);
    }

    private void sendConfigurationAndStartDreamOrDockAppLocked(String category) {
        this.mHoldingConfiguration = false;
        updateConfigurationLocked();
        boolean dockAppStarted = false;
        if (category != null) {
            Intent homeIntent = buildHomeIntent(category);
            if (Sandman.shouldStartDockApp(getContext(), homeIntent)) {
                try {
                    int result = ActivityManager.getService().startActivityWithConfig(null, null, homeIntent, null, null, null, 0, 0, this.mConfiguration, null, -2);
                    if (ActivityManager.isStartResultSuccessful(result)) {
                        dockAppStarted = true;
                    } else if (result != -91) {
                        String str = TAG;
                        Slog.e(str, "Could not start dock app: " + homeIntent + ", startActivityWithConfig result " + result);
                    }
                } catch (RemoteException ex) {
                    String str2 = TAG;
                    Slog.e(str2, "Could not start dock app: " + homeIntent, ex);
                }
            }
        }
        sendConfigurationLocked();
        if (category != null && !dockAppStarted) {
            Sandman.startDreamWhenDockedIfAppropriate(getContext());
        }
    }

    private void adjustStatusBarCarModeLocked() {
        int i;
        Context context = getContext();
        if (this.mStatusBarManager == null) {
            this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        }
        if (this.mStatusBarManager != null) {
            StatusBarManager statusBarManager = this.mStatusBarManager;
            if (this.mCarModeEnabled) {
                i = DumpState.DUMP_FROZEN;
            } else {
                i = 0;
            }
            statusBarManager.disable(i);
        }
    }

    /* access modifiers changed from: private */
    public void updateComputedNightModeLocked() {
        if (this.mTwilightManager != null) {
            TwilightState state = this.mTwilightManager.getLastTwilightState();
            if (state != null) {
                this.mComputedNightMode = state.isNight();
            }
        }
    }

    private void registerVrStateListener() {
        IVrManager vrManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
        if (vrManager != null) {
            try {
                vrManager.registerListener(this.mVrStateCallbacks);
            } catch (RemoteException e) {
                String str = TAG;
                Slog.e(str, "Failed to register VR mode state listener: " + e);
            }
        }
    }
}

package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.AppGlobals;
import android.app.IApplicationThread;
import android.app.IUiModeManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.PowerSaveState;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.service.dreams.Sandman;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.Slog;
import com.android.internal.app.DisableCarModeActivity;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.util.DumpUtils;
import com.android.server.pm.DumpState;
import com.android.server.twilight.TwilightListener;
import com.android.server.twilight.TwilightManager;
import com.android.server.twilight.TwilightState;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.function.Consumer;

/* access modifiers changed from: package-private */
public final class UiModeManagerService extends SystemService {
    private static final boolean ENABLE_LAUNCH_DESK_DOCK_APP = true;
    private static final boolean LOG = false;
    private static final String OLD_HW_DARK_TAG = "dark";
    public static final String OVERLAY_THEME = "persist.deep.theme_0";
    private static final String SYSTEM_PROPERTY_DEVICE_THEME = "persist.sys.theme";
    private static final String TAG = UiModeManager.class.getSimpleName();
    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        /* class com.android.server.UiModeManagerService.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (((action.hashCode() == -1538406691 && action.equals("android.intent.action.BATTERY_CHANGED")) ? (char) 0 : 65535) == 0) {
                UiModeManagerService.this.mCharging = intent.getIntExtra("plugged", 0) != 0;
            }
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
    private boolean mCharging = false;
    private boolean mComputedNightMode;
    private Configuration mConfiguration = new Configuration();
    int mCurUiMode = 0;
    private CustomTimeDarkThemeHelper mCustomTimeDarkThemeHelper;
    private int mDefaultUiModeType;
    private boolean mDeskModeKeepsScreenOn;
    private final BroadcastReceiver mDockModeReceiver = new BroadcastReceiver() {
        /* class com.android.server.UiModeManagerService.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            UiModeManagerService.this.updateDockState(intent.getIntExtra("android.intent.extra.DOCK_STATE", 0));
        }
    };
    private int mDockState = 0;
    private boolean mEnableCarDockLaunch = true;
    private final Handler mHandler = new Handler();
    private boolean mHoldingConfiguration = false;
    private int mLastBroadcastState = 0;
    private final LocalService mLocalService = new LocalService();
    final Object mLock = new Object();
    private int mNightMode = 1;
    private boolean mNightModeLocked = false;
    private NotificationManager mNotificationManager;
    private boolean mPowerSave = false;
    private final BroadcastReceiver mResultReceiver = new BroadcastReceiver() {
        /* class com.android.server.UiModeManagerService.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
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
    private final IUiModeManager.Stub mService = new IUiModeManager.Stub() {
        /* class com.android.server.UiModeManagerService.AnonymousClass7 */

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
            } finally {
                Binder.restoreCallingIdentity(ident);
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
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getCurrentModeType() {
            int i;
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (UiModeManagerService.this.mLock) {
                    i = UiModeManagerService.this.mCurUiMode & 15;
                }
                return i;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void setNightMode(int mode) {
            setNightModeForUser(mode, UserHandle.getCallingUserId());
        }

        public void setNightModeForUser(int mode, int userHandle) {
            if (isNightModeLocked() && UiModeManagerService.this.getContext().checkCallingOrSelfPermission("android.permission.MODIFY_DAY_NIGHT_MODE") != 0) {
                Slog.e(UiModeManagerService.TAG, "Night mode locked, requires MODIFY_DAY_NIGHT_MODE permission");
            } else if (!UiModeManagerService.this.mSetupWizardComplete) {
                Slog.i(UiModeManagerService.TAG, "Night mode cannot be changed before setup wizard completes.");
            } else if (mode == 0 || mode == 1 || mode == 2) {
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (UiModeManagerService.this.mLock) {
                        String str = UiModeManagerService.TAG;
                        Slog.i(str, "setNightMode: mNightMode = " + UiModeManagerService.this.mNightMode + ", mode = " + mode + " for user " + userHandle);
                        if (UiModeManagerService.this.mNightMode != mode) {
                            if (!UiModeManagerService.this.mCarModeEnabled) {
                                String str2 = UiModeManagerService.TAG;
                                Slog.i(str2, "setNightMode: set ui_night_mode " + mode + " for user " + userHandle);
                                Settings.Secure.putIntForUser(UiModeManagerService.this.getContext().getContentResolver(), "ui_night_mode", mode, userHandle);
                                if (UserManager.get(UiModeManagerService.this.getContext()).isPrimaryUser()) {
                                    SystemProperties.set(UiModeManagerService.SYSTEM_PROPERTY_DEVICE_THEME, Integer.toString(mode));
                                }
                            }
                            UiModeManagerService.this.mNightMode = mode;
                            UiModeManagerService.this.updateLocked(0, 0);
                        }
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("Unknown mode: " + mode);
            }
        }

        public void setUserIdAndNightMode(int userId, int mode) {
            setNightModeForUser(mode, userId);
        }

        public int getNightMode() {
            int i;
            synchronized (UiModeManagerService.this.mLock) {
                i = UiModeManagerService.this.mNightMode;
            }
            return i;
        }

        public boolean isUiModeLocked() {
            boolean z;
            synchronized (UiModeManagerService.this.mLock) {
                z = UiModeManagerService.this.mUiModeLocked;
            }
            return z;
        }

        public boolean isNightModeLocked() {
            boolean z;
            synchronized (UiModeManagerService.this.mLock) {
                z = UiModeManagerService.this.mNightModeLocked;
            }
            return z;
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
    private boolean mSetupWizardComplete;
    private final ContentObserver mSetupWizardObserver = new ContentObserver(this.mHandler) {
        /* class com.android.server.UiModeManagerService.AnonymousClass6 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            if (UiModeManagerService.this.setupWizardCompleteForCurrentUser()) {
                UiModeManagerService.this.mSetupWizardComplete = true;
                UiModeManagerService.this.getContext().getContentResolver().unregisterContentObserver(UiModeManagerService.this.mSetupWizardObserver);
                Context context = UiModeManagerService.this.getContext();
                UiModeManagerService.this.updateNightModeFromSettings(context, context.getResources(), UserHandle.getCallingUserId());
                UiModeManagerService.this.updateLocked(0, 0);
            }
        }
    };
    private StatusBarManager mStatusBarManager;
    boolean mSystemReady;
    private boolean mTelevision;
    private final TwilightListener mTwilightListener = new TwilightListener() {
        /* class com.android.server.UiModeManagerService.AnonymousClass4 */

        @Override // com.android.server.twilight.TwilightListener
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
    private boolean mUiModeLocked = false;
    private boolean mVrHeadset;
    private final IVrStateCallbacks mVrStateCallbacks = new IVrStateCallbacks.Stub() {
        /* class com.android.server.UiModeManagerService.AnonymousClass5 */

        public void onVrStateChanged(boolean enabled) {
            synchronized (UiModeManagerService.this.mLock) {
                UiModeManagerService.this.mVrHeadset = enabled;
                if (UiModeManagerService.this.mSystemReady) {
                    UiModeManagerService.this.updateLocked(0, 0);
                }
            }
        }
    };
    private PowerManager.WakeLock mWakeLock;
    private boolean mWatch;

    public UiModeManagerService(Context context) {
        super(context);
    }

    private static Intent buildHomeIntent(String category) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory(category);
        intent.setFlags(270532608);
        return intent;
    }

    @Override // com.android.server.SystemService
    public void onSwitchUser(int userHandle) {
        super.onSwitchUser(userHandle);
        getContext().getContentResolver().unregisterContentObserver(this.mSetupWizardObserver);
        verifySetupWizardCompleted();
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        Context context = getContext();
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(26, TAG);
        verifySetupWizardCompleted();
        context.registerReceiver(this.mDockModeReceiver, new IntentFilter("android.intent.action.DOCK_EVENT"));
        context.registerReceiver(this.mBatteryReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        PowerManagerInternal localPowerManager = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mPowerSave = localPowerManager.getLowPowerState(16).batterySaverEnabled;
        localPowerManager.registerLowPowerModeObserver(16, new Consumer() {
            /* class com.android.server.$$Lambda$UiModeManagerService$vYS4_RzjAavNRF50rrGN0tXI5JM */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                UiModeManagerService.this.lambda$onStart$0$UiModeManagerService((PowerSaveState) obj);
            }
        });
        this.mConfiguration.setToDefaults();
        Resources res = context.getResources();
        this.mDefaultUiModeType = res.getInteger(17694783);
        boolean z = true;
        this.mCarModeKeepsScreenOn = res.getInteger(17694761) == 1;
        this.mDeskModeKeepsScreenOn = res.getInteger(17694785) == 1;
        this.mEnableCarDockLaunch = res.getBoolean(17891436);
        this.mUiModeLocked = res.getBoolean(17891478);
        this.mNightModeLocked = res.getBoolean(17891477);
        PackageManager pm = context.getPackageManager();
        if (!pm.hasSystemFeature("android.hardware.type.television") && !pm.hasSystemFeature("android.software.leanback")) {
            z = false;
        }
        this.mTelevision = z;
        this.mWatch = pm.hasSystemFeature("android.hardware.type.watch");
        updateNightModeFromSettings(context, res, UserHandle.getCallingUserId());
        SystemServerInitThreadPool.get().submit(new Runnable() {
            /* class com.android.server.$$Lambda$UiModeManagerService$vuGxqIEDBezs_XyzNAh0Bonp5g */

            @Override // java.lang.Runnable
            public final void run() {
                UiModeManagerService.this.lambda$onStart$1$UiModeManagerService();
            }
        }, TAG + ".onStart");
        publishBinderService("uimode", this.mService);
        publishLocalService(UiModeManagerInternal.class, this.mLocalService);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        context.registerReceiver(new UserSwitchedReceiver(), filter, null, this.mHandler);
        if (this.mCustomTimeDarkThemeHelper == null) {
            this.mCustomTimeDarkThemeHelper = new CustomTimeDarkThemeHelper(this.mService, this.mHandler);
        }
        CustomTimeDarkThemeHelper customTimeDarkThemeHelper = this.mCustomTimeDarkThemeHelper;
        if (customTimeDarkThemeHelper != null) {
            customTimeDarkThemeHelper.onStarted(context, 0);
        }
    }

    public /* synthetic */ void lambda$onStart$0$UiModeManagerService(PowerSaveState state) {
        synchronized (this.mLock) {
            if (this.mPowerSave != state.batterySaverEnabled) {
                this.mPowerSave = state.batterySaverEnabled;
                if (this.mSystemReady) {
                    updateLocked(0, 0);
                }
            }
        }
    }

    public /* synthetic */ void lambda$onStart$1$UiModeManagerService() {
        synchronized (this.mLock) {
            updateConfigurationLocked();
            sendConfigurationLocked();
        }
    }

    private void verifySetupWizardCompleted() {
        Context context = getContext();
        int userId = UserHandle.getCallingUserId();
        if (!setupWizardCompleteForCurrentUser()) {
            this.mSetupWizardComplete = false;
            context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("user_setup_complete"), false, this.mSetupWizardObserver, userId);
            return;
        }
        this.mSetupWizardComplete = true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean setupWizardCompleteForCurrentUser() {
        return Settings.Secure.getIntForUser(getContext().getContentResolver(), "user_setup_complete", 0, UserHandle.getCallingUserId()) == 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateNightModeFromSettings(Context context, Resources res, int userId) {
        int defaultNightMode = res.getInteger(17694776);
        int oldNightMode = this.mNightMode;
        if (this.mSetupWizardComplete) {
            this.mNightMode = Settings.Secure.getIntForUser(context.getContentResolver(), "ui_night_mode", defaultNightMode, userId);
        } else {
            this.mNightMode = defaultNightMode;
        }
        boolean isHoat = context.getPackageManager().isUpgrade();
        String str = TAG;
        Slog.i(str, "updateNightModeFromSettings, isHoat =  " + isHoat + " VERSION.SDK_INT  = " + Build.VERSION.SDK_INT + " VERSION_CODES.Q = 29 called by = " + Debug.getCallers(6));
        if (isHoat && userId == 0) {
            Slog.i(TAG, "updateNightModeFromSettings, this is a hota ");
            if (this.mNightMode != 2 && OLD_HW_DARK_TAG.equalsIgnoreCase(SystemProperties.get(OVERLAY_THEME))) {
                Settings.Secure.putIntForUser(getContext().getContentResolver(), "ui_night_mode", 2, userId);
                this.mNightMode = 2;
                String str2 = TAG;
                Slog.i(str2, "updateNightModeFromSettings oldNightMode = " + oldNightMode + "new night mode = " + this.mNightMode);
            }
        }
        return oldNightMode != this.mNightMode;
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
            pw.print(" (");
            pw.print(Shell.nightModeToStr(this.mNightMode));
            pw.print(") ");
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

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        CustomTimeDarkThemeHelper customTimeDarkThemeHelper;
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
        } else if (phase == 1000 && (customTimeDarkThemeHelper = this.mCustomTimeDarkThemeHelper) != null) {
            customTimeDarkThemeHelper.onBootCompleted();
        }
    }

    /* access modifiers changed from: package-private */
    public void setCarModeLocked(boolean enabled, int flags) {
        if (this.mCarModeEnabled != enabled) {
            this.mCarModeEnabled = enabled;
            if (!this.mCarModeEnabled) {
                Context context = getContext();
                updateNightModeFromSettings(context, context.getResources(), UserHandle.getCallingUserId());
            }
        }
        this.mCarModeEnableFlags = flags;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDockState(int newState) {
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
        if (state == 1 || state == 3 || state == 4) {
            return true;
        }
        return false;
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
            TwilightManager twilightManager = this.mTwilightManager;
            if (twilightManager != null) {
                twilightManager.registerListener(this.mTwilightListener, this.mHandler);
            }
            updateComputedNightModeLocked();
            if (this.mComputedNightMode) {
                i = 32;
            } else {
                i = 16;
            }
            uiMode = uiMode2 | i;
        } else {
            TwilightManager twilightManager2 = this.mTwilightManager;
            if (twilightManager2 != null) {
                twilightManager2.unregisterListener(this.mTwilightListener);
            }
            uiMode = uiMode2 | (this.mNightMode << 4);
        }
        if (!isSystemManagerInstalled() && this.mPowerSave && !this.mCarModeEnabled) {
            uiMode = (uiMode & -17) | 32;
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
                ActivityTaskManager.getService().updateConfiguration(this.mConfiguration);
            } catch (RemoteException e) {
                Slog.w(TAG, "Failure communicating with activity manager", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateLocked(int enableFlags, int disableFlags) {
        String action = null;
        String oldAction = null;
        int i = this.mLastBroadcastState;
        if (i == 2) {
            adjustStatusBarCarModeLocked();
            oldAction = UiModeManager.ACTION_EXIT_CAR_MODE;
        } else if (isDeskDockState(i)) {
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
            intent.putExtra("enableFlags", enableFlags);
            intent.putExtra("disableFlags", disableFlags);
            intent.addFlags(268435456);
            getContext().sendOrderedBroadcastAsUser(intent, UserHandle.CURRENT, null, this.mResultReceiver, null, -1, null, null);
            this.mHoldingConfiguration = true;
            updateConfigurationLocked();
        } else {
            String category = null;
            if (this.mCarModeEnabled) {
                if (this.mEnableCarDockLaunch && (enableFlags & 1) != 0) {
                    category = "android.intent.category.CAR_DOCK";
                }
            } else if (isDeskDockState(this.mDockState)) {
                if ((enableFlags & 1) != 0) {
                    category = "android.intent.category.DESK_DOCK";
                }
            } else if ((disableFlags & 1) != 0) {
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
    /* access modifiers changed from: public */
    private void updateAfterBroadcastLocked(String action, int enableFlags, int disableFlags) {
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

    /* JADX WARNING: Removed duplicated region for block: B:21:0x007a A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:24:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    private void sendConfigurationAndStartDreamOrDockAppLocked(String category) {
        Intent homeIntent;
        RemoteException ex;
        this.mHoldingConfiguration = false;
        updateConfigurationLocked();
        boolean dockAppStarted = false;
        if (category != null) {
            Intent homeIntent2 = buildHomeIntent(category);
            if (Sandman.shouldStartDockApp(getContext(), homeIntent2)) {
                try {
                    homeIntent = homeIntent2;
                    try {
                        int result = ActivityTaskManager.getService().startActivityWithConfig((IApplicationThread) null, (String) null, homeIntent2, (String) null, (IBinder) null, (String) null, 0, 0, this.mConfiguration, (Bundle) null, -2);
                        if (ActivityManager.isStartResultSuccessful(result)) {
                            dockAppStarted = true;
                        } else if (result != -91) {
                            Slog.e(TAG, "Could not start dock app: " + homeIntent + ", startActivityWithConfig result " + result);
                        }
                    } catch (RemoteException e) {
                        ex = e;
                        Slog.e(TAG, "Could not start dock app: " + homeIntent, ex);
                        sendConfigurationLocked();
                        if (category == null) {
                            return;
                        }
                    }
                } catch (RemoteException e2) {
                    ex = e2;
                    homeIntent = homeIntent2;
                    Slog.e(TAG, "Could not start dock app: " + homeIntent, ex);
                    sendConfigurationLocked();
                    if (category == null) {
                    }
                }
            }
        }
        sendConfigurationLocked();
        if (category == null && !dockAppStarted) {
            Sandman.startDreamWhenDockedIfAppropriate(getContext());
        }
    }

    private void adjustStatusBarCarModeLocked() {
        int i;
        Context context = getContext();
        if (this.mStatusBarManager == null) {
            this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        }
        StatusBarManager statusBarManager = this.mStatusBarManager;
        if (statusBarManager != null) {
            if (this.mCarModeEnabled) {
                i = DumpState.DUMP_FROZEN;
            } else {
                i = 0;
            }
            statusBarManager.disable(i);
        }
        if (this.mNotificationManager == null) {
            this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        }
        NotificationManager notificationManager = this.mNotificationManager;
        if (notificationManager == null) {
            return;
        }
        if (this.mCarModeEnabled) {
            this.mNotificationManager.notifyAsUser(null, 10, new Notification.Builder(context, SystemNotificationChannels.CAR_MODE).setSmallIcon(17303532).setDefaults(4).setOngoing(true).setWhen(0).setColor(context.getColor(17170460)).setContentTitle(context.getString(17039744)).setContentText(context.getString(17039743)).setContentIntent(PendingIntent.getActivityAsUser(context, 0, new Intent(context, DisableCarModeActivity.class), 0, null, UserHandle.CURRENT)).build(), UserHandle.ALL);
            return;
        }
        notificationManager.cancelAsUser(null, 10, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateComputedNightModeLocked() {
        TwilightState state;
        TwilightManager twilightManager = this.mTwilightManager;
        if (twilightManager != null && (state = twilightManager.getLastTwilightState()) != null) {
            this.mComputedNightMode = state.isNight();
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

    /* access modifiers changed from: private */
    public static class Shell extends ShellCommand {
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

        public int onCommand(String cmd) {
            if (cmd == null) {
                return handleDefaultCommands(cmd);
            }
            try {
                if ((cmd.hashCode() == 104817688 && cmd.equals("night")) ? false : true) {
                    return handleDefaultCommands(cmd);
                }
                return handleNightMode();
            } catch (RemoteException e) {
                PrintWriter err = getErrPrintWriter();
                err.println("Remote exception: " + e);
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

        /* access modifiers changed from: private */
        public static String nightModeToStr(int mode) {
            if (mode == 0) {
                return NIGHT_MODE_STR_AUTO;
            }
            if (mode == 1) {
                return NIGHT_MODE_STR_NO;
            }
            if (mode != 2) {
                return NIGHT_MODE_STR_UNKNOWN;
            }
            return NIGHT_MODE_STR_YES;
        }

        /* JADX WARNING: Removed duplicated region for block: B:17:0x003a  */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x0041 A[RETURN] */
        private static int strToNightMode(String modeStr) {
            boolean z;
            int hashCode = modeStr.hashCode();
            if (hashCode != 3521) {
                if (hashCode != 119527) {
                    if (hashCode == 3005871 && modeStr.equals(NIGHT_MODE_STR_AUTO)) {
                        z = true;
                        if (z) {
                            return 2;
                        }
                        if (!z) {
                            return !z ? -1 : 0;
                        }
                        return 1;
                    }
                } else if (modeStr.equals(NIGHT_MODE_STR_YES)) {
                    z = false;
                    if (z) {
                    }
                }
            } else if (modeStr.equals(NIGHT_MODE_STR_NO)) {
                z = true;
                if (z) {
                }
            }
            z = true;
            if (z) {
            }
        }
    }

    public final class LocalService extends UiModeManagerInternal {
        public LocalService() {
        }

        @Override // com.android.server.UiModeManagerInternal
        public boolean isNightMode() {
            boolean isIt;
            synchronized (UiModeManagerService.this.mLock) {
                isIt = (UiModeManagerService.this.mConfiguration.uiMode & 32) != 0;
            }
            return isIt;
        }
    }

    private final class UserSwitchedReceiver extends BroadcastReceiver {
        private UserSwitchedReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            synchronized (UiModeManagerService.this.mLock) {
                int currentId = intent.getIntExtra("android.intent.extra.user_handle", 0);
                String str = UiModeManagerService.TAG;
                Slog.i(str, "UserSwitchedReceiver: currentId=" + currentId);
                if (UiModeManagerService.this.updateNightModeFromSettings(context, context.getResources(), currentId)) {
                    UiModeManagerService.this.updateLocked(0, 0);
                }
                if (UiModeManagerService.this.mCustomTimeDarkThemeHelper != null) {
                    UiModeManagerService.this.mCustomTimeDarkThemeHelper.onUserSwitched(context, currentId);
                }
            }
        }
    }

    private boolean isSystemManagerInstalled() {
        try {
            IPackageManager pm = AppGlobals.getPackageManager();
            if (pm == null) {
                Slog.w(TAG, "isSystemManagerInstalled pm is null");
                return false;
            }
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.huawei.systemmanager", 128, UserHandle.getUserId(Process.myUid()));
            if (applicationInfo != null) {
                return applicationInfo.enabled;
            }
            return false;
        } catch (RemoteException e) {
            String str = TAG;
            Slog.e(str, "isSystemManagerInstalled  Exception : " + e.getMessage());
        }
    }
}

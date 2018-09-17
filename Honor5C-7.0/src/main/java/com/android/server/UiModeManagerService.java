package com.android.server;

import android.app.ActivityManagerNative;
import android.app.IUiModeManager.Stub;
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
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.service.dreams.Sandman;
import android.util.Slog;
import com.android.server.twilight.TwilightListener;
import com.android.server.twilight.TwilightManager;
import com.android.server.twilight.TwilightState;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import java.io.FileDescriptor;
import java.io.PrintWriter;

final class UiModeManagerService extends SystemService {
    private static final boolean ENABLE_LAUNCH_DESK_DOCK_APP = true;
    private static final boolean LOG = false;
    private static final String TAG = null;
    private final BroadcastReceiver mBatteryReceiver;
    private int mCarModeEnableFlags;
    private boolean mCarModeEnabled;
    private boolean mCarModeKeepsScreenOn;
    private boolean mCharging;
    private boolean mComputedNightMode;
    private Configuration mConfiguration;
    int mCurUiMode;
    private int mDefaultUiModeType;
    private boolean mDeskModeKeepsScreenOn;
    private final BroadcastReceiver mDockModeReceiver;
    private int mDockState;
    private boolean mEnableCarDockLaunch;
    private final Handler mHandler;
    private boolean mHoldingConfiguration;
    private int mLastBroadcastState;
    final Object mLock;
    private int mNightMode;
    private boolean mNightModeLocked;
    private final BroadcastReceiver mResultReceiver;
    private final IBinder mService;
    private int mSetUiMode;
    private StatusBarManager mStatusBarManager;
    boolean mSystemReady;
    private boolean mTelevision;
    private final TwilightListener mTwilightListener;
    private TwilightManager mTwilightManager;
    private boolean mUiModeLocked;
    private WakeLock mWakeLock;
    private boolean mWatch;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.UiModeManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.UiModeManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.UiModeManagerService.<clinit>():void");
    }

    public UiModeManagerService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mDockState = 0;
        this.mLastBroadcastState = 0;
        this.mNightMode = 1;
        this.mCarModeEnabled = false;
        this.mCharging = false;
        this.mEnableCarDockLaunch = ENABLE_LAUNCH_DESK_DOCK_APP;
        this.mUiModeLocked = false;
        this.mNightModeLocked = false;
        this.mCurUiMode = 0;
        this.mSetUiMode = 0;
        this.mHoldingConfiguration = false;
        this.mConfiguration = new Configuration();
        this.mHandler = new Handler();
        this.mResultReceiver = new BroadcastReceiver() {
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
        this.mDockModeReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                UiModeManagerService.this.updateDockState(intent.getIntExtra("android.intent.extra.DOCK_STATE", 0));
            }
        };
        this.mBatteryReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean z = false;
                UiModeManagerService uiModeManagerService = UiModeManagerService.this;
                if (intent.getIntExtra("plugged", 0) != 0) {
                    z = UiModeManagerService.ENABLE_LAUNCH_DESK_DOCK_APP;
                }
                uiModeManagerService.mCharging = z;
                synchronized (UiModeManagerService.this.mLock) {
                    if (UiModeManagerService.this.mSystemReady) {
                        UiModeManagerService.this.updateLocked(0, 0);
                    }
                }
            }
        };
        this.mTwilightListener = new TwilightListener() {
            public void onTwilightStateChanged() {
                UiModeManagerService.this.updateTwilight();
            }
        };
        this.mService = new Stub() {
            public void enableCarMode(int flags) {
                if (isUiModeLocked()) {
                    Slog.e(UiModeManagerService.TAG, "enableCarMode while UI mode is locked");
                    return;
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    synchronized (UiModeManagerService.this.mLock) {
                        UiModeManagerService.this.setCarModeLocked(UiModeManagerService.ENABLE_LAUNCH_DESK_DOCK_APP, flags);
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
                long ident = Binder.clearCallingIdentity();
                try {
                    int i;
                    synchronized (UiModeManagerService.this.mLock) {
                        i = UiModeManagerService.this.mCurUiMode & 15;
                    }
                    return i;
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }

            public void setNightMode(int mode) {
                if (!isNightModeLocked() || UiModeManagerService.this.getContext().checkCallingOrSelfPermission("android.permission.MODIFY_DAY_NIGHT_MODE") == 0) {
                    switch (mode) {
                        case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                        case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                        case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                            long ident = Binder.clearCallingIdentity();
                            try {
                                synchronized (UiModeManagerService.this.mLock) {
                                    if (UiModeManagerService.this.mNightMode != mode) {
                                        Secure.putInt(UiModeManagerService.this.getContext().getContentResolver(), "ui_night_mode", mode);
                                        UiModeManagerService.this.mNightMode = mode;
                                        UiModeManagerService.this.updateLocked(0, 0);
                                    }
                                    break;
                                }
                                return;
                            } finally {
                                Binder.restoreCallingIdentity(ident);
                            }
                        default:
                            throw new IllegalArgumentException("Unknown mode: " + mode);
                    }
                }
                Slog.e(UiModeManagerService.TAG, "Night mode locked, requires MODIFY_DAY_NIGHT_MODE permission");
            }

            public int getNightMode() {
                int -get1;
                synchronized (UiModeManagerService.this.mLock) {
                    -get1 = UiModeManagerService.this.mNightMode;
                }
                return -get1;
            }

            public boolean isUiModeLocked() {
                boolean -get3;
                synchronized (UiModeManagerService.this.mLock) {
                    -get3 = UiModeManagerService.this.mUiModeLocked;
                }
                return -get3;
            }

            public boolean isNightModeLocked() {
                boolean -get2;
                synchronized (UiModeManagerService.this.mLock) {
                    -get2 = UiModeManagerService.this.mNightModeLocked;
                }
                return -get2;
            }

            protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                if (UiModeManagerService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                    pw.println("Permission Denial: can't dump uimode service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                } else {
                    UiModeManagerService.this.dumpImpl(pw);
                }
            }
        };
    }

    private static Intent buildHomeIntent(String category) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory(category);
        intent.setFlags(270532608);
        return intent;
    }

    public void onStart() {
        boolean z;
        boolean z2 = false;
        boolean z3 = ENABLE_LAUNCH_DESK_DOCK_APP;
        Context context = getContext();
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(26, TAG);
        context.registerReceiver(this.mDockModeReceiver, new IntentFilter("android.intent.action.DOCK_EVENT"));
        context.registerReceiver(this.mBatteryReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        this.mConfiguration.setToDefaults();
        Resources res = context.getResources();
        this.mDefaultUiModeType = res.getInteger(17694797);
        if (res.getInteger(17694795) == 1) {
            z = ENABLE_LAUNCH_DESK_DOCK_APP;
        } else {
            z = false;
        }
        this.mCarModeKeepsScreenOn = z;
        if (res.getInteger(17694793) == 1) {
            z2 = ENABLE_LAUNCH_DESK_DOCK_APP;
        }
        this.mDeskModeKeepsScreenOn = z2;
        this.mEnableCarDockLaunch = res.getBoolean(17956926);
        this.mUiModeLocked = res.getBoolean(17956927);
        this.mNightModeLocked = res.getBoolean(17956928);
        PackageManager pm = context.getPackageManager();
        if (!pm.hasSystemFeature("android.hardware.type.television")) {
            z3 = pm.hasSystemFeature("android.software.leanback");
        }
        this.mTelevision = z3;
        this.mWatch = pm.hasSystemFeature("android.hardware.type.watch");
        this.mNightMode = Secure.getInt(context.getContentResolver(), "ui_night_mode", res.getInteger(17694798));
        synchronized (this) {
            updateConfigurationLocked();
            sendConfigurationLocked();
        }
        publishBinderService("uimode", this.mService);
    }

    void dumpImpl(PrintWriter pw) {
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
                pw.print("  mTwilightService.getCurrentState()=");
                pw.println(this.mTwilightManager.getCurrentState());
            }
        }
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_SYSTEM_SERVICES_READY) {
            synchronized (this.mLock) {
                boolean z;
                this.mTwilightManager = (TwilightManager) getLocalService(TwilightManager.class);
                if (this.mTwilightManager != null) {
                    this.mTwilightManager.registerListener(this.mTwilightListener, this.mHandler);
                }
                this.mSystemReady = ENABLE_LAUNCH_DESK_DOCK_APP;
                if (this.mDockState == 2) {
                    z = ENABLE_LAUNCH_DESK_DOCK_APP;
                } else {
                    z = false;
                }
                this.mCarModeEnabled = z;
                updateComputedNightModeLocked();
                updateLocked(0, 0);
            }
        }
    }

    void setCarModeLocked(boolean enabled, int flags) {
        if (this.mCarModeEnabled != enabled) {
            this.mCarModeEnabled = enabled;
        }
        this.mCarModeEnableFlags = flags;
    }

    private void updateDockState(int newState) {
        boolean z = ENABLE_LAUNCH_DESK_DOCK_APP;
        synchronized (this.mLock) {
            if (newState != this.mDockState) {
                this.mDockState = newState;
                if (this.mDockState != 2) {
                    z = false;
                }
                setCarModeLocked(z, 0);
                if (this.mSystemReady) {
                    updateLocked(1, 0);
                }
            }
        }
    }

    private static boolean isDeskDockState(int state) {
        switch (state) {
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
            case H.REPORT_LOSING_FOCUS /*3*/:
            case H.DO_TRAVERSAL /*4*/:
                return ENABLE_LAUNCH_DESK_DOCK_APP;
            default:
                return false;
        }
    }

    private void updateConfigurationLocked() {
        int uiMode = this.mDefaultUiModeType;
        if (!this.mUiModeLocked) {
            if (this.mTelevision) {
                uiMode = 4;
            } else if (this.mWatch) {
                uiMode = 6;
            } else if (this.mCarModeEnabled) {
                uiMode = 3;
            } else if (isDeskDockState(this.mDockState)) {
                uiMode = 2;
            }
        }
        if (this.mNightMode == 0) {
            int i;
            updateComputedNightModeLocked();
            if (this.mComputedNightMode) {
                i = 32;
            } else {
                i = 16;
            }
            uiMode |= i;
        } else {
            uiMode |= this.mNightMode << 4;
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
                ActivityManagerNative.getDefault().updateConfiguration(this.mConfiguration);
            } catch (RemoteException e) {
                Slog.w(TAG, "Failure communicating with activity manager", e);
            }
        }
    }

    void updateLocked(int enableFlags, int disableFlags) {
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
                    getContext().sendBroadcastAsUser(new Intent(oldAction), UserHandle.ALL);
                }
                this.mLastBroadcastState = 2;
                action = UiModeManager.ACTION_ENTER_CAR_MODE;
            }
        } else if (!isDeskDockState(this.mDockState)) {
            this.mLastBroadcastState = 0;
            action = oldAction;
        } else if (!isDeskDockState(this.mLastBroadcastState)) {
            if (oldAction != null) {
                getContext().sendBroadcastAsUser(new Intent(oldAction), UserHandle.ALL);
            }
            this.mLastBroadcastState = this.mDockState;
            action = UiModeManager.ACTION_ENTER_DESK_MODE;
        }
        if (action != null) {
            Intent intent = new Intent(action);
            intent.putExtra("enableFlags", enableFlags);
            intent.putExtra("disableFlags", disableFlags);
            getContext().sendOrderedBroadcastAsUser(intent, UserHandle.CURRENT, null, this.mResultReceiver, null, -1, null, null);
            this.mHoldingConfiguration = ENABLE_LAUNCH_DESK_DOCK_APP;
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
        boolean keepScreenOn = this.mCharging ? (this.mCarModeEnabled && this.mCarModeKeepsScreenOn && (this.mCarModeEnableFlags & 2) == 0) ? ENABLE_LAUNCH_DESK_DOCK_APP : this.mCurUiMode == 2 ? this.mDeskModeKeepsScreenOn : false : false;
        if (keepScreenOn == this.mWakeLock.isHeld()) {
            return;
        }
        if (keepScreenOn) {
            this.mWakeLock.acquire();
        } else {
            this.mWakeLock.release();
        }
    }

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

    private void sendConfigurationAndStartDreamOrDockAppLocked(String category) {
        this.mHoldingConfiguration = false;
        updateConfigurationLocked();
        boolean dockAppStarted = false;
        if (category != null) {
            Intent homeIntent = buildHomeIntent(category);
            if (Sandman.shouldStartDockApp(getContext(), homeIntent)) {
                try {
                    int result = ActivityManagerNative.getDefault().startActivityWithConfig(null, null, homeIntent, null, null, null, 0, 0, this.mConfiguration, null, -2);
                    if (result >= 0) {
                        dockAppStarted = ENABLE_LAUNCH_DESK_DOCK_APP;
                    } else if (result != -1) {
                        Slog.e(TAG, "Could not start dock app: " + homeIntent + ", startActivityWithConfig result " + result);
                    }
                } catch (RemoteException ex) {
                    Slog.e(TAG, "Could not start dock app: " + homeIntent, ex);
                }
            }
        }
        sendConfigurationLocked();
        if (category != null && !dockAppStarted) {
            Sandman.startDreamWhenDockedIfAppropriate(getContext());
        }
    }

    private void adjustStatusBarCarModeLocked() {
        Context context = getContext();
        if (this.mStatusBarManager == null) {
            this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        }
        if (this.mStatusBarManager != null) {
            int i;
            StatusBarManager statusBarManager = this.mStatusBarManager;
            if (this.mCarModeEnabled) {
                i = DumpState.DUMP_FROZEN;
            } else {
                i = 0;
            }
            statusBarManager.disable(i);
        }
    }

    void updateTwilight() {
        synchronized (this.mLock) {
            if (this.mNightMode == 0) {
                updateComputedNightModeLocked();
                updateLocked(0, 0);
            }
        }
    }

    private void updateComputedNightModeLocked() {
        if (this.mTwilightManager != null) {
            TwilightState state = this.mTwilightManager.getCurrentState();
            if (state != null) {
                this.mComputedNightMode = state.isNight();
            }
        }
    }
}

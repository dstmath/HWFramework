package com.android.server.input;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Flog;
import android.util.Log;
import android.view.IDockedStackListener.Stub;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;
import com.android.server.LocalServices;
import com.android.server.am.ActivityManagerService;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.provider.FrontFingerPrintSettings;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.List;

public class FrontFingerprintNavigation {
    private static final String FINGER_PRINT_ACTION_KEYEVENT = "com.android.huawei.FINGER_PRINT_ACTION_KEYEVENT";
    private static final long FP_HOME_VIBRATE_TIME = 60;
    public static final String FRONT_FINGERPRINT_BUTTON_LIGHT_MODE = "button_light_mode";
    public static final int FRONT_FINGERPRINT_KEYCODE_HOME_UP = 515;
    public static final String FRONT_FINGERPRINT_SWAP_KEY_POSITION = "swap_key_position";
    public static final String HAPTIC_FEEDBACK_TRIKEY_SETTINGS = "physic_navi_haptic_feedback_enabled";
    public static final String INTENT_KEY = "keycode";
    private static final int INVALID_NAVIMODE = -10000;
    private static final String TAG = null;
    public static final int VIBRATOR_MODE_LONG_PRESS_FOR_FRONT_FP = 12;
    public static final int VIBRATOR_MODE_LONG_PRESS_FOR_HOME_FRONT_FP = 13;
    public static final int VIBRATOR_MODE_SHORT_PRESS_FOR_FRONT_FP = 11;
    private boolean isNormalRunmode;
    private ActivityManager mAm;
    private Context mContext;
    private boolean mDeviceProvisioned;
    private boolean mDockedStackMinimized;
    private FingerPrintHomeUpInspector mFingerPrintUpInspector;
    private FingerprintManager mFingerprintManager;
    private final Handler mHandler;
    private boolean mHapticEnabled;
    private boolean mIsFPKeyguardEnable;
    private boolean mIsWakeUpScreen;
    private PowerManager mPowerManager;
    private final ContentResolver mResolver;
    private SettingsObserver mSettingsObserver;
    private FingerprintNavigationInspector mSystemUIBackInspector;
    private FingerprintNavigationInspector mSystemUIHomeInspector;
    private FingerprintNavigationInspector mSystemUIRecentInspector;
    private int mTrikeyNaviMode;
    private Vibrator mVibrator;

    abstract class FingerprintNavigationInspector {
        FingerprintNavigationInspector() {
        }

        public boolean probe(InputEvent event) {
            return false;
        }

        public void handle(InputEvent event) {
        }
    }

    final class FingerPrintHomeUpInspector extends FingerprintNavigationInspector {
        FingerPrintHomeUpInspector() {
            super();
        }

        public boolean probe(InputEvent event) {
            if (!FrontFingerprintNavigation.this.isSpecialKey(event, FrontFingerprintNavigation.FRONT_FINGERPRINT_KEYCODE_HOME_UP) || FrontFingerprintNavigation.this.isScreenOff() || FrontFingerprintNavigation.this.isSettingEnroll() || FrontFingerprintNavigation.this.isStrongBox()) {
                FrontFingerprintNavigation.this.mIsWakeUpScreen = false;
                return false;
            }
            Log.d(FrontFingerprintNavigation.TAG, "FingerPrintHomeUpInspector State ok");
            return true;
        }

        public void handle(InputEvent event) {
            KeyEvent ev = (KeyEvent) event;
            if (FrontFingerprintNavigation.this.mDeviceProvisioned && ev.getAction() != 0) {
                if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1 && FrontFingerPrintSettings.isSupportTrikey()) {
                    if (!FrontFingerprintNavigation.this.mIsWakeUpScreen) {
                        Log.i(FrontFingerprintNavigation.TAG, "handle FingerPrintHomeUpInspector!");
                        FrontFingerprintNavigation.this.startVibrate(FrontFingerprintNavigation.VIBRATOR_MODE_SHORT_PRESS_FOR_FRONT_FP);
                    }
                } else if (!(FrontFingerPrintSettings.isNaviBarEnabled(FrontFingerprintNavigation.this.mResolver) || FrontFingerprintNavigation.this.mIsWakeUpScreen)) {
                    Log.i(FrontFingerprintNavigation.TAG, "handle FingerPrintHomeUpInspector!");
                    FrontFingerprintNavigation.this.startVibrate(FrontFingerprintNavigation.VIBRATOR_MODE_SHORT_PRESS_FOR_FRONT_FP);
                }
                FrontFingerprintNavigation.this.mIsWakeUpScreen = false;
            }
        }
    }

    class SettingsObserver extends ContentObserver {
        final /* synthetic */ FrontFingerprintNavigation this$0;

        SettingsObserver(FrontFingerprintNavigation this$0, Handler handler) {
            boolean z;
            boolean z2 = true;
            this.this$0 = this$0;
            super(handler);
            registerContentObserver(UserHandle.myUserId());
            if (Secure.getIntForUser(this$0.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0) {
                z = true;
            } else {
                z = false;
            }
            this$0.mDeviceProvisioned = z;
            this$0.mTrikeyNaviMode = System.getIntForUser(this$0.mResolver, FrontFingerprintNavigation.FRONT_FINGERPRINT_SWAP_KEY_POSITION, FrontFingerPrintSettings.getDefaultNaviMode(), ActivityManager.getCurrentUser());
            if (Secure.getIntForUser(this$0.mResolver, "fp_keyguard_enable", 0, ActivityManager.getCurrentUser()) != 0) {
                z = true;
            } else {
                z = false;
            }
            this$0.mIsFPKeyguardEnable = z;
            if (System.getIntForUser(this$0.mResolver, FrontFingerprintNavigation.HAPTIC_FEEDBACK_TRIKEY_SETTINGS, 1, ActivityManager.getCurrentUser()) == 0) {
                z2 = false;
            }
            this$0.mHapticEnabled = z2;
        }

        public void registerContentObserver(int userId) {
            this.this$0.mResolver.registerContentObserver(System.getUriFor(FrontFingerprintNavigation.FRONT_FINGERPRINT_SWAP_KEY_POSITION), false, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor("device_provisioned"), false, this, userId);
            this.this$0.mResolver.registerContentObserver(Secure.getUriFor("fp_keyguard_enable"), false, this, userId);
            this.this$0.mResolver.registerContentObserver(System.getUriFor(FrontFingerprintNavigation.HAPTIC_FEEDBACK_TRIKEY_SETTINGS), false, this, userId);
        }

        public void onChange(boolean selfChange) {
            boolean z;
            boolean z2 = true;
            FrontFingerprintNavigation frontFingerprintNavigation = this.this$0;
            if (Secure.getIntForUser(this.this$0.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0) {
                z = true;
            } else {
                z = false;
            }
            frontFingerprintNavigation.mDeviceProvisioned = z;
            this.this$0.mTrikeyNaviMode = System.getIntForUser(this.this$0.mResolver, FrontFingerprintNavigation.FRONT_FINGERPRINT_SWAP_KEY_POSITION, FrontFingerPrintSettings.getDefaultNaviMode(), ActivityManager.getCurrentUser());
            frontFingerprintNavigation = this.this$0;
            if (Secure.getIntForUser(this.this$0.mResolver, "fp_keyguard_enable", 0, ActivityManager.getCurrentUser()) != 0) {
                z = true;
            } else {
                z = false;
            }
            frontFingerprintNavigation.mIsFPKeyguardEnable = z;
            FrontFingerprintNavigation frontFingerprintNavigation2 = this.this$0;
            if (System.getIntForUser(this.this$0.mResolver, FrontFingerprintNavigation.HAPTIC_FEEDBACK_TRIKEY_SETTINGS, 1, ActivityManager.getCurrentUser()) == 0) {
                z2 = false;
            }
            frontFingerprintNavigation2.mHapticEnabled = z2;
        }
    }

    final class SystemUIBackInspector extends FingerprintNavigationInspector {
        SystemUIBackInspector() {
            super();
        }

        public boolean probe(InputEvent event) {
            if (!FrontFingerprintNavigation.this.isSpecialKey(event, WifiProCommonUtils.RESP_CODE_UNSTABLE) || FrontFingerprintNavigation.this.isScreenOff() || FrontFingerprintNavigation.this.isSettingEnroll() || FrontFingerprintNavigation.this.isStrongBox()) {
                return false;
            }
            Log.d(FrontFingerprintNavigation.TAG, "SystemUIBackInspector State ok");
            return true;
        }

        public void handle(InputEvent event) {
            KeyEvent ev = (KeyEvent) event;
            if (FrontFingerprintNavigation.this.mDeviceProvisioned && ev.getAction() != 0) {
                Log.i(FrontFingerprintNavigation.TAG, "mSystemUIBackInspector handle sendKeyEvent : 4");
                if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1 && FrontFingerPrintSettings.isSupportTrikey()) {
                    if (!FrontFingerprintNavigation.this.isSingleTrikeyNaviMode()) {
                        FrontFingerprintNavigation.this.sendKeyEvent(3);
                        FrontFingerprintNavigation.this.startVibrate(FrontFingerprintNavigation.VIBRATOR_MODE_SHORT_PRESS_FOR_FRONT_FP);
                    } else if (FrontFingerprintNavigation.this.isMMITesting()) {
                        Log.d(FrontFingerprintNavigation.TAG, "MMITesting now.");
                    } else {
                        FrontFingerprintNavigation.this.sendKeyEvent(4);
                        FrontFingerprintNavigation.this.notifyTrikeyEvent(4);
                    }
                } else if (!FrontFingerPrintSettings.isNaviBarEnabled(FrontFingerprintNavigation.this.mResolver)) {
                    FrontFingerprintNavigation.this.sendKeyEvent(4);
                    FrontFingerprintNavigation.this.notifyTrikeyEvent(4);
                }
            }
        }
    }

    final class SystemUIHomeInspector extends FingerprintNavigationInspector {
        SystemUIHomeInspector() {
            super();
        }

        public boolean probe(InputEvent event) {
            if ((!FrontFingerprintNavigation.this.isSpecialKey(event, 66) && !FrontFingerprintNavigation.this.isSpecialKey(event, 502)) || FrontFingerprintNavigation.this.isSettingEnroll() || FrontFingerprintNavigation.this.isStrongBox()) {
                return false;
            }
            Log.d(FrontFingerprintNavigation.TAG, "mSystemUIHomeInspector State ok");
            return true;
        }

        public void handle(InputEvent event) {
            KeyEvent ev = (KeyEvent) event;
            if (FrontFingerprintNavigation.this.mDeviceProvisioned) {
                if (FrontFingerprintNavigation.this.isScreenOff()) {
                    if (ev.getAction() == 1) {
                        Flog.bdReport(FrontFingerprintNavigation.this.mContext, 15);
                        Log.i(FrontFingerprintNavigation.TAG, "hasEnrolledFingerprints is:" + FrontFingerprintNavigation.this.mFingerprintManager.hasEnrolledFingerprints() + "mIsFPKeyguardEnable is:" + FrontFingerprintNavigation.this.mIsFPKeyguardEnable);
                        if (!(FrontFingerprintNavigation.this.mFingerprintManager.hasEnrolledFingerprints() && FrontFingerprintNavigation.this.mIsFPKeyguardEnable)) {
                            FrontFingerprintNavigation.this.mIsWakeUpScreen = true;
                            FrontFingerprintNavigation.this.mPowerManager.wakeUp(SystemClock.uptimeMillis());
                        }
                    }
                } else if (ev.getAction() != 0) {
                    Log.i(FrontFingerprintNavigation.TAG, "mSystemUIHomeInspector handle sendKeyEvent KEYCODE_HOME");
                    if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1 && FrontFingerPrintSettings.isSupportTrikey()) {
                        if (FrontFingerprintNavigation.this.isSingleTrikeyNaviMode()) {
                            Flog.bdReport(FrontFingerprintNavigation.this.mContext, 9);
                            FrontFingerprintNavigation.this.startVibrate(FrontFingerprintNavigation.VIBRATOR_MODE_LONG_PRESS_FOR_HOME_FRONT_FP);
                            FrontFingerprintNavigation.this.sendKeyEvent(3);
                            FrontFingerprintNavigation.this.notifyTrikeyEvent(3);
                        } else if (FrontFingerprintNavigation.this.isKeyguardLocked()) {
                            FrontFingerprintNavigation.this.mIsWakeUpScreen = true;
                        } else if (FrontFingerprintNavigation.this.isMMITesting()) {
                            Log.d(FrontFingerprintNavigation.TAG, "MMITesting now.");
                        } else {
                            FrontFingerprintNavigation.this.startVoiceAssist();
                            FrontFingerprintNavigation.this.startVibrate(FrontFingerprintNavigation.VIBRATOR_MODE_LONG_PRESS_FOR_FRONT_FP);
                        }
                    } else if (!FrontFingerPrintSettings.isNaviBarEnabled(FrontFingerprintNavigation.this.mResolver)) {
                        Flog.bdReport(FrontFingerprintNavigation.this.mContext, 9);
                        FrontFingerprintNavigation.this.startVibrate(FrontFingerprintNavigation.VIBRATOR_MODE_LONG_PRESS_FOR_HOME_FRONT_FP);
                        FrontFingerprintNavigation.this.sendKeyEvent(3);
                        FrontFingerprintNavigation.this.notifyTrikeyEvent(3);
                    }
                }
            }
        }
    }

    final class SystemUIRecentInspector extends FingerprintNavigationInspector {
        SystemUIRecentInspector() {
            super();
        }

        public boolean probe(InputEvent event) {
            if (FrontFingerprintNavigation.this.isScreenOff() || FrontFingerprintNavigation.this.isKeyguardLocked() || FrontFingerprintNavigation.this.isSettingEnroll() || FrontFingerprintNavigation.this.isSuperPowerSaveMode() || FrontFingerprintNavigation.this.isAlarm() || FrontFingerprintNavigation.this.isStrongBox() || !FrontFingerprintNavigation.this.isValidRecentKeyEvent(event)) {
                return false;
            }
            Log.d(FrontFingerprintNavigation.TAG, "SystemUIRecentInspector State ok");
            return true;
        }

        public void handle(InputEvent event) {
            KeyEvent ev = (KeyEvent) event;
            if (!FrontFingerprintNavigation.this.mDeviceProvisioned || ev.getAction() == 0) {
                return;
            }
            if (!FrontFingerprintNavigation.this.isTopTaskHome() || FrontFingerprintNavigation.this.mDockedStackMinimized) {
                if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1 && FrontFingerPrintSettings.isSupportTrikey()) {
                    if (FrontFingerprintNavigation.this.isSingleTrikeyNaviMode()) {
                        if (FrontFingerprintNavigation.this.isMMITesting()) {
                            Log.d(FrontFingerprintNavigation.TAG, "MMITesting now.");
                            return;
                        }
                        Flog.bdReport(FrontFingerprintNavigation.this.mContext, 10);
                        Log.i(FrontFingerprintNavigation.TAG, "SystemUIRecentInspector handle sendKeyEvent KEYCODE_APP_SWITCH");
                        FrontFingerprintNavigation.this.sendKeyEvent(187);
                        FrontFingerprintNavigation.this.notifyTrikeyEvent(187);
                    }
                } else if (!FrontFingerPrintSettings.isNaviBarEnabled(FrontFingerprintNavigation.this.mResolver)) {
                    Flog.bdReport(FrontFingerprintNavigation.this.mContext, 10);
                    Log.i(FrontFingerprintNavigation.TAG, "SystemUIRecentInspector handle sendKeyEvent KEYCODE_APP_SWITCH");
                    FrontFingerprintNavigation.this.sendKeyEvent(187);
                    FrontFingerprintNavigation.this.notifyTrikeyEvent(187);
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.input.FrontFingerprintNavigation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.input.FrontFingerprintNavigation.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.input.FrontFingerprintNavigation.<clinit>():void");
    }

    public FrontFingerprintNavigation(Context context) {
        this.mDeviceProvisioned = true;
        this.mVibrator = null;
        this.mIsFPKeyguardEnable = false;
        this.mTrikeyNaviMode = -1;
        this.mDockedStackMinimized = false;
        this.mAm = null;
        this.mIsWakeUpScreen = false;
        this.isNormalRunmode = "normal".equals(SystemProperties.get("ro.runmode", "normal"));
        this.mHapticEnabled = true;
        this.mContext = context;
        this.mHandler = new Handler();
        this.mResolver = context.getContentResolver();
        this.mSettingsObserver = new SettingsObserver(this, this.mHandler);
        this.mSystemUIBackInspector = new SystemUIBackInspector();
        this.mSystemUIRecentInspector = new SystemUIRecentInspector();
        this.mSystemUIHomeInspector = new SystemUIHomeInspector();
        this.mFingerPrintUpInspector = new FingerPrintHomeUpInspector();
        this.mAm = (ActivityManager) context.getSystemService("activity");
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mFingerprintManager = (FingerprintManager) this.mContext.getSystemService("fingerprint");
        this.mVibrator = (SystemVibrator) ((Vibrator) this.mContext.getSystemService("vibrator"));
    }

    private void updateDockedStackFlag() {
        try {
            WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(new Stub() {
                public void onDividerVisibilityChanged(boolean visible) throws RemoteException {
                }

                public void onDockedStackExistsChanged(boolean exists) throws RemoteException {
                }

                public void onDockedStackMinimizedChanged(boolean minimized, long animDuration) throws RemoteException {
                    Log.d(FrontFingerprintNavigation.TAG, "onDockedStackMinimizedChanged:" + minimized);
                    FrontFingerprintNavigation.this.mDockedStackMinimized = minimized;
                }

                public void onAdjustedForImeChanged(boolean adjustedForIme, long animDuration) throws RemoteException {
                }

                public void onDockSideChanged(int newDockSide) throws RemoteException {
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Failed registering docked stack exists listener", e);
        }
    }

    public void systemRunning() {
        Log.d(TAG, "systemRunning");
        updateDockedStackFlag();
        initDefaultNaviValue();
    }

    private void initDefaultNaviValue() {
        boolean deviceProvisioned = false;
        if (this.mResolver != null && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION) {
            Log.d(TAG, "initDefaultNaviValue with user:" + ActivityManager.getCurrentUser());
            initDefaultHapticProp();
            if (System.getIntForUser(this.mResolver, "enable_navbar", INVALID_NAVIMODE, ActivityManager.getCurrentUser()) == INVALID_NAVIMODE) {
                int naviBarStatus = FrontFingerPrintSettings.isNaviBarEnabled(this.mResolver) ? 1 : 0;
                Log.d(TAG, "init defaultNaviBarStatus to:" + naviBarStatus);
                System.putIntForUser(this.mResolver, "enable_navbar", naviBarStatus, ActivityManager.getCurrentUser());
            }
            if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                if (System.getIntForUser(this.mResolver, FRONT_FINGERPRINT_SWAP_KEY_POSITION, INVALID_NAVIMODE, ActivityManager.getCurrentUser()) == INVALID_NAVIMODE) {
                    if (Secure.getIntForUser(this.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0) {
                        deviceProvisioned = true;
                    }
                    if (!deviceProvisioned) {
                        int trikeyNaviMode = FrontFingerPrintSettings.getDefaultNaviMode();
                        Log.d(TAG, "init default trikeyNaviMode to:" + trikeyNaviMode);
                        System.putIntForUser(this.mResolver, FRONT_FINGERPRINT_SWAP_KEY_POSITION, trikeyNaviMode, ActivityManager.getCurrentUser());
                    } else if (FrontFingerPrintSettings.isChinaArea()) {
                        Log.d(TAG, "init default trikeyNaviMode to singleButtonMode!");
                        System.putIntForUser(this.mResolver, FRONT_FINGERPRINT_SWAP_KEY_POSITION, -1, ActivityManager.getCurrentUser());
                    }
                }
                int buttonLightMode = System.getIntForUser(this.mResolver, FRONT_FINGERPRINT_BUTTON_LIGHT_MODE, INVALID_NAVIMODE, ActivityManager.getCurrentUser());
                if (buttonLightMode == INVALID_NAVIMODE) {
                    Log.d(TAG, "init default buttonlight mode to:" + buttonLightMode);
                    System.putIntForUser(this.mResolver, FRONT_FINGERPRINT_BUTTON_LIGHT_MODE, 1, ActivityManager.getCurrentUser());
                }
            }
        }
    }

    private void initDefaultHapticProp() {
        if ((FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0 || FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) && System.getIntForUser(this.mResolver, HAPTIC_FEEDBACK_TRIKEY_SETTINGS, INVALID_NAVIMODE, ActivityManager.getCurrentUser()) == INVALID_NAVIMODE) {
            Log.d(TAG, "init default hapicProp to enabled!");
            System.putIntForUser(this.mResolver, HAPTIC_FEEDBACK_TRIKEY_SETTINGS, 1, ActivityManager.getCurrentUser());
        }
    }

    private boolean isTopTaskHome() {
        boolean z = false;
        if (this.mAm == null) {
            return false;
        }
        List<RunningTaskInfo> tasks = this.mAm.getRunningTasks(1);
        if (!(tasks == null || tasks.isEmpty())) {
            RunningTaskInfo topTask = (RunningTaskInfo) tasks.get(0);
            if (topTask != null) {
                if (topTask.stackId == 0 && !getHomes().contains(topTask.topActivity.getPackageName())) {
                    z = true;
                }
                return z;
            }
        }
        return false;
    }

    private List<String> getHomes() {
        List<String> packages = new ArrayList();
        PackageManager packageManager = this.mContext.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        for (ResolveInfo info : packageManager.queryIntentActivities(intent, HwGlobalActionsData.FLAG_REBOOT)) {
            packages.add(info.activityInfo.packageName);
        }
        return packages;
    }

    private void notifyTrikeyEvent(int keyCode) {
        if (this.mDeviceProvisioned) {
            Intent intent = new Intent(FINGER_PRINT_ACTION_KEYEVENT);
            intent.putExtra(INTENT_KEY, keyCode);
            intent.setPackage("com.android.systemui");
            intent.addFlags(268435456);
            this.mContext.sendBroadcast(intent, "android.permission.STATUS_BAR");
        }
    }

    public boolean handleFingerprintEvent(InputEvent event) {
        if (!FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION || !this.isNormalRunmode) {
            Log.d(TAG, "do not support frontfingerprint");
            return false;
        } else if (this.mSystemUIBackInspector.probe(event)) {
            this.mSystemUIBackInspector.handle(event);
            return true;
        } else if (this.mSystemUIRecentInspector.probe(event)) {
            this.mSystemUIRecentInspector.handle(event);
            return true;
        } else if (this.mSystemUIHomeInspector.probe(event)) {
            this.mSystemUIHomeInspector.handle(event);
            return true;
        } else if (!this.mFingerPrintUpInspector.probe(event)) {
            return false;
        } else {
            this.mFingerPrintUpInspector.handle(event);
            return true;
        }
    }

    private boolean isScreenOff() {
        boolean z = false;
        PowerManager power = (PowerManager) this.mContext.getSystemService("power");
        if (power == null) {
            return false;
        }
        if (!power.isScreenOn()) {
            z = true;
        }
        return z;
    }

    private String getTopApp() {
        String pkgName = ((ActivityManagerService) ServiceManager.getService("activity")).topAppName();
        Log.d(TAG, "TopApp is " + pkgName);
        return pkgName;
    }

    private boolean isSpecialKey(InputEvent event, int code) {
        if (!(event instanceof KeyEvent)) {
            return false;
        }
        KeyEvent ev = (KeyEvent) event;
        Log.d(TAG, "keycode is : " + ev.getKeyCode());
        if (ev.getKeyCode() == code) {
            return true;
        }
        return false;
    }

    private void sendKeyEvent(int keycode) {
        int[] actions = new int[]{0, 1};
        for (int keyEvent : actions) {
            long curTime = SystemClock.uptimeMillis();
            InputManager.getInstance().injectInputEvent(new KeyEvent(curTime, curTime, keyEvent, keycode, 0, 0, -1, 0, 8, 257), 0);
        }
    }

    private boolean isSuperPowerSaveMode() {
        return SystemProperties.getBoolean("sys.super_power_save", false);
    }

    public void setCurrentUser(int newUserId) {
        Log.d(TAG, "setCurrentUser:" + newUserId);
        this.mSettingsObserver.registerContentObserver(newUserId);
        this.mSettingsObserver.onChange(true);
    }

    private boolean isSingleTrikeyNaviMode() {
        return this.mTrikeyNaviMode < 0;
    }

    private boolean isValidRecentKeyEvent(InputEvent event) {
        boolean z = true;
        if (1 == this.mContext.getResources().getConfiguration().orientation) {
            if (!isSpecialKey(event, 514)) {
                z = isSpecialKey(event, 513);
            }
            return z;
        } else if (2 != this.mContext.getResources().getConfiguration().orientation) {
            return false;
        } else {
            if (!isSpecialKey(event, 511)) {
                z = isSpecialKey(event, HwGlobalActionsData.FLAG_SILENTMODE_VIBRATE);
            }
            return z;
        }
    }

    private boolean isKeyguardLocked() {
        KeyguardManager keyguard = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (keyguard != null) {
            return keyguard.isKeyguardLocked();
        }
        return false;
    }

    private boolean isStrongBox() {
        String activityName = getTopApp();
        String activity_strongbox = "com.huawei.hidisk/.strongbox.ui.activity.StrongBoxVerifyPassActivity";
        if (activityName != null) {
            return activityName.equals(activity_strongbox);
        }
        return false;
    }

    private boolean isAlarm() {
        String pkgName = getTopApp();
        String pkg_alarm = "com.android.deskclock/.alarmclock.LockAlarmFullActivity";
        if (pkgName != null) {
            return pkgName.equals(pkg_alarm);
        }
        return false;
    }

    private boolean isSettingEnroll() {
        String pkgName = getTopApp();
        String pkg_setting = "com.android.settings/.fingerprint.enrollment.FingerprintEnrollActivity";
        if (pkgName != null) {
            return pkgName.equals(pkg_setting);
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startVibrate(int virbateMode) {
        if (!(isKeyguardLocked() || !this.mHapticEnabled || "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false")) || this.mVibrator == null)) {
            this.mVibrator.hwVibrate(null, virbateMode);
        }
    }

    private void startVoiceAssist() {
        try {
            ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).startAssist(new Bundle());
        } catch (Exception exp) {
            Log.e(TAG, "startVoiceAssist error:" + exp.getMessage());
        }
    }

    private boolean isMMITesting() {
        return "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false"));
    }
}

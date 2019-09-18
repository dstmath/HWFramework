package com.android.server.input;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.KeyguardManager;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Flog;
import android.util.Log;
import android.view.IDockedStackListener;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;
import com.android.server.LocalServices;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.provider.FrontFingerPrintSettings;
import huawei.com.android.server.fingerprint.FingerViewController;
import java.util.List;

public class FrontFingerprintNavigation {
    private static final String FINGER_PRINT_ACTION_KEYEVENT = "com.android.huawei.FINGER_PRINT_ACTION_KEYEVENT";
    private static final long FP_HOME_VIBRATE_TIME = 60;
    public static final String FRONT_FINGERPRINT_BUTTON_LIGHT_MODE = "button_light_mode";
    public static final int FRONT_FINGERPRINT_KEYCODE_HOME_UP = 515;
    public static final String FRONT_FINGERPRINT_SWAP_KEY_POSITION = "swap_key_position";
    private static final String GESTURE_NAVIGATION = "secure_gesture_navigation";
    public static final String HAPTIC_FEEDBACK_TRIKEY_SETTINGS = "physic_navi_haptic_feedback_enabled";
    public static final String INTENT_KEY = "keycode";
    private static final int INVALID_NAVIMODE = -10000;
    private static final boolean IS_SUPPORT_GESTURE_NAV = SystemProperties.getBoolean("ro.config.gesture_front_support", false);
    /* access modifiers changed from: private */
    public static final String TAG = FrontFingerprintNavigation.class.getSimpleName();
    /* access modifiers changed from: private */
    public int VIBRATOR_MODE_LONG_PRESS_FOR_FRONT_FP = 16;
    /* access modifiers changed from: private */
    public int VIBRATOR_MODE_LONG_PRESS_FOR_HOME_FRONT_FP = 16;
    /* access modifiers changed from: private */
    public int VIBRATOR_MODE_SHORT_PRESS_FOR_FRONT_FP = 8;
    private boolean isNormalRunmode = "normal".equals(SystemProperties.get("ro.runmode", "normal"));
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public boolean mDeviceProvisioned = true;
    DMDUtils mDmdUtils = null;
    /* access modifiers changed from: private */
    public boolean mDockedStackMinimized = false;
    private int mFingerPrintId = -1;
    private FingerPrintHomeUpInspector mFingerPrintUpInspector;
    /* access modifiers changed from: private */
    public FingerprintManager mFingerprintManager;
    /* access modifiers changed from: private */
    public boolean mFingerprintRemoveHome = SystemProperties.getBoolean("ro.config.finger_remove_home", false);
    private final Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mHapticEnabled = true;
    /* access modifiers changed from: private */
    public boolean mIsFPKeyguardEnable = false;
    /* access modifiers changed from: private */
    public boolean mIsGestureNavEnable = false;
    private boolean mIsPadDevice = false;
    /* access modifiers changed from: private */
    public boolean mIsWakeUpScreen = false;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public final ContentResolver mResolver;
    private SettingsObserver mSettingsObserver;
    private FingerprintNavigationInspector mSystemUIBackInspector;
    private FingerprintNavigationInspector mSystemUIHomeInspector;
    private FingerprintNavigationInspector mSystemUIRecentInspector;
    /* access modifiers changed from: private */
    public int mTrikeyNaviMode = -1;
    /* access modifiers changed from: private */
    public int mVibrateHomeUpMode = 8;
    private Vibrator mVibrator = null;

    final class FingerPrintHomeUpInspector extends FingerprintNavigationInspector {
        FingerPrintHomeUpInspector() {
            super();
        }

        public boolean probe(InputEvent event) {
            if (!FrontFingerprintNavigation.this.isSpecialKey(event, FrontFingerprintNavigation.FRONT_FINGERPRINT_KEYCODE_HOME_UP) || FrontFingerprintNavigation.this.isScreenOff() || FrontFingerprintNavigation.this.isSettingEnroll() || FrontFingerprintNavigation.this.isSettingCalibrationIntro() || FrontFingerprintNavigation.this.isStrongBox()) {
                boolean unused = FrontFingerprintNavigation.this.mIsWakeUpScreen = false;
                return false;
            }
            Log.d(FrontFingerprintNavigation.TAG, "FingerPrintHomeUpInspector State ok");
            return true;
        }

        public void handle(InputEvent event) {
            KeyEvent ev = (KeyEvent) event;
            if (!FrontFingerprintNavigation.this.mDeviceProvisioned || ev.getAction() == 0) {
                return;
            }
            if (FrontFingerprintNavigation.this.isFrontFpGestureNavEnable()) {
                Log.d(FrontFingerprintNavigation.TAG, "now in Gesture Navigation Mode, do not vibrate for long press");
                return;
            }
            if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1 || !FrontFingerPrintSettings.isSupportTrikey()) {
                if (!FrontFingerPrintSettings.isNaviBarEnabled(FrontFingerprintNavigation.this.mResolver) && !FrontFingerprintNavigation.this.mIsWakeUpScreen) {
                    Log.i(FrontFingerprintNavigation.TAG, "handle FingerPrintHomeUpInspector!");
                    FrontFingerprintNavigation.this.startVibrate(FrontFingerprintNavigation.this.mVibrateHomeUpMode);
                }
            } else if (!FrontFingerprintNavigation.this.mIsWakeUpScreen) {
                Log.i(FrontFingerprintNavigation.TAG, "handle FingerPrintHomeUpInspector!");
                FrontFingerprintNavigation.this.startVibrate(FrontFingerprintNavigation.this.VIBRATOR_MODE_SHORT_PRESS_FOR_FRONT_FP);
            }
            boolean unused = FrontFingerprintNavigation.this.mIsWakeUpScreen = false;
        }
    }

    abstract class FingerprintNavigationInspector {
        FingerprintNavigationInspector() {
        }

        public boolean probe(InputEvent event) {
            return false;
        }

        public void handle(InputEvent event) {
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            registerContentObserver(UserHandle.myUserId());
            boolean z = true;
            boolean unused = FrontFingerprintNavigation.this.mDeviceProvisioned = Settings.Secure.getIntForUser(FrontFingerprintNavigation.this.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0;
            int unused2 = FrontFingerprintNavigation.this.mTrikeyNaviMode = Settings.System.getIntForUser(FrontFingerprintNavigation.this.mResolver, "swap_key_position", FrontFingerPrintSettings.getDefaultNaviMode(), ActivityManager.getCurrentUser());
            boolean unused3 = FrontFingerprintNavigation.this.mIsFPKeyguardEnable = Settings.Secure.getIntForUser(FrontFingerprintNavigation.this.mResolver, "fp_keyguard_enable", 0, ActivityManager.getCurrentUser()) != 0;
            boolean unused4 = FrontFingerprintNavigation.this.mHapticEnabled = Settings.System.getIntForUser(FrontFingerprintNavigation.this.mResolver, "physic_navi_haptic_feedback_enabled", 1, ActivityManager.getCurrentUser()) != 0;
            boolean unused5 = FrontFingerprintNavigation.this.mIsGestureNavEnable = Settings.Secure.getIntForUser(FrontFingerprintNavigation.this.mResolver, "secure_gesture_navigation", 0, ActivityManager.getCurrentUser()) == 0 ? false : z;
        }

        public void registerContentObserver(int userId) {
            FrontFingerprintNavigation.this.mResolver.registerContentObserver(Settings.System.getUriFor("swap_key_position"), false, this, userId);
            FrontFingerprintNavigation.this.mResolver.registerContentObserver(Settings.System.getUriFor("device_provisioned"), false, this, userId);
            FrontFingerprintNavigation.this.mResolver.registerContentObserver(Settings.Secure.getUriFor("fp_keyguard_enable"), false, this, userId);
            FrontFingerprintNavigation.this.mResolver.registerContentObserver(Settings.System.getUriFor("physic_navi_haptic_feedback_enabled"), false, this, userId);
            FrontFingerprintNavigation.this.mResolver.registerContentObserver(Settings.Secure.getUriFor("secure_gesture_navigation"), false, this, userId);
        }

        public void onChange(boolean selfChange) {
            boolean z = true;
            boolean unused = FrontFingerprintNavigation.this.mDeviceProvisioned = Settings.Secure.getIntForUser(FrontFingerprintNavigation.this.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) != 0;
            int unused2 = FrontFingerprintNavigation.this.mTrikeyNaviMode = Settings.System.getIntForUser(FrontFingerprintNavigation.this.mResolver, "swap_key_position", FrontFingerPrintSettings.getDefaultNaviMode(), ActivityManager.getCurrentUser());
            boolean unused3 = FrontFingerprintNavigation.this.mIsFPKeyguardEnable = Settings.Secure.getIntForUser(FrontFingerprintNavigation.this.mResolver, "fp_keyguard_enable", 0, ActivityManager.getCurrentUser()) != 0;
            boolean unused4 = FrontFingerprintNavigation.this.mHapticEnabled = Settings.System.getIntForUser(FrontFingerprintNavigation.this.mResolver, "physic_navi_haptic_feedback_enabled", 1, ActivityManager.getCurrentUser()) != 0;
            FrontFingerprintNavigation frontFingerprintNavigation = FrontFingerprintNavigation.this;
            if (Settings.Secure.getIntForUser(FrontFingerprintNavigation.this.mResolver, "secure_gesture_navigation", 0, ActivityManager.getCurrentUser()) == 0) {
                z = false;
            }
            boolean unused5 = frontFingerprintNavigation.mIsGestureNavEnable = z;
        }
    }

    final class SystemUIBackInspector extends FingerprintNavigationInspector {
        SystemUIBackInspector() {
            super();
        }

        public boolean probe(InputEvent event) {
            if (!FrontFingerprintNavigation.this.isSpecialKey(event, WifiProCommonUtils.RESP_CODE_UNSTABLE) || FrontFingerprintNavigation.this.isScreenOff() || FrontFingerprintNavigation.this.isSettingEnroll() || FrontFingerprintNavigation.this.isSettingCalibrationIntro() || FrontFingerprintNavigation.this.isStrongBox()) {
                return false;
            }
            Log.d(FrontFingerprintNavigation.TAG, "SystemUIBackInspector State ok");
            return true;
        }

        public void handle(InputEvent event) {
            KeyEvent ev = (KeyEvent) event;
            if (FrontFingerprintNavigation.this.mDeviceProvisioned && ev.getAction() != 0) {
                Log.i(FrontFingerprintNavigation.TAG, "mSystemUIBackInspector handle sendKeyEvent : 4");
                if (FrontFingerprintNavigation.this.isFrontFpGestureNavEnable()) {
                    Log.d(FrontFingerprintNavigation.TAG, "now in Gesture Navigation Mode, do not handle KEYCODE_BACK");
                    return;
                }
                if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1 || !FrontFingerPrintSettings.isSupportTrikey()) {
                    if (!FrontFingerPrintSettings.isNaviBarEnabled(FrontFingerprintNavigation.this.mResolver)) {
                        FrontFingerprintNavigation.this.sendKeyEvent(4);
                        FrontFingerprintNavigation.this.notifyTrikeyEvent(4);
                    } else if (FrontFingerprintNavigation.this.isMMITesting()) {
                        Log.d(FrontFingerprintNavigation.TAG, "MMITesting now.");
                    } else if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0) {
                        Log.i(FrontFingerprintNavigation.TAG, "handle home event as NaviBarEnabled.");
                        if (!FrontFingerprintNavigation.this.mFingerprintRemoveHome || !FrontFingerPrintSettings.isNaviBarEnabled(FrontFingerprintNavigation.this.mResolver)) {
                            FrontFingerprintNavigation.this.sendKeyEvent(3);
                            FrontFingerprintNavigation.this.startVibrate(FrontFingerprintNavigation.this.VIBRATOR_MODE_SHORT_PRESS_FOR_FRONT_FP);
                        } else {
                            Log.d(FrontFingerprintNavigation.TAG, "Clicking to home was removed");
                        }
                    }
                } else if (!FrontFingerprintNavigation.this.isSingleTrikeyNaviMode()) {
                    FrontFingerprintNavigation.this.sendKeyEvent(3);
                    FrontFingerprintNavigation.this.startVibrate(FrontFingerprintNavigation.this.VIBRATOR_MODE_SHORT_PRESS_FOR_FRONT_FP);
                } else if (FrontFingerprintNavigation.this.isMMITesting()) {
                    Log.d(FrontFingerprintNavigation.TAG, "MMITesting now.");
                } else {
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
            if ((!FrontFingerprintNavigation.this.isSpecialKey(event, 66) && !FrontFingerprintNavigation.this.isSpecialKey(event, 502)) || FrontFingerprintNavigation.this.isSettingEnroll() || FrontFingerprintNavigation.this.isSettingCalibrationIntro() || FrontFingerprintNavigation.this.isStrongBox()) {
                return false;
            }
            Log.d(FrontFingerprintNavigation.TAG, "mSystemUIHomeInspector State ok");
            return true;
        }

        public void handle(InputEvent event) {
            KeyEvent ev = (KeyEvent) event;
            if (FrontFingerprintNavigation.this.mDeviceProvisioned) {
                if (!FrontFingerprintNavigation.this.isScreenOff()) {
                    if (ev.getAction() != 0) {
                        Log.i(FrontFingerprintNavigation.TAG, "mSystemUIHomeInspector handle sendKeyEvent KEYCODE_HOME");
                        if (FrontFingerprintNavigation.this.isFrontFpGestureNavEnable()) {
                            Log.d(FrontFingerprintNavigation.TAG, "now in Gesture Navigation Mode, do not handle KEYCODE_FINGERPRINT_LONGPRESS");
                        } else if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1 || !FrontFingerPrintSettings.isSupportTrikey()) {
                            if (!FrontFingerPrintSettings.isNaviBarEnabled(FrontFingerprintNavigation.this.mResolver)) {
                                Flog.bdReport(FrontFingerprintNavigation.this.mContext, 9);
                                FrontFingerprintNavigation.this.startVibrate(FrontFingerprintNavigation.this.VIBRATOR_MODE_LONG_PRESS_FOR_HOME_FRONT_FP);
                                FrontFingerprintNavigation.this.sendKeyEvent(3);
                                FrontFingerprintNavigation.this.notifyTrikeyEvent(3);
                                FrontFingerprintNavigation.this.checkLockMode();
                            }
                        } else if (FrontFingerprintNavigation.this.isSingleTrikeyNaviMode()) {
                            Flog.bdReport(FrontFingerprintNavigation.this.mContext, 9);
                            FrontFingerprintNavigation.this.startVibrate(FrontFingerprintNavigation.this.VIBRATOR_MODE_LONG_PRESS_FOR_HOME_FRONT_FP);
                            FrontFingerprintNavigation.this.sendKeyEvent(3);
                            FrontFingerprintNavigation.this.notifyTrikeyEvent(3);
                            FrontFingerprintNavigation.this.checkLockMode();
                        } else if (FrontFingerprintNavigation.this.isKeyguardLocked()) {
                            boolean unused = FrontFingerprintNavigation.this.mIsWakeUpScreen = true;
                        } else if (FrontFingerprintNavigation.this.isMMITesting()) {
                            Log.d(FrontFingerprintNavigation.TAG, "MMITesting now.");
                        } else {
                            FrontFingerprintNavigation.this.startVoiceAssist();
                            FrontFingerprintNavigation.this.startVibrate(FrontFingerprintNavigation.this.VIBRATOR_MODE_LONG_PRESS_FOR_FRONT_FP);
                        }
                    }
                } else if (ev.getAction() == 1) {
                    Flog.bdReport(FrontFingerprintNavigation.this.mContext, 15);
                    FingerprintManager unused2 = FrontFingerprintNavigation.this.mFingerprintManager = (FingerprintManager) FrontFingerprintNavigation.this.mContext.getSystemService("fingerprint");
                    if (FrontFingerprintNavigation.this.mFingerprintManager == null) {
                        Log.e(FrontFingerprintNavigation.TAG, "mFingerprintManager is null");
                        return;
                    }
                    String access$000 = FrontFingerprintNavigation.TAG;
                    Log.i(access$000, "hasEnrolledFingerprints is:" + FrontFingerprintNavigation.this.mFingerprintManager.hasEnrolledFingerprints() + "mIsFPKeyguardEnable is:" + FrontFingerprintNavigation.this.mIsFPKeyguardEnable);
                    if (!FrontFingerprintNavigation.this.mFingerprintManager.hasEnrolledFingerprints() || !FrontFingerprintNavigation.this.mIsFPKeyguardEnable) {
                        boolean unused3 = FrontFingerprintNavigation.this.mIsWakeUpScreen = true;
                        FrontFingerprintNavigation.this.mPowerManager.wakeUp(SystemClock.uptimeMillis());
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
            if (FrontFingerprintNavigation.this.isScreenOff() || FrontFingerprintNavigation.this.isKeyguardLocked() || FrontFingerprintNavigation.this.isSettingEnroll() || FrontFingerprintNavigation.this.isSettingCalibrationIntro() || FrontFingerprintNavigation.this.isSuperPowerSaveMode() || FrontFingerprintNavigation.this.isAlarm() || FrontFingerprintNavigation.this.isStrongBox() || !FrontFingerprintNavigation.this.isValidRecentKeyEvent(event)) {
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
            if (FrontFingerprintNavigation.this.isTopTaskRecent() && !FrontFingerprintNavigation.this.mDockedStackMinimized) {
                return;
            }
            if (FrontFingerprintNavigation.this.isFrontFpGestureNavEnable()) {
                Log.d(FrontFingerprintNavigation.TAG, "now in Gesture Navigation Mode, do not handle KEYCODE_APP_SWITCH");
                return;
            }
            if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1 || !FrontFingerPrintSettings.isSupportTrikey()) {
                if (!FrontFingerPrintSettings.isNaviBarEnabled(FrontFingerprintNavigation.this.mResolver)) {
                    Flog.bdReport(FrontFingerprintNavigation.this.mContext, 10);
                    if (HwFrameworkFactory.getVRSystemServiceManager() == null || !HwFrameworkFactory.getVRSystemServiceManager().isVRMode()) {
                        Log.i(FrontFingerprintNavigation.TAG, "SystemUIRecentInspector handle sendKeyEvent KEYCODE_APP_SWITCH");
                        FrontFingerprintNavigation.this.sendKeyEvent(187);
                        FrontFingerprintNavigation.this.notifyTrikeyEvent(187);
                    } else {
                        Log.d(FrontFingerprintNavigation.TAG, "Now is VRMode,.return");
                    }
                }
            } else if (FrontFingerprintNavigation.this.isSingleTrikeyNaviMode()) {
                if (FrontFingerprintNavigation.this.isMMITesting()) {
                    Log.d(FrontFingerprintNavigation.TAG, "MMITesting now.");
                    return;
                }
                Flog.bdReport(FrontFingerprintNavigation.this.mContext, 10);
                Log.i(FrontFingerprintNavigation.TAG, "SystemUIRecentInspector handle sendKeyEvent KEYCODE_APP_SWITCH");
                FrontFingerprintNavigation.this.sendKeyEvent(187);
                FrontFingerprintNavigation.this.notifyTrikeyEvent(187);
            }
        }
    }

    public FrontFingerprintNavigation(Context context) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.mResolver = context.getContentResolver();
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mSystemUIBackInspector = new SystemUIBackInspector();
        this.mSystemUIRecentInspector = new SystemUIRecentInspector();
        this.mSystemUIHomeInspector = new SystemUIHomeInspector();
        this.mFingerPrintUpInspector = new FingerPrintHomeUpInspector();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mDmdUtils = new DMDUtils(context);
    }

    private void updateDockedStackFlag() {
        try {
            WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(new IDockedStackListener.Stub() {
                public void onDividerVisibilityChanged(boolean visible) throws RemoteException {
                }

                public void onDockedStackExistsChanged(boolean exists) throws RemoteException {
                }

                public void onDockedStackMinimizedChanged(boolean minimized, long animDuration, boolean isHomeStackResizable) throws RemoteException {
                    String access$000 = FrontFingerprintNavigation.TAG;
                    Log.d(access$000, "onDockedStackMinimizedChanged:" + minimized);
                    boolean unused = FrontFingerprintNavigation.this.mDockedStackMinimized = minimized;
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
        initDefaultVibrateValue();
    }

    private void initDefaultVibrateValue() {
        this.VIBRATOR_MODE_SHORT_PRESS_FOR_FRONT_FP = SystemProperties.getInt("ro.config.trikey_vibrate_touch", 8);
        String str = TAG;
        Log.d(str, "The trikey touch vibrate config value is:" + this.VIBRATOR_MODE_SHORT_PRESS_FOR_FRONT_FP);
        this.VIBRATOR_MODE_LONG_PRESS_FOR_FRONT_FP = SystemProperties.getInt("ro.config.trikey_vibrate_press", 16);
        this.VIBRATOR_MODE_LONG_PRESS_FOR_HOME_FRONT_FP = this.VIBRATOR_MODE_LONG_PRESS_FOR_FRONT_FP;
        String str2 = TAG;
        Log.d(str2, "The trikey longPress vibrate config value is:" + this.VIBRATOR_MODE_LONG_PRESS_FOR_FRONT_FP);
        this.mIsPadDevice = isPad();
        String str3 = TAG;
        Log.d(str3, "mIsPadDevice is:" + this.mIsPadDevice);
        this.mFingerPrintId = SystemProperties.getInt("sys.fingerprint.deviceId", -1);
        this.mVibrateHomeUpMode = SystemProperties.getInt("ro.config.trikey_vibrate_touch_up", this.VIBRATOR_MODE_SHORT_PRESS_FOR_FRONT_FP);
        String str4 = TAG;
        Log.d(str4, "mVibrateHomeUpMode is:" + this.mVibrateHomeUpMode);
    }

    private void initDefaultNaviValue() {
        if (this.mResolver != null && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION) {
            Log.d(TAG, "initDefaultNaviValue with user:" + ActivityManager.getCurrentUser());
            initDefaultHapticProp();
            initDefaultNaviBarStatus();
            boolean deviceProvisioned = true;
            if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                if (Settings.System.getIntForUser(this.mResolver, "swap_key_position", INVALID_NAVIMODE, ActivityManager.getCurrentUser()) == INVALID_NAVIMODE) {
                    if (Settings.Secure.getIntForUser(this.mResolver, "device_provisioned", 0, ActivityManager.getCurrentUser()) == 0) {
                        deviceProvisioned = false;
                    }
                    if (!deviceProvisioned) {
                        int trikeyNaviMode = FrontFingerPrintSettings.getDefaultNaviMode();
                        Log.d(TAG, "init default trikeyNaviMode to:" + trikeyNaviMode);
                        Settings.System.putIntForUser(this.mResolver, "swap_key_position", trikeyNaviMode, ActivityManager.getCurrentUser());
                    } else if (FrontFingerPrintSettings.isChinaArea()) {
                        Log.d(TAG, "init default trikeyNaviMode to singleButtonMode!");
                        Settings.System.putIntForUser(this.mResolver, "swap_key_position", -1, ActivityManager.getCurrentUser());
                    }
                }
                if (Settings.System.getIntForUser(this.mResolver, "button_light_mode", INVALID_NAVIMODE, ActivityManager.getCurrentUser()) == INVALID_NAVIMODE) {
                    int buttonLightMode = FrontFingerPrintSettings.getDefaultBtnLightMode();
                    Log.d(TAG, "init default buttonlight mode to:" + buttonLightMode);
                    Settings.System.putIntForUser(this.mResolver, "button_light_mode", buttonLightMode, ActivityManager.getCurrentUser());
                }
            }
        }
    }

    private void initDefaultHapticProp() {
        if ((FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0 || FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) && Settings.System.getIntForUser(this.mResolver, "physic_navi_haptic_feedback_enabled", INVALID_NAVIMODE, ActivityManager.getCurrentUser()) == INVALID_NAVIMODE) {
            Log.d(TAG, "init default hapicProp to enabled!");
            Settings.System.putIntForUser(this.mResolver, "physic_navi_haptic_feedback_enabled", 1, ActivityManager.getCurrentUser());
        }
    }

    private void initDefaultNaviBarStatus() {
        int userID = ActivityManager.getCurrentUser();
        if (Settings.System.getIntForUser(this.mResolver, "enable_navbar", INVALID_NAVIMODE, userID) == INVALID_NAVIMODE) {
            int naviBarStatus = FrontFingerPrintSettings.isNaviBarEnabled(this.mResolver);
            Log.d(TAG, "init defaultNaviBarStatus to:" + ((int) naviBarStatus));
            int status = 1;
            if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0) {
                if (!(Settings.Secure.getIntForUser(this.mResolver, "device_provisioned", 0, userID) != 0)) {
                    Settings.System.putIntForUser(this.mResolver, "enable_navbar", naviBarStatus, userID);
                } else if (FrontFingerPrintSettings.isChinaArea()) {
                    if (FrontFingerPrintSettings.SINGLE_VIRTUAL_NAVIGATION_MODE != 1) {
                        status = 0;
                    }
                    Settings.System.putIntForUser(this.mResolver, "enable_navbar", status, userID);
                } else {
                    Settings.System.putIntForUser(this.mResolver, "enable_navbar", 1, userID);
                }
            } else if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                Settings.System.putIntForUser(this.mResolver, "enable_navbar", naviBarStatus, userID);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isTopTaskRecent() {
        boolean isRunningTaskInRecentStack = false;
        try {
            List<ActivityManager.RunningTaskInfo> tasks = ActivityManager.getService().getFilteredTasks(1, 0, 2);
            if (tasks.isEmpty()) {
                return false;
            }
            if (tasks.get(0).configuration.windowConfiguration.getActivityType() == 3) {
                isRunningTaskInRecentStack = true;
            }
            return isRunningTaskInRecentStack;
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void notifyTrikeyEvent(int keyCode) {
        if (this.mDeviceProvisioned) {
            int isShowNaviGuide = Settings.System.getIntForUser(this.mResolver, "systemui_tips_already_shown", 0, ActivityManager.getCurrentUser());
            String str = TAG;
            Log.d(str, "isShowNaviGuide:" + isShowNaviGuide);
            if (isShowNaviGuide == 0) {
                Intent intent = new Intent(FINGER_PRINT_ACTION_KEYEVENT);
                intent.putExtra("keycode", keyCode);
                intent.setPackage(FingerViewController.PKGNAME_OF_KEYGUARD);
                intent.addFlags(268435456);
                this.mContext.sendBroadcast(intent, "android.permission.STATUS_BAR");
            }
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

    /* access modifiers changed from: private */
    public boolean isScreenOff() {
        PowerManager power = (PowerManager) this.mContext.getSystemService("power");
        if (power != null) {
            return !power.isScreenOn();
        }
        return false;
    }

    private String getTopApp() {
        String pkgName = ServiceManager.getService("activity").topAppName();
        String str = TAG;
        Log.d(str, "TopApp is " + pkgName);
        return pkgName;
    }

    private boolean isKeyguardOccluded() {
        HwPhoneWindowManager policy = (HwPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
        return policy != null && policy.isKeyguardOccluded();
    }

    /* access modifiers changed from: private */
    public boolean isSpecialKey(InputEvent event, int code) {
        if (!(event instanceof KeyEvent)) {
            return false;
        }
        KeyEvent ev = (KeyEvent) event;
        String str = TAG;
        Log.d(str, "keycode is : " + ev.getKeyCode());
        if (ev.getKeyCode() == code) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void sendKeyEvent(int keycode) {
        int[] actions = {0, 1};
        for (int keyEvent : actions) {
            long curTime = SystemClock.uptimeMillis();
            KeyEvent ev = new KeyEvent(curTime, curTime, keyEvent, keycode, 0, 0, this.mFingerPrintId, 0, 8, 257);
            InputManager.getInstance().injectInputEvent(ev, 0);
        }
    }

    /* access modifiers changed from: private */
    public boolean isSuperPowerSaveMode() {
        return SystemProperties.getBoolean("sys.super_power_save", false);
    }

    public void setCurrentUser(int newUserId) {
        String str = TAG;
        Log.d(str, "setCurrentUser:" + newUserId);
        initDefaultValueWithUser();
        this.mSettingsObserver.registerContentObserver(newUserId);
        this.mSettingsObserver.onChange(true);
    }

    private void initDefaultValueWithUser() {
        if (this.mResolver != null && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION) {
            int userID = ActivityManager.getCurrentUser();
            String str = TAG;
            Log.d(str, "initDefaultNaviValue with user:" + userID);
            initDefaultHapticProp();
            if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                if (Settings.System.getIntForUser(this.mResolver, "swap_key_position", INVALID_NAVIMODE, userID) == INVALID_NAVIMODE) {
                    int trikeyNaviMode = FrontFingerPrintSettings.getDefaultNaviMode();
                    String str2 = TAG;
                    Log.d(str2, "init default trikeyNaviMode to:" + trikeyNaviMode);
                    Settings.System.putIntForUser(this.mResolver, "swap_key_position", trikeyNaviMode, userID);
                }
                if (Settings.System.getIntForUser(this.mResolver, "button_light_mode", INVALID_NAVIMODE, userID) == INVALID_NAVIMODE) {
                    int buttonLightMode = FrontFingerPrintSettings.getDefaultBtnLightMode();
                    String str3 = TAG;
                    Log.d(str3, "init user buttonlight mode to:" + buttonLightMode);
                    Settings.System.putIntForUser(this.mResolver, "button_light_mode", buttonLightMode, userID);
                }
            }
            if (Settings.System.getIntForUser(this.mResolver, "enable_navbar", INVALID_NAVIMODE, userID) == INVALID_NAVIMODE) {
                int naviBarStatus = FrontFingerPrintSettings.isNaviBarEnabled(this.mResolver);
                String str4 = TAG;
                Log.d(str4, "init defaultNaviBarStatus to:" + ((int) naviBarStatus));
                Settings.System.putIntForUser(this.mResolver, "enable_navbar", naviBarStatus, userID);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isSingleTrikeyNaviMode() {
        return this.mTrikeyNaviMode < 0;
    }

    /* access modifiers changed from: private */
    public void checkLockMode() {
        IActivityManager activityManager = ActivityManager.getService();
        try {
            if (activityManager.isInLockTaskMode()) {
                activityManager.stopSystemLockTaskMode();
                Log.i(TAG, "longclick exit lockMode");
            }
        } catch (RemoteException e) {
            String str = TAG;
            Log.i(str, "exit lockMode exception " + e.toString());
        }
    }

    /* access modifiers changed from: private */
    public boolean isValidRecentKeyEvent(InputEvent event) {
        boolean z = false;
        if (1 == this.mContext.getResources().getConfiguration().orientation) {
            if (isSpecialKey(event, 514) || isSpecialKey(event, 513)) {
                z = true;
            }
            return z;
        } else if (2 != this.mContext.getResources().getConfiguration().orientation) {
            return false;
        } else {
            if (isSpecialKey(event, 511) || isSpecialKey(event, 512)) {
                z = true;
            }
            return z;
        }
    }

    /* access modifiers changed from: private */
    public boolean isKeyguardLocked() {
        KeyguardManager keyguard = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (keyguard != null) {
            return keyguard.isKeyguardLocked();
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isStrongBox() {
        String activityName = getTopApp();
        if (activityName != null) {
            return activityName.equals("com.huawei.hidisk/.strongbox.ui.activity.StrongBoxVerifyPassActivity");
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isAlarm() {
        String pkgName = getTopApp();
        if (pkgName != null) {
            return pkgName.equals("com.android.deskclock/.alarmclock.LockAlarmFullActivity");
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isSettingEnroll() {
        String pkgName = getTopApp();
        if (pkgName != null) {
            return pkgName.equals("com.android.settings/.fingerprint.enrollment.FingerprintEnrollActivity");
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isSettingCalibrationIntro() {
        String pkgName = getTopApp();
        if (pkgName != null) {
            return pkgName.equals("com.android.settings/.fingerprint.enrollment.FingerprintCalibrationIntroActivity");
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void startVibrate(int virbateMode) {
        if (!isKeyguardLocked() && this.mHapticEnabled && !"true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false")) && this.mVibrator != null) {
            if (this.mIsPadDevice) {
                String str = TAG;
                Log.d(str, "startVibrateWithPattern:" + virbateMode);
                this.mVibrator.hwVibrate(null, virbateMode);
            } else {
                String str2 = TAG;
                Log.d(str2, "startVibrateWithConfigProp:" + virbateMode);
                this.mVibrator.vibrate((long) virbateMode);
            }
        }
    }

    private boolean isPad() {
        if ("tablet".equals(SystemProperties.get("ro.build.characteristics", ""))) {
            Log.d(TAG, "current device is pad!");
            return true;
        }
        Log.d(TAG, "current device is phone!");
        return false;
    }

    /* access modifiers changed from: private */
    public void startVoiceAssist() {
        try {
            ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).startAssist(new Bundle());
        } catch (Exception exp) {
            String str = TAG;
            Log.e(str, "startVoiceAssist error:" + exp.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public boolean isMMITesting() {
        return "true".equals(SystemProperties.get("runtime.mmitest.isrunning", "false"));
    }

    /* access modifiers changed from: private */
    public boolean isFrontFpGestureNavEnable() {
        return IS_SUPPORT_GESTURE_NAV && this.mIsGestureNavEnable && (!isKeyguardOccluded() || !isKeyguardLocked());
    }
}

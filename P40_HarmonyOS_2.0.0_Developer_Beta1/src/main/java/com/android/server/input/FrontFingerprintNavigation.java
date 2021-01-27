package com.android.server.input;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ActivityInfoEx;
import android.database.ContentObserver;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Flog;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import com.android.server.gesture.GestureNavConst;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.ActivityManagerExt;
import com.huawei.android.app.ActivityTaskManagerExt;
import com.huawei.android.app.WindowConfigurationEx;
import com.huawei.android.app.WindowManagerExt;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.hardware.input.InputManagerEx;
import com.huawei.android.os.PowerManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.VibratorEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.view.IDockedStackListenerEx;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import com.huawei.server.statusbar.StatusBarManagerInternalEx;
import com.huawei.utils.HwPartFactoryWraper;
import huawei.android.provider.FrontFingerPrintSettings;
import java.util.List;

public class FrontFingerprintNavigation {
    private static final String FINGER_PRINT_ACTION_KEYEVENT = "com.android.huawei.FINGER_PRINT_ACTION_KEYEVENT";
    private static final String FP_KEYGUARD_ENABLE = "fp_keyguard_enable";
    public static final String FRONT_FINGERPRINT_BUTTON_LIGHT_MODE = "button_light_mode";
    public static final int FRONT_FINGERPRINT_KEYCODE_HOME_UP = 515;
    public static final String FRONT_FINGERPRINT_SWAP_KEY_POSITION = "swap_key_position";
    private static final String GESTURE_NAV_TRIKEY_SETTINGS = "secure_gesture_navigation";
    public static final String HAPTIC_FEEDBACK_TRIKEY_SETTINGS = "physic_navi_haptic_feedback_enabled";
    private static final int INIT_FINGER_PRINT_ID = -1;
    public static final String INTENT_KEY = "keycode";
    private static final int INVALID_NAVIMODE = -10000;
    private static final boolean IS_SUPPORT_GESTURE_NAV = SystemPropertiesEx.getBoolean("ro.config.gesture_front_support", false);
    private static final String LOG_MMI_TESTINT_NOW = "MMITesting now.";
    private static final String TAG = FrontFingerprintNavigation.class.getSimpleName();
    private static final int TRIKEY_NAVI_MODE = -1;
    private static final long VIBRATE_TIME = 60;
    private static final int VIBRATOR_MODE_LONG_PRESS = 16;
    private static final int VIBRATOR_MODE_SHORT_PRESS = 8;
    private Context mContext;
    private int mFingerPrintId = -1;
    private FingerPrintHomeUpInspector mFingerPrintUpInspector;
    private FingerprintManager mFingerprintManager;
    private final Handler mHandler;
    private boolean mIsDeviceProvisioned = true;
    private boolean mIsDockedStackMinimized = false;
    private boolean mIsFingerprintRemoveHome = SystemPropertiesEx.getBoolean("ro.config.finger_remove_home", false);
    private boolean mIsFpKeyguardEnable = false;
    private boolean mIsGestureNavEnable = false;
    private boolean mIsHapticEnabled = true;
    private boolean mIsNormalRunmode = "normal".equals(SystemPropertiesEx.get("ro.runmode", "normal"));
    private boolean mIsPadDevice = false;
    private boolean mIsWakeUpScreen = false;
    private PowerManager mPowerManager;
    private final ContentResolver mResolver;
    private SettingsObserver mSettingsObserver;
    private FingerprintNavigationInspector mSystemUiBackInspector;
    private FingerprintNavigationInspector mSystemUiHomeInspector;
    private FingerprintNavigationInspector mSystemUiRecentInspector;
    private int mTrikeyNaviMode = -1;
    private int mVibrateHomeUpMode = 8;
    private Vibrator mVibrator = null;
    private int mVibratorModeLongPressForFrontFp = VIBRATOR_MODE_LONG_PRESS;
    private int mVibratorModeLongPressForHomeFrontFp = VIBRATOR_MODE_LONG_PRESS;
    private int mVibratorModeShortPressForFrontFp = 8;

    public FrontFingerprintNavigation(Context context) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.mResolver = context.getContentResolver();
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mSystemUiBackInspector = new SystemUiBackInspector();
        this.mSystemUiRecentInspector = new SystemUiRecentInspector();
        this.mSystemUiHomeInspector = new SystemUiHomeInspector();
        this.mFingerPrintUpInspector = new FingerPrintHomeUpInspector();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
    }

    private void updateDockedStackFlag() {
        try {
            WindowManagerExt.registerDockedStackListener(new IDockedStackListenerEx() {
                /* class com.android.server.input.FrontFingerprintNavigation.AnonymousClass1 */

                public void onDividerVisibilityChanged(boolean isVisible) throws RemoteException {
                }

                public void onDockedStackExistsChanged(boolean isExists) throws RemoteException {
                }

                public void onDockedStackMinimizedChanged(boolean isMinimized, long animDuration, boolean isHomeStackResizable) throws RemoteException {
                    String str = FrontFingerprintNavigation.TAG;
                    Log.i(str, "onDockedStackMinimizedChanged:" + isMinimized);
                    FrontFingerprintNavigation.this.mIsDockedStackMinimized = isMinimized;
                }

                public void onAdjustedForImeChanged(boolean isAdjustedForIme, long animDuration) throws RemoteException {
                }

                public void onDockSideChanged(int newDockSide) throws RemoteException {
                }
            });
        } catch (RemoteException e) {
            String str = TAG;
            Log.e(str, "Failed registering docked stack exists listener" + e.getMessage());
        }
    }

    public void systemRunning() {
        Log.d(TAG, "systemRunning");
        updateDockedStackFlag();
        initDefaultNaviValue();
        initDefaultVibrateValue();
    }

    private void initDefaultVibrateValue() {
        this.mVibratorModeShortPressForFrontFp = SystemPropertiesEx.getInt("ro.config.trikey_vibrate_touch", 8);
        String str = TAG;
        Log.i(str, "The trikey touch vibrate config value is:" + this.mVibratorModeShortPressForFrontFp);
        this.mVibratorModeLongPressForFrontFp = SystemPropertiesEx.getInt("ro.config.trikey_vibrate_press", (int) VIBRATOR_MODE_LONG_PRESS);
        this.mVibratorModeLongPressForHomeFrontFp = this.mVibratorModeLongPressForFrontFp;
        String str2 = TAG;
        Log.i(str2, "The trikey longPress vibrate config value is:" + this.mVibratorModeLongPressForFrontFp);
        this.mIsPadDevice = isPad();
        String str3 = TAG;
        Log.i(str3, "mIsPadDevice is:" + this.mIsPadDevice);
        this.mFingerPrintId = SystemPropertiesEx.getInt("sys.fingerprint.deviceId", -1);
        this.mVibrateHomeUpMode = SystemPropertiesEx.getInt("ro.config.trikey_vibrate_touch_up", this.mVibratorModeShortPressForFrontFp);
        String str4 = TAG;
        Log.i(str4, "mVibrateHomeUpMode is:" + this.mVibrateHomeUpMode);
    }

    private void initDefaultNaviValue() {
        if (this.mResolver != null && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION) {
            String str = TAG;
            Log.i(str, "initDefaultNaviValue with user:" + ActivityManagerEx.getCurrentUser());
            initDefaultHapticProp();
            initDefaultNaviBarStatus();
            if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                initTrikeyNaviMode();
                if (SettingsEx.System.getIntForUser(this.mResolver, FRONT_FINGERPRINT_BUTTON_LIGHT_MODE, (int) INVALID_NAVIMODE, ActivityManagerEx.getCurrentUser()) == INVALID_NAVIMODE) {
                    int buttonLightMode = FrontFingerPrintSettings.getDefaultBtnLightMode();
                    String str2 = TAG;
                    Log.i(str2, "init default buttonlight mode to:" + buttonLightMode);
                    SettingsEx.System.putIntForUser(this.mResolver, FRONT_FINGERPRINT_BUTTON_LIGHT_MODE, buttonLightMode, ActivityManagerEx.getCurrentUser());
                }
            }
        }
    }

    private void initTrikeyNaviMode() {
        if (SettingsEx.System.getIntForUser(this.mResolver, FRONT_FINGERPRINT_SWAP_KEY_POSITION, (int) INVALID_NAVIMODE, ActivityManagerEx.getCurrentUser()) != INVALID_NAVIMODE) {
            Log.i(TAG, "trikeyNaviMode is not INVALID_NAVIMODE!");
            return;
        }
        boolean isDeviceProvisioned = false;
        if (SettingsEx.Secure.getIntForUser(this.mResolver, "device_provisioned", 0, ActivityManagerEx.getCurrentUser()) != 0) {
            isDeviceProvisioned = true;
        }
        if (!isDeviceProvisioned) {
            int trikeyNaviMode = FrontFingerPrintSettings.getDefaultNaviMode();
            String str = TAG;
            Log.i(str, "init default trikeyNaviMode to:" + trikeyNaviMode);
            SettingsEx.System.putIntForUser(this.mResolver, FRONT_FINGERPRINT_SWAP_KEY_POSITION, trikeyNaviMode, ActivityManagerEx.getCurrentUser());
        } else if (FrontFingerPrintSettings.isChinaArea()) {
            Log.i(TAG, "init default trikeyNaviMode to singleButtonMode!");
            SettingsEx.System.putIntForUser(this.mResolver, FRONT_FINGERPRINT_SWAP_KEY_POSITION, (int) INVALID_NAVIMODE, ActivityManagerEx.getCurrentUser());
        }
    }

    private void initDefaultHapticProp() {
        if ((FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0 || FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) && SettingsEx.System.getIntForUser(this.mResolver, HAPTIC_FEEDBACK_TRIKEY_SETTINGS, (int) INVALID_NAVIMODE, ActivityManagerEx.getCurrentUser()) == INVALID_NAVIMODE) {
            Log.d(TAG, "init default hapicProp to enabled!");
            SettingsEx.System.putIntForUser(this.mResolver, HAPTIC_FEEDBACK_TRIKEY_SETTINGS, 1, ActivityManagerEx.getCurrentUser());
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: com.android.server.input.FrontFingerprintNavigation */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v2, types: [boolean, int] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void initDefaultNaviBarStatus() {
        int userId = ActivityManagerEx.getCurrentUser();
        if (SettingsEx.System.getIntForUser(this.mResolver, "enable_navbar", (int) INVALID_NAVIMODE, userId) == INVALID_NAVIMODE) {
            ?? isNaviBarEnabled = FrontFingerPrintSettings.isNaviBarEnabled(this.mResolver);
            String str = TAG;
            Log.i(str, "init defaultNaviBarStatus to:" + (isNaviBarEnabled == true ? 1 : 0));
            if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0) {
                putNaviBarStatusForUser(userId, isNaviBarEnabled);
            } else if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                SettingsEx.System.putIntForUser(this.mResolver, "enable_navbar", (int) isNaviBarEnabled, userId);
            }
        }
    }

    private void putNaviBarStatusForUser(int userId, int naviBarStatus) {
        int status = 0;
        if (!(SettingsEx.Secure.getIntForUser(this.mResolver, "device_provisioned", 0, userId) != 0)) {
            SettingsEx.System.putIntForUser(this.mResolver, "enable_navbar", naviBarStatus, userId);
        } else if (FrontFingerPrintSettings.isChinaArea()) {
            if (FrontFingerPrintSettings.SINGLE_VIRTUAL_NAVIGATION_MODE == 1) {
                status = 1;
            }
            SettingsEx.System.putIntForUser(this.mResolver, "enable_navbar", status, userId);
        } else {
            SettingsEx.System.putIntForUser(this.mResolver, "enable_navbar", 1, userId);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTopTaskRecent() {
        try {
            List<ActivityManager.RunningTaskInfo> tasks = ActivityManagerExt.getFilteredTasks(1, 0, 2);
            if (tasks.isEmpty()) {
                return false;
            }
            return WindowConfigurationEx.getActivityType(tasks.get(0)) == 3;
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyTrikeyEvent(int keyCode) {
        if (this.mIsDeviceProvisioned) {
            int isShowNaviGuide = SettingsEx.System.getIntForUser(this.mResolver, "systemui_tips_already_shown", 0, ActivityManagerEx.getCurrentUser());
            String str = TAG;
            Log.i(str, "isShowNaviGuide:" + isShowNaviGuide);
            if (isShowNaviGuide == 0) {
                Intent intent = new Intent(FINGER_PRINT_ACTION_KEYEVENT);
                intent.putExtra(INTENT_KEY, keyCode);
                intent.setPackage("com.android.systemui");
                intent.addFlags(268435456);
                this.mContext.sendBroadcast(intent, "android.permission.STATUS_BAR");
            }
        }
    }

    public boolean handleFingerprintEvent(InputEvent event) {
        if (!FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION || !this.mIsNormalRunmode || isGestureNavEnable()) {
            Log.i(TAG, "do not support frontfingerprint");
            return false;
        } else if (this.mSystemUiBackInspector.probe(event)) {
            this.mSystemUiBackInspector.handle(event);
            return true;
        } else if (this.mSystemUiRecentInspector.probe(event)) {
            this.mSystemUiRecentInspector.handle(event);
            return true;
        } else if (this.mSystemUiHomeInspector.probe(event)) {
            this.mSystemUiHomeInspector.handle(event);
            return true;
        } else if (!this.mFingerPrintUpInspector.probe(event)) {
            return false;
        } else {
            this.mFingerPrintUpInspector.handle(event);
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isScreenOff() {
        PowerManager power = (PowerManager) this.mContext.getSystemService("power");
        if (power != null) {
            return !power.isScreenOn();
        }
        return false;
    }

    private String getTopApp() {
        ActivityInfo topActivity = ActivityManagerEx.getLastResumedActivity();
        if (topActivity == null) {
            return null;
        }
        String pkgName = ActivityInfoEx.getComponentName(topActivity).flattenToShortString();
        String str = TAG;
        Log.i(str, "TopApp is " + pkgName);
        return pkgName;
    }

    private boolean isKeyguardOccluded() {
        HwPhoneWindowManager policy = WindowManagerPolicyEx.getInstance().getHwPhoneWindowManager();
        return policy != null && policy.isKeyguardOccluded();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSpecialKey(InputEvent event, int code) {
        if (!(event instanceof KeyEvent)) {
            return false;
        }
        KeyEvent ev = (KeyEvent) event;
        String str = TAG;
        Log.i(str, "keycode is : " + ev.getKeyCode());
        if (ev.getKeyCode() == code) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendKeyEvent(int keycode) {
        int[] actions;
        Log.i(TAG, "sendKeyEvent keycode is : " + keycode);
        for (int i : new int[]{0, 1}) {
            long curTime = SystemClock.uptimeMillis();
            InputManagerEx.injectInputEvent(InputManagerEx.getInstance(), new KeyEvent(curTime, curTime, i, keycode, 0, 0, this.mFingerPrintId, 0, 8, 257), InputManagerEx.getInjectInputEventModeAsync());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSuperPowerSaveMode() {
        return SystemPropertiesEx.getBoolean(GestureNavConst.KEY_SUPER_SAVE_MODE, false);
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

    final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            registerContentObserver(UserHandleEx.myUserId());
            boolean z = true;
            FrontFingerprintNavigation.this.mIsDeviceProvisioned = SettingsEx.Secure.getIntForUser(FrontFingerprintNavigation.this.mResolver, "device_provisioned", 0, ActivityManagerEx.getCurrentUser()) != 0;
            FrontFingerprintNavigation.this.mTrikeyNaviMode = SettingsEx.System.getIntForUser(FrontFingerprintNavigation.this.mResolver, FrontFingerprintNavigation.FRONT_FINGERPRINT_SWAP_KEY_POSITION, FrontFingerPrintSettings.getDefaultNaviMode(), ActivityManagerEx.getCurrentUser());
            FrontFingerprintNavigation.this.mIsFpKeyguardEnable = SettingsEx.Secure.getIntForUser(FrontFingerprintNavigation.this.mResolver, FrontFingerprintNavigation.FP_KEYGUARD_ENABLE, 0, ActivityManagerEx.getCurrentUser()) != 0;
            FrontFingerprintNavigation.this.mIsHapticEnabled = SettingsEx.System.getIntForUser(FrontFingerprintNavigation.this.mResolver, FrontFingerprintNavigation.HAPTIC_FEEDBACK_TRIKEY_SETTINGS, 1, ActivityManagerEx.getCurrentUser()) != 0;
            FrontFingerprintNavigation.this.mIsGestureNavEnable = SettingsEx.Secure.getIntForUser(FrontFingerprintNavigation.this.mResolver, "secure_gesture_navigation", 0, ActivityManagerEx.getCurrentUser()) == 0 ? false : z;
        }

        public void registerContentObserver(int userId) {
            ContentResolverExt.registerContentObserver(FrontFingerprintNavigation.this.mResolver, Settings.System.getUriFor(FrontFingerprintNavigation.FRONT_FINGERPRINT_SWAP_KEY_POSITION), false, this, userId);
            ContentResolverExt.registerContentObserver(FrontFingerprintNavigation.this.mResolver, Settings.System.getUriFor("device_provisioned"), false, this, userId);
            ContentResolverExt.registerContentObserver(FrontFingerprintNavigation.this.mResolver, Settings.Secure.getUriFor(FrontFingerprintNavigation.FP_KEYGUARD_ENABLE), false, this, userId);
            ContentResolverExt.registerContentObserver(FrontFingerprintNavigation.this.mResolver, Settings.System.getUriFor(FrontFingerprintNavigation.HAPTIC_FEEDBACK_TRIKEY_SETTINGS), false, this, userId);
            ContentResolverExt.registerContentObserver(FrontFingerprintNavigation.this.mResolver, Settings.Secure.getUriFor("secure_gesture_navigation"), false, this, userId);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            FrontFingerprintNavigation frontFingerprintNavigation = FrontFingerprintNavigation.this;
            boolean z = true;
            frontFingerprintNavigation.mIsDeviceProvisioned = SettingsEx.Secure.getIntForUser(frontFingerprintNavigation.mResolver, "device_provisioned", 0, ActivityManagerEx.getCurrentUser()) != 0;
            FrontFingerprintNavigation frontFingerprintNavigation2 = FrontFingerprintNavigation.this;
            frontFingerprintNavigation2.mTrikeyNaviMode = SettingsEx.System.getIntForUser(frontFingerprintNavigation2.mResolver, FrontFingerprintNavigation.FRONT_FINGERPRINT_SWAP_KEY_POSITION, FrontFingerPrintSettings.getDefaultNaviMode(), ActivityManagerEx.getCurrentUser());
            FrontFingerprintNavigation frontFingerprintNavigation3 = FrontFingerprintNavigation.this;
            frontFingerprintNavigation3.mIsFpKeyguardEnable = SettingsEx.Secure.getIntForUser(frontFingerprintNavigation3.mResolver, FrontFingerprintNavigation.FP_KEYGUARD_ENABLE, 0, ActivityManagerEx.getCurrentUser()) != 0;
            FrontFingerprintNavigation frontFingerprintNavigation4 = FrontFingerprintNavigation.this;
            frontFingerprintNavigation4.mIsHapticEnabled = SettingsEx.System.getIntForUser(frontFingerprintNavigation4.mResolver, FrontFingerprintNavigation.HAPTIC_FEEDBACK_TRIKEY_SETTINGS, 1, ActivityManagerEx.getCurrentUser()) != 0;
            FrontFingerprintNavigation frontFingerprintNavigation5 = FrontFingerprintNavigation.this;
            if (SettingsEx.Secure.getIntForUser(frontFingerprintNavigation5.mResolver, "secure_gesture_navigation", 0, ActivityManagerEx.getCurrentUser()) == 0) {
                z = false;
            }
            frontFingerprintNavigation5.mIsGestureNavEnable = z;
        }
    }

    public void setCurrentUser(int newUserId) {
        String str = TAG;
        Log.i(str, "setCurrentUser:" + newUserId);
        initDefaultValueWithUser();
        this.mSettingsObserver.registerContentObserver(newUserId);
        this.mSettingsObserver.onChange(true);
    }

    /* JADX WARN: Type inference failed for: r3v3, types: [boolean, int] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void initDefaultValueWithUser() {
        if (this.mResolver != null && FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION) {
            int userId = ActivityManagerEx.getCurrentUser();
            String str = TAG;
            Log.i(str, "initDefaultNaviValue with user:" + userId);
            initDefaultHapticProp();
            if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                if (SettingsEx.System.getIntForUser(this.mResolver, FRONT_FINGERPRINT_SWAP_KEY_POSITION, (int) INVALID_NAVIMODE, userId) == INVALID_NAVIMODE) {
                    int trikeyNaviMode = FrontFingerPrintSettings.getDefaultNaviMode();
                    String str2 = TAG;
                    Log.i(str2, "init default trikeyNaviMode to:" + trikeyNaviMode);
                    SettingsEx.System.putIntForUser(this.mResolver, FRONT_FINGERPRINT_SWAP_KEY_POSITION, trikeyNaviMode, userId);
                }
                if (SettingsEx.System.getIntForUser(this.mResolver, FRONT_FINGERPRINT_BUTTON_LIGHT_MODE, (int) INVALID_NAVIMODE, userId) == INVALID_NAVIMODE) {
                    int buttonLightMode = FrontFingerPrintSettings.getDefaultBtnLightMode();
                    String str3 = TAG;
                    Log.i(str3, "init user buttonlight mode to:" + buttonLightMode);
                    SettingsEx.System.putIntForUser(this.mResolver, FRONT_FINGERPRINT_BUTTON_LIGHT_MODE, buttonLightMode, userId);
                }
            }
            if (SettingsEx.System.getIntForUser(this.mResolver, "enable_navbar", (int) INVALID_NAVIMODE, userId) == INVALID_NAVIMODE) {
                ?? isNaviBarEnabled = FrontFingerPrintSettings.isNaviBarEnabled(this.mResolver);
                String str4 = TAG;
                Log.i(str4, "init defaultNaviBarStatus to:" + (isNaviBarEnabled == true ? 1 : 0));
                SettingsEx.System.putIntForUser(this.mResolver, "enable_navbar", (int) isNaviBarEnabled, userId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSingleTrikeyNaviMode() {
        return this.mTrikeyNaviMode < 0;
    }

    final class SystemUiBackInspector extends FingerprintNavigationInspector {
        SystemUiBackInspector() {
            super();
        }

        @Override // com.android.server.input.FrontFingerprintNavigation.FingerprintNavigationInspector
        public boolean probe(InputEvent event) {
            if (!FrontFingerprintNavigation.this.isSpecialKey(event, 601) || FrontFingerprintNavigation.this.isScreenOff() || FrontFingerprintNavigation.this.isSettingEnroll() || FrontFingerprintNavigation.this.isSettingCalibrationIntro() || FrontFingerprintNavigation.this.isStrongBox()) {
                return false;
            }
            Log.i(FrontFingerprintNavigation.TAG, "SystemUIBackInspector State ok");
            return true;
        }

        @Override // com.android.server.input.FrontFingerprintNavigation.FingerprintNavigationInspector
        public void handle(InputEvent event) {
            KeyEvent ev = null;
            if (event instanceof KeyEvent) {
                ev = (KeyEvent) event;
            }
            if (ev != null && FrontFingerprintNavigation.this.mIsDeviceProvisioned && ev.getAction() != 0) {
                Log.i(FrontFingerprintNavigation.TAG, "mSystemUiBackInspector handle sendKeyEvent : 4");
                if (!FrontFingerPrintSettings.isGestureNavigationMode(FrontFingerprintNavigation.this.mResolver)) {
                    handleNaviTrikeySetting();
                } else if (FrontFingerprintNavigation.this.mIsFingerprintRemoveHome) {
                    Log.i(FrontFingerprintNavigation.TAG, "Clicking to home was removed in gesture navigation mode");
                } else {
                    FrontFingerprintNavigation.this.sendKeyEvent(3);
                    FrontFingerprintNavigation frontFingerprintNavigation = FrontFingerprintNavigation.this;
                    frontFingerprintNavigation.startVibrate(frontFingerprintNavigation.mVibratorModeShortPressForFrontFp);
                    Log.i(FrontFingerprintNavigation.TAG, "now in Gesture Navigation Mode,trans to home");
                }
            }
        }

        private void handleNaviTrikeySetting() {
            if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1 || !FrontFingerPrintSettings.isSupportTrikey()) {
                if (!FrontFingerPrintSettings.isNaviBarEnabled(FrontFingerprintNavigation.this.mResolver)) {
                    FrontFingerprintNavigation.this.sendKeyEvent(4);
                    FrontFingerprintNavigation.this.notifyTrikeyEvent(4);
                    return;
                }
                handleNaviBarEvent();
            } else if (!FrontFingerprintNavigation.this.isSingleTrikeyNaviMode()) {
                FrontFingerprintNavigation.this.sendKeyEvent(3);
                FrontFingerprintNavigation frontFingerprintNavigation = FrontFingerprintNavigation.this;
                frontFingerprintNavigation.startVibrate(frontFingerprintNavigation.mVibratorModeShortPressForFrontFp);
            } else if (FrontFingerprintNavigation.this.isMmiTesting()) {
                Log.i(FrontFingerprintNavigation.TAG, FrontFingerprintNavigation.LOG_MMI_TESTINT_NOW);
            } else {
                FrontFingerprintNavigation.this.sendKeyEvent(4);
                FrontFingerprintNavigation.this.notifyTrikeyEvent(4);
            }
        }

        private void handleNaviBarEvent() {
            if (FrontFingerprintNavigation.this.isMmiTesting()) {
                Log.i(FrontFingerprintNavigation.TAG, FrontFingerprintNavigation.LOG_MMI_TESTINT_NOW);
            } else if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0) {
                Log.i(FrontFingerprintNavigation.TAG, "handle home event as NaviBarEnabled.");
                if (!FrontFingerprintNavigation.this.mIsFingerprintRemoveHome || !FrontFingerPrintSettings.isNaviBarEnabled(FrontFingerprintNavigation.this.mResolver)) {
                    FrontFingerprintNavigation.this.sendKeyEvent(3);
                    FrontFingerprintNavigation frontFingerprintNavigation = FrontFingerprintNavigation.this;
                    frontFingerprintNavigation.startVibrate(frontFingerprintNavigation.mVibratorModeShortPressForFrontFp);
                    return;
                }
                Log.i(FrontFingerprintNavigation.TAG, "Clicking to home was removed");
            }
        }
    }

    final class SystemUiRecentInspector extends FingerprintNavigationInspector {
        SystemUiRecentInspector() {
            super();
        }

        @Override // com.android.server.input.FrontFingerprintNavigation.FingerprintNavigationInspector
        public boolean probe(InputEvent event) {
            if (FrontFingerprintNavigation.this.isScreenOff() || FrontFingerprintNavigation.this.isKeyguardLocked() || FrontFingerprintNavigation.this.isSettingEnroll() || FrontFingerprintNavigation.this.isSettingCalibrationIntro() || FrontFingerprintNavigation.this.isSuperPowerSaveMode() || FrontFingerprintNavigation.this.isAlarm() || FrontFingerprintNavigation.this.isStrongBox() || !FrontFingerprintNavigation.this.isValidRecentKeyEvent(event)) {
                return false;
            }
            Log.i(FrontFingerprintNavigation.TAG, "SystemUIRecentInspector State ok");
            return true;
        }

        @Override // com.android.server.input.FrontFingerprintNavigation.FingerprintNavigationInspector
        public void handle(InputEvent event) {
            KeyEvent ev = (KeyEvent) event;
            if (!FrontFingerprintNavigation.this.mIsDeviceProvisioned || ev.getAction() == 0) {
                return;
            }
            if (FrontFingerprintNavigation.this.isTopTaskRecent() && !FrontFingerprintNavigation.this.mIsDockedStackMinimized) {
                return;
            }
            if (FrontFingerPrintSettings.isGestureNavigationMode(FrontFingerprintNavigation.this.mResolver)) {
                Log.i(FrontFingerprintNavigation.TAG, "now in Gesture Navigation Mode, do not handle KEYCODE_APP_SWITCH");
            } else if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1 || !FrontFingerPrintSettings.isSupportTrikey()) {
                if (!FrontFingerPrintSettings.isNaviBarEnabled(FrontFingerprintNavigation.this.mResolver)) {
                    Flog.bdReport(FrontFingerprintNavigation.this.mContext, 991310010);
                    if (HwPartFactoryWraper.getVRSystemServiceManager() == null || !HwPartFactoryWraper.getVRSystemServiceManager().isVRMode()) {
                        Log.i(FrontFingerprintNavigation.TAG, "SystemUiRecentInspector handle sendKeyEvent KEYCODE_APP_SWITCH");
                        FrontFingerprintNavigation.this.sendKeyEvent(187);
                        FrontFingerprintNavigation.this.notifyTrikeyEvent(187);
                        return;
                    }
                    Log.i(FrontFingerprintNavigation.TAG, "Now is VRMode. return");
                }
            } else if (!FrontFingerprintNavigation.this.isSingleTrikeyNaviMode()) {
            } else {
                if (FrontFingerprintNavigation.this.isMmiTesting()) {
                    Log.d(FrontFingerprintNavigation.TAG, FrontFingerprintNavigation.LOG_MMI_TESTINT_NOW);
                    return;
                }
                Flog.bdReport(FrontFingerprintNavigation.this.mContext, 991310010);
                Log.i(FrontFingerprintNavigation.TAG, "SystemUiRecentInspector handle sendKeyEvent KEYCODE_APP_SWITCH");
                FrontFingerprintNavigation.this.sendKeyEvent(187);
                FrontFingerprintNavigation.this.notifyTrikeyEvent(187);
            }
        }
    }

    final class FingerPrintHomeUpInspector extends FingerprintNavigationInspector {
        FingerPrintHomeUpInspector() {
            super();
        }

        @Override // com.android.server.input.FrontFingerprintNavigation.FingerprintNavigationInspector
        public boolean probe(InputEvent event) {
            if (!FrontFingerprintNavigation.this.isSpecialKey(event, FrontFingerprintNavigation.FRONT_FINGERPRINT_KEYCODE_HOME_UP) || FrontFingerprintNavigation.this.isScreenOff() || FrontFingerprintNavigation.this.isSettingEnroll() || FrontFingerprintNavigation.this.isSettingCalibrationIntro() || FrontFingerprintNavigation.this.isStrongBox()) {
                FrontFingerprintNavigation.this.mIsWakeUpScreen = false;
                return false;
            }
            Log.i(FrontFingerprintNavigation.TAG, "FingerPrintHomeUpInspector State ok");
            return true;
        }

        @Override // com.android.server.input.FrontFingerprintNavigation.FingerprintNavigationInspector
        public void handle(InputEvent event) {
            KeyEvent ev = (KeyEvent) event;
            if (!FrontFingerprintNavigation.this.mIsDeviceProvisioned || ev.getAction() == 0) {
                return;
            }
            if (FrontFingerPrintSettings.isGestureNavigationMode(FrontFingerprintNavigation.this.mResolver)) {
                Log.i(FrontFingerprintNavigation.TAG, "now in Gesture Navigation Mode, do not vibrate for long press");
                return;
            }
            if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1 || !FrontFingerPrintSettings.isSupportTrikey()) {
                if (!FrontFingerPrintSettings.isNaviBarEnabled(FrontFingerprintNavigation.this.mResolver) && !FrontFingerprintNavigation.this.mIsWakeUpScreen) {
                    Log.i(FrontFingerprintNavigation.TAG, "handle FingerPrintHomeUpInspector!");
                    FrontFingerprintNavigation frontFingerprintNavigation = FrontFingerprintNavigation.this;
                    frontFingerprintNavigation.startVibrate(frontFingerprintNavigation.mVibrateHomeUpMode);
                }
            } else if (!FrontFingerprintNavigation.this.mIsWakeUpScreen) {
                Log.i(FrontFingerprintNavigation.TAG, "handle FingerPrintHomeUpInspector!");
                FrontFingerprintNavigation frontFingerprintNavigation2 = FrontFingerprintNavigation.this;
                frontFingerprintNavigation2.startVibrate(frontFingerprintNavigation2.mVibratorModeShortPressForFrontFp);
            }
            FrontFingerprintNavigation.this.mIsWakeUpScreen = false;
        }
    }

    final class SystemUiHomeInspector extends FingerprintNavigationInspector {
        SystemUiHomeInspector() {
            super();
        }

        @Override // com.android.server.input.FrontFingerprintNavigation.FingerprintNavigationInspector
        public boolean probe(InputEvent event) {
            if ((!FrontFingerprintNavigation.this.isSpecialKey(event, 66) && !FrontFingerprintNavigation.this.isSpecialKey(event, 502)) || FrontFingerprintNavigation.this.isSettingEnroll() || FrontFingerprintNavigation.this.isSettingCalibrationIntro() || FrontFingerprintNavigation.this.isStrongBox()) {
                return false;
            }
            Log.i(FrontFingerprintNavigation.TAG, "mSystemUIHomeInspector State ok");
            return true;
        }

        @Override // com.android.server.input.FrontFingerprintNavigation.FingerprintNavigationInspector
        public void handle(InputEvent event) {
            KeyEvent ev = (KeyEvent) event;
            if (FrontFingerprintNavigation.this.mIsDeviceProvisioned) {
                if (!FrontFingerprintNavigation.this.isScreenOff()) {
                    if (ev.getAction() != 0) {
                        Log.i(FrontFingerprintNavigation.TAG, "mSystemUiHomeInspector handle sendKeyEvent KEYCODE_HOME");
                        if (FrontFingerPrintSettings.isGestureNavigationMode(FrontFingerprintNavigation.this.mResolver)) {
                            Log.i(FrontFingerprintNavigation.TAG, "now in Gesture Navigation Mode, do not handle KEYCODE_FINGERPRINT_LONGPRESS");
                        } else {
                            handleNaviTrickeySetting();
                        }
                    }
                } else if (ev.getAction() == 1) {
                    Flog.bdReport(FrontFingerprintNavigation.this.mContext, 991310015);
                    FrontFingerprintNavigation frontFingerprintNavigation = FrontFingerprintNavigation.this;
                    frontFingerprintNavigation.mFingerprintManager = (FingerprintManager) frontFingerprintNavigation.mContext.getSystemService("fingerprint");
                    if (FrontFingerprintNavigation.this.mFingerprintManager == null) {
                        Log.e(FrontFingerprintNavigation.TAG, "mFingerprintManager is null");
                        return;
                    }
                    String str = FrontFingerprintNavigation.TAG;
                    Log.i(str, "hasEnrolledFingerprints is:" + FrontFingerprintNavigation.this.mFingerprintManager.hasEnrolledFingerprints() + "mIsFpKeyguardEnable is:" + FrontFingerprintNavigation.this.mIsFpKeyguardEnable);
                    if (!FrontFingerprintNavigation.this.mFingerprintManager.hasEnrolledFingerprints() || !FrontFingerprintNavigation.this.mIsFpKeyguardEnable) {
                        FrontFingerprintNavigation.this.mIsWakeUpScreen = true;
                        PowerManagerEx.wakeUp(FrontFingerprintNavigation.this.mPowerManager, SystemClock.uptimeMillis(), FrontFingerprintNavigation.TAG);
                    }
                }
            }
        }

        private void handleNaviTrickeySetting() {
            if (FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1 || !FrontFingerPrintSettings.isSupportTrikey()) {
                if (!FrontFingerPrintSettings.isNaviBarEnabled(FrontFingerprintNavigation.this.mResolver)) {
                    Flog.bdReport(FrontFingerprintNavigation.this.mContext, 991310009);
                    FrontFingerprintNavigation frontFingerprintNavigation = FrontFingerprintNavigation.this;
                    frontFingerprintNavigation.startVibrate(frontFingerprintNavigation.mVibratorModeLongPressForHomeFrontFp);
                    FrontFingerprintNavigation.this.sendKeyEvent(3);
                    FrontFingerprintNavigation.this.notifyTrikeyEvent(3);
                    FrontFingerprintNavigation.this.checkLockMode();
                }
            } else if (FrontFingerprintNavigation.this.isSingleTrikeyNaviMode()) {
                Flog.bdReport(FrontFingerprintNavigation.this.mContext, 991310009);
                FrontFingerprintNavigation frontFingerprintNavigation2 = FrontFingerprintNavigation.this;
                frontFingerprintNavigation2.startVibrate(frontFingerprintNavigation2.mVibratorModeLongPressForHomeFrontFp);
                FrontFingerprintNavigation.this.sendKeyEvent(3);
                FrontFingerprintNavigation.this.notifyTrikeyEvent(3);
                FrontFingerprintNavigation.this.checkLockMode();
            } else {
                handleKeyguardLock();
            }
        }

        private void handleKeyguardLock() {
            if (FrontFingerprintNavigation.this.isKeyguardLocked()) {
                FrontFingerprintNavigation.this.mIsWakeUpScreen = true;
            } else if (FrontFingerprintNavigation.this.isMmiTesting()) {
                Log.d(FrontFingerprintNavigation.TAG, FrontFingerprintNavigation.LOG_MMI_TESTINT_NOW);
            } else {
                FrontFingerprintNavigation.this.startVoiceAssist();
                FrontFingerprintNavigation frontFingerprintNavigation = FrontFingerprintNavigation.this;
                frontFingerprintNavigation.startVibrate(frontFingerprintNavigation.mVibratorModeLongPressForFrontFp);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkLockMode() {
        try {
            if (ActivityTaskManagerExt.isInLockTaskMode()) {
                ActivityTaskManagerExt.stopSystemLockTaskMode();
                Log.i(TAG, "longclick exit lockMode");
            }
        } catch (RemoteException e) {
            String str = TAG;
            Log.i(str, "exit lockMode exception " + e.toString());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidRecentKeyEvent(InputEvent event) {
        if (this.mContext.getResources().getConfiguration().orientation == 1) {
            return isSpecialKey(event, 514) || isSpecialKey(event, 513);
        }
        if (this.mContext.getResources().getConfiguration().orientation == 2) {
            return isSpecialKey(event, 511) || isSpecialKey(event, 512);
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isKeyguardLocked() {
        KeyguardManager keyguard = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (keyguard != null) {
            return keyguard.isKeyguardLocked();
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isStrongBox() {
        String activityName = getTopApp();
        if (activityName != null) {
            return activityName.equals("com.huawei.hidisk/.strongbox.ui.activity.StrongBoxVerifyPassActivity");
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAlarm() {
        String pkgName = getTopApp();
        if (pkgName == null) {
            return false;
        }
        if (pkgName.equals("com.huawei.deskclock/com.android.deskclock.alarmclock.LockAlarmFullActivity") || pkgName.equals("com.android.deskclock/.alarmclock.LockAlarmFullActivity")) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSettingEnroll() {
        String pkgName = getTopApp();
        if (pkgName != null) {
            return pkgName.equals("com.android.settings/.fingerprint.enrollment.FingerprintEnrollActivity");
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSettingCalibrationIntro() {
        String pkgName = getTopApp();
        if (pkgName != null) {
            return pkgName.equals("com.android.settings/.fingerprint.enrollment.FingerprintCalibrationIntroActivity");
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startVibrate(int virbateMode) {
        if (!isKeyguardLocked() && this.mIsHapticEnabled && !"true".equals(SystemPropertiesEx.get("runtime.mmitest.isrunning", "false")) && this.mVibrator != null) {
            if (this.mIsPadDevice) {
                String str = TAG;
                Log.i(str, "startVibrateWithPattern:" + virbateMode);
                VibratorEx.vibrate(this.mVibrator, (long) VIBRATE_TIME);
                return;
            }
            String str2 = TAG;
            Log.i(str2, "startVibrateWithConfigProp:" + virbateMode);
            VibratorEx.vibrate(this.mVibrator, (long) virbateMode);
        }
    }

    private boolean isPad() {
        if (GestureNavConst.DEVICE_TYPE_TABLET.equals(SystemPropertiesEx.get("ro.build.characteristics", BuildConfig.FLAVOR))) {
            Log.i(TAG, "current device is pad!");
            return true;
        }
        Log.i(TAG, "current device is phone!");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startVoiceAssist() {
        StatusBarManagerInternalEx.startAssist(new Bundle());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMmiTesting() {
        return "true".equals(SystemPropertiesEx.get("runtime.mmitest.isrunning", "false"));
    }

    private boolean isGestureNavEnable() {
        return IS_SUPPORT_GESTURE_NAV && this.mIsGestureNavEnable && (!isKeyguardOccluded() || !isKeyguardLocked());
    }
}

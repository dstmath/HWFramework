package huawei.com.android.internal.policy;

import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import com.android.internal.policy.IPressGestureDetector;
import huawei.android.provider.HwSettings.System;
import java.lang.reflect.InvocationTargetException;
import org.json.JSONException;
import org.json.JSONObject;

public class PressGestureDetector implements IPressGestureDetector {
    private static final String CALL_PACKAGE_NAME = "packageName";
    private static final int COUNTS_FINGER_ONE = 1;
    private static final int COUNTS_FINGER_TWO = 2;
    private static final int COUNTS_FINGER_ZERO = 0;
    private static final long DEFAULT_GESTURE_TIME_OUT_LIMIT = 450;
    private static final long GESTURE_CONFLICT_POINTERS_ANGLE_LIMIT = 60;
    private static final long GESTURE_CONFLICT_POINTERS_DISTANCE_LIMIT = 100;
    private static final String HITOUCH_PKG_NAME = "com.huawei.hitouch";
    private static final String INCALLUI_PKG_NAME = "com.android.incallui";
    private static final String INTENT_PACKAGE_NAME = "pkgName";
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", "default"));
    private static final String MAIN_TOUCH_POINT_X = "x";
    private static final String MAIN_TOUCH_POINT_Y = "y";
    private static final String METHOD_ISTOUCHEFFECTIVE_HIACTION = "isTouchEffective";
    private static final int POINTERS_MIN_DISTANCE_DP = 16;
    private static final int SCREEN_POINTER_MARGIN_DP = 5;
    private static final String SECOND_TOUCH_POINT_X = "x1";
    private static final String SECOND_TOUCH_POINT_Y = "y1";
    private static final int SECUREIME_POPUP = 1;
    private static final String SECUREIME_STATUS = "secure_input_status";
    private static final int SETTINGS_SWITCH_ON = 1;
    private static final String TAG = "PressGestureDetector";
    private static final float TOUCH_MOVE_BOUND_X = 30.0f;
    private static final float TOUCH_MOVE_BOUND_Y = 50.0f;
    private static final long TOUCH_TWO_FINGERS_TIME_OUT_LIMIT = 150;
    private static final String URI_HIACTION_MANAGER_PROVIDER = "content://com.huawei.hiaction.provider.HiActionManagerProvider";
    private static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";
    private final Context mContext;
    private final Context mContextActivity;
    private final FrameLayout mDecorView;
    private int mDisplayHeigh = 0;
    private float mDisplayScale = 0.0f;
    private int mDisplayWidth = 0;
    private boolean mDistanceALot = false;
    private int mFingerCount = 0;
    private boolean mGestureInterrupted;
    private boolean mHiTouchRestricted;
    private boolean mIsPhoneLongClickSwipe = false;
    private float mLongPressDownX = 0.0f;
    private float mLongPressDownY = 0.0f;
    private float mLongPressPointerDownX = 0.0f;
    private float mLongPressPointerDownY = 0.0f;
    private float mPointX = 0.0f;
    private float mPointY = 0.0f;
    private float mSecondPointX = 0.0f;
    private float mSecondPointY = 0.0f;
    private final StatusChangeSensor mSensor;
    private boolean mSensorRegistered = false;
    private boolean mStatus;
    private boolean mStatusChecked;
    private boolean mTextBoomEntered;
    private long mTouchDownTime;
    private long mTouchPointerDownTime;
    private final int mTouchSlop;
    private long mTriggerTime = DEFAULT_GESTURE_TIME_OUT_LIMIT;

    private class StatusChangeSensor {
        private static final String HITOUCH_SWITCH = "hitouch_enabled";
        private static final String KEY_CHILDMODE_STATUS = "childmode_status";
        private static final String MULTI_WINDOW_MODE = "split_screen_mode";
        private static final int SETTINGS_SWITCH_OFF = 0;
        private static final int SETTINGS_SWITCH_ON = 1;
        private static final String USER_GUIDE_SETUP_FLAG = "device_provisioned";
        private ContentObserver AccessibilitySwitchObserver;
        private ContentObserver ChildModeSwitchObserver;
        private ContentObserver SettingsHiTouchSwitchObserver;
        private boolean isAccessibilityEnabled;
        private boolean isChildMode;
        private boolean isDeviceProvisionedChecked;
        private boolean isHiTouchEnabled;
        private boolean isInMultiWindowMode;
        private boolean isLandscapeOrient;
        private boolean isTouchEffective_HiAction;

        /* synthetic */ StatusChangeSensor(PressGestureDetector this$0, StatusChangeSensor -this1) {
            this();
        }

        private StatusChangeSensor() {
            this.isHiTouchEnabled = true;
            this.isAccessibilityEnabled = false;
            this.isLandscapeOrient = false;
            this.isInMultiWindowMode = false;
            this.isTouchEffective_HiAction = true;
            this.isChildMode = false;
            this.isDeviceProvisionedChecked = false;
            this.SettingsHiTouchSwitchObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    StatusChangeSensor.this.updateSwitchStatus();
                }
            };
            this.AccessibilitySwitchObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    StatusChangeSensor.this.updateAccessibilityStatus();
                }
            };
            this.ChildModeSwitchObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    StatusChangeSensor.this.updateChildModeStatus();
                }
            };
        }

        public boolean getStatus() {
            updateChildModeStatus();
            checkMultiWindowModeStatus();
            this.isTouchEffective_HiAction = checkEffectiveStatusFromHiAction(PressGestureDetector.this.mContextActivity.getPackageName());
            return (!this.isHiTouchEnabled || (PressGestureDetector.this.isSecurityIMEPopup() ^ 1) == 0 || (this.isAccessibilityEnabled ^ 1) == 0 || (this.isLandscapeOrient ^ 1) == 0 || (this.isInMultiWindowMode ^ 1) == 0 || !this.isTouchEffective_HiAction) ? false : this.isChildMode ^ 1;
        }

        public void onConfigurationChanged(Configuration newConfig) {
            updateScreenOriatationStatus();
        }

        public boolean checkDeviceProvisioned() {
            if (this.isDeviceProvisionedChecked) {
                return true;
            }
            if (Secure.getInt(PressGestureDetector.this.mContextActivity.getContentResolver(), USER_GUIDE_SETUP_FLAG, 1) == 0) {
                Log.v(PressGestureDetector.TAG, "User guide setup is undergoing...");
                return false;
            }
            Log.v(PressGestureDetector.TAG, "User setup is finished.");
            this.isDeviceProvisionedChecked = true;
            return true;
        }

        public void registerObserver() {
            updateSwitchStatus();
            updateAccessibilityStatus();
            updateScreenOriatationStatus();
            updateChildModeStatus();
            ContentResolver resolver = PressGestureDetector.this.mContext.getContentResolver();
            resolver.registerContentObserver(Global.getUriFor(HITOUCH_SWITCH), true, this.SettingsHiTouchSwitchObserver);
            resolver.registerContentObserver(Secure.getUriFor("enabled_accessibility_services"), true, this.AccessibilitySwitchObserver);
            resolver.registerContentObserver(Secure.getUriFor(KEY_CHILDMODE_STATUS), true, this.ChildModeSwitchObserver);
        }

        public void unregisterObserver() {
            ContentResolver resolver = PressGestureDetector.this.mContext.getContentResolver();
            resolver.unregisterContentObserver(this.SettingsHiTouchSwitchObserver);
            resolver.unregisterContentObserver(this.AccessibilitySwitchObserver);
            resolver.unregisterContentObserver(this.ChildModeSwitchObserver);
        }

        private void updateSwitchStatus() {
            if (1 == Global.getInt(PressGestureDetector.this.mContextActivity.getContentResolver(), HITOUCH_SWITCH, 1)) {
                this.isHiTouchEnabled = true;
                Log.i(PressGestureDetector.TAG, "HiTouch Setting Switch, ON");
                return;
            }
            this.isHiTouchEnabled = false;
            Log.i(PressGestureDetector.TAG, "HiTouch Setting Switch, OFF");
        }

        private void updateAccessibilityStatus() {
            int accessibilityEnabled = 0;
            try {
                accessibilityEnabled = Secure.getInt(PressGestureDetector.this.mContextActivity.getContentResolver(), "accessibility_enabled");
            } catch (SettingNotFoundException e) {
                Log.e(PressGestureDetector.TAG, e.getMessage());
            }
            if (accessibilityEnabled == 1) {
                String services = Secure.getString(PressGestureDetector.this.mContextActivity.getContentResolver(), "enabled_accessibility_services");
                if (services != null) {
                    this.isAccessibilityEnabled = services.toLowerCase().contains(PressGestureDetector.this.mContextActivity.getPackageName().toLowerCase());
                    if (this.isAccessibilityEnabled) {
                        Log.v(PressGestureDetector.TAG, "Accessibility Services contains " + PressGestureDetector.this.mContextActivity.getPackageName() + ": " + this.isAccessibilityEnabled);
                        return;
                    }
                    return;
                }
                return;
            }
            this.isAccessibilityEnabled = false;
        }

        private void updateChildModeStatus() {
            int userId = UserHandle.myUserId();
            if (PressGestureDetector.this.isClonedProfile(userId)) {
                userId = 0;
            }
            if (1 == Secure.getIntForUser(PressGestureDetector.this.mContextActivity.getContentResolver(), KEY_CHILDMODE_STATUS, 0, userId)) {
                this.isChildMode = true;
                Log.i(PressGestureDetector.TAG, "Child Mode Switch, ON");
                return;
            }
            this.isChildMode = false;
        }

        private void updateScreenOriatationStatus() {
            if (PressGestureDetector.IS_TABLET) {
                this.isLandscapeOrient = false;
                return;
            }
            if (PressGestureDetector.this.mContextActivity.getResources().getConfiguration().orientation == 2) {
                this.isLandscapeOrient = true;
                Log.i(PressGestureDetector.TAG, "ORIENTATION_LANDSCAPE");
            } else {
                this.isLandscapeOrient = false;
            }
        }

        private void checkMultiWindowModeStatus() {
            if (1 == Secure.getInt(PressGestureDetector.this.mContextActivity.getContentResolver(), MULTI_WINDOW_MODE, 0)) {
                this.isInMultiWindowMode = true;
                Log.i(PressGestureDetector.TAG, "Check MultiWindow Mode: " + this.isInMultiWindowMode);
                return;
            }
            this.isInMultiWindowMode = false;
        }

        private boolean checkEffectiveStatusFromHiAction(String pkgName) {
            Uri uri = Uri.parse(PressGestureDetector.URI_HIACTION_MANAGER_PROVIDER);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("packageName", pkgName);
                try {
                    Bundle result = PressGestureDetector.this.getHitouchStartUpContext().getContentResolver().call(uri, PressGestureDetector.METHOD_ISTOUCHEFFECTIVE_HIACTION, jsonObject.toString(), null);
                    if (result == null) {
                        return false;
                    }
                    Log.i(PressGestureDetector.TAG, "Checking pkgName: " + pkgName + " Checking result: " + result.getBoolean(PressGestureDetector.METHOD_ISTOUCHEFFECTIVE_HIACTION));
                    return result.getBoolean(PressGestureDetector.METHOD_ISTOUCHEFFECTIVE_HIACTION);
                } catch (IllegalArgumentException e) {
                    Log.e(PressGestureDetector.TAG, "Get error " + e.getMessage() + " when calling isTouchEffective.");
                    return false;
                }
            } catch (JSONException e2) {
                Log.e(PressGestureDetector.TAG, "Get error " + e2.getMessage() + " when building jsonObject.");
                return false;
            }
        }
    }

    public PressGestureDetector(Context context, FrameLayout docerView, Context contextActivity) {
        this.mContext = context;
        this.mContextActivity = contextActivity;
        this.mDecorView = docerView;
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mSensor = new StatusChangeSensor(this, null);
        updateDisplayParameters();
    }

    public boolean isLongPressSwipe() {
        return this.mIsPhoneLongClickSwipe;
    }

    private boolean isSecurityIMEPopup() {
        boolean isSecrutiyIMEPopup = 1 == Global.getInt(this.mContext.getContentResolver(), SECUREIME_STATUS, 0);
        Log.i(TAG, "isSecrutiyIMEPopup:" + isSecrutiyIMEPopup);
        return isSecrutiyIMEPopup;
    }

    public boolean isRejectedUser() {
        int userId = UserHandle.myUserId();
        if (userId == 0) {
            return false;
        }
        try {
            if (UserManager.get(this.mContextActivity).getUserInfo(userId).isClonedProfile()) {
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "get Cloned Profile failed. Reject start hitouch.");
        }
        return true;
    }

    public static boolean isAbroadArea() {
        return SystemProperties.get("ro.config.hw_optb", System.FINGERSENSE_KNUCKLE_GESTURE_OFF).equals("156") ^ 1;
    }

    public void onAttached(int windowType) {
        boolean z = true;
        boolean z2 = false;
        this.mHiTouchRestricted = isAbroadArea();
        if (this.mHiTouchRestricted) {
            Log.i(TAG, "HiTouch restricted: AboardArea.");
            return;
        }
        boolean z3;
        if (windowType >= 1000) {
            z3 = true;
        } else {
            z3 = false;
        }
        this.mHiTouchRestricted = z3;
        if (this.mHiTouchRestricted) {
            Log.i(TAG, "HiTouch restricted: Sub windows restricted.");
            return;
        }
        this.mHiTouchRestricted = isComputerMode();
        if (this.mHiTouchRestricted) {
            Log.i(TAG, "HiTouch restricted: is computer mode.");
            return;
        }
        if (!(isLauncherApp() || matchPackage(HITOUCH_PKG_NAME))) {
            z = matchPackage(INCALLUI_PKG_NAME);
        }
        this.mHiTouchRestricted = z;
        if (!this.mHiTouchRestricted) {
            if (((KeyguardManager) this.mContext.getSystemService("keyguard")).isKeyguardLocked()) {
                z2 = WECHAT_PACKAGE_NAME.equals(this.mContextActivity.getPackageName()) ^ 1;
            }
            this.mHiTouchRestricted = z2;
            if (this.mHiTouchRestricted) {
                Log.i(TAG, "HiTouch restricted: Keyguard locked restricted.");
                return;
            }
            this.mHiTouchRestricted = isRejectedUser();
            if (this.mHiTouchRestricted) {
                Log.i(TAG, "HiTouch restricted: unsupport user type.");
            }
        }
    }

    public void onDetached() {
        if (!this.mHiTouchRestricted) {
            if (this.mSensorRegistered) {
                this.mSensor.unregisterObserver();
                this.mSensorRegistered = false;
            }
            resetSwipeFlag();
        }
    }

    public void handleBackKey() {
        if (!this.mHiTouchRestricted) {
            resetSwipeFlag();
        }
    }

    public void handleConfigurationChanged(Configuration newConfig) {
        updateDisplayParameters();
        this.mSensor.onConfigurationChanged(newConfig);
    }

    public boolean dispatchTouchEvent(MotionEvent ev, boolean isHandling) {
        if (isHandling || this.mHiTouchRestricted) {
            return false;
        }
        if (this.mDecorView.getParent() == this.mDecorView.getViewRootImpl()) {
            switch (ev.getAction() & 255) {
                case 0:
                    this.mFingerCount = 1;
                    this.mTextBoomEntered = false;
                    this.mDistanceALot = false;
                    this.mGestureInterrupted = false;
                    this.mStatusChecked = false;
                    this.mIsPhoneLongClickSwipe = false;
                    int actionIndexDown = ev.getActionIndex();
                    this.mLongPressDownX = ev.getX(actionIndexDown);
                    this.mLongPressDownY = ev.getY(actionIndexDown);
                    this.mTouchDownTime = SystemClock.uptimeMillis();
                    break;
                case 1:
                    this.mFingerCount = 0;
                    resetSwipeFlag();
                    break;
                case 2:
                    if (!this.mTextBoomEntered && this.mFingerCount >= 2 && ev.getPointerCount() >= 2 && !this.mGestureInterrupted && ((!this.mStatusChecked || (this.mStatus ^ 1) == 0) && this.mSensor.checkDeviceProvisioned())) {
                        float mainPointX = ev.getX(0);
                        float mainPointY = ev.getY(0);
                        float secondPointX = ev.getX(1);
                        float secondPointY = ev.getY(1);
                        if (this.mDistanceALot) {
                            Log.v(TAG, "HiTouch Miss: Moved a lot, X1: " + Math.abs(this.mLongPressDownX - mainPointX) + " Y1: " + Math.abs(this.mLongPressDownY - mainPointY) + " || X2: " + Math.abs(this.mLongPressPointerDownX - secondPointX) + " Y2: " + Math.abs(this.mLongPressPointerDownY - secondPointY));
                            break;
                        }
                        boolean needRemove = false;
                        if (this.mDecorView.pointInView(mainPointX, mainPointY, (float) this.mTouchSlop)) {
                            if (Math.abs(this.mLongPressDownX - mainPointX) >= TOUCH_MOVE_BOUND_X || Math.abs(this.mLongPressDownY - mainPointY) >= TOUCH_MOVE_BOUND_Y) {
                                needRemove = true;
                                this.mDistanceALot = true;
                            }
                            if (Math.abs(this.mLongPressPointerDownX - secondPointX) >= TOUCH_MOVE_BOUND_X || Math.abs(this.mLongPressPointerDownY - secondPointY) >= TOUCH_MOVE_BOUND_Y) {
                                needRemove = true;
                                this.mDistanceALot = true;
                            }
                            if (!checkPointsLocation(mainPointX, mainPointY, secondPointX, secondPointY)) {
                                this.mGestureInterrupted = true;
                                needRemove = true;
                            }
                        } else {
                            needRemove = true;
                            Log.i(TAG, "HiTouch Miss: point OUT of DecorView");
                        }
                        if (needRemove) {
                            Log.v(TAG, "Gesture doesn't match, stop text boom.");
                            resetSwipeFlag();
                            break;
                        }
                        long intervalTwoFingers = Math.abs(this.mTouchPointerDownTime - this.mTouchDownTime);
                        if (intervalTwoFingers > TOUCH_TWO_FINGERS_TIME_OUT_LIMIT) {
                            Log.i(TAG, "HiTouch Miss: Too large time interval(TwoFingers), " + intervalTwoFingers);
                            break;
                        }
                        if (!this.mSensorRegistered) {
                            this.mSensor.registerObserver();
                            this.mSensorRegistered = true;
                        }
                        if (checkTouchForBoom(ev.getSize(), mainPointX, mainPointY, secondPointX, secondPointY)) {
                            return true;
                        }
                    }
                    break;
                case 3:
                    resetSwipeFlag();
                    break;
                case 5:
                    Log.i(TAG, "ACTION_POINTER_DOWN.");
                    this.mFingerCount++;
                    this.mTextBoomEntered = false;
                    this.mDistanceALot = false;
                    this.mStatusChecked = false;
                    this.mIsPhoneLongClickSwipe = false;
                    if (this.mFingerCount > 2) {
                        Log.i(TAG, "HiTouch Miss: more than two pointers.");
                        this.mGestureInterrupted = true;
                    }
                    int actionIndexPointerDown = ev.getActionIndex();
                    if (this.mFingerCount == 2) {
                        this.mLongPressPointerDownX = ev.getX(actionIndexPointerDown);
                        this.mLongPressPointerDownY = ev.getY(actionIndexPointerDown);
                    }
                    this.mTouchPointerDownTime = SystemClock.uptimeMillis();
                    break;
                case 6:
                    this.mFingerCount--;
                    resetSwipeFlag();
                    break;
            }
        }
        Log.i(TAG, "mDecorView.getParent(): " + this.mDecorView.getParent() + " mDecorView.getViewRootImpl(): " + this.mDecorView.getViewRootImpl());
        resetSwipeFlag();
        return false;
    }

    private boolean isLauncherApp() {
        String pkgName = this.mContext.getPackageName();
        if (pkgName == null) {
            Log.i(TAG, "Can't get package name info, enable textboom by default");
            return false;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        ResolveInfo res = this.mContext.getPackageManager().resolveActivity(intent, 0);
        if (res == null) {
            Log.i(TAG, "ResolveInfo is null.");
            return false;
        } else if (res.activityInfo == null) {
            Log.i(TAG, "ActivityInfo is null.");
            return false;
        } else if (!pkgName.equals(res.activityInfo.packageName)) {
            return false;
        } else {
            Log.i(TAG, "HiTouch restricted: is Launcher App");
            return true;
        }
    }

    private boolean matchPackage(String pkgName) {
        if (!pkgName.equals(this.mContext.getPackageName())) {
            return false;
        }
        Log.i(TAG, "HiTouch restricted: match package " + pkgName);
        return true;
    }

    private boolean isComputerMode() {
        boolean z = false;
        try {
            Class<?> hwPCUtils = Class.forName("android.util.HwPCUtils");
            if (hwPCUtils != null) {
                Boolean isInPad = (Boolean) hwPCUtils.getMethod("enabledInPad", new Class[0]).invoke(hwPCUtils, new Object[0]);
                Boolean isInPcMode = (Boolean) hwPCUtils.getMethod("isPcCastMode", new Class[0]).invoke(hwPCUtils, new Object[0]);
                Log.i(TAG, "enabledInPad = " + isInPad + ",isPcCastMode = " + isInPcMode);
                if (isInPad.booleanValue()) {
                    z = isInPcMode.booleanValue();
                }
                return z;
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "fail to getIsInPCScreen ClassNotFoundException");
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "fail to getIsInPCScreen NoSuchMethodException");
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "fail to getIsInPCScreen IllegalAccessException");
        } catch (InvocationTargetException e4) {
            Log.e(TAG, "fail to getIsInPCScreen InvocationTargetException");
        } catch (IllegalArgumentException e5) {
            Log.e(TAG, "fail to getIsInPCScreen IllegalArgumentException");
        }
        return false;
    }

    private void resetSwipeFlag() {
        this.mIsPhoneLongClickSwipe = false;
    }

    private boolean checkTouchForBoom(float touchSize, float x, float y, float x1, float y1) {
        long interval = SystemClock.uptimeMillis() - this.mTouchDownTime;
        if (!this.mStatusChecked) {
            this.mStatus = this.mSensor.getStatus();
            if (checkTwoFingersGestureConflictScene(x, y, x1, y1)) {
                this.mTriggerTime = Global.getLong(this.mContextActivity.getContentResolver(), "hitouch_triggerTime", DEFAULT_GESTURE_TIME_OUT_LIMIT);
                Log.v(TAG, "Conflict scene, adaptive trigger time: " + this.mTriggerTime);
            } else {
                this.mTriggerTime = DEFAULT_GESTURE_TIME_OUT_LIMIT;
            }
            this.mStatusChecked = true;
        }
        if (!this.mStatus) {
            return false;
        }
        this.mIsPhoneLongClickSwipe = true;
        if (interval < this.mTriggerTime) {
            return false;
        }
        this.mPointX = x;
        this.mPointY = y;
        this.mSecondPointX = x1;
        this.mSecondPointY = y1;
        launchHiTouchService();
        this.mTextBoomEntered = true;
        return true;
    }

    public double getDistance(float x1, float y1, float x2, float y2) {
        float _x = Math.abs(x1 - x2);
        float _y = Math.abs(y1 - y2);
        return Math.sqrt((double) ((_x * _x) + (_y * _y)));
    }

    private void launchHiTouchService() {
        Intent intent = new Intent();
        intent.setClassName(HITOUCH_PKG_NAME, "com.huawei.hitouch.HiTouchService");
        intent.putExtra(MAIN_TOUCH_POINT_X, this.mPointX);
        intent.putExtra(MAIN_TOUCH_POINT_Y, this.mPointY);
        intent.putExtra(SECOND_TOUCH_POINT_X, this.mSecondPointX);
        intent.putExtra(SECOND_TOUCH_POINT_Y, this.mSecondPointY);
        intent.putExtra("pkgName", this.mContextActivity.getPackageName());
        Log.i(TAG, "launch HiTouch Service.");
        Context context = getHitouchStartUpContext();
        if (context != null) {
            context.startService(intent);
        } else {
            Log.i(TAG, "get context failed, do not launch Hitouch Service");
        }
    }

    private int dp2px(float dpValue) {
        return (int) ((this.mDisplayScale * dpValue) + 0.5f);
    }

    private int px2dp(float pxValue) {
        return (int) ((pxValue / this.mDisplayScale) + 0.5f);
    }

    private boolean checkPointsLocation(float pointX1, float pointY1, float pointX2, float pointY2) {
        int margin = dp2px(5.0f);
        if (pointX1 < ((float) margin) || pointX1 > ((float) (this.mDisplayWidth - margin))) {
            Log.d(TAG, "HiTouch Miss: Right: " + margin + " X1: " + pointX1 + " Left: " + (this.mDisplayWidth - margin));
            return false;
        } else if (pointX2 < ((float) margin) || pointX2 > ((float) (this.mDisplayWidth - margin))) {
            Log.d(TAG, "HiTouch Miss: Right: " + margin + " X2: " + pointX2 + " Left: " + (this.mDisplayWidth - margin));
            return false;
        } else if (pointY1 < ((float) margin) || pointY1 > ((float) (this.mDisplayHeigh - margin))) {
            Log.d(TAG, "HiTouch Miss: Top: " + margin + " Y1: " + pointY1 + " Bottom: " + (this.mDisplayHeigh - margin));
            return false;
        } else if (pointY2 < ((float) margin) || pointY2 > ((float) (this.mDisplayHeigh - margin))) {
            Log.d(TAG, "HiTouch Miss: Top: " + margin + " Y2: " + pointY2 + " Bottom: " + (this.mDisplayHeigh - margin));
            return false;
        } else {
            if (getDistance(pointX1, pointY1, pointX2, pointY2) >= ((double) dp2px(16.0f))) {
                return true;
            }
            Log.d(TAG, "HiTouch Miss: pointers are too close.");
            return false;
        }
    }

    private void updateDisplayParameters() {
        DisplayMetrics dm = this.mContextActivity.getResources().getDisplayMetrics();
        this.mDisplayScale = dm.density;
        this.mDisplayWidth = dm.widthPixels;
        this.mDisplayHeigh = dm.heightPixels;
    }

    private int getAngleByTwoPointers(float firstPntX, float firstPntY, float SecondPntX, float SecondPntY) {
        return 90 - ((int) ((Math.atan(((double) Math.abs(firstPntY - SecondPntY)) / ((double) Math.abs(firstPntX - SecondPntX))) / 3.141592653589793d) * 180.0d));
    }

    private boolean checkTwoFingersGestureConflictScene(float firstPntX, float firstPntY, float SecondPntX, float SecondPntY) {
        if (((long) getAngleByTwoPointers(firstPntX, firstPntY, SecondPntX, SecondPntY)) >= GESTURE_CONFLICT_POINTERS_ANGLE_LIMIT || ((long) px2dp((float) getDistance(firstPntX, firstPntY, SecondPntX, SecondPntY))) <= GESTURE_CONFLICT_POINTERS_DISTANCE_LIMIT) {
            return false;
        }
        return true;
    }

    private boolean isClonedProfile(int userId) {
        if (userId == 0) {
            return false;
        }
        boolean isClonedProfile = false;
        try {
            isClonedProfile = UserManager.get(this.mContextActivity).getUserInfo(userId).isClonedProfile();
        } catch (Exception e) {
            Log.e(TAG, "get Cloned Profile failed.");
        }
        return isClonedProfile;
    }

    private Context getHitouchStartUpContext() {
        if (!isClonedProfile(UserHandle.myUserId())) {
            return this.mContextActivity;
        }
        Log.i(TAG, "Cloned profile is true.");
        Context context = null;
        try {
            context = this.mContextActivity.createPackageContextAsUser(HITOUCH_PKG_NAME, 0, new UserHandle(0));
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getHitouchStartUpContext:" + e);
        }
        if (context != null) {
            return context;
        }
        Log.d(TAG, "context is null");
        return this.mContextActivity;
    }
}

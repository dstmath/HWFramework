package com.android.server.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hdm.HwDeviceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.HwSlog;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;
import com.android.server.am.HwActivityManagerService;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.android.os.HwGeneralManager;
import huawei.android.provider.FrontFingerPrintSettings;

public class NavigationBarPolicy implements GestureDetector.OnGestureListener {
    private static final boolean ANTI_TOUCH_ENABLED;
    static final boolean DEBUG = false;
    private static final int DOUBLE_SWIP_TIMEOUT = 500;
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    static final int HIT_REGION_SCALE = 4;
    static int HIT_REGION_TO_MAX = 20;
    static int HIT_REGION_TO_TOP_BOTTOM = 130;
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    public static final int SWIPE_FROM_BOTTOM = 2;
    public static final int SWIPE_FROM_RIGHT = 3;
    private static final String TAG = "NavigationBarPolicy";
    private static final boolean mSupportGameAssist = (SystemProperties.getInt("ro.config.gameassist", 0) == 1);
    private boolean IS_SUPPORT_PRESSURE;
    private HwActivityManagerService mAms;
    private Context mContext = null;
    private GestureDetector mDetector = null;
    private boolean mEnableSwipeInCurrentGameApp;
    boolean mForceMinNavigationBar;
    private int mHoleHeight;
    private boolean mImmersiveMode;
    private boolean mIsValidGesture;
    private long mLastGetGameControlTime;
    private int mLastSwipeOritation;
    boolean mMinNavigationBar;
    private PhoneWindowManager mPolicy = null;
    private long mSecondToLastGetGameControlTime;
    private int mSecondToLastSwipeOritation;
    private Toast mToast;
    private Point realSize;

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.config.gameassist.anti-touch", 0) == 1) {
            z = true;
        }
        ANTI_TOUCH_ENABLED = z;
    }

    public NavigationBarPolicy(Context context, PhoneWindowManager policy) {
        boolean z = false;
        this.mMinNavigationBar = false;
        this.mForceMinNavigationBar = false;
        this.mIsValidGesture = false;
        this.IS_SUPPORT_PRESSURE = false;
        this.mImmersiveMode = false;
        this.realSize = new Point();
        this.mHoleHeight = 0;
        this.mContext = context;
        this.mPolicy = policy;
        this.mDetector = new GestureDetector(context, this);
        if (SystemProperties.getBoolean("sys.bopd", false)) {
            Settings.Global.putInt(this.mContext.getContentResolver(), "navigationbar_is_min", 0);
        }
        this.mMinNavigationBar = Settings.Global.getInt(this.mContext.getContentResolver(), "navigationbar_is_min", 0) != 0 ? true : z;
        updateRealSize();
        this.IS_SUPPORT_PRESSURE = HwGeneralManager.getInstance().isSupportForce();
        parseHole();
    }

    public void addPointerEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
    }

    private void reset() {
        this.mIsValidGesture = false;
    }

    public void setImmersiveMode(boolean mode) {
        this.mImmersiveMode = mode;
    }

    private boolean touchDownIsValid(float pointX, float pointY) {
        boolean z = false;
        if (this.mPolicy.mDisplay == null || this.mForceMinNavigationBar || (this.mPolicy.mKeyguardDelegate.isShowing() && !this.mPolicy.mKeyguardDelegate.isOccluded())) {
            return false;
        }
        if (this.IS_SUPPORT_PRESSURE && !IS_CHINA_AREA && !this.mImmersiveMode) {
            return false;
        }
        boolean ret = false;
        HIT_REGION_TO_MAX = (int) (((double) this.mContext.getResources().getDimensionPixelSize(17105186)) / 3.5d);
        if (this.mMinNavigationBar) {
            updateRealSize();
            int i = this.mPolicy.mNavigationBarPosition;
            PhoneWindowManager phoneWindowManager = this.mPolicy;
            if (i == 4) {
                if (pointY > ((float) (this.realSize.y - HIT_REGION_TO_MAX))) {
                    z = true;
                }
                ret = z;
            } else if (isNaviBarNearByHole()) {
                if (pointX > ((float) ((this.realSize.x - this.mHoleHeight) - HIT_REGION_TO_MAX))) {
                    z = true;
                }
                ret = z;
            } else {
                if (pointX > ((float) (this.realSize.x - HIT_REGION_TO_MAX))) {
                    z = true;
                }
                ret = z;
            }
        }
        return ret;
    }

    private void updateRealSize() {
        if (this.mPolicy.mDisplay != null) {
            this.mPolicy.mDisplay.getRealSize(this.realSize);
        }
    }

    public void updateNavigationBar(boolean minNaviBar) {
        this.mMinNavigationBar = minNaviBar;
        Settings.Global.putInt(this.mContext.getContentResolver(), "navigationbar_is_min", minNaviBar);
        this.mPolicy.mWindowManagerFuncs.reevaluateStatusBarSize(true);
    }

    private void sendBroadcast(boolean minNaviBar) {
        HwSlog.d(TAG, "sendBroadcast minNaviBar = " + minNaviBar);
        Intent intent = new Intent("com.huawei.navigationbar.statuschange");
        intent.putExtra("minNavigationBar", minNaviBar);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        StatisticalUtils.reportc(this.mContext, 61);
    }

    public boolean onDown(MotionEvent event) {
        this.mIsValidGesture = touchDownIsValid(event.getRawX(), event.getRawY());
        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (isFlingOnFrontNaviMode() && !isInLockTaskMode()) {
            HwSlog.d(TAG, "onFling::FRONT_FINGERPRINT_NAVIGATION, return! ");
            return false;
        } else if (isGestureNavigationEnable()) {
            HwSlog.d(TAG, "onFling::Gest_Navigation_Enable, return! ");
            return false;
        } else if (HwDeviceManager.disallowOp(103)) {
            return false;
        } else {
            boolean z = true;
            if (this.IS_SUPPORT_PRESSURE && IS_CHINA_AREA) {
                boolean ret = false;
                if (this.mMinNavigationBar) {
                    float pointX = e2.getX();
                    float pointY = e2.getY();
                    int i = this.mPolicy.mNavigationBarPosition;
                    PhoneWindowManager phoneWindowManager = this.mPolicy;
                    if (i == 4) {
                        ret = pointY < ((float) (this.realSize.y - HIT_REGION_TO_TOP_BOTTOM));
                    } else if (isNaviBarNearByHole()) {
                        ret = pointX < ((float) ((this.realSize.x - this.mHoleHeight) - HIT_REGION_TO_TOP_BOTTOM));
                    } else {
                        ret = pointX < ((float) (this.realSize.x - HIT_REGION_TO_TOP_BOTTOM));
                    }
                }
                if (!ret) {
                    HwSlog.d(TAG, "onFling::move distance is not enough, return! ");
                    return false;
                }
            }
            if (!this.mIsValidGesture) {
                HwSlog.d(TAG, "onFling::not valid gesture , " + this.mIsValidGesture + ", return!");
                return false;
            }
            int i2 = this.mPolicy.mNavigationBarPosition;
            PhoneWindowManager phoneWindowManager2 = this.mPolicy;
            if (i2 == 4) {
                if (e1.getRawY() >= e2.getRawY() && getGameControlReslut(2)) {
                    return false;
                }
                if (e1.getRawY() >= e2.getRawY()) {
                    z = false;
                }
                sendBroadcast(z);
            } else if (e1.getRawX() >= e2.getRawX() && getGameControlReslut(3)) {
                return false;
            } else {
                if (e1.getRawX() >= e2.getRawX()) {
                    z = false;
                }
                sendBroadcast(z);
            }
            reset();
            return false;
        }
    }

    public void setEnableSwipeInCurrentGameApp(boolean enable) {
        this.mEnableSwipeInCurrentGameApp = enable;
    }

    public boolean getEnableSwipeInCurrentGameApp() {
        return this.mEnableSwipeInCurrentGameApp;
    }

    public void setHwAms(HwActivityManagerService ams) {
        this.mAms = ams;
    }

    public boolean getGameControlReslut(int swipeOritation) {
        boolean z = false;
        if (!mSupportGameAssist || !ANTI_TOUCH_ENABLED || this.mAms == null) {
            return false;
        }
        StringBuffer sb = new StringBuffer("getGameControlReslut : ");
        sb.append("mEnableSwipeInCurrentGameApp=");
        sb.append(getEnableSwipeInCurrentGameApp());
        sb.append(", mAms.isGameKeyControlOn=");
        sb.append(this.mAms.isGameKeyControlOn());
        if (getEnableSwipeInCurrentGameApp() || !this.mAms.isGameKeyControlOn()) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        boolean result = true;
        if (currentTime - this.mLastGetGameControlTime < 500 && this.mLastSwipeOritation == swipeOritation && this.mLastGetGameControlTime - this.mSecondToLastGetGameControlTime < 500 && this.mSecondToLastSwipeOritation == this.mLastSwipeOritation) {
            Log.i(TAG, "onFling swipe show navbar unlocked.");
            setEnableSwipeInCurrentGameApp(true);
            result = false;
        }
        if (!result) {
            z = true;
        }
        showGameControlNavToast(z, currentTime - this.mLastGetGameControlTime);
        this.mSecondToLastSwipeOritation = this.mLastSwipeOritation;
        this.mSecondToLastGetGameControlTime = this.mLastGetGameControlTime;
        this.mLastSwipeOritation = swipeOritation;
        this.mLastGetGameControlTime = currentTime;
        return result;
    }

    private boolean isFlingOnFrontNaviMode() {
        return "normal".equals(SystemProperties.get("ro.runmode", "normal")) && FRONT_FINGERPRINT_NAVIGATION && !isNaviBarEnabled();
    }

    private boolean isNaviBarEnabled() {
        if (this.mContext == null) {
            return true;
        }
        boolean isNaviBarEnable = FrontFingerPrintSettings.isNaviBarEnabled(this.mContext.getContentResolver());
        Log.d(TAG, "isNaviBarEnable is: " + isNaviBarEnable);
        return isNaviBarEnable;
    }

    private boolean isGestureNavigationEnable() {
        if (this.mContext != null) {
            return FrontFingerPrintSettings.isGestureNavigationMode(this.mContext.getContentResolver());
        }
        return false;
    }

    private boolean isInLockTaskMode() {
        ActivityManager mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (!FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION || FrontFingerPrintSettings.FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 0 || !mActivityManager.isInLockTaskMode()) {
            return false;
        }
        return true;
    }

    public void onLongPress(MotionEvent arg0) {
    }

    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        return false;
    }

    public void onShowPress(MotionEvent arg0) {
    }

    public boolean onSingleTapUp(MotionEvent arg0) {
        reset();
        return false;
    }

    private void parseHole() {
        String[] props = SystemProperties.get("ro.config.hw_notch_size", "").split(",");
        if (props != null && props.length == 4) {
            this.mHoleHeight = Integer.parseInt(props[1]);
            Log.d(TAG, "mHoleHeight = " + this.mHoleHeight);
        }
    }

    private boolean isNaviBarNearByHole() {
        if (this.mHoleHeight <= 0 || this.mPolicy.mScreenRotation != 3) {
            return false;
        }
        return true;
    }

    private void showGameControlNavToast(boolean isEnableSwipe, long timeElapse) {
        if (isEnableSwipe || !isNaviBarEnabled() || isGestureNavigationEnable()) {
            if (this.mToast != null) {
                this.mToast.cancel();
            }
            return;
        }
        if (timeElapse >= 500) {
            if (this.mToast != null) {
                this.mToast.cancel();
            }
            this.mToast = Toast.makeText(this.mContext, 33686230, 1);
            this.mToast.show();
        }
    }
}

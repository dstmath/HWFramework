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
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;
import com.android.server.am.HwActivityManagerService;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.WindowManagerService;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.android.os.HwGeneralManager;
import huawei.android.provider.FrontFingerPrintSettings;

public class NavigationBarPolicy implements GestureDetector.OnGestureListener {
    private static final boolean ANTI_TOUCH_ENABLED;
    static final boolean DEBUG = false;
    private static final int DOUBLE_SWIP_TIMEOUT = 750;
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
    private NavigationCallOut mNavCallOut;
    private PhoneWindowManager mPolicy = null;
    private long mSecondToLastGetGameControlTime;
    private int mSecondToLastSwipeOritation;
    private WindowManagerService mService;
    private Toast mToast;
    private Point realSize;

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.config.gameassist.anti-touch", 0) == 1) {
            z = true;
        }
        ANTI_TOUCH_ENABLED = z;
    }

    public NavigationBarPolicy(Context context, PhoneWindowManager policy, NavigationCallOut navCallOut, WindowManagerService service) {
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
        this.mService = service;
        this.mDetector = new GestureDetector(context, this, this.mService.getWindowManagerServiceEx().getGestureDetectorHandler());
        this.mNavCallOut = navCallOut;
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
        NavigationCallOut navigationCallOut;
        boolean ret = false;
        if (getDisplay() == null || this.mForceMinNavigationBar || this.mContext.getDisplayId() != 0 || (this.mPolicy.mKeyguardDelegate.isShowing() && !this.mPolicy.mKeyguardDelegate.isOccluded())) {
            return false;
        }
        if (this.IS_SUPPORT_PRESSURE && !IS_CHINA_AREA && !this.mImmersiveMode) {
            return false;
        }
        HIT_REGION_TO_MAX = (int) (((double) this.mContext.getResources().getDimensionPixelSize(17105307)) / 3.5d);
        if (!this.mMinNavigationBar) {
            return false;
        }
        updateRealSize();
        int navigationBarPosition = getNavigationBarPosition();
        PhoneWindowManager phoneWindowManager = this.mPolicy;
        if (navigationBarPosition == 4) {
            HwSlog.d(TAG, "pointY:" + pointY + "realSize.y:" + this.realSize.y + "HIT_REGION_TO_MAX:" + HIT_REGION_TO_MAX);
            if (pointY > ((float) (this.realSize.y - HIT_REGION_TO_MAX))) {
                ret = true;
            }
            if (!ret || (navigationCallOut = this.mNavCallOut) == null) {
                return ret;
            }
            return !navigationCallOut.isControlCenterArea(pointX, pointY);
        }
        HwSlog.d(TAG, "pointX:" + pointX + "realSize.x:" + this.realSize.x + "HIT_REGION_TO_MAX:" + HIT_REGION_TO_MAX + "mHoleHeight:" + this.mHoleHeight);
        if (isNaviBarNearByHole()) {
            if (pointX > ((float) ((this.realSize.x - this.mHoleHeight) - HIT_REGION_TO_MAX))) {
                ret = true;
            }
            return ret;
        }
        if (pointX > ((float) (this.realSize.x - HIT_REGION_TO_MAX))) {
            ret = true;
        }
        return ret;
    }

    private void updateRealSize() {
        if (getDisplay() != null) {
            getDisplay().getRealSize(this.realSize);
        }
    }

    public void updateNavigationBar(boolean minNaviBar) {
        this.mMinNavigationBar = minNaviBar;
        Settings.Global.putInt(this.mContext.getContentResolver(), "navigationbar_is_min", minNaviBar ? 1 : 0);
        this.mPolicy.mWindowManagerFuncs.reevaluateStatusBarSize(true);
    }

    private void sendBroadcast(boolean minNaviBar) {
        HwSlog.d(TAG, "sendBroadcast minNaviBar = " + minNaviBar);
        Intent intent = new Intent("com.huawei.navigationbar.statuschange");
        intent.putExtra("minNavigationBar", minNaviBar);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        StatisticalUtils.reportc(this.mContext, 61);
    }

    private Display getDisplay() {
        return this.mContext.getDisplay();
    }

    private int getNavigationBarPosition() {
        if (this.mPolicy.mDefaultDisplayPolicy != null) {
            return this.mPolicy.mDefaultDisplayPolicy.getNavBarPosition();
        }
        PhoneWindowManager phoneWindowManager = this.mPolicy;
        return 4;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onDown(MotionEvent event) {
        this.mIsValidGesture = touchDownIsValid(event.getRawX(), event.getRawY());
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
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
            if (this.IS_SUPPORT_PRESSURE && IS_CHINA_AREA) {
                boolean ret = false;
                if (this.mMinNavigationBar) {
                    float pointX = e2.getX();
                    float pointY = e2.getY();
                    int navigationBarPosition = getNavigationBarPosition();
                    PhoneWindowManager phoneWindowManager = this.mPolicy;
                    boolean z = true;
                    if (navigationBarPosition == 4) {
                        if (pointY >= ((float) (this.realSize.y - HIT_REGION_TO_TOP_BOTTOM))) {
                            z = false;
                        }
                        ret = z;
                    } else if (isNaviBarNearByHole()) {
                        if (pointX >= ((float) ((this.realSize.x - this.mHoleHeight) - HIT_REGION_TO_TOP_BOTTOM))) {
                            z = false;
                        }
                        ret = z;
                    } else {
                        if (pointX >= ((float) (this.realSize.x - HIT_REGION_TO_TOP_BOTTOM))) {
                            z = false;
                        }
                        ret = z;
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
            int navigationBarPosition2 = getNavigationBarPosition();
            PhoneWindowManager phoneWindowManager2 = this.mPolicy;
            if (navigationBarPosition2 == 4) {
                if (e1.getRawY() >= e2.getRawY() && getGameControlReslut(2)) {
                    return false;
                }
                HwSlog.d(TAG, "sendBroadcast getGameControlReslut" + getGameControlReslut(2) + "1#getRawY" + e1.getRawY() + "2#getRawY" + e2.getRawY());
                if (e1.getRawY() > e2.getRawY()) {
                    sendBroadcast(false);
                }
            } else if (e1.getRawX() >= e2.getRawX() && getGameControlReslut(3)) {
                return false;
            } else {
                HwSlog.d(TAG, "sendBroadcast getGameControlReslut" + getGameControlReslut(3) + "1#getRawX" + e1.getRawX() + "2#getRawX" + e2.getRawX());
                if (e1.getRawX() > e2.getRawX()) {
                    sendBroadcast(false);
                }
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
        int i;
        boolean z = false;
        if (!mSupportGameAssist || !ANTI_TOUCH_ENABLED) {
            return false;
        }
        boolean isGameKeyControlOn = ActivityManagerEx.isGameKeyControlOn();
        StringBuffer sb = new StringBuffer("getGameControlReslut : ");
        sb.append("mEnableSwipeInCurrentGameApp=");
        sb.append(this.mEnableSwipeInCurrentGameApp);
        sb.append(", ActivityManagerEx.isGameKeyControlOn=");
        sb.append(isGameKeyControlOn);
        if (this.mEnableSwipeInCurrentGameApp || !isGameKeyControlOn) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        boolean result = true;
        long j = this.mLastGetGameControlTime;
        if (currentTime - j < 750 && (i = this.mLastSwipeOritation) == swipeOritation && j - this.mSecondToLastGetGameControlTime < 750 && this.mSecondToLastSwipeOritation == i) {
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

    public boolean getMinNavigationBar() {
        return this.mMinNavigationBar;
    }

    private boolean isFlingOnFrontNaviMode() {
        return "normal".equals(SystemProperties.get("ro.runmode", "normal")) && FRONT_FINGERPRINT_NAVIGATION && !isNaviBarEnabled();
    }

    private boolean isNaviBarEnabled() {
        Context context = this.mContext;
        if (context == null) {
            return true;
        }
        boolean isNaviBarEnable = FrontFingerPrintSettings.isNaviBarEnabled(context.getContentResolver());
        Log.d(TAG, "isNaviBarEnable is: " + isNaviBarEnable);
        return isNaviBarEnable;
    }

    private boolean isGestureNavigationEnable() {
        Context context = this.mContext;
        if (context != null) {
            return FrontFingerPrintSettings.isGestureNavigationMode(context.getContentResolver());
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

    @Override // android.view.GestureDetector.OnGestureListener
    public void onLongPress(MotionEvent arg0) {
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onShowPress(MotionEvent arg0) {
    }

    @Override // android.view.GestureDetector.OnGestureListener
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
        if (this.mHoleHeight <= 0 || this.mPolicy.getDefaultDisplayPolicy().getDisplayRotation() != 3) {
            return false;
        }
        return true;
    }

    private void showGameControlNavToast(boolean isEnableSwipe, long timeElapse) {
        if (isEnableSwipe || !isNaviBarEnabled() || isGestureNavigationEnable()) {
            Toast toast = this.mToast;
            if (toast != null) {
                toast.cancel();
            }
        } else if (timeElapse >= 750) {
            Toast toast2 = this.mToast;
            if (toast2 != null) {
                toast2.cancel();
            }
            this.mToast = Toast.makeText(this.mContext, 33686226, 1);
            this.mToast.show();
        }
    }
}

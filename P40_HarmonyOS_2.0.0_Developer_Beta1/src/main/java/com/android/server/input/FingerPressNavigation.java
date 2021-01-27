package com.android.server.input;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.telephony.MSimTelephonyManager;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicyEx;
import com.android.server.wm.WindowStateEx;
import com.huawei.android.content.ContextEx;
import com.huawei.android.hardware.input.InputManagerEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.statistical.StatisticalUtils;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.android.view.WindowManagerEx;
import com.huawei.utils.HwPartResourceUtils;
import java.util.ArrayList;
import java.util.List;

public class FingerPressNavigation {
    private static final int APPKEYVIBEPATTERN_0 = 0;
    private static final int APPKEYVIBEPATTERN_1 = 10;
    private static final int APPKEYVIBEPATTERN_2 = 20;
    private static final int APPKEYVIBEPATTERN_3 = 30;
    private static final int APPPRESSUREABOVELIMIT = 30;
    private static final int APP_LEFT_STATUS = 0;
    private static final int APP_RIGHT_STATUS = 1;
    private static final int BACK_ON_LEFT = 0;
    private static final int BACK_ON_RIGHT = 1;
    private static final int DEFAULT_STATUS_BAR_HEIGHT = 30;
    private static final int DISTANCE_TO_TOP_OR_BOTTOM = 150;
    static final boolean IS_DEBUG = false;
    static final boolean IS_DEBUG_TOUCH = false;
    private static final float LANDSCAPE_HEIGHT_BOTTOM = 0.95f;
    private static final float LANDSCAPE_HEIGHT_BOTTOM_FORAPP = 0.9f;
    private static final float LANDSCAPE_HEIGHT_LEFT_BOTTOM = 0.625f;
    private static final float LANDSCAPE_HEIGHT_LEFT_TOP = 0.375f;
    private static final float LANDSCAPE_HEIGHT_TOP_FORAPP = 0.1f;
    private static final int NAVI_BAR_TYPE0 = 0;
    private static final int NAVI_BAR_TYPE1 = 1;
    private static final int NAVI_BAR_TYPE2 = 2;
    private static final int NAVI_BAR_TYPE3 = 3;
    private static final String NONE_APP = "none_app";
    private static final float PORTRAIT_HEIGHT_BOTTOM = 0.975f;
    private static final float PORTRAIT_HEIGHT_BOTTOM1 = 0.982f;
    private static final float PORTRAIT_HEIGHT_BOTTOM2 = 0.975f;
    private static final float PORTRAIT_WIDTH_BOTTOM_LEFT = 0.375f;
    private static final float PORTRAIT_WIDTH_BOTTOM_RIGHT = 0.625f;
    private static final float PORTRAIT_WIDTH_LEFT = 0.1f;
    private static final float PORTRAIT_WIDTH_RIGHT = 0.9f;
    private static final int POSITION_ON_LEFT = 0;
    private static final int POSITION_ON_RIGHT = 1;
    private static final int PRESSURELIMIT = -1;
    private static final float PRESSURELIMIT_OFFSET = 0.9f;
    private static final int REPEAT_PATTERN = -1;
    private static final int START_INFO_LENGTH = 2;
    private static final float STATUS_BAR_HEIGHT_OFFSET = 1.7f;
    private static final String TAG = "pressure:FingerPressNavi";
    private static List<String> sAllResumePackNames = new ArrayList();
    private static FingerPressNavigation sFingerPressNavi = null;
    private long[] mAppKeyVibePatterns = {0, 10, 20, 30};
    private Object mAppLock = new Object();
    private int mAppPressureAboveStep = 0;
    private Object mBackLock = new Object();
    private HwCircleWindow mCircle = null;
    private Context mContext = null;
    private long mDownTime = 0;
    private WindowStateEx mFocusedWindow;
    private String mFocusedWindowPackageName;
    private int mHeighLeftBottom = 0;
    private int mHeighLeftTop = 0;
    private int mHeight = 0;
    private int mHeightBottom = 0;
    private int mHeightBottom1 = 0;
    private int mHeightBottom2 = 0;
    private int mHeightBottomForApp = 0;
    private int mHeightTopForApp = 0;
    private boolean mIsAllComsume;
    private boolean mIsAppAdd = false;
    private boolean mIsAppDownTrace = false;
    private boolean mIsAppDownTraceForFilter = false;
    private boolean mIsAppNaviTrace = false;
    private boolean mIsBackAdd = false;
    private boolean mIsFullScreen = false;
    private boolean mIsGameScene = false;
    private boolean mIsImmersiveMode = false;
    private boolean mIsKeyguardOn = false;
    private boolean mIsNaviDownTrace = false;
    private boolean mIsNaviTrace = false;
    private boolean mIsPortrait = true;
    private boolean mIsUpAdd = false;
    private KeyguardManager mKeyguardManager;
    private String mLeftPkgName = NONE_APP;
    private int mMode = 0;
    private int mNaviBarPos = 0;
    private HwPhoneWindowManager mPolicy;
    private String mRightPkgName = NONE_APP;
    private int mStatusBarHeight = 30;
    private Object mUpLock = new Object();
    private Vibrator mVibrator = null;
    private int mWidth = 0;
    private int mWidthBottomLeft = 0;
    private int mWidthBottomRight = 0;
    private int mWidthLeft = 0;
    private int mWidthRight = 0;

    static {
        sAllResumePackNames.add("com.dianping.v1");
        sAllResumePackNames.add("com.sina.weibo");
        sAllResumePackNames.add("com.tencent.mm");
    }

    protected FingerPressNavigation(Context context) {
        this.mContext = context;
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mCircle = HwCircleWindow.getInstance(this.mContext);
        this.mStatusBarHeight = this.mContext.getResources().getDimensionPixelSize(HwPartResourceUtils.getResourceId("status_bar_height"));
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        Vibrator vibrator = this.mVibrator;
        if (vibrator != null && !vibrator.hasVibrator()) {
            this.mVibrator = null;
        }
        this.mPolicy = WindowManagerPolicyEx.getInstance().getHwPhoneWindowManager();
    }

    public static synchronized FingerPressNavigation getInstance(Context context) {
        FingerPressNavigation fingerPressNavigation;
        synchronized (FingerPressNavigation.class) {
            if (sFingerPressNavi == null) {
                sFingerPressNavi = new FingerPressNavigation(context);
            }
            fingerPressNavigation = sFingerPressNavi;
        }
        return fingerPressNavigation;
    }

    public void setNeedTip(boolean isNeedTip) {
        HwCircleWindow hwCircleWindow = this.mCircle;
        if (hwCircleWindow != null) {
            hwCircleWindow.setNeedTip(isNeedTip);
        }
    }

    public void setMode(int mode) {
        this.mMode = mode;
        HwCircleWindow hwCircleWindow = this.mCircle;
        if (hwCircleWindow != null) {
            hwCircleWindow.setMode(mode);
        }
    }

    public void setGameScene(boolean isGame) {
        this.mIsGameScene = isGame;
    }

    private void performBackDown(int keycode) {
        synchronized (this.mBackLock) {
            if (!this.mIsBackAdd) {
                sendEvent(0, 0, keycode);
                this.mIsBackAdd = true;
            }
        }
    }

    private void stopPerformBackDown() {
        synchronized (this.mBackLock) {
            if (this.mIsBackAdd) {
                this.mIsBackAdd = false;
            }
        }
    }

    private void performBackUp(int keyCode) {
        synchronized (this.mUpLock) {
            if (!this.mIsUpAdd) {
                sendEvent(1, 0, keyCode);
                this.mIsUpAdd = true;
                StatisticalUtils.reportc(this.mContext, 81);
            }
        }
    }

    private void stopPerformBackUp() {
        synchronized (this.mUpLock) {
            if (this.mIsUpAdd) {
                this.mIsUpAdd = false;
            }
        }
    }

    public void setImmersiveMode(boolean isImmersiveMode) {
        this.mIsImmersiveMode = isImmersiveMode;
        HwCircleWindow hwCircleWindow = this.mCircle;
        if (hwCircleWindow != null) {
            hwCircleWindow.setImmersiveMode(isImmersiveMode);
        }
    }

    public void setDisplayWidthAndHeight(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        if (height > width) {
            int i = this.mWidth;
            this.mWidthLeft = (int) (((float) i) * 0.1f);
            this.mWidthRight = (int) (((float) i) * 0.9f);
            this.mWidthBottomLeft = (int) (((float) i) * 0.375f);
            this.mWidthBottomRight = (int) (((float) i) * 0.625f);
            int i2 = this.mHeight;
            this.mHeightBottom = (int) (((float) i2) * 0.975f);
            this.mHeightBottom1 = (int) (((float) i2) * PORTRAIT_HEIGHT_BOTTOM1);
            this.mHeightBottom2 = (int) (((float) i2) * 0.975f);
            this.mIsPortrait = true;
        } else {
            int i3 = this.mHeight;
            this.mHeighLeftTop = (int) (((float) i3) * 0.375f);
            this.mHeighLeftBottom = (int) (((float) i3) * 0.625f);
            this.mHeightBottom = (int) (((float) this.mWidth) * LANDSCAPE_HEIGHT_BOTTOM);
            this.mHeightTopForApp = (int) (((float) i3) * 0.1f);
            this.mHeightBottomForApp = (int) (((float) i3) * 0.9f);
            this.mIsPortrait = false;
        }
        HwCircleWindow hwCircleWindow = this.mCircle;
        if (hwCircleWindow != null) {
            hwCircleWindow.setDisplay(width, height);
        }
    }

    private void reset() {
        this.mIsNaviTrace = false;
        this.mIsNaviDownTrace = false;
        this.mIsAppDownTrace = false;
        this.mIsAppNaviTrace = false;
        stopPerformBackDown();
        stopPerformBackUp();
        stopPerformAppStart();
    }

    public void createPointerCircleAnimation() {
        Log.v(TAG, "createPointerCircleAnimation");
        this.mCircle = HwCircleWindow.getInstance(this.mContext);
        this.mCircle.createCircleWindow();
    }

    public void destoryPointerCircleAnimation() {
        Log.v(TAG, "destoryPointerCircleAnimation");
        HwCircleWindow hwCircleWindow = this.mCircle;
        if (hwCircleWindow != null) {
            hwCircleWindow.destoryCircleWindowAndBitmap();
        }
    }

    private boolean isKeyguardLocked() {
        KeyguardManager keyguardManager = this.mKeyguardManager;
        if (keyguardManager != null) {
            return keyguardManager.isKeyguardLocked();
        }
        Log.e(TAG, "isKeyguardLocked mKeyguardManager is null");
        return false;
    }

    private boolean isKeyguardFocus() {
        return this.mIsKeyguardOn;
    }

    public void setCurFocusWindow(WindowStateEx focus) {
        this.mFocusedWindow = focus;
        WindowStateEx windowStateEx = this.mFocusedWindow;
        String str = null;
        WindowManager.LayoutParams focusAttrs = windowStateEx != null ? windowStateEx.getAttrs() : null;
        boolean z = false;
        if ((WindowManagerEx.LayoutParamsEx.getPrivateFlagKeyguard() & (focusAttrs != null ? WindowManagerEx.LayoutParamsEx.getPrivateFlags(focusAttrs) : 0)) != 0) {
            z = true;
        }
        this.mIsKeyguardOn = z;
        HwCircleWindow hwCircleWindow = this.mCircle;
        if (hwCircleWindow != null) {
            hwCircleWindow.setCanNaviDraw(!this.mIsKeyguardOn);
        }
        if (focusAttrs != null) {
            str = focusAttrs.packageName;
        }
        this.mFocusedWindowPackageName = str;
        updateAllComsume(this.mFocusedWindowPackageName);
    }

    private void updateAllComsume(String packageName) {
        if (!this.mIsPortrait) {
            this.mIsAllComsume = false;
        } else if (packageName == null) {
            this.mIsAllComsume = false;
            this.mHeightBottom = this.mHeightBottom2;
        } else if (sAllResumePackNames.contains(packageName)) {
            this.mIsAllComsume = true;
            this.mHeightBottom = this.mHeightBottom1;
        } else {
            this.mIsAllComsume = false;
            this.mHeightBottom = this.mHeightBottom2;
        }
    }

    public void setIsTopFullScreen(boolean isTopFullScreen) {
        this.mIsFullScreen = isTopFullScreen;
    }

    private boolean interceptByPressureNavi(MotionEvent event, float pressure, int keyEvent) {
        boolean isNeedComsumeTmp = false;
        int action = event.getAction();
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    float pressureLimit = this.mCircle.getPressureLimit(keyEvent);
                    this.mDownTime = SystemClock.uptimeMillis();
                    if (!this.mIsNaviDownTrace || pressure <= pressureLimit) {
                        return false;
                    }
                    this.mIsNaviTrace = true;
                    this.mIsAppNaviTrace = false;
                    performBackDown(keyEvent);
                    performBackUp(keyEvent);
                    return true;
                } else if (action != 3) {
                    return false;
                }
            }
            if (this.mIsNaviDownTrace && this.mIsNaviTrace) {
                isNeedComsumeTmp = true;
                this.mIsNaviTrace = false;
                this.mIsNaviDownTrace = false;
            }
            reset();
            return isNeedComsumeTmp;
        }
        this.mIsNaviDownTrace = true;
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0019, code lost:
        if (r1 != 3) goto L_0x004c;
     */
    private boolean interceptByAppNavi(MotionEvent event, float pressure, int status) {
        int action = event.getAction();
        int eventX = (int) event.getX();
        int eventY = (int) event.getY();
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    float pressureLimit = this.mCircle.getPressureLimit(-1);
                    this.mDownTime = SystemClock.uptimeMillis();
                    if (this.mIsAppDownTrace && pressure > pressureLimit) {
                        this.mIsNaviTrace = false;
                        this.mIsAppNaviTrace = true;
                        performAppStart(status);
                    }
                }
            }
            if (this.mIsAppDownTrace && this.mIsAppNaviTrace) {
                this.mIsAppNaviTrace = false;
                this.mIsAppDownTrace = false;
            }
            reset();
        } else {
            this.mIsAppDownTrace = true;
        }
        return getNeedComsumetmpByEvent(eventX, eventY, action);
    }

    private boolean getNeedComsumetmpByEvent(int eventX, int eventY, int action) {
        if (this.mIsPortrait) {
            if (this.mIsFullScreen) {
                return false;
            }
            if ((eventX <= this.mWidthRight && eventX >= this.mWidthLeft) || eventY >= this.mStatusBarHeight) {
                return false;
            }
            if (action == 0 || action == 2) {
                return true;
            }
            return false;
        } else if (this.mIsFullScreen) {
            return false;
        } else {
            int i = this.mStatusBarHeight;
            if (((float) eventX) >= ((float) i) * STATUS_BAR_HEIGHT_OFFSET || eventY >= i) {
                return false;
            }
            if (action == 0 || action == 2) {
                return true;
            }
            return false;
        }
    }

    private void performAppStart(int status) {
        synchronized (this.mAppLock) {
            if (!this.mIsAppAdd) {
                handleAppNavi(status);
                this.mIsAppAdd = true;
            }
        }
    }

    private void stopPerformAppStart() {
        synchronized (this.mAppLock) {
            if (this.mIsAppAdd) {
                this.mIsAppAdd = false;
            }
        }
    }

    public void setAppRightPkg(String appRight) {
        this.mRightPkgName = appRight;
    }

    public void setAppLeftPkg(String apLeft) {
        this.mLeftPkgName = apLeft;
    }

    /* access modifiers changed from: package-private */
    public void handleAppNavi(int status) {
        if (status == 0) {
            if (!NONE_APP.equals(this.mLeftPkgName)) {
                startActivity(this.mLeftPkgName);
            }
            Vibrator vibrator = this.mVibrator;
            if (vibrator != null) {
                vibrator.vibrate(this.mAppKeyVibePatterns, -1);
            }
        } else if (status == 1) {
            if (!NONE_APP.equals(this.mRightPkgName)) {
                startActivity(this.mRightPkgName);
            }
            Vibrator vibrator2 = this.mVibrator;
            if (vibrator2 != null) {
                vibrator2.vibrate(this.mAppKeyVibePatterns, -1);
            }
        }
    }

    private void startActivity(String startInfo) {
        if (startInfo != null) {
            String[] startInfos = startInfo.split(";");
            if (startInfos.length == 2) {
                try {
                    Log.v(TAG, "pressure:FingerPressNavi startFingerPressActivity and this app is : " + startInfos[0] + " ; " + startInfos[1]);
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.addCategory("android.intent.category.LAUNCHER");
                    intent.setClassName(startInfos[0], startInfos[1]);
                    intent.addFlags(805306368);
                    ContextEx.startActivityAsUser(this.mContext, intent, (Bundle) null, UserHandleEx.CURRENT);
                    Context context = this.mContext;
                    StatisticalUtils.reporte(context, 82, "{pkg:" + startInfos[0] + "}");
                } catch (IllegalStateException e) {
                    Log.e(TAG, "get IllegalStateException");
                } catch (SecurityException e2) {
                    Log.e(TAG, "get SecurityException");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setPressureLimit(float limit) {
        HwCircleWindow hwCircleWindow = this.mCircle;
        if (hwCircleWindow != null) {
            hwCircleWindow.setPressureLimit(limit);
        }
    }

    private void handleByAnimation(MotionEvent event, int type) {
        HwCircleWindow hwCircleWindow = this.mCircle;
        if (hwCircleWindow != null) {
            hwCircleWindow.onTouchEvent(event, type);
        }
    }

    private static boolean isMultiSimEnabled() {
        try {
            return MSimTelephonyManager.getDefault().isMultiSimEnabled();
        } catch (NoExtAPIException e) {
            Log.w(TAG, "FingerPressNavigation->isMultiSimEnabled->NoExtAPIException!");
            return false;
        }
    }

    private boolean isPhoneInCall() {
        if (isMultiSimEnabled()) {
            int phoneCount = MSimTelephonyManager.getDefault().getPhoneCount();
            for (int i = 0; i < phoneCount; i++) {
                if (MSimTelephonyManager.getDefault().getCallState(i) != 0) {
                    return true;
                }
            }
            return false;
        } else if (TelephonyManagerEx.getDefault().getCallState() != 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isHandledByLeftEdge(float eventX, float eventY) {
        if (this.mIsPortrait) {
            if (eventX >= ((float) this.mWidthLeft) || eventY >= ((float) this.mStatusBarHeight) * STATUS_BAR_HEIGHT_OFFSET) {
                return false;
            }
            return true;
        } else if (eventY <= ((float) this.mHeightBottomForApp) || eventX >= ((float) this.mStatusBarHeight) * STATUS_BAR_HEIGHT_OFFSET) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isHandledByRightEdge(float eventX, float eventY) {
        if (this.mIsPortrait) {
            if (eventX <= ((float) this.mWidthRight) || eventY >= ((float) this.mStatusBarHeight) * STATUS_BAR_HEIGHT_OFFSET) {
                return false;
            }
            return true;
        } else if (eventY >= ((float) this.mHeightTopForApp) || eventX >= ((float) this.mStatusBarHeight) * STATUS_BAR_HEIGHT_OFFSET) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isHandledByBottomCenter(float eventX, float eventY) {
        if (this.mIsPortrait) {
            if (eventY <= ((float) this.mHeightBottom) || eventX >= ((float) this.mWidthBottomRight) || eventX <= ((float) this.mWidthBottomLeft)) {
                return false;
            }
            return true;
        } else if (eventX <= ((float) this.mHeightBottom) || eventY <= ((float) this.mHeighLeftTop) || eventY >= ((float) this.mHeighLeftBottom)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isHandledByBottomLeft(float eventX, float eventY) {
        if (this.mIsPortrait) {
            if (eventY <= ((float) this.mHeightBottom) || eventX >= ((float) this.mWidthBottomLeft)) {
                return false;
            }
            return true;
        } else if (eventX <= ((float) this.mHeightBottom) || eventY <= ((float) this.mHeighLeftBottom)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isHandledByBottomRight(float eventX, float eventY) {
        if (this.mIsPortrait) {
            if (eventY <= ((float) this.mHeightBottom) || eventX <= ((float) this.mWidthBottomRight)) {
                return false;
            }
            return true;
        } else if (eventX <= ((float) this.mHeightBottom) || eventY >= ((float) this.mHeighLeftTop)) {
            return false;
        } else {
            return true;
        }
    }

    public void setNaviBarPosition(int type) {
        if (type == 0 || type == 2) {
            this.mNaviBarPos = 0;
        } else if (type == 1 || type == 3) {
            this.mNaviBarPos = 1;
        } else {
            Log.d(TAG, "setNaviBarPosition not handle ele");
        }
    }

    private int getKeyCode(int pos) {
        if (pos != 0) {
            if (pos != 1) {
                if (this.mNaviBarPos == 0) {
                    return 4;
                }
                return 187;
            } else if (this.mNaviBarPos == 0) {
                return 187;
            } else {
                return 4;
            }
        } else if (this.mNaviBarPos == 0) {
            return 4;
        } else {
            return 187;
        }
    }

    private int getAnimationType(int pos) {
        if (pos != 0) {
            if (pos != 1) {
                if (this.mNaviBarPos == 0) {
                    return 2;
                }
                return 3;
            } else if (this.mNaviBarPos == 0) {
                return 3;
            } else {
                return 2;
            }
        } else if (this.mNaviBarPos == 0) {
            return 2;
        } else {
            return 3;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0015, code lost:
        if (r1 != 3) goto L_0x0041;
     */
    private boolean filterAppStartEvent(MotionEvent event) {
        int i;
        int action = event.getAction();
        float pressure = event.getPressure();
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    float pressureLimit = this.mCircle.getPressureLimit(-1) * 0.9f;
                    if (this.mIsAppDownTraceForFilter && pressure >= pressureLimit) {
                        this.mAppPressureAboveStep++;
                        if (this.mAppPressureAboveStep > 30) {
                            this.mAppPressureAboveStep = 31;
                        }
                    }
                }
            }
            this.mIsAppDownTraceForFilter = false;
        } else {
            this.mIsAppDownTraceForFilter = true;
            this.mAppPressureAboveStep = 0;
        }
        if (!this.mIsAppDownTraceForFilter || (i = this.mAppPressureAboveStep) > 30 || i <= 0) {
            return false;
        }
        return true;
    }

    private boolean getComsumeByPkgStatus(MotionEvent motionEvent, float pressure, int pkgStatus) {
        if (isKeyguardLocked() || isPhoneInCall() || filterAppStartEvent(motionEvent)) {
            return true;
        }
        handleByAnimation(motionEvent, 4);
        return !interceptByAppNavi(motionEvent, pressure, pkgStatus);
    }

    private boolean getComsumeAtBottomCenter(MotionEvent motionEvent, float pressure) {
        boolean isComsume = true;
        if (!isKeyguardLocked()) {
            handleByAnimation(motionEvent, 1);
            isComsume = true ^ interceptByPressureNavi(motionEvent, pressure, 3);
        }
        if (this.mIsAllComsume) {
            return false;
        }
        return isComsume;
    }

    private boolean getComsumeAtBottomLeft(MotionEvent motionEvent, float pressure) {
        boolean isComsume = true;
        if (!isKeyguardLocked()) {
            handleByAnimation(motionEvent, getAnimationType(0));
            isComsume = !interceptByPressureNavi(motionEvent, pressure, getKeyCode(0));
        } else if (!isKeyguardFocus() && getKeyCode(0) == 4) {
            handleByAnimation(motionEvent, getAnimationType(0));
            isComsume = !interceptByPressureNavi(motionEvent, pressure, getKeyCode(0));
        }
        if (this.mIsAllComsume) {
            return false;
        }
        return isComsume;
    }

    private boolean getComsumeAtBottomRight(MotionEvent motionEvent, float pressure) {
        boolean isComsume = true;
        if (!isKeyguardLocked()) {
            handleByAnimation(motionEvent, getAnimationType(1));
            isComsume = !interceptByPressureNavi(motionEvent, pressure, getKeyCode(1));
        } else if (!isKeyguardFocus() && getKeyCode(1) == 4) {
            handleByAnimation(motionEvent, getAnimationType(1));
            isComsume = !interceptByPressureNavi(motionEvent, pressure, getKeyCode(1));
        }
        if (this.mIsAllComsume) {
            return false;
        }
        return isComsume;
    }

    public boolean filterPressueInputEvent(InputEvent event) {
        HwPhoneWindowManager hwPhoneWindowManager;
        String str;
        String str2;
        boolean isComsume = true;
        if (event instanceof MotionEvent) {
            MotionEvent motionEvent = (MotionEvent) event;
            float eventX = motionEvent.getX();
            float eventY = motionEvent.getY();
            float pressure = motionEvent.getPressure();
            if (!this.mIsGameScene && !this.mIsFullScreen && (str2 = this.mRightPkgName) != null && !NONE_APP.equals(str2) && isHandledByRightEdge(eventX, eventY)) {
                isComsume = getComsumeByPkgStatus(motionEvent, pressure, 1);
            } else if (!this.mIsGameScene && !this.mIsFullScreen && (str = this.mLeftPkgName) != null && !NONE_APP.equals(str) && isHandledByLeftEdge(eventX, eventY)) {
                isComsume = getComsumeByPkgStatus(motionEvent, pressure, 0);
            } else if (!this.mIsImmersiveMode && this.mMode == 1 && isHandledByBottomCenter(eventX, eventY)) {
                isComsume = getComsumeAtBottomCenter(motionEvent, pressure);
            } else if (!this.mIsImmersiveMode && this.mMode == 1 && isHandledByBottomLeft(eventX, eventY)) {
                isComsume = getComsumeAtBottomLeft(motionEvent, pressure);
            } else if (this.mIsImmersiveMode || this.mMode != 1 || !isHandledByBottomRight(eventX, eventY)) {
                HwCircleWindow hwCircleWindow = this.mCircle;
                if (hwCircleWindow != null) {
                    hwCircleWindow.resetAnimaion();
                    this.mCircle.destoryCircleWindowForAPP();
                }
                reset();
                isComsume = true;
            } else {
                isComsume = getComsumeAtBottomRight(motionEvent, pressure);
            }
            if (this.mMode == 1 && this.mIsAllComsume && (hwPhoneWindowManager = this.mPolicy) != null) {
                hwPhoneWindowManager.addPointerEvent(motionEvent);
            }
        }
        return isComsume;
    }

    /* access modifiers changed from: package-private */
    public void sendEvent(int action, int flags, int code) {
        sendEvent(action, flags, code, SystemClock.uptimeMillis());
    }

    /* access modifiers changed from: package-private */
    public void sendEvent(int action, int flags, int code, long when) {
        InputManagerEx.injectInputEvent(InputManagerEx.getInstance(), new KeyEvent(this.mDownTime, when, action, code, 0, 0, -1, 0, flags | 8 | 64, 257), InputManagerEx.getInjectInputEventModeAsync());
    }
}

package com.android.server.input;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.Vibrator;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.android.server.LocalServices;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.WindowState;
import com.huawei.android.statistical.StatisticalUtils;
import com.huawei.android.util.NoExtAPIException;
import java.util.ArrayList;

public class FingerPressNavigation {
    private static final int APP_LEFT_STATUS = 0;
    private static final int APP_RIGHT_STATUS = 1;
    private static final int BACK_ON_LEFT = 0;
    private static final int BACK_ON_RIGHT = 1;
    static final boolean DEBUG = false;
    static final boolean DEBUG_TOUCH = false;
    private static final int DISTANCE_TO_TOP_OR_BOTTOM = 150;
    private static final int POSITION_ON_LEFT = 0;
    private static final int POSITION_ON_RIGHT = 1;
    private static final String TAG = "pressure:FingerPressNavi";
    private static final int appPressueAboveLimit = 30;
    private static ArrayList<String> mAllResumePackName = new ArrayList<>();
    private static FingerPressNavigation mFingerPressNavi = null;
    private String LeftPkgName = "none_app";
    private int REPEAT_PATTERN = -1;
    private String RightPkgName = "none_app";
    private boolean appAdd = false;
    private boolean appDownTrace = false;
    private boolean appDownTraceForFilter = false;
    private Object appLock = new Object();
    private boolean appNaviTrace = false;
    private int appPressueAboveStep = 0;
    private boolean backAdd = false;
    private boolean mAllComsume;
    private long[] mAppKeyVibePattern = {0, 10, 20, 30};
    private Object mBackLock = new Object();
    private HwCircleWindow mCircle = null;
    private Context mContext = null;
    private long mDownTime = 0;
    private WindowState mFocusedWindow;
    private String mFocusedWindowPackageName;
    private int mHeighLeftBottom = 0;
    private int mHeighLeftTOP = 0;
    private int mHeight = 0;
    private int mHeightBottom = 0;
    private int mHeightBottom1 = 0;
    private int mHeightBottom2 = 0;
    private int mHeightBottomForApp = 0;
    private int mHeightTopForApp = 0;
    private boolean mImmersiveMode = false;
    private boolean mIsFullScreen = false;
    private boolean mIsGameScene = false;
    private boolean mIsPortrait = true;
    private KeyguardManager mKeyguardManager;
    private boolean mKeyguardOn = false;
    private int mMode = 0;
    private int mNaviBarPos = 0;
    private HwPhoneWindowManager mPolicy;
    private int mStatusBarHeight = 30;
    private Vibrator mVibrator = null;
    private int mWidth = 0;
    private int mWidthBottomLeft = 0;
    private int mWidthBottomRight = 0;
    private int mWidthLeft = 0;
    private int mWidthRight = 0;
    private boolean naviDownTrace = false;
    private boolean naviTrace = false;
    private boolean upAdd = false;
    private Object upLock = new Object();

    static {
        mAllResumePackName.add("com.dianping.v1");
        mAllResumePackName.add("com.sina.weibo");
        mAllResumePackName.add("com.tencent.mm");
    }

    protected FingerPressNavigation(Context context) {
        this.mContext = context;
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mCircle = HwCircleWindow.getInstance(this.mContext);
        this.mStatusBarHeight = this.mContext.getResources().getDimensionPixelSize(17105318);
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        if (this.mVibrator != null && !this.mVibrator.hasVibrator()) {
            this.mVibrator = null;
        }
        this.mPolicy = (HwPhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class);
    }

    public static synchronized FingerPressNavigation getInstance(Context context) {
        FingerPressNavigation fingerPressNavigation;
        synchronized (FingerPressNavigation.class) {
            if (mFingerPressNavi == null) {
                mFingerPressNavi = new FingerPressNavigation(context);
            }
            fingerPressNavigation = mFingerPressNavi;
        }
        return fingerPressNavigation;
    }

    public void setNeedTip(boolean needtip) {
        if (this.mCircle != null) {
            this.mCircle.setNeedTip(needtip);
        }
    }

    public void setMode(int mode) {
        this.mMode = mode;
        if (this.mCircle != null) {
            this.mCircle.setMode(mode);
        }
    }

    public void setGameScene(boolean isGame) {
        this.mIsGameScene = isGame;
    }

    private void performBackDown(int keycode) {
        synchronized (this.mBackLock) {
            if (!this.backAdd) {
                sendEvent(0, 0, keycode);
                this.backAdd = true;
            }
        }
    }

    private void stopPerformBackDown() {
        synchronized (this.mBackLock) {
            if (this.backAdd) {
                this.backAdd = false;
            }
        }
    }

    private void performBackUp(int keycode) {
        synchronized (this.upLock) {
            if (!this.upAdd) {
                sendEvent(1, 0, keycode);
                this.upAdd = true;
                StatisticalUtils.reportc(this.mContext, 81);
            }
        }
    }

    private void stopPerformBackUp() {
        synchronized (this.upLock) {
            if (this.upAdd) {
                this.upAdd = false;
            }
        }
    }

    public void setImmersiveMode(boolean mode) {
        this.mImmersiveMode = mode;
        if (this.mCircle != null) {
            this.mCircle.setImmersiveMode(mode);
        }
    }

    public void setDisplayWidthAndHeight(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        if (height > width) {
            this.mWidthLeft = (int) (((double) this.mWidth) * 0.1d);
            this.mWidthRight = (int) (((double) this.mWidth) * 0.9d);
            this.mWidthBottomLeft = (int) (((double) this.mWidth) * 0.375d);
            this.mWidthBottomRight = (int) (((double) this.mWidth) * 0.625d);
            this.mHeightBottom = (int) (((double) this.mHeight) * 0.975d);
            this.mHeightBottom1 = (int) (((double) this.mHeight) * 0.982d);
            this.mHeightBottom2 = (int) (((double) this.mHeight) * 0.975d);
            this.mIsPortrait = true;
        } else {
            this.mHeighLeftTOP = (int) (((double) this.mHeight) * 0.375d);
            this.mHeighLeftBottom = (int) (((double) this.mHeight) * 0.625d);
            this.mHeightBottom = (int) (((double) this.mWidth) * 0.95d);
            this.mHeightTopForApp = (int) (((double) this.mHeight) * 0.1d);
            this.mHeightBottomForApp = (int) (((double) this.mHeight) * 0.9d);
            this.mIsPortrait = false;
        }
        if (this.mCircle != null) {
            this.mCircle.setDisplay(width, height);
        }
    }

    private void reset() {
        this.naviTrace = false;
        this.naviDownTrace = false;
        this.appDownTrace = false;
        this.appNaviTrace = false;
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
        if (this.mCircle != null) {
            this.mCircle.destoryCircleWindowAndBitmap();
        }
    }

    private boolean isKeyguardLocked() {
        if (this.mKeyguardManager != null) {
            return this.mKeyguardManager.isKeyguardLocked();
        }
        Log.e(TAG, "isKeyguardLocked mKeyguardManager is null");
        return false;
    }

    private boolean isKeyguardFocus() {
        return this.mKeyguardOn;
    }

    public void setCurFocusWindow(WindowState focus) {
        this.mFocusedWindow = focus;
        String str = null;
        WindowManager.LayoutParams focusAttrs = this.mFocusedWindow != null ? this.mFocusedWindow.getAttrs() : null;
        boolean z = false;
        if (((focusAttrs != null ? focusAttrs.privateFlags : 0) & 1024) != 0) {
            z = true;
        }
        this.mKeyguardOn = z;
        if (this.mCircle != null) {
            this.mCircle.setCanNaviDraw(!this.mKeyguardOn);
        }
        if (focusAttrs != null) {
            str = focusAttrs.packageName;
        }
        this.mFocusedWindowPackageName = str;
        updateAllComsume(this.mFocusedWindowPackageName);
    }

    private void updateAllComsume(String packageName) {
        if (!this.mIsPortrait) {
            this.mAllComsume = false;
        } else if (packageName == null) {
            this.mAllComsume = false;
            this.mHeightBottom = this.mHeightBottom2;
        } else if (mAllResumePackName.contains(packageName)) {
            this.mAllComsume = true;
            this.mHeightBottom = this.mHeightBottom1;
        } else {
            this.mAllComsume = false;
            this.mHeightBottom = this.mHeightBottom2;
        }
    }

    public void setIsTopFullScreen(boolean isTopFullScreen) {
        this.mIsFullScreen = isTopFullScreen;
    }

    private boolean interceptByPressureNavi(MotionEvent event, float pressue, int keyevent) {
        boolean needComsumetmp = false;
        switch (event.getAction()) {
            case 0:
                this.naviDownTrace = true;
                return false;
            case 1:
            case 3:
                if (this.naviDownTrace && this.naviTrace) {
                    needComsumetmp = true;
                    this.naviTrace = false;
                    this.naviDownTrace = false;
                }
                reset();
                return needComsumetmp;
            case 2:
                float pressuelimit = this.mCircle.getPressureLimit(keyevent);
                this.mDownTime = SystemClock.uptimeMillis();
                if (!this.naviDownTrace || pressue <= pressuelimit) {
                    return false;
                }
                this.naviTrace = true;
                this.appNaviTrace = false;
                performBackDown(keyevent);
                performBackUp(keyevent);
                return true;
            default:
                return false;
        }
    }

    private boolean interceptByAppNavi(MotionEvent event, float pressue, int status) {
        boolean needComsumetmp = false;
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case 0:
                this.appDownTrace = true;
                break;
            case 1:
            case 3:
                if (this.appDownTrace && this.appNaviTrace) {
                    needComsumetmp = true;
                    this.appNaviTrace = false;
                    this.appDownTrace = false;
                }
                reset();
                break;
            case 2:
                float pressuelimit = this.mCircle.getPressureLimit(-1);
                this.mDownTime = SystemClock.uptimeMillis();
                if (this.appDownTrace && pressue > pressuelimit) {
                    this.naviTrace = false;
                    this.appNaviTrace = true;
                    needComsumetmp = true;
                    performAppStart(status);
                    break;
                }
        }
        if (this.mIsPortrait) {
            if (this.mIsFullScreen) {
                return needComsumetmp;
            }
            if ((x <= this.mWidthRight && x >= this.mWidthLeft) || y >= this.mStatusBarHeight) {
                return needComsumetmp;
            }
            if (action == 0 || action == 2) {
                return true;
            }
            return needComsumetmp;
        } else if (this.mIsFullScreen || ((double) x) >= ((double) this.mStatusBarHeight) * 1.7d || y >= this.mStatusBarHeight) {
            return needComsumetmp;
        } else {
            if (action == 0 || action == 2) {
                return true;
            }
            return needComsumetmp;
        }
    }

    private void performAppStart(int status) {
        synchronized (this.appLock) {
            if (!this.appAdd) {
                handleAppNavi(status);
                this.appAdd = true;
            }
        }
    }

    private void stopPerformAppStart() {
        synchronized (this.appLock) {
            if (this.appAdd) {
                this.appAdd = false;
            }
        }
    }

    public void setAppRightPkg(String App_Right) {
        this.RightPkgName = App_Right;
    }

    public void setAppLeftPkg(String App_Left) {
        this.LeftPkgName = App_Left;
    }

    /* access modifiers changed from: package-private */
    public void handleAppNavi(int status) {
        switch (status) {
            case 0:
                if (!"none_app".equals(this.LeftPkgName)) {
                    startActivity(this.LeftPkgName);
                }
                if (this.mVibrator != null) {
                    this.mVibrator.vibrate(this.mAppKeyVibePattern, this.REPEAT_PATTERN);
                    return;
                }
                return;
            case 1:
                if (!"none_app".equals(this.RightPkgName)) {
                    startActivity(this.RightPkgName);
                }
                if (this.mVibrator != null) {
                    this.mVibrator.vibrate(this.mAppKeyVibePattern, this.REPEAT_PATTERN);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void startActivity(String startInfo) {
        if (startInfo != null) {
            String[] startInfos = startInfo.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            if (startInfos != null && startInfos.length == 2) {
                try {
                    Log.v(TAG, "pressure:FingerPressNavi startFingerPressActivity and this app is : " + startInfos[0] + " ; " + startInfos[1]);
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.addCategory("android.intent.category.LAUNCHER");
                    intent.setClassName(startInfos[0], startInfos[1]);
                    intent.addFlags(805306368);
                    this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                    Context context = this.mContext;
                    StatisticalUtils.reporte(context, 82, "{pkg:" + startInfos[0] + "}");
                } catch (Exception ex) {
                    Log.e(TAG, "get exception: " + ex);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setPressureLimit(float limit) {
        if (this.mCircle != null) {
            this.mCircle.setPressureLimit(limit);
        }
    }

    private void handleByAnimation(MotionEvent event, int type) {
        if (this.mCircle != null) {
            this.mCircle.onTouchEvent(event, type);
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
        } else if (TelephonyManager.getDefault().getCallState() != 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isHandledByLeftEdge(MotionEvent event, float x, float y, float pressue) {
        if (this.mIsPortrait) {
            if (x < ((float) this.mWidthLeft) && ((double) y) < ((double) this.mStatusBarHeight) * 1.7d) {
                return true;
            }
        } else if (y > ((float) this.mHeightBottomForApp) && ((double) x) < ((double) this.mStatusBarHeight) * 1.7d) {
            return true;
        }
        return false;
    }

    private boolean isHandledByRightEdge(MotionEvent event, float x, float y, float pressue) {
        if (this.mIsPortrait) {
            if (x > ((float) this.mWidthRight) && ((double) y) < ((double) this.mStatusBarHeight) * 1.7d) {
                return true;
            }
        } else if (y < ((float) this.mHeightTopForApp) && ((double) x) < ((double) this.mStatusBarHeight) * 1.7d) {
            return true;
        }
        return false;
    }

    private boolean isHandledByBottomCenter(MotionEvent event, float x, float y, float pressue) {
        if (this.mIsPortrait) {
            if (y > ((float) this.mHeightBottom) && x < ((float) this.mWidthBottomRight) && x > ((float) this.mWidthBottomLeft)) {
                return true;
            }
        } else if (x > ((float) this.mHeightBottom) && y > ((float) this.mHeighLeftTOP) && y < ((float) this.mHeighLeftBottom)) {
            return true;
        }
        return false;
    }

    private boolean isHandledByBottomLeft(MotionEvent event, float x, float y, float pressue) {
        if (this.mIsPortrait) {
            if (y > ((float) this.mHeightBottom) && x < ((float) this.mWidthBottomLeft)) {
                return true;
            }
        } else if (x > ((float) this.mHeightBottom) && y > ((float) this.mHeighLeftBottom)) {
            return true;
        }
        return false;
    }

    private boolean isHandledByBottomRight(MotionEvent event, float x, float y, float pressue) {
        if (this.mIsPortrait) {
            if (y > ((float) this.mHeightBottom) && x > ((float) this.mWidthBottomRight)) {
                return true;
            }
        } else if (x > ((float) this.mHeightBottom) && y < ((float) this.mHeighLeftTOP)) {
            return true;
        }
        return false;
    }

    public void setNaviBarPosition(int type) {
        if (type == 0 || type == 2) {
            this.mNaviBarPos = 0;
        } else if (type == 1 || type == 3) {
            this.mNaviBarPos = 1;
        }
    }

    private int getKeyCode(int pos) {
        switch (pos) {
            case 0:
                if (this.mNaviBarPos == 0) {
                    return 4;
                }
                return 187;
            case 1:
                if (this.mNaviBarPos == 0) {
                    return 187;
                }
                return 4;
            default:
                if (this.mNaviBarPos == 0) {
                    return 4;
                }
                return 187;
        }
    }

    private int getAnimationType(int pos) {
        switch (pos) {
            case 0:
                if (this.mNaviBarPos == 0) {
                    return 2;
                }
                return 3;
            case 1:
                if (this.mNaviBarPos == 0) {
                    return 3;
                }
                return 2;
            default:
                if (this.mNaviBarPos == 0) {
                    return 2;
                }
                return 3;
        }
    }

    private boolean filterAppStartEvent(MotionEvent event) {
        int action = event.getAction();
        float pressue = event.getPressure();
        switch (action) {
            case 0:
                this.appDownTraceForFilter = true;
                this.appPressueAboveStep = 0;
                break;
            case 1:
            case 3:
                this.appDownTraceForFilter = false;
                break;
            case 2:
                float pressuelimit = this.mCircle.getPressureLimit(-1) * 0.9f;
                if (this.appDownTraceForFilter && pressue >= pressuelimit) {
                    this.appPressueAboveStep++;
                    if (this.appPressueAboveStep > 30) {
                        this.appPressueAboveStep = 31;
                        break;
                    }
                }
                break;
        }
        if (!this.appDownTraceForFilter || this.appPressueAboveStep > 30 || this.appPressueAboveStep <= 0) {
            return false;
        }
        return true;
    }

    public boolean filterPressueInputEvent(InputEvent event) {
        boolean isComsume = true;
        if (event instanceof MotionEvent) {
            MotionEvent motionevent = (MotionEvent) event;
            float x = motionevent.getX();
            float y = motionevent.getY();
            float pressue = motionevent.getPressure();
            if (this.mIsGameScene || this.mIsFullScreen || this.RightPkgName == null || "none_app".equals(this.RightPkgName) || !isHandledByRightEdge(motionevent, x, y, pressue)) {
                if (this.mIsGameScene || this.mIsFullScreen || this.LeftPkgName == null || "none_app".equals(this.LeftPkgName) || !isHandledByLeftEdge(motionevent, x, y, pressue)) {
                    if (!this.mImmersiveMode && this.mMode == 1 && isHandledByBottomCenter(motionevent, x, y, pressue)) {
                        if (!isKeyguardLocked()) {
                            handleByAnimation(motionevent, 1);
                            isComsume = !interceptByPressureNavi(motionevent, pressue, 3);
                        }
                        if (this.mAllComsume) {
                            isComsume = false;
                        }
                    } else if (!this.mImmersiveMode && this.mMode == 1 && isHandledByBottomLeft(motionevent, x, y, pressue)) {
                        if (!isKeyguardLocked()) {
                            handleByAnimation(motionevent, getAnimationType(0));
                            isComsume = !interceptByPressureNavi(motionevent, pressue, getKeyCode(0));
                        } else if (!isKeyguardFocus() && getKeyCode(0) == 4) {
                            handleByAnimation(motionevent, getAnimationType(0));
                            isComsume = !interceptByPressureNavi(motionevent, pressue, getKeyCode(0));
                        }
                        if (this.mAllComsume) {
                            isComsume = false;
                        }
                    } else if (this.mImmersiveMode || this.mMode != 1 || !isHandledByBottomRight(motionevent, x, y, pressue)) {
                        if (this.mCircle != null) {
                            this.mCircle.resetAnimaion();
                            this.mCircle.destoryCircleWindowForAPP();
                        }
                        reset();
                        isComsume = true;
                    } else {
                        if (!isKeyguardLocked()) {
                            handleByAnimation(motionevent, getAnimationType(1));
                            isComsume = !interceptByPressureNavi(motionevent, pressue, getKeyCode(1));
                        } else if (!isKeyguardFocus() && getKeyCode(1) == 4) {
                            handleByAnimation(motionevent, getAnimationType(1));
                            isComsume = !interceptByPressureNavi(motionevent, pressue, getKeyCode(1));
                        }
                        if (this.mAllComsume) {
                            isComsume = false;
                        }
                    }
                } else if (!isKeyguardLocked() && !isPhoneInCall() && !filterAppStartEvent(motionevent)) {
                    handleByAnimation(motionevent, 4);
                    isComsume = !interceptByAppNavi(motionevent, pressue, 0);
                }
            } else if (!isKeyguardLocked() && !isPhoneInCall() && !filterAppStartEvent(motionevent)) {
                handleByAnimation(motionevent, 4);
                isComsume = !interceptByAppNavi(motionevent, pressue, 1);
            }
            if (this.mMode == 1 && this.mAllComsume && this.mPolicy != null) {
                this.mPolicy.addPointerEvent(motionevent);
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
        KeyEvent ev = new KeyEvent(this.mDownTime, when, action, code, 0, 0, -1, 0, flags | 8 | 64, 257);
        InputManager.getInstance().injectInputEvent(ev, 0);
    }
}

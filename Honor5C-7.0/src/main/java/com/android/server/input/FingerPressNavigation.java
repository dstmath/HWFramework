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
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy;
import com.android.server.LocalServices;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wm.WindowState;
import com.huawei.android.statistical.StatisticalUtils;
import com.huawei.android.util.NoExtAPIException;
import huawei.com.android.server.policy.HwGlobalActionsData;
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
    private static ArrayList<String> mAllResumePackName;
    private static FingerPressNavigation mFingerPressNavi;
    private String LeftPkgName;
    private int REPEAT_PATTERN;
    private String RightPkgName;
    private boolean appAdd;
    private boolean appDownTrace;
    private boolean appDownTraceForFilter;
    private Object appLock;
    private boolean appNaviTrace;
    private int appPressueAboveStep;
    private boolean backAdd;
    private boolean mAllComsume;
    private long[] mAppKeyVibePattern;
    private Object mBackLock;
    private HwCircleWindow mCircle;
    private Context mContext;
    private long mDownTime;
    private WindowState mFocusedWindow;
    private String mFocusedWindowPackageName;
    private int mHeighLeftBottom;
    private int mHeighLeftTOP;
    private int mHeight;
    private int mHeightBottom;
    private int mHeightBottom1;
    private int mHeightBottom2;
    private int mHeightBottomForApp;
    private int mHeightTopForApp;
    private boolean mImmersiveMode;
    private boolean mIsFullScreen;
    private boolean mIsGameScene;
    private boolean mIsPortrait;
    private KeyguardManager mKeyguardManager;
    private boolean mKeyguardOn;
    private int mMode;
    private int mNaviBarPos;
    private HwPhoneWindowManager mPolicy;
    private int mStatusBarHeight;
    private Vibrator mVibrator;
    private int mWidth;
    private int mWidthBottomLeft;
    private int mWidthBottomRight;
    private int mWidthLeft;
    private int mWidthRight;
    private boolean naviDownTrace;
    private boolean naviTrace;
    private boolean upAdd;
    private Object upLock;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.input.FingerPressNavigation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.input.FingerPressNavigation.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.input.FingerPressNavigation.<clinit>():void");
    }

    protected FingerPressNavigation(Context context) {
        this.mContext = null;
        this.mCircle = null;
        this.mDownTime = 0;
        this.mWidth = POSITION_ON_LEFT;
        this.mHeight = POSITION_ON_LEFT;
        this.mWidthLeft = POSITION_ON_LEFT;
        this.mWidthRight = POSITION_ON_LEFT;
        this.mHeightBottom = POSITION_ON_LEFT;
        this.mHeightBottom1 = POSITION_ON_LEFT;
        this.mHeightBottom2 = POSITION_ON_LEFT;
        this.mWidthBottomLeft = POSITION_ON_LEFT;
        this.mWidthBottomRight = POSITION_ON_LEFT;
        this.mHeighLeftTOP = POSITION_ON_LEFT;
        this.mHeighLeftBottom = POSITION_ON_LEFT;
        this.mHeightTopForApp = POSITION_ON_LEFT;
        this.mHeightBottomForApp = POSITION_ON_LEFT;
        this.naviTrace = DEBUG_TOUCH;
        this.naviDownTrace = DEBUG_TOUCH;
        this.appDownTrace = DEBUG_TOUCH;
        this.appDownTraceForFilter = DEBUG_TOUCH;
        this.appPressueAboveStep = POSITION_ON_LEFT;
        this.appNaviTrace = DEBUG_TOUCH;
        this.mIsPortrait = true;
        this.LeftPkgName = "none_app";
        this.RightPkgName = "none_app";
        this.mStatusBarHeight = appPressueAboveLimit;
        this.mNaviBarPos = POSITION_ON_LEFT;
        this.mKeyguardOn = DEBUG_TOUCH;
        this.mIsFullScreen = DEBUG_TOUCH;
        this.mMode = POSITION_ON_LEFT;
        this.mImmersiveMode = DEBUG_TOUCH;
        this.mIsGameScene = DEBUG_TOUCH;
        this.mVibrator = null;
        this.REPEAT_PATTERN = -1;
        this.mAppKeyVibePattern = new long[]{0, 10, 20, 30};
        this.mBackLock = new Object();
        this.backAdd = DEBUG_TOUCH;
        this.upLock = new Object();
        this.upAdd = DEBUG_TOUCH;
        this.appLock = new Object();
        this.appAdd = DEBUG_TOUCH;
        this.mContext = context;
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mCircle = HwCircleWindow.getInstance(this.mContext);
        this.mStatusBarHeight = this.mContext.getResources().getDimensionPixelSize(17104919);
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        if (!(this.mVibrator == null || this.mVibrator.hasVibrator())) {
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
                sendEvent(POSITION_ON_LEFT, POSITION_ON_LEFT, keycode);
                this.backAdd = true;
            }
        }
    }

    private void stopPerformBackDown() {
        synchronized (this.mBackLock) {
            if (this.backAdd) {
                this.backAdd = DEBUG_TOUCH;
            }
        }
    }

    private void performBackUp(int keycode) {
        synchronized (this.upLock) {
            if (!this.upAdd) {
                sendEvent(POSITION_ON_RIGHT, POSITION_ON_LEFT, keycode);
                this.upAdd = true;
                StatisticalUtils.reportc(this.mContext, 81);
            }
        }
    }

    private void stopPerformBackUp() {
        synchronized (this.upLock) {
            if (this.upAdd) {
                this.upAdd = DEBUG_TOUCH;
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
            this.mIsPortrait = DEBUG_TOUCH;
        }
        if (this.mCircle != null) {
            this.mCircle.setDisplay(width, height);
        }
    }

    private void reset() {
        this.naviTrace = DEBUG_TOUCH;
        this.naviDownTrace = DEBUG_TOUCH;
        this.appDownTrace = DEBUG_TOUCH;
        this.appNaviTrace = DEBUG_TOUCH;
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
        return DEBUG_TOUCH;
    }

    private boolean isKeyguardFocus() {
        return this.mKeyguardOn;
    }

    public void setCurFocusWindow(WindowState focus) {
        LayoutParams focusAttrs;
        int privateFlags;
        boolean z;
        String str;
        boolean z2 = DEBUG_TOUCH;
        this.mFocusedWindow = focus;
        if (this.mFocusedWindow != null) {
            focusAttrs = this.mFocusedWindow.getAttrs();
        } else {
            focusAttrs = null;
        }
        if (focusAttrs != null) {
            privateFlags = focusAttrs.privateFlags;
        } else {
            privateFlags = POSITION_ON_LEFT;
        }
        if ((privateFlags & HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) != 0) {
            z = true;
        } else {
            z = DEBUG_TOUCH;
        }
        this.mKeyguardOn = z;
        if (this.mCircle != null) {
            HwCircleWindow hwCircleWindow = this.mCircle;
            if (!this.mKeyguardOn) {
                z2 = true;
            }
            hwCircleWindow.setCanNaviDraw(z2);
        }
        if (focusAttrs != null) {
            str = focusAttrs.packageName;
        } else {
            str = null;
        }
        this.mFocusedWindowPackageName = str;
        updateAllComsume(this.mFocusedWindowPackageName);
    }

    private void updateAllComsume(String packageName) {
        if (!this.mIsPortrait) {
            this.mAllComsume = DEBUG_TOUCH;
        } else if (packageName == null) {
            this.mAllComsume = DEBUG_TOUCH;
            this.mHeightBottom = this.mHeightBottom2;
        } else if (mAllResumePackName.contains(packageName)) {
            this.mAllComsume = true;
            this.mHeightBottom = this.mHeightBottom1;
        } else {
            this.mAllComsume = DEBUG_TOUCH;
            this.mHeightBottom = this.mHeightBottom2;
        }
    }

    public void setIsTopFullScreen(boolean isTopFullScreen) {
        this.mIsFullScreen = isTopFullScreen;
    }

    private boolean interceptByPressureNavi(MotionEvent event, float pressue, int keyevent) {
        boolean needComsumetmp = DEBUG_TOUCH;
        switch (event.getAction()) {
            case POSITION_ON_LEFT /*0*/:
                this.naviDownTrace = true;
                return DEBUG_TOUCH;
            case POSITION_ON_RIGHT /*1*/:
            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                if (this.naviDownTrace && this.naviTrace) {
                    needComsumetmp = true;
                    this.naviTrace = DEBUG_TOUCH;
                    this.naviDownTrace = DEBUG_TOUCH;
                }
                reset();
                return needComsumetmp;
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                float pressuelimit = this.mCircle.getPressureLimit(keyevent);
                this.mDownTime = SystemClock.uptimeMillis();
                if (!this.naviDownTrace || pressue <= pressuelimit) {
                    return DEBUG_TOUCH;
                }
                this.naviTrace = true;
                this.appNaviTrace = DEBUG_TOUCH;
                performBackDown(keyevent);
                performBackUp(keyevent);
                return true;
            default:
                return DEBUG_TOUCH;
        }
    }

    private boolean interceptByAppNavi(MotionEvent event, float pressue, int status) {
        boolean needComsumetmp = DEBUG_TOUCH;
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case POSITION_ON_LEFT /*0*/:
                this.appDownTrace = true;
                break;
            case POSITION_ON_RIGHT /*1*/:
            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                if (this.appDownTrace && this.appNaviTrace) {
                    needComsumetmp = true;
                    this.appNaviTrace = DEBUG_TOUCH;
                    this.appDownTrace = DEBUG_TOUCH;
                }
                reset();
                break;
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                float pressuelimit = this.mCircle.getPressureLimit(-1);
                this.mDownTime = SystemClock.uptimeMillis();
                if (this.appDownTrace && pressue > pressuelimit) {
                    this.naviTrace = DEBUG_TOUCH;
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
                this.appAdd = DEBUG_TOUCH;
            }
        }
    }

    public void setAppRightPkg(String App_Right) {
        this.RightPkgName = App_Right;
    }

    public void setAppLeftPkg(String App_Left) {
        this.LeftPkgName = App_Left;
    }

    void handleAppNavi(int status) {
        switch (status) {
            case POSITION_ON_LEFT /*0*/:
                if (!"none_app".equals(this.LeftPkgName)) {
                    startActivity(this.LeftPkgName);
                }
                if (this.mVibrator != null) {
                    this.mVibrator.vibrate(this.mAppKeyVibePattern, this.REPEAT_PATTERN);
                }
            case POSITION_ON_RIGHT /*1*/:
                if (!"none_app".equals(this.RightPkgName)) {
                    startActivity(this.RightPkgName);
                }
                if (this.mVibrator != null) {
                    this.mVibrator.vibrate(this.mAppKeyVibePattern, this.REPEAT_PATTERN);
                }
            default:
        }
    }

    private void startActivity(String startInfo) {
        if (startInfo != null) {
            String[] startInfos = startInfo.split(CPUCustBaseConfig.CPUCONFIG_GAP_IDENTIFIER);
            if (startInfos != null && startInfos.length == 2) {
                try {
                    Log.v(TAG, "pressure:FingerPressNavi startFingerPressActivity and this app is : " + startInfos[POSITION_ON_LEFT] + " ; " + startInfos[POSITION_ON_RIGHT]);
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.addCategory("android.intent.category.LAUNCHER");
                    intent.setClassName(startInfos[POSITION_ON_LEFT], startInfos[POSITION_ON_RIGHT]);
                    intent.addFlags(805306368);
                    this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                    StatisticalUtils.reporte(this.mContext, 82, "{pkg:" + startInfos[POSITION_ON_LEFT] + "}");
                } catch (Exception ex) {
                    Log.e(TAG, "get exception: " + ex);
                }
            }
        }
    }

    void setPressureLimit(float limit) {
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
        boolean flag = DEBUG_TOUCH;
        try {
            flag = MSimTelephonyManager.getDefault().isMultiSimEnabled();
        } catch (NoExtAPIException e) {
            Log.w(TAG, "FingerPressNavigation->isMultiSimEnabled->NoExtAPIException!");
        }
        return flag;
    }

    private boolean isPhoneInCall() {
        if (isMultiSimEnabled()) {
            for (int i = POSITION_ON_LEFT; i < MSimTelephonyManager.getDefault().getPhoneCount(); i += POSITION_ON_RIGHT) {
                if (MSimTelephonyManager.getDefault().getCallState(i) != 0) {
                    return true;
                }
            }
            return DEBUG_TOUCH;
        } else if (TelephonyManager.getDefault().getCallState() != 0) {
            return true;
        } else {
            return DEBUG_TOUCH;
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
        return DEBUG_TOUCH;
    }

    private boolean isHandledByRightEdge(MotionEvent event, float x, float y, float pressue) {
        if (this.mIsPortrait) {
            if (x > ((float) this.mWidthRight) && ((double) y) < ((double) this.mStatusBarHeight) * 1.7d) {
                return true;
            }
        } else if (y < ((float) this.mHeightTopForApp) && ((double) x) < ((double) this.mStatusBarHeight) * 1.7d) {
            return true;
        }
        return DEBUG_TOUCH;
    }

    private boolean isHandledByBottomCenter(MotionEvent event, float x, float y, float pressue) {
        if (this.mIsPortrait) {
            if (y > ((float) this.mHeightBottom) && x < ((float) this.mWidthBottomRight) && x > ((float) this.mWidthBottomLeft)) {
                return true;
            }
        } else if (x > ((float) this.mHeightBottom) && y > ((float) this.mHeighLeftTOP) && y < ((float) this.mHeighLeftBottom)) {
            return true;
        }
        return DEBUG_TOUCH;
    }

    private boolean isHandledByBottomLeft(MotionEvent event, float x, float y, float pressue) {
        if (this.mIsPortrait) {
            if (y > ((float) this.mHeightBottom) && x < ((float) this.mWidthBottomLeft)) {
                return true;
            }
        } else if (x > ((float) this.mHeightBottom) && y > ((float) this.mHeighLeftBottom)) {
            return true;
        }
        return DEBUG_TOUCH;
    }

    private boolean isHandledByBottomRight(MotionEvent event, float x, float y, float pressue) {
        if (this.mIsPortrait) {
            if (y > ((float) this.mHeightBottom) && x > ((float) this.mWidthBottomRight)) {
                return true;
            }
        } else if (x > ((float) this.mHeightBottom) && y < ((float) this.mHeighLeftTOP)) {
            return true;
        }
        return DEBUG_TOUCH;
    }

    public void setNaviBarPosition(int type) {
        if (type == 0 || type == 2) {
            this.mNaviBarPos = POSITION_ON_LEFT;
        } else if (type == POSITION_ON_RIGHT || type == 3) {
            this.mNaviBarPos = POSITION_ON_RIGHT;
        }
    }

    private int getKeyCode(int pos) {
        switch (pos) {
            case POSITION_ON_LEFT /*0*/:
                if (this.mNaviBarPos == 0) {
                    return 4;
                }
                return 187;
            case POSITION_ON_RIGHT /*1*/:
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
            case POSITION_ON_LEFT /*0*/:
                if (this.mNaviBarPos == 0) {
                    return 2;
                }
                return 3;
            case POSITION_ON_RIGHT /*1*/:
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
            case POSITION_ON_LEFT /*0*/:
                this.appDownTraceForFilter = true;
                this.appPressueAboveStep = POSITION_ON_LEFT;
                break;
            case POSITION_ON_RIGHT /*1*/:
            case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                this.appDownTraceForFilter = DEBUG_TOUCH;
                break;
            case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                float pressuelimit = this.mCircle.getPressureLimit(-1) * 0.9f;
                if (this.appDownTraceForFilter && pressue >= pressuelimit) {
                    this.appPressueAboveStep += POSITION_ON_RIGHT;
                    if (this.appPressueAboveStep > appPressueAboveLimit) {
                        this.appPressueAboveStep = 31;
                        break;
                    }
                }
                break;
        }
        if (!this.appDownTraceForFilter || this.appPressueAboveStep > appPressueAboveLimit || this.appPressueAboveStep <= 0) {
            return DEBUG_TOUCH;
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
                    if (!this.mImmersiveMode && this.mMode == POSITION_ON_RIGHT && isHandledByBottomCenter(motionevent, x, y, pressue)) {
                        if (!isKeyguardLocked()) {
                            handleByAnimation(motionevent, POSITION_ON_RIGHT);
                            isComsume = interceptByPressureNavi(motionevent, pressue, 3) ? DEBUG_TOUCH : true;
                        }
                        if (this.mAllComsume) {
                            isComsume = DEBUG_TOUCH;
                        }
                    } else if (!this.mImmersiveMode && this.mMode == POSITION_ON_RIGHT && isHandledByBottomLeft(motionevent, x, y, pressue)) {
                        if (!isKeyguardLocked()) {
                            handleByAnimation(motionevent, getAnimationType(POSITION_ON_LEFT));
                            isComsume = interceptByPressureNavi(motionevent, pressue, getKeyCode(POSITION_ON_LEFT)) ? DEBUG_TOUCH : true;
                        } else if (!isKeyguardFocus() && getKeyCode(POSITION_ON_LEFT) == 4) {
                            handleByAnimation(motionevent, getAnimationType(POSITION_ON_LEFT));
                            isComsume = interceptByPressureNavi(motionevent, pressue, getKeyCode(POSITION_ON_LEFT)) ? DEBUG_TOUCH : true;
                        }
                        if (this.mAllComsume) {
                            isComsume = DEBUG_TOUCH;
                        }
                    } else if (!this.mImmersiveMode && this.mMode == POSITION_ON_RIGHT && isHandledByBottomRight(motionevent, x, y, pressue)) {
                        if (!isKeyguardLocked()) {
                            handleByAnimation(motionevent, getAnimationType(POSITION_ON_RIGHT));
                            isComsume = interceptByPressureNavi(motionevent, pressue, getKeyCode(POSITION_ON_RIGHT)) ? DEBUG_TOUCH : true;
                        } else if (!isKeyguardFocus() && getKeyCode(POSITION_ON_RIGHT) == 4) {
                            handleByAnimation(motionevent, getAnimationType(POSITION_ON_RIGHT));
                            isComsume = interceptByPressureNavi(motionevent, pressue, getKeyCode(POSITION_ON_RIGHT)) ? DEBUG_TOUCH : true;
                        }
                        if (this.mAllComsume) {
                            isComsume = DEBUG_TOUCH;
                        }
                    } else {
                        if (this.mCircle != null) {
                            this.mCircle.resetAnimaion();
                            this.mCircle.destoryCircleWindowForAPP();
                        }
                        reset();
                        isComsume = true;
                    }
                } else if (!(isKeyguardLocked() || isPhoneInCall() || filterAppStartEvent(motionevent))) {
                    handleByAnimation(motionevent, 4);
                    isComsume = interceptByAppNavi(motionevent, pressue, POSITION_ON_LEFT) ? DEBUG_TOUCH : true;
                }
            } else if (!(isKeyguardLocked() || isPhoneInCall() || filterAppStartEvent(motionevent))) {
                handleByAnimation(motionevent, 4);
                isComsume = interceptByAppNavi(motionevent, pressue, POSITION_ON_RIGHT) ? DEBUG_TOUCH : true;
            }
            if (this.mMode == POSITION_ON_RIGHT && this.mAllComsume && this.mPolicy != null) {
                this.mPolicy.addPointerEvent(motionevent);
            }
        }
        return isComsume;
    }

    void sendEvent(int action, int flags, int code) {
        sendEvent(action, flags, code, SystemClock.uptimeMillis());
    }

    void sendEvent(int action, int flags, int code, long when) {
        InputManager.getInstance().injectInputEvent(new KeyEvent(this.mDownTime, when, action, code, POSITION_ON_LEFT, POSITION_ON_LEFT, -1, POSITION_ON_LEFT, (flags | 8) | 64, 257), POSITION_ON_LEFT);
    }
}

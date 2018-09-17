package com.huawei.android.hwaps;

import android.app.HwApsInterface;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import java.util.HashMap;

public class SmartLowpowerBrowser implements ISmartLowpowerBrowser {
    private static final int ACTION_DOWN = 0;
    public static final int ACTION_MOVE = 2;
    private static final int ACTION_UP = 1;
    private static final HashMap<String, Integer> BROWSERS_MAP = new HashMap<String, Integer>() {
        {
            put("com.tencent.mtt", Integer.valueOf(1));
            put("com.baidu.browser.apps", Integer.valueOf(2));
            put("com.ijinshan.browser_fast", Integer.valueOf(3));
        }
    };
    private static final int BROWSER_BAIDU = 2;
    private static final int BROWSER_LIEBAO = 3;
    private static final int BROWSER_QQ = 1;
    private static final int CHECK_ACTION_INTERVAL_MILLIS = 167;
    private static final boolean CONSEQUENT_FLAG = true;
    private static final boolean DEBUG = true;
    private static final int DETECT_INTERVAL_MILLIS = 500;
    private static final int DETECT_NEWFRAME_BEGIN = 0;
    private static final int DETECT_NEWFRAME_CONTINUE = 3;
    private static final int DETECT_NEWFRAME_INFRAME = 2;
    private static final int DETECT_NEWFRAME_NEW = 1;
    private static final int DETECT_STATE_DETECTING = 2;
    private static final int DETECT_STATE_NOTHING = 4;
    private static final int DETECT_STATE_RECORDING = 1;
    private static final int DETECT_STATE_SKIP = 3;
    private static final int DETECT_STATE_TOUCHING = 0;
    private static final int DIFF_FRAME_COUNT_UP_LIMIT = 3;
    private static final int FLAG_SLB_MODULE = 262144;
    private static final int FRAME_COUNT_UP_LIMIT = 27;
    private static final int GTCC_DETECT_INTERVAL_MILLIS = 3000;
    private static final boolean LOG = false;
    private static final int MAX_DETECT_CYCLE = 3000;
    private static final int MAX_FRAME_ANIMATION_DURATION = 700;
    public static final int MAX_KEYEVENT_DELAY_MS = 200;
    private static final int RESET_INTERVAL_MILLIS = 30000;
    private static final int RETRY_COUNT_UP_LIMIT = 100;
    private static final int STEP_LENGTH = 5;
    private static final String TAG = "Hwaps";
    private static final int UNSAFE_COUNT_UP_LIMIT = 10;
    private static final int VSYNC_INTERVAL_MILLIS = 17;
    private static final int WAIT_BEFORE_DETECT_MILLIS = 2000;
    private static boolean sIsBrowser = LOG;
    private static boolean sIsSLBTurnedOn = LOG;
    private long GTCCStartTimeStamp = 0;
    private boolean GTCCSwitch = LOG;
    private int animation_autofling = 0;
    private int animation_autofling_last = 0;
    private int animation_cssstyle = 0;
    private int animation_cssstyle_last = 0;
    private int animation_frame = 0;
    private int animation_frame_last = 0;
    private int animation_move = 0;
    private int animation_move_last = 0;
    private int animation_script = 0;
    private int animation_script_last = 0;
    private boolean continueGTCCDetect = LOG;
    private int detect1 = 0;
    private int detect1_rec = 0;
    private int detect2 = 0;
    private int detect2_rec = 0;
    private int detect3 = 0;
    private int detect3_rec = 0;
    private int detect4 = 0;
    private int detect4_rec = 0;
    private int detect5 = 0;
    private int detect6 = 0;
    private int detect6_rec = 0;
    private int detectState = 0;
    private int detectStateDiffFrameCount = 0;
    private long detect_frameAnimate_oldMs = 0;
    private int detect_newFrame = 0;
    private int detect_newFrame_last = 0;
    private long detect_oldMs_save = 0;
    private long detestStartTime = 0;
    private int factor_script = 0;
    private int frameCount = 0;
    private long last_drawTime = 0;
    private boolean mDoSkip = true;
    private long mDrawTimeStamp = 0;
    private boolean mDrawing = LOG;
    private boolean mErrorFlag = LOG;
    private boolean mIsFirstCheckBrowserProcess = true;
    private boolean mIsFirstCheckSwitch = true;
    private boolean mIsFrameScheduled = true;
    private boolean mLastDoSkip = true;
    private String mLastKeyCodeMsg = "";
    private int mMessageGroupCount = 0;
    private String mPackageName = "";
    private int mRetryCount = 0;
    private boolean mSafeSkip = true;
    private long mTouchTimeStamp = 0;
    private boolean mTouching = LOG;
    private boolean mTrySkip = true;
    private int mUnsafeCount = 0;
    private int mWhichBrowser = -1;
    private int ncount = 0;
    private int ncount_ani_css = 0;
    private int ncount_ani_script = 0;
    private boolean needTurnOver = LOG;
    private int playingVideo = 0;
    private int skipStateDiffFrameCount = 0;
    private int stepCount = 0;
    private boolean thisTurnSkip = true;
    private long this_drawTime = 0;

    public void setPlayingVideoSLB(boolean isPlayingVideo) {
        this.playingVideo = (isPlayingVideo ? 1 : -1) + this.playingVideo;
        if (this.playingVideo < 0) {
            this.playingVideo = 0;
        }
    }

    private void setPkgName(String pkgName) {
        this.mPackageName = pkgName;
    }

    public void setFrameScheduledSLB() {
        this.mIsFrameScheduled = true;
    }

    private void setDrawState() {
        this.mDrawing = true;
        this.mDrawTimeStamp = SystemClock.uptimeMillis();
    }

    private boolean getDrawState() {
        if (!this.mDrawing) {
            return LOG;
        }
        this.mDrawing = LOG;
        return true;
    }

    public boolean getTouchState() {
        return this.mTouching;
    }

    public boolean isSLBSwitchOn() {
        boolean z = true;
        if (this.mIsFirstCheckSwitch) {
            if (1 != SystemProperties.getInt("sys.aps.slbswitch", 1)) {
                z = LOG;
            } else if (FLAG_SLB_MODULE != (SystemProperties.getInt("sys.aps.support", 0) & FLAG_SLB_MODULE)) {
                z = LOG;
            }
            sIsSLBTurnedOn = z;
            this.mIsFirstCheckSwitch = LOG;
        }
        if (SystemProperties.getInt("debug.aps.slb", 0) == 4) {
            return LOG;
        }
        return sIsSLBTurnedOn;
    }

    private boolean isBrowserProcess() {
        if (this.mIsFirstCheckBrowserProcess) {
            if (this.mPackageName == null || this.mPackageName.length() == 0) {
                return LOG;
            }
            Integer k = (Integer) BROWSERS_MAP.get(this.mPackageName);
            if (k != null) {
                this.mWhichBrowser = k.intValue();
                sIsBrowser = true;
                Log.i(TAG, "APS: SLB: working state: working, mWhichBrowser:" + this.mWhichBrowser);
            }
            this.mIsFirstCheckBrowserProcess = LOG;
        }
        return sIsBrowser;
    }

    public void setTouchState(int touchState) {
        boolean z = true;
        if (touchState == 0 || touchState == 1 || touchState == 2) {
            if (touchState == 1) {
                z = LOG;
            }
            this.mTouching = z;
            this.mTouchTimeStamp = SystemClock.uptimeMillis();
            ApsCommon.logI(TAG, "APS: SLB: mTouchTimeStamp:" + this.mTouchTimeStamp);
        }
    }

    public static boolean needTouchState() {
        boolean z = LOG;
        if (SystemProperties.getInt("debug.aps.slb", 0) == 4) {
            return LOG;
        }
        if (sIsBrowser) {
            z = sIsSLBTurnedOn;
        }
        return z;
    }

    public boolean initSLB(String pkgName) {
        if (!isSLBSwitchOn()) {
            return LOG;
        }
        setPkgName(pkgName);
        return isBrowserProcess();
    }

    public boolean doProcessDrawSLB(long drawingTime, boolean viewScrollChanged, boolean handlingPointerEvent) {
        switch (this.mWhichBrowser) {
            case 1:
            case 2:
            case 3:
                return skipDraw(drawingTime, viewScrollChanged, handlingPointerEvent);
            default:
                return LOG;
        }
    }

    private void init() {
        this.detect1 = 0;
        this.detect2 = 0;
        this.detect3 = 0;
        this.detect4 = 0;
        this.detect5 = 0;
        this.detect6 = 0;
        this.detect1_rec = 0;
        this.detect2_rec = 0;
        this.detect3_rec = 0;
        this.detect4_rec = 0;
        this.detect6_rec = 0;
        this.detect_newFrame = 0;
        this.detect_newFrame_last = this.detect_newFrame;
        this.this_drawTime = 0;
        this.last_drawTime = 0;
        this.detect_oldMs_save = 0;
        this.detect_frameAnimate_oldMs = 0;
        this.animation_script = 0;
        this.animation_frame = 0;
        this.animation_move = 0;
        this.animation_autofling = 0;
        this.animation_cssstyle = 0;
        this.ncount = 0;
        this.ncount_ani_script = 0;
        this.ncount_ani_css = 0;
        this.factor_script = 0;
    }

    private boolean detectAnimation(long drawingTime, boolean viewScrollChanged, boolean handlingPointerEvent) {
        boolean equals;
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        int DETECT_DEPTH = this.mWhichBrowser == 2 ? 8 : 7;
        int FEATURE_DEPTH = DETECT_DEPTH + 1;
        if (stackTraceElements[DETECT_DEPTH].getClassName().equals("org.chromium.android_webview.AwContents")) {
            equals = stackTraceElements[DETECT_DEPTH].getMethodName().equals("postInvalidateOnAnimation");
        } else {
            equals = LOG;
        }
        if (!equals) {
            return LOG;
        }
        this.detect1_rec = 0;
        this.detect2_rec = 0;
        this.detect3_rec = 0;
        this.detect4_rec = 0;
        this.detect6_rec = 0;
        if (stackTraceElements[FEATURE_DEPTH].getClassName().equals("org.chromium.content.browser.ContentViewCore") && stackTraceElements[FEATURE_DEPTH].getMethodName().equals("nativeOnTouchEvent")) {
            this.detect1++;
            this.detect1_rec = 1;
        } else if (stackTraceElements[FEATURE_DEPTH].getClassName().equals("org.chromium.android_webview.AwContents") && stackTraceElements[FEATURE_DEPTH].getMethodName().equals("nativeScrollTo")) {
            this.detect2++;
            this.detect2_rec = 1;
        } else if (stackTraceElements[FEATURE_DEPTH].getClassName().equals("org.chromium.android_webview.AwContents") && stackTraceElements[FEATURE_DEPTH].getMethodName().equals("nativeOnDraw")) {
            this.detect3++;
            this.detect3_rec = 1;
        } else if (stackTraceElements[FEATURE_DEPTH].getClassName().equals("org.chromium.base.SystemMessageHandler") && stackTraceElements[FEATURE_DEPTH].getMethodName().equals("nativeDoRunLoopOnce")) {
            this.detect4++;
            this.detect4_rec = 1;
            this.detect5++;
        } else if (!stackTraceElements[FEATURE_DEPTH].getClassName().equals("org.chromium.ui.base.WindowAndroid") || !stackTraceElements[FEATURE_DEPTH].getMethodName().equals("nativeOnVSync")) {
            return LOG;
        } else {
            this.detect6++;
            this.detect6_rec = 1;
        }
        long now_Ms = SystemClock.uptimeMillis();
        this.this_drawTime = drawingTime;
        this.detect_newFrame = 2;
        if (this.this_drawTime > this.last_drawTime) {
            if (0 == this.last_drawTime) {
                this.detect_newFrame = 0;
                this.detect_newFrame_last = this.detect_newFrame;
            } else if (now_Ms - this.last_drawTime > EventAnalyzed.STRATEGY_TIME) {
                init();
                this.detect_newFrame = 0;
                this.detect_newFrame_last = this.detect_newFrame;
            } else if (this.this_drawTime - this.last_drawTime >= 34 || now_Ms - this.this_drawTime >= 34) {
                this.detect_newFrame = 1;
            } else {
                this.detect_newFrame = 3;
            }
        }
        switch (this.detect_newFrame) {
            case 1:
            case 3:
                this.detect_newFrame_last = this.detect_newFrame;
                this.animation_move = 0;
                this.animation_autofling = 0;
                if (this.detect1 > 0) {
                    this.animation_move = 1;
                } else if (this.detect2 > 0) {
                    this.animation_autofling = 1;
                }
                this.detect1 = 0;
                this.detect2 = 0;
                if (!(1 == this.animation_move || 1 == this.animation_autofling)) {
                    long delta = this.mTouchTimeStamp - this.this_drawTime;
                    if (delta >= 0 && delta <= 17) {
                        this.animation_move = 1;
                    } else if (viewScrollChanged || handlingPointerEvent) {
                        this.animation_autofling = 1;
                    }
                    this.animation_script = 0;
                    if (this.detect3 > 0) {
                        this.animation_script = 1;
                    } else if (this.detect6 > 0) {
                        this.animation_script = 1;
                    }
                    this.detect3 = 0;
                    this.detect6 = 0;
                    this.animation_cssstyle = 0;
                    if (this.detect5 > 0 && this.this_drawTime - this.last_drawTime < 34 && now_Ms - this.this_drawTime < 34) {
                        this.animation_cssstyle = 1;
                    }
                    this.detect5 = 0;
                    if (now_Ms - this.detect_frameAnimate_oldMs > 700) {
                        this.detect_frameAnimate_oldMs = now_Ms;
                        if (this.animation_move != 0 || this.animation_autofling != 0) {
                            this.animation_frame = 3;
                            if (this.detect4 == 0) {
                                this.animation_frame = 0;
                            }
                            if (1 == this.animation_cssstyle) {
                                this.animation_frame = 0;
                            }
                        } else if (this.detect4 == 0) {
                            this.animation_frame = 0;
                        } else if (this.detect4 > 1 && this.detect4 < 12) {
                            this.animation_frame = 1;
                        } else if (this.detect4 > 15 && this.detect4 < 22) {
                            this.animation_frame = 2;
                        } else if (1 == this.animation_cssstyle) {
                            this.animation_frame = 0;
                        } else {
                            this.animation_frame = 3;
                        }
                        this.detect4 = 0;
                    }
                    this.animation_frame = 0;
                    break;
                }
                break;
            case 2:
                if (this.detect_newFrame_last != 0) {
                    if (now_Ms - this.last_drawTime >= 100 && now_Ms - this.detect_frameAnimate_oldMs > 700) {
                        this.detect_frameAnimate_oldMs = now_Ms;
                        if (this.animation_move != 0 || this.animation_autofling != 0) {
                            this.animation_frame = 12;
                        } else if (this.detect4 > 1 && this.detect4 < 10) {
                            this.animation_frame = 10;
                        } else if (this.detect4 == 0) {
                            this.animation_frame = 0;
                        } else {
                            this.animation_frame = 11;
                        }
                        this.detect4 = 0;
                        break;
                    }
                }
                this.detect_newFrame = 0;
                break;
                break;
        }
        ApsCommon.logI(TAG, "APS: SLB: AnimateDetect@:[" + this.detect_newFrame + "]" + " script:" + this.animation_script + " css:" + this.animation_cssstyle + " frame:" + this.animation_frame + " move:" + this.animation_move + " autofling:" + this.animation_autofling + " [" + this.detect1_rec + "," + this.detect2_rec + "," + this.detect3_rec + "," + this.detect4_rec + "," + this.detect6_rec + "] now_Ms:" + now_Ms + ", drawingTime: " + drawingTime + ", last_drawTime: " + this.last_drawTime + " ,viewScrollChanged:" + viewScrollChanged + " ,handlingPointerEvent:" + handlingPointerEvent + ", mTouchTimeStamp:" + this.mTouchTimeStamp);
        this.last_drawTime = this.this_drawTime;
        return true;
    }

    private boolean skipDraw(long drawingTime, boolean viewScrollChanged, boolean handlingPointerEvent) {
        if (this.playingVideo > 0) {
            ApsCommon.logI(TAG, "APS: SLB: media playingVideo:" + this.playingVideo);
            this.detectState = 0;
            return LOG;
        }
        boolean discard = LOG;
        int diffValue = -2;
        long now_Ms = SystemClock.uptimeMillis();
        if (now_Ms - this.mTouchTimeStamp < 2000) {
            this.detectState = 0;
        } else if (this.detectState == 0) {
            this.detectState = 1;
        }
        switch (this.detectState) {
            case 0:
                detectAnimation(drawingTime, viewScrollChanged, handlingPointerEvent);
                if (this.GTCCSwitch) {
                    switchGTCC(LOG);
                    break;
                }
                break;
            case 1:
                if (detectAnimation(drawingTime, viewScrollChanged, handlingPointerEvent)) {
                    this.detestStartTime = now_Ms;
                    this.animation_script_last = this.animation_script;
                    this.animation_cssstyle_last = this.animation_cssstyle;
                    this.animation_frame_last = this.animation_frame;
                    this.animation_move_last = this.animation_move;
                    this.animation_autofling_last = this.animation_autofling;
                    this.detectState = 2;
                    this.frameCount = 0;
                    switchGTCC(true);
                    this.detectStateDiffFrameCount = 0;
                    break;
                }
                break;
            case 2:
                if (detectAnimation(drawingTime, viewScrollChanged, handlingPointerEvent)) {
                    if ((1 == this.animation_script || 1 == this.animation_cssstyle) && this.animation_frame == 0 && this.animation_move == 0 && this.animation_autofling == 0) {
                        this.frameCount++;
                    } else {
                        this.frameCount = 0;
                    }
                    diffValue = getGTCCDiffValue();
                    if (diffValue != 0) {
                        this.detectStateDiffFrameCount++;
                    }
                    if (now_Ms - this.detestStartTime > 500) {
                        if (this.frameCount < FRAME_COUNT_UP_LIMIT) {
                            switchGTCC(LOG);
                            this.detectState = 4;
                            break;
                        }
                        this.needTurnOver = true;
                        this.thisTurnSkip = true;
                        this.stepCount = 0;
                        this.continueGTCCDetect = this.detectStateDiffFrameCount < 3 ? true : LOG;
                        if (this.continueGTCCDetect) {
                            this.skipStateDiffFrameCount = 0;
                            this.GTCCStartTimeStamp = SystemClock.uptimeMillis();
                        } else {
                            switchGTCC(LOG);
                        }
                        this.detectState = 3;
                        discard = true;
                        break;
                    }
                }
                break;
            case 3:
                if (this.needTurnOver) {
                    this.thisTurnSkip ^= 1;
                } else {
                    this.stepCount++;
                    this.thisTurnSkip = true;
                    if (this.stepCount >= 5) {
                        this.thisTurnSkip = LOG;
                        this.stepCount = 0;
                    }
                }
                if (this.continueGTCCDetect) {
                    if (now_Ms - this.GTCCStartTimeStamp < EventAnalyzed.STRATEGY_TIME) {
                        diffValue = getGTCCDiffValue();
                        if (diffValue != 0) {
                            int i = this.skipStateDiffFrameCount + 1;
                            this.skipStateDiffFrameCount = i;
                            if (i >= 3) {
                                this.continueGTCCDetect = LOG;
                                switchGTCC(LOG);
                            }
                        }
                    } else {
                        this.continueGTCCDetect = LOG;
                        this.needTurnOver = LOG;
                        this.thisTurnSkip = true;
                        switchGTCC(LOG);
                    }
                }
                if (4 != getKeyCode()) {
                    discard = this.thisTurnSkip;
                    break;
                }
                ApsCommon.logI(TAG, "APS: SLB: key event coming, stop skip !");
                this.detectState = 0;
                discard = LOG;
                break;
            default:
                discard = LOG;
                break;
        }
        ApsCommon.logI(TAG, "APS: SLB: discard5: " + (discard ? 1 : 0) + " in:[" + this.detectState + "], playingVideo:" + this.playingVideo + ", needTurnOver:" + this.needTurnOver + "  [" + " script:" + this.animation_script + "|" + this.animation_script_last + " css:" + this.animation_cssstyle + "|" + this.animation_cssstyle_last + " frame:" + this.animation_frame + "|" + this.animation_frame_last + " move:" + this.animation_move + "|" + this.animation_move_last + " autofling:" + this.animation_autofling + "|" + this.animation_autofling_last + "], now_Ms: " + now_Ms + ", detestStartTime:" + this.detestStartTime + " , mTouchTimeStamp:" + this.mTouchTimeStamp + " , handlingPointerEvent:" + handlingPointerEvent + " , continueGTCCDetect:" + this.continueGTCCDetect + " , detectStateDiffFrameCount:" + this.detectStateDiffFrameCount + " , skipStateDiffFrameCount:" + this.skipStateDiffFrameCount + " , diffValue:" + diffValue + " , stepCount:" + this.stepCount);
        return discard;
    }

    private void parseGTCCResult(String queryResultStr, int[] gtcc) {
        try {
            gtcc[0] = -1;
            if (queryResultStr == null || queryResultStr.isEmpty()) {
                ApsCommon.logI(TAG, "APS: SLB: qr is empty or null. qr:" + queryResultStr);
                return;
            }
            String[] numbers = queryResultStr.split("\\|");
            int length = numbers.length;
            if (length != 5) {
                Log.e(TAG, "APS: SLB: parseGTCCResult fail! length:" + length + ", qr:" + queryResultStr);
                return;
            }
            int i = 0;
            while (i < length) {
                if (!(numbers[i] == null || (numbers[i].isEmpty() ^ 1) == 0 || !numbers[i].matches("[-\\+]?[0-9]+"))) {
                    gtcc[i] = Integer.valueOf(numbers[i]).intValue();
                }
                i++;
            }
        } catch (Exception e) {
            Log.e(TAG, "APS: SLB: parseGTCCResult fail! qr:" + queryResultStr + ", Exception:" + e);
        }
    }

    private void switchGTCC(boolean onoff) {
        try {
            HwApsInterface.nativeSetNeedGTCCResult(onoff ? 1 : 0);
            this.GTCCSwitch = onoff;
        } catch (Exception e) {
            ApsCommon.logI(TAG, "APS: SLB: switchGTCC fail1! Exception:" + e);
        }
    }

    private int getGTCCDiffValue() {
        try {
            int[] gtccQueryResult = new int[5];
            parseGTCCResult(HwApsInterface.nativeGetGTCCResult(), gtccQueryResult);
            return gtccQueryResult[0];
        } catch (Exception e) {
            ApsCommon.logI(TAG, "APS: SLB: getGTCCDiffValue fail2! Exception:" + e);
            return -2;
        }
    }

    private int getKeyCode() {
        int keycode = -1;
        try {
            String msg = SystemProperties.get("sys.aps.keycode", "");
            if (!(msg.length() == 0 || (msg.equals(this.mLastKeyCodeMsg) ^ 1) == 0)) {
                String[] infos = msg.split("\\|");
                if (2 == infos.length && isCurrentKeyEvent(infos[0])) {
                    keycode = Integer.valueOf(infos[1]).intValue();
                }
                this.mLastKeyCodeMsg = msg;
            }
            return keycode;
        } catch (Exception e) {
            Log.e(TAG, "APS: SLB: getKeyCode fail! Exception:" + e);
            return -1;
        }
    }

    private boolean isCurrentKeyEvent(String info) {
        boolean result = LOG;
        if (info != null) {
            try {
                if ((info.isEmpty() ^ 1) != 0 && info.matches("[-\\+]?[0-9]+")) {
                    long delay = SystemClock.uptimeMillis() - Long.valueOf(info).longValue();
                    if (delay >= 0 && delay <= 200) {
                        result = true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "APS: SLB: isCurrentKeyEvent fail! Exception:" + e);
                return LOG;
            }
        }
        return result;
    }
}

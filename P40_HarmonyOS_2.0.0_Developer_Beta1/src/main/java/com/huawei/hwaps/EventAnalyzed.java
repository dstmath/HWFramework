package com.huawei.hwaps;

import android.aps.IApsManager;
import android.aps.IApsManagerServiceCallback;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.HwLog;
import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
import com.huawei.hwaps.FpsRequest;
import java.util.List;

public class EventAnalyzed implements IEventAnalyzed {
    private static final int ACTION_CANCEL = 3;
    private static final int ACTION_DOWN = 0;
    private static final int ACTION_MASK = 255;
    private static final int ACTION_MOVE = 2;
    private static final int ACTION_OUTSIDE = 4;
    private static final int ACTION_POINTER_DOWN = 5;
    private static final int ACTION_POINTER_UP = 6;
    private static final int ACTION_UP = 1;
    private static final int DEFAULT_MAX_FPS = 60;
    private static final int INT_DEF = -1;
    private static final int MAX_RATIO = 100;
    private static final int MIN_APS_ENABLE_FPS = 15;
    private static final int MIN_TIME_INTERNAL = 60000;
    private static final int NANOS_PER_SECOND = 1000000000;
    private static final int NORMAL_MODE_VALUE = 2;
    private static final int PERFORCE_MODE_VALUE = 3;
    private static final int POWER_MODE_VALUE = 1;
    private static final int STOP_TOUCH_MAX_FPS_TIME = 3000;
    private static final int SUPER_MODE_MODE_VALUE = 4;
    private static final String TAG = "Hwaps";
    private static final String VERSION = "11.0.0.4";
    private IApsManager mApsManager;
    private IApsManagerServiceCallback mApsManagerServiceCallback;
    private int mBaseFps = -1;
    private FpsRequest mBaseFpsRequest;
    private int mDefaultMaxFps = -1;
    private long mEpsTouchCount = 0;
    private FpsController mFpsController;
    private Handler mHandler;
    private boolean mIsFirstTimeResumeFps = true;
    private boolean mIsRegisteCallbackSucceed = false;
    private int mLastMode = -1;
    private long mLastTime = System.currentTimeMillis();
    private int mMaxFps = -1;
    private FpsRequest mMaxFpsRequest;
    private String mPkgName;
    private int mPowerModeFps = -1;
    private ResumeFpsByTouch mResumeFpsByTouch;
    private FpsRequest miAwareFpsRequest;

    public void initAps(Context context, int myPid) {
        Log.i(TAG, "APS: EventAnalyzed: initAPS: version is 11.0.0.4");
        this.mHandler = new Handler();
        this.mApsManager = HwFrameworkFactory.getApsManager();
        this.mPkgName = context.getPackageName();
        this.mBaseFpsRequest = new FpsRequest(FpsRequest.SceneTypeE.EXACTLY_IDENTIFY);
        this.miAwareFpsRequest = new FpsRequest(FpsRequest.SceneTypeE.EXACTLY_IDENTIFY);
        this.mMaxFpsRequest = new FpsRequest(FpsRequest.SceneTypeE.OPENGL_SETTING);
        this.mFpsController = new FpsController();
        this.mBaseFps = this.mApsManager.getFps(this.mPkgName);
        this.mMaxFps = this.mApsManager.getMaxFps(this.mPkgName);
        this.mDefaultMaxFps = SystemProperties.getInt("sys.aps.defaultmaxfps", 60);
        this.mPowerModeFps = SystemProperties.getInt("sys.aps.powermodefps", -1);
        this.mResumeFpsByTouch = new ResumeFpsByTouch();
    }

    private boolean isSupportPowerModeFps() {
        String str;
        String enablePackage = SystemProperties.get("sys.aps.game.pkgname", StorageManagerExt.INVALID_KEY_DESC);
        if (this.mPowerModeFps == -1 || (str = this.mPkgName) == null || !str.equals(enablePackage)) {
            return false;
        }
        return true;
    }

    public void setHasOnPaused(boolean isPaused) {
        if (this.mHandler != null && this.mResumeFpsByTouch != null) {
            this.mIsFirstTimeResumeFps = true;
        }
    }

    private boolean isPerformanceMode(int mode) {
        return mode == 3;
    }

    private boolean isPowerMode(int mode) {
        return mode == 1 || mode == 4;
    }

    private boolean isNormalMode(int mode) {
        return mode == 2;
    }

    /* access modifiers changed from: private */
    public class ResumeFpsByTouch implements Runnable {
        private ResumeFpsByTouch() {
        }

        @Override // java.lang.Runnable
        public void run() {
            if (EventAnalyzed.this.mResumeFpsByTouch != null) {
                EventAnalyzed.this.mMaxFpsRequest.stop();
                Log.d(EventAnalyzed.TAG, "APS: EventAnalyzed: ResumeFpsByTouch: adjust to the base fps = " + EventAnalyzed.this.mBaseFps + ", and the package = " + EventAnalyzed.this.mPkgName);
            }
        }
    }

    private void processBaseFps(int mode) {
        if (isPerformanceMode(mode)) {
            FpsRequest fpsRequest = this.mBaseFpsRequest;
            if (fpsRequest != null) {
                fpsRequest.stop();
                return;
            }
            return;
        }
        int i = this.mBaseFps;
        if (i >= 15 && i <= this.mDefaultMaxFps) {
            this.mBaseFpsRequest.start(i);
            ApsCommon.logD(TAG, "APS: EventAnalyzed: processBaseFps: set the base fps = " + this.mBaseFps + ", and the package = " + this.mPkgName);
        }
    }

    private void processIdleFps(int action) {
        int i = this.mMaxFps;
        if (i >= 15 && i > this.mBaseFps) {
            if (action == 0 || this.mIsFirstTimeResumeFps) {
                this.mIsFirstTimeResumeFps = false;
                this.mHandler.removeCallbacks(this.mResumeFpsByTouch);
                this.mMaxFpsRequest.start(this.mMaxFps);
                ApsCommon.logD(TAG, "APS: EventAnalyzed: processIdleFps: resume to the max fps = " + this.mMaxFps + ", and the package = " + this.mPkgName);
            } else if (action == 1) {
                this.mHandler.postDelayed(this.mResumeFpsByTouch, 3000);
            } else {
                ApsCommon.logD(TAG, "APS: EventAnalyzed: processIdleFps: not action down or up");
            }
        }
    }

    public void processiAwareFps(int fps) {
        if (fps >= 15 && fps < this.mDefaultMaxFps) {
            this.miAwareFpsRequest.start(fps);
            Log.d(TAG, "APS: EventAnalyzed: processiAwareFps: iAware set the fps = " + fps + ", and the package = " + this.mPkgName);
        } else if (fps >= this.mDefaultMaxFps) {
            this.miAwareFpsRequest.stop();
            Log.d(TAG, "APS: EventAnalyzed: processiAwareFps: iAware resume the fps");
        } else if (fps == -1) {
            this.miAwareFpsRequest.stop();
            Log.d(TAG, "APS: EventAnalyzed: processiAwareFps: app is background and iAware resume the fps");
        } else {
            Log.d(TAG, "APS: EventAnalyzed: processiAwareFps: do nothing");
        }
    }

    private void processDynamicFps(int action) {
        if (action != 0) {
            registerCallbackInApsManagerService();
        }
    }

    private void reInitFpsPara(int mode) {
        if (isPerformanceMode(mode)) {
            int i = this.mDefaultMaxFps;
            this.mBaseFps = i;
            this.mMaxFps = i;
        } else {
            this.mBaseFps = this.mApsManager.getFps(this.mPkgName);
            this.mMaxFps = this.mApsManager.getMaxFps(this.mPkgName);
            if (this.mBaseFps == -1) {
                this.mBaseFps = this.mDefaultMaxFps;
            }
            if (this.mMaxFps == -1) {
                this.mMaxFps = this.mDefaultMaxFps;
            }
            if (isPowerMode(mode) && isSupportPowerModeFps()) {
                int i2 = this.mPowerModeFps;
                int i3 = this.mBaseFps;
                if (i2 > i3) {
                    i2 = i3;
                }
                this.mBaseFps = i2;
                int i4 = this.mPowerModeFps;
                int i5 = this.mMaxFps;
                if (i4 > i5) {
                    i4 = i5;
                }
                this.mMaxFps = i4;
            }
        }
        Log.d(TAG, "APS: EventAnalyzed: reInitFpsPara :mBaseFps = " + this.mBaseFps + "; mMaxFps = " + this.mMaxFps);
    }

    public void processAnalyze(int action) {
        if (action == 0 || action == 2) {
            this.mEpsTouchCount++;
        }
        if (action == 1 || action == 0 || action == 4) {
            long now = System.currentTimeMillis();
            if (now - this.mLastTime > 60000) {
                HwLog.dubaie("DUBAI_TAG_EPS_TOUCH_INFO", "touch=" + this.mEpsTouchCount);
                this.mLastTime = now;
                this.mEpsTouchCount = 0;
            }
            int mode = SystemProperties.getInt("sys.aps.power_mode", 0);
            if (this.mLastMode != mode) {
                reInitFpsPara(mode);
                processBaseFps(mode);
            }
            processDynamicFps(action);
            processIdleFps(action);
            this.mLastMode = mode;
        }
    }

    /* access modifiers changed from: package-private */
    public class ApsManagerServiceCallback extends IApsManagerServiceCallback.Stub {
        ApsManagerServiceCallback() {
        }

        public void onAppsInfoChanged(List<String> list) {
        }

        public void doCallback(int apsCallbackCode, int data) {
            Log.d(EventAnalyzed.TAG, "APS: EventAnalyzed: doCallback: aps callback code: " + apsCallbackCode + ", data:" + data);
            if (apsCallbackCode != 0) {
                Log.d(EventAnalyzed.TAG, "APS: EventAnalyzed: doCallback, error apsCallbackCode:" + apsCallbackCode);
                return;
            }
            EventAnalyzed.this.processiAwareFps(data);
        }
    }

    private void registerCallbackInApsManagerService() {
        try {
            if (!this.mIsRegisteCallbackSucceed) {
                if (this.mApsManager == null) {
                    this.mApsManager = HwFrameworkFactory.getApsManager();
                }
                if (this.mApsManagerServiceCallback == null) {
                    this.mApsManagerServiceCallback = new ApsManagerServiceCallback();
                }
                if (!(this.mApsManagerServiceCallback == null || this.mPkgName == null)) {
                    this.mIsRegisteCallbackSucceed = this.mApsManager.registerCallback(this.mPkgName, this.mApsManagerServiceCallback);
                }
                Log.d(TAG, "APS: EventAnalyzed: registerCallbackInApsManagerService, mPkgName:" + this.mPkgName + "; result = " + this.mIsRegisteCallbackSucceed);
            }
        } catch (NoSuchMethodError e) {
            Log.e(TAG, "APS: EventAnalyzed: registerCallbackInApsManagerService, exception:" + e);
        }
    }

    private void unregisterCallbackInApsManagerService() {
        try {
            if (this.mIsRegisteCallbackSucceed) {
                Log.d(TAG, "APS: EventAnalyzed: unregisterCallbackInApsManagerService, mPkgName:" + this.mPkgName);
                if (this.mApsManager == null) {
                    this.mApsManager = HwFrameworkFactory.getApsManager();
                }
                this.mApsManager.registerCallback(this.mPkgName, (IApsManagerServiceCallback) null);
                this.mIsRegisteCallbackSucceed = false;
            }
        } catch (NoSuchMethodError e) {
            Log.e(TAG, "APS: EventAnalyzed: unregisterCallbackInApsManagerService, exception:" + e);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        unregisterCallbackInApsManagerService();
    }

    public int getCustScreenDimDurationLocked(int screenOffTimeout) {
        int maxDimRatio = SystemProperties.getInt("sys.aps.maxDimRatio", -1);
        int minBrightDuration = SystemProperties.getInt("sys.aps.minBrightDuration", -1);
        if (maxDimRatio == -1 || minBrightDuration == -1 || screenOffTimeout <= minBrightDuration) {
            return -1;
        }
        return (screenOffTimeout * maxDimRatio) / 100;
    }

    public boolean isSkipFrame(long now, long lastFrameTimeNanos) {
        FpsRequest fpsRequest;
        if (this.mBaseFps == this.mMaxFps || (fpsRequest = this.mBaseFpsRequest) == null || this.mFpsController == null) {
            return false;
        }
        int targetFps = fpsRequest.getTargetFps();
        if (targetFps == this.mDefaultMaxFps || targetFps == 0) {
            this.mFpsController.setUiFrameState(false);
            return false;
        }
        this.mFpsController.setUiFrameState(true);
        if (now - lastFrameTimeNanos < ((long) (NANOS_PER_SECOND / (targetFps + 1)))) {
            return true;
        }
        return false;
    }
}

package huawei.android.hwscrollerboost;

import android.iawareperf.UniPerf;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.IAwareSdk;
import android.scrollerboost.IScrollerBoostMgr;
import huawei.android.app.admin.ConstantValue;
import huawei.android.provider.HwSettings;

public class HwScrollerBoostMgrImpl implements IScrollerBoostMgr {
    private static final long BOOST_TIME_LENGTH = 1000;
    private static final int DEFAULT_ENABLE_SKIPPED_FRAMES = 0;
    private static final int DEFAULT_ENABLE_SKIPPED_FRAMES_FREEZE = 3;
    private static final int DEFAULT_FREEZE_DURATION = 4500;
    private static final int FLING_BOOST_END = -1;
    private static final int INVALID_VALUE = -1;
    private static final int MAX_TIME = 10000;
    private static final float NORMAL_STOP_VELOCITY = 100.0f;
    private static final int SERIES_FLING_GAP = 500;
    private static final int SWITCH_SCROLLER_BOOST = 8;
    private static final String TAG = "HwScrollerBoostMgrImpl";
    private static HwScrollerBoostMgrImpl sInstance;
    private static Object sLock = new Object();
    private boolean mBoostByEachFling;
    private int mBoostDefaultDuration;
    private int mBoostDuration;
    private boolean mBoostSwitch;
    private boolean mEnableBoostByJank;
    private boolean mEnableFling;
    private long mEnableSkippedFrames;
    private long mEnableSkippedFramesEx;
    private Runnable mFlingBoostEnd;
    private Handler mFlingBoostHandler;
    private long mFreezeLastTime;
    private int mFreeze_duration;
    private long mLastBoostTime;
    private boolean mStartBoost;

    private HwScrollerBoostMgrImpl() {
        this.mLastBoostTime = 0;
        this.mEnableSkippedFrames = 0;
        this.mEnableSkippedFramesEx = 3;
        this.mFreezeLastTime = 0;
        this.mFlingBoostHandler = null;
        this.mFlingBoostEnd = null;
        this.mFlingBoostHandler = new Handler(Looper.getMainLooper());
        this.mFlingBoostEnd = new Runnable() {
            public void run() {
                HwScrollerBoostMgrImpl.this.listFling(-1);
            }
        };
    }

    public static HwScrollerBoostMgrImpl getDefault() {
        HwScrollerBoostMgrImpl hwScrollerBoostMgrImpl;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new HwScrollerBoostMgrImpl();
            }
            hwScrollerBoostMgrImpl = sInstance;
        }
        return hwScrollerBoostMgrImpl;
    }

    private void initBoostProperty() {
        this.mBoostSwitch = false;
        this.mBoostDefaultDuration = SystemProperties.getInt("persist.sys.boost.durationms", -1);
        if (this.mBoostDefaultDuration > 0 && this.mBoostDefaultDuration <= 10000) {
            this.mEnableSkippedFrames = (long) SystemProperties.getInt("persist.sys.boost.skipframe", 0);
            this.mBoostByEachFling = SystemProperties.getBoolean("persist.sys.boost.byeachfling", false);
            this.mBoostSwitch = true;
        }
    }

    private void initFreezeProperty() {
        this.mEnableSkippedFramesEx = (long) SystemProperties.getInt("persist.sys.boost.f_skipframe", 3);
        this.mFreeze_duration = SystemProperties.getInt("persist.sys.fast_h_duration", DEFAULT_FREEZE_DURATION);
        this.mFreezeLastTime = SystemClock.uptimeMillis();
    }

    private boolean isAwareScrollerBoostEnable() {
        boolean awareEnable = SystemProperties.getBoolean("persist.sys.enable_iaware", false);
        boolean cpuEnable = "1".equals(SystemProperties.get("persist.sys.cpuset.enable", HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF));
        int featureFlag = SystemProperties.getInt("persist.sys.cpuset.subswitch", 0);
        if (!awareEnable || !cpuEnable || (featureFlag & 8) == 0) {
            return false;
        }
        return true;
    }

    public void init() {
        this.mEnableFling = SystemProperties.getBoolean("ro.config.enable_perfhub_fling", true);
        if (isAwareScrollerBoostEnable()) {
            initBoostProperty();
        }
        initFreezeProperty();
    }

    private boolean isBoostEnable() {
        return this.mBoostSwitch && isPerformanceMode();
    }

    private void boost(int duration) {
        this.mStartBoost = true;
        if (duration < -1 || duration == 0 || duration > this.mBoostDefaultDuration) {
            this.mBoostDuration = this.mBoostDefaultDuration;
        } else {
            this.mBoostDuration = duration;
        }
        this.mLastBoostTime = System.currentTimeMillis();
        if (this.mEnableBoostByJank) {
            doScrollerBoost();
        }
    }

    private boolean isPerformanceMode() {
        return "true".equals(SystemProperties.get("persist.sys.performance", "false"));
    }

    private void doScrollerBoost() {
        if (this.mBoostDuration > 0) {
            UniPerf.getInstance().uniPerfEvent(4097, "", new int[]{0});
        } else {
            UniPerf.getInstance().uniPerfEvent(4097, "", new int[]{-1});
        }
        this.mStartBoost = false;
    }

    public void listFling(int duration) {
        if (isBoostEnable()) {
            boost(duration);
        } else if (!this.mEnableFling) {
        } else {
            if (duration > 0) {
                if (!(this.mFlingBoostHandler == null || this.mFlingBoostEnd == null)) {
                    this.mFlingBoostHandler.removeCallbacks(this.mFlingBoostEnd);
                }
                UniPerf.getInstance().uniPerfEvent(4112, "", new int[]{0});
            } else if (duration == -1) {
                UniPerf.getInstance().uniPerfEvent(4112, "", new int[]{-1});
            }
        }
    }

    public void finishListFling(float currVelocity) {
        if (Math.abs(currVelocity) <= NORMAL_STOP_VELOCITY) {
            listFling(-1);
        } else if (this.mEnableFling && this.mFlingBoostHandler != null && this.mFlingBoostEnd != null) {
            this.mFlingBoostHandler.removeCallbacks(this.mFlingBoostEnd);
            this.mFlingBoostHandler.postDelayed(this.mFlingBoostEnd, 500);
        }
    }

    public void updateFrameJankInfo(long skippedFrames) {
        updateFrameJankInfoEx(skippedFrames);
        if (this.mBoostSwitch) {
            if ((this.mBoostByEachFling || !this.mEnableBoostByJank) && this.mStartBoost) {
                long scrollerBoostTime = System.currentTimeMillis() - this.mLastBoostTime;
                if (skippedFrames >= this.mEnableSkippedFrames && scrollerBoostTime <= BOOST_TIME_LENGTH) {
                    if (!this.mBoostByEachFling) {
                        this.mEnableBoostByJank = true;
                    }
                    doScrollerBoost();
                }
            }
        }
    }

    private void updateFrameJankInfoEx(long skippedFrames) {
        if (skippedFrames >= this.mEnableSkippedFramesEx) {
            long curTime = SystemClock.uptimeMillis();
            if (curTime - this.mFreezeLastTime > ((long) this.mFreeze_duration)) {
                IAwareSdk.asyncReportData(ConstantValue.transaction_getPersistentApp, "skippedframe", 0);
                this.mFreezeLastTime = curTime;
            }
        }
    }
}

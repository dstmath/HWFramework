package android.scrollerboost;

import android.iawareperf.UniPerf;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.IAwareSdk;
import android.util.LogException;

public class ScrollerBoostManager {
    private static final long BOOST_TIME_LENGTH = 1000;
    private static final int DEFAULT_ENABLE_SKIPPED_FRAMES = 0;
    private static final int DEFAULT_ENABLE_SKIPPED_FRAMES_FREEZE = 3;
    private static final int DEFAULT_FREEZE_DURATION = 4500;
    private static final int INVALID_VALUE = -1;
    private static final int MAX_TIME = 10000;
    private static final int SWITCH_SCROLLER_BOOST = 8;
    private static final String TAG = "ScrollerBoostManager";
    private static ScrollerBoostManager sScrollerBoostManager;
    private boolean mBoostByEachFling;
    private int mBoostDefaultDuration;
    private int mBoostDuration;
    private boolean mBoostSwitch;
    private boolean mEnableBoostByJank;
    private boolean mEnableFling;
    private long mEnableSkippedFrames = 0;
    private long mEnableSkippedFramesEx = 3;
    private long mFreezeLastTime = 0;
    private int mFreeze_duration;
    private long mLastBoostTime = 0;
    private boolean mStartBoost;

    private ScrollerBoostManager() {
    }

    public static synchronized ScrollerBoostManager getInstance() {
        ScrollerBoostManager scrollerBoostManager;
        synchronized (ScrollerBoostManager.class) {
            if (sScrollerBoostManager == null) {
                sScrollerBoostManager = new ScrollerBoostManager();
            }
            scrollerBoostManager = sScrollerBoostManager;
        }
        return scrollerBoostManager;
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
        boolean cpuEnable = "1".equals(SystemProperties.get("persist.sys.cpuset.enable", "0"));
        int featureFlag = SystemProperties.getInt("persist.sys.cpuset.subswitch", 0);
        if (awareEnable && cpuEnable && (featureFlag & 8) != 0) {
            return true;
        }
        return false;
    }

    public void init() {
        this.mEnableFling = SystemProperties.getBoolean("ro.config.enable_perfhub_fling", true);
        if (isAwareScrollerBoostEnable()) {
            initBoostProperty();
        }
        initFreezeProperty();
    }

    private boolean isBoostEnable() {
        return this.mBoostSwitch ? isPerformanceMode() : false;
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
        UniPerf.getInstance().uniPerfEvent(4097, LogException.NO_VALUE, new int[]{this.mBoostDuration});
        this.mStartBoost = false;
    }

    public void listFling(int duration) {
        if (isBoostEnable()) {
            boost(duration);
        } else if (this.mEnableFling) {
            UniPerf.getInstance().uniPerfEvent(4112, LogException.NO_VALUE, new int[]{duration});
        }
    }

    public void updateFrameJankInfo(long skippedFrames) {
        updateFrameJankInfoEx(skippedFrames);
        if (!this.mBoostSwitch) {
            return;
        }
        if ((this.mBoostByEachFling || !this.mEnableBoostByJank) && this.mStartBoost) {
            long scrollerBoostTime = System.currentTimeMillis() - this.mLastBoostTime;
            if (skippedFrames >= this.mEnableSkippedFrames && scrollerBoostTime <= 1000) {
                if (!this.mBoostByEachFling) {
                    this.mEnableBoostByJank = true;
                }
                doScrollerBoost();
            }
        }
    }

    private void updateFrameJankInfoEx(long skippedFrames) {
        if (skippedFrames >= this.mEnableSkippedFramesEx) {
            long curTime = SystemClock.uptimeMillis();
            if (curTime - this.mFreezeLastTime > ((long) this.mFreeze_duration)) {
                IAwareSdk.asyncReportData(3003, "skippedframe", 0);
                this.mFreezeLastTime = curTime;
            }
        }
    }
}

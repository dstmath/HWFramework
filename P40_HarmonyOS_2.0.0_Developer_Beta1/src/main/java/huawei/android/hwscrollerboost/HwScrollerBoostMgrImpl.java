package huawei.android.hwscrollerboost;

import android.common.HwFrameworkFactory;
import android.iawareperf.IHwRtgSchedImpl;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.rms.iaware.IAwareSdk;
import android.scrollerboostmanager.IScrollerBoostMgr;

public class HwScrollerBoostMgrImpl implements IScrollerBoostMgr {
    private static final int DEFAULT_ENABLE_SKIPPED_FRAMES_FREEZE = 3;
    private static final int DEFAULT_FREEZE_DURATION = 4500;
    private static final int INVALID_VALUE = -1;
    private static final Object SLOCK = new Object();
    private static final String TAG = "HwScrollerBoostMgrImpl";
    private static HwScrollerBoostMgrImpl sInstance;
    private boolean mEnableFling;
    private long mEnableSkippedFramesEx = 3;
    private int mFreezeDuration;
    private long mFreezeLastTime = 0;

    private HwScrollerBoostMgrImpl() {
    }

    public static HwScrollerBoostMgrImpl getDefault() {
        HwScrollerBoostMgrImpl hwScrollerBoostMgrImpl;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new HwScrollerBoostMgrImpl();
            }
            hwScrollerBoostMgrImpl = sInstance;
        }
        return hwScrollerBoostMgrImpl;
    }

    private void initFreezeProperty() {
        this.mEnableSkippedFramesEx = (long) SystemProperties.getInt("persist.sys.boost.f_skipframe", 3);
        this.mFreezeDuration = SystemProperties.getInt("persist.sys.fast_h_duration", (int) DEFAULT_FREEZE_DURATION);
        this.mFreezeLastTime = SystemClock.uptimeMillis();
    }

    public void init() {
        this.mEnableFling = SystemProperties.getBoolean("ro.config.enable_perfhub_fling", true);
        initFreezeProperty();
    }

    public void listFling(int duration) {
        if (this.mEnableFling) {
            IHwRtgSchedImpl hwRtgSchedImpl = HwFrameworkFactory.getHwRtgSchedImpl();
            if (duration > 0) {
                if (hwRtgSchedImpl != null) {
                    hwRtgSchedImpl.doFlingStart();
                }
                IAwareSdk.asyncSendData("UniperfClient", 4112, 0);
            }
            if (duration == -1) {
                if (hwRtgSchedImpl != null) {
                    hwRtgSchedImpl.doFlingStop();
                }
                IAwareSdk.asyncSendData("UniperfClient", 4112, -1);
            }
        }
    }

    public void updateFrameJankInfo(long skippedFrames) {
        updateFrameJankInfoEx(skippedFrames);
    }

    private void updateFrameJankInfoEx(long skippedFrames) {
        if (skippedFrames >= this.mEnableSkippedFramesEx) {
            long curTime = SystemClock.uptimeMillis();
            if (curTime - this.mFreezeLastTime > ((long) this.mFreezeDuration)) {
                IAwareSdk.asyncReportData(3003, "skippedframe", 0);
                this.mFreezeLastTime = curTime;
            }
        }
    }
}
